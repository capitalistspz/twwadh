package capitalistspz.twwadh.command;

import capitalistspz.twwadh.interfaces.IHaveLocationStorage;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.command.argument.PosArgument;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

public class LocationPosArgument implements PosArgument {
    public static final DynamicCommandExceptionType UNKNOWN_LOCATION_EXCEPTION = new DynamicCommandExceptionType(id -> Text.translatable("location.unknown", id));
    Identifier id;

    public LocationPosArgument(Identifier id){
        this.id = id;
    }
    public static final SuggestionProvider<ServerCommandSource> SUGGESTION_PROVIDER = (context, builder)
            -> CommandSource.suggestIdentifiers(((IHaveLocationStorage)context.getSource().getServer()).getLocationStorage().getIds(), builder);
    public static LocationPosArgument parse(StringReader reader) throws CommandSyntaxException, InvalidIdentifierException {
        return new LocationPosArgument(Identifier.fromCommandInput(reader));
    }

    public static Vec3d getPos(CommandContext<ServerCommandSource> context, String name) throws CommandSyntaxException {
        var id = IdentifierArgumentType.getIdentifier(context, name);
        var storage = ((IHaveLocationStorage)context.getSource().getServer()).getLocationStorage();
        var pos = storage.get(id);
        if (pos == null){
            throw UNKNOWN_LOCATION_EXCEPTION.create(id);
        }
        return pos;
    }

    @Override
    public Vec3d toAbsolutePos(ServerCommandSource source) {
        var storage = ((IHaveLocationStorage)source.getServer()).getLocationStorage();
        return storage.get(id);
    }

    public Identifier getIdentifier(){
        return id;
    }

    @Override
    public Vec2f toAbsoluteRotation(ServerCommandSource source) {
        return Vec2f.ZERO;
    }

    @Override
    public boolean isXRelative() {
        return false;
    }

    @Override
    public boolean isYRelative() {
        return false;
    }

    @Override
    public boolean isZRelative() {
        return false;
    }
}
