package capitalistspz.twwadh.mixin.command.argument;

import capitalistspz.twwadh.command.argument.LocationPosArgument;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.DefaultPosArgument;
import net.minecraft.command.argument.PosArgument;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockPosArgumentType.class)
public class BlockPosArgumentTypeMxn {
    @Inject(method = "parse(Lcom/mojang/brigadier/StringReader;)Lnet/minecraft/command/argument/PosArgument;",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/command/argument/DefaultPosArgument;parse(Lcom/mojang/brigadier/StringReader;)Lnet/minecraft/command/argument/DefaultPosArgument;"),
            cancellable = true)
    private void parseId(StringReader stringReader, CallbackInfoReturnable<PosArgument> cir) throws CommandSyntaxException {
        var pos = stringReader.getCursor();
        try {
            cir.setReturnValue(DefaultPosArgument.parse(stringReader));
        } catch (Exception e) {
            stringReader.setCursor(pos);
            var locArg = LocationPosArgument.parse(stringReader);
            cir.setReturnValue(locArg);
        }
    }
}
