package capitalistspz.twwadh.mixin.server.command;

import capitalistspz.twwadh.interfaces.IForcedMount;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.RideCommand;
import net.minecraft.server.command.ServerCommandSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

@Mixin(RideCommand.class)
public abstract class RideCommandMxn {
    @ModifyArg(method = "register",
            at = @At(value = "INVOKE",
                    target = "Lcom/mojang/brigadier/CommandDispatcher;register(Lcom/mojang/brigadier/builder/LiteralArgumentBuilder;)Lcom/mojang/brigadier/tree/LiteralCommandNode;"))
    private static <S> LiteralArgumentBuilder<S> addForcedArgument(LiteralArgumentBuilder<S> command) {
        var lab = argument("target", EntityArgumentType.entity()).then(
                literal("mount").then(
                        argument("vehicle", EntityArgumentType.entity()).then(
                                argument("forced", BoolArgumentType.bool())
                                        .executes(cmd -> executeMountForced(cmd.getSource(), EntityArgumentType.getEntity(cmd, "target"), EntityArgumentType.getEntity(cmd, "vehicle"), BoolArgumentType.getBool(cmd, "forced"))))));
        return command.then((ArgumentBuilder<S, ?>) lab);
    }

    private static int executeMountForced(ServerCommandSource src, Entity rider, Entity vehicle, boolean forced) {
        int result = executeMount(src, rider, vehicle);
        if (forced && (rider instanceof PlayerEntity player)) {
            ((IForcedMount) player).setForciblyMounted(true);
        }
        return result;
    }

    @Shadow
    private static int executeMount(ServerCommandSource source, Entity rider, Entity vehicle) {
        return 0;
    }

}

