package capitalistspz.twwadh.server.command;

import capitalistspz.twwadh.command.AxisArgumentType;
import capitalistspz.twwadh.command.LocationPosArgument;
import capitalistspz.twwadh.interfaces.IHaveLocationStorage;
import capitalistspz.twwadh.util.TextHelper;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.command.argument.Vec3ArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;
public class LocationCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher){
        LiteralArgumentBuilder<ServerCommandSource> command = CommandManager.literal("location");
        var commandRemove = literal("remove").then(
                argument("identifier", IdentifierArgumentType.identifier()).
                        suggests(LocationPosArgument.SUGGESTION_PROVIDER).
                        executes(cmd -> executeRemoveLocation(cmd.getSource(), IdentifierArgumentType.getIdentifier(cmd, "identifier"))));

        var commandSet = literal("set").then(
                argument("identifier", IdentifierArgumentType.identifier()).suggests(LocationPosArgument.SUGGESTION_PROVIDER).then(
                        argument("position", Vec3ArgumentType.vec3()).
                                executes(cmd -> executeSetLocation(cmd.getSource(), IdentifierArgumentType.getIdentifier(cmd, "identifier"), Vec3ArgumentType.getVec3(cmd, "position")))));
        var commandGet = literal("get").
                executes(cmd -> executeListLocationNamespaces(cmd.getSource())).then(
                argument("namespace", StringArgumentType.word()).
                        executes(cmd -> executeListLocationEntries(cmd.getSource(), StringArgumentType.getString(cmd, "namespace")))).then(
                argument("identifier", IdentifierArgumentType.identifier()).suggests(LocationPosArgument.SUGGESTION_PROVIDER).
                        executes(cmd -> executeGetLocation(cmd.getSource(), IdentifierArgumentType.getIdentifier(cmd, "identifier"))).then(
                        argument("axis", AxisArgumentType.axis()).
                                executes(cmd -> executeGetAxis(cmd.getSource(), IdentifierArgumentType.getIdentifier(cmd ,"identifier"), AxisArgumentType.getAxis(cmd, "axis"), null)).then(
                                argument("scale", DoubleArgumentType.doubleArg()).
                                        executes(cmd -> executeGetAxis(cmd.getSource(), IdentifierArgumentType.getIdentifier(cmd ,"identifier"), AxisArgumentType.getAxis(cmd, "axis"), DoubleArgumentType.getDouble(cmd, "scale"))))));
        command.then(commandRemove).
                then(commandSet).
                then(commandGet);
        dispatcher.register(command);

    }
    public static int executeGetAxis(ServerCommandSource src, Identifier id, Direction.Axis axis, Double scale){
        var server = (IHaveLocationStorage)src.getServer();
        var pos = server.getLocationStorage().get(id);

        if (pos != null){
            if (scale == null){
                var value = pos.getComponentAlongAxis(axis);
                src.sendFeedback(Text.translatableWithFallback(
                        "twwadh.location.get.axis.success",
                        String.format("%s has the following %s : %sd", id, axis, value),
                        id.toString()), false);
                return (int)value;
            }
            else {
                var value = (int)(pos.getComponentAlongAxis(axis) * scale);
                src.sendFeedback(Text.translatableWithFallback(
                        "twwadh.location.get.axis.scaled.success",
                        String.format("%s %s after scale factor of %s is %s", id, axis, scale, value),
                        id, axis, scale, value), false);
                return value;
            }
        }
        else {
            src.sendError(Text.translatable(
                    "twwadh.location.get.fail",
                    String.format("Location %s does not exist", id),
                    id.toString()));
            return 0;
        }
    }
    public static int executeSetLocation(ServerCommandSource src, Identifier id, Vec3d vec3d){
        var server = (IHaveLocationStorage)src.getServer();
        var storage = server.getLocationStorage();
        storage.set(id, vec3d);

        var coordsText = TextHelper.getInteractivePosText(vec3d);
        src.sendFeedback(Text.translatableWithFallback(
                "twwadh.location.set.success",
                String.format("Set %s to position [%s, %s, %s]", id, vec3d.x, vec3d.y, vec3d.z),
                id.toString(), coordsText), false);
        return SINGLE_SUCCESS;
    }

    public static int executeGetLocation(ServerCommandSource src, Identifier id){
        var server = (IHaveLocationStorage)src.getServer();
        var storage = server.getLocationStorage();
        var pos = storage.get(id);
        if (pos != null){
            src.sendFeedback(Text.translatableWithFallback(
                    "twwadh.location.get.success",
                    String.format("Location %s is at %s", id, pos),
                    id.toString()), false);
            return SINGLE_SUCCESS;
        }
        else {
            src.sendError(Text.translatable(
                    "twwadh.location.get.fail",
                    String.format("Unable to get location %s because it does not exist", id),
                    id.toString()));
            return 0;
        }
    }



    public static int executeListLocationEntries(ServerCommandSource src, String id){
        var server = (IHaveLocationStorage)src.getServer();
        var storage = server.getLocationStorage();
        var data = storage.getEntries(id).toList();

        StringBuilder stringBuilder = new StringBuilder();
        data.forEach((entry) -> stringBuilder.append("%s: %s\n".formatted(entry.getKey(),entry.getValue())));

        src.sendFeedback(Text.translatableWithFallback(
                "twwadh.location.namespace.get.success",
                "Listing location entries in namespace %s: ".formatted(id),
                id), false);
        src.sendFeedback(Text.literal(stringBuilder.toString()), false);
        return data.size();
    }
    public static int executeListLocationNamespaces(ServerCommandSource src){
        var server = (IHaveLocationStorage)src.getServer();
        var storage = server.getLocationStorage();
        var data = storage.getKeys().toList();
        StringBuilder stringBuilder = new StringBuilder();
        data.forEach((entry) -> stringBuilder.append("%s\n".formatted(entry)));

        src.sendFeedback(Text.translatableWithFallback(
                "twwadh.location.namespaces.get.success",
                "Listing all namespaces: "), false);
        src.sendFeedback(Text.literal(stringBuilder.toString()), false);
        return data.size();
    }

    public static int executeRemoveLocation(ServerCommandSource src, Identifier id){
        var server = (IHaveLocationStorage)src.getServer();
        var storage = server.getLocationStorage();
        if (storage.remove(id)){
            src.sendFeedback(Text.translatableWithFallback(
                    "twwadh.location.remove.success",
                    String.format("Removed location %s", id),
                    id.toString()), false);
            return SINGLE_SUCCESS;
        }
        else {
            src.sendError(Text.translatable(
                    "twwadh.location.remove.fail",
                    String.format("Unable to remove location %s because it does not exist", id),
                    id.toString()));
            return 0;
        }

    }





}
