package capitalistspz.twwadh.mixin.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.TeamArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.command.ExecuteCommand;
import net.minecraft.server.command.ServerCommandSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.mojang.brigadier.builder.LiteralArgumentBuilder.literal;
import static com.mojang.brigadier.builder.RequiredArgumentBuilder.argument;

@SuppressWarnings({"unchecked", "rawtypes"})
@Mixin(ExecuteCommand.class)
public abstract class ExecuteCommandMxn {
    @Shadow
    private static ArgumentBuilder<ServerCommandSource, ?> addConditionLogic(CommandNode<ServerCommandSource> root, ArgumentBuilder<ServerCommandSource, ?> builder, boolean positive, ExecuteCommand.Condition condition) {
        return null;
    }

    @Inject(method="addConditionArguments", at=@At("TAIL"))
    private static void addTeamCondition(CommandNode<ServerCommandSource> root, LiteralArgumentBuilder<ServerCommandSource> argumentBuilder, boolean positive, CommandRegistryAccess commandRegistryAccess, CallbackInfoReturnable<ArgumentBuilder<ServerCommandSource, ?>> cir){
        var matchesLogic = addConditionLogic(root, argument("team", TeamArgumentType.team()), positive, (ctx) -> {
            Entity entity = EntityArgumentType.getEntity(ctx, "target");
            Team team = TeamArgumentType.getTeam(ctx, "team");
            return entity.isTeamPlayer(team);
        });

        var equalsLogic = addConditionLogic(root, argument("source", EntityArgumentType.entity()), positive, (ctx) -> {
            Entity target = EntityArgumentType.getEntity(ctx, "target");
            Entity source = EntityArgumentType.getEntity(ctx, "source");
            return target.isTeammate(source);
        });

        var teamCondition = literal("team").then(
                argument("target", EntityArgumentType.entity()).then(
                        literal("=").then((ArgumentBuilder<Object, ?>)(Object)equalsLogic)).then(
                        literal("matches").then((ArgumentBuilder<Object, ?>)(Object) matchesLogic)));

        argumentBuilder.then((LiteralArgumentBuilder)teamCondition);
    }

}
