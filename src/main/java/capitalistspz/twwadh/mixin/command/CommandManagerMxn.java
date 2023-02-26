package capitalistspz.twwadh.mixin.command;

import capitalistspz.twwadh.server.command.LocationCommand;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CommandManager.class)
public abstract class CommandManagerMxn {
    @Inject(method = "<init>",
            at = @At(value = "RETURN"))
    void addCommands(CommandManager.RegistrationEnvironment environment, CommandRegistryAccess commandRegistryAccess, CallbackInfo ci) {
        LocationCommand.register(this.dispatcher);
    }

    @Shadow
    @Final
    private CommandDispatcher<ServerCommandSource> dispatcher;

}
