package capitalistspz.twwadh.mixin.command.arguments;

import capitalistspz.twwadh.command.LocationPosArgument;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.argument.DefaultPosArgument;
import net.minecraft.command.argument.PosArgument;
import net.minecraft.command.argument.Vec3ArgumentType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Vec3ArgumentType.class)
public class Vec3ArgumentTypeMxn {

    @Shadow @Final private boolean centerIntegers;

    @Inject(at=@At(value = "INVOKE", target = "Lnet/minecraft/command/argument/DefaultPosArgument;parse(Lcom/mojang/brigadier/StringReader;Z)Lnet/minecraft/command/argument/DefaultPosArgument;"), method="parse(Lcom/mojang/brigadier/StringReader;)Lnet/minecraft/command/argument/PosArgument;", cancellable = true)
    private void parseId(StringReader stringReader, CallbackInfoReturnable<PosArgument> cir) throws CommandSyntaxException {
        var pos = stringReader.getCursor();
        try {
            cir.setReturnValue(DefaultPosArgument.parse(stringReader, this.centerIntegers));
        } catch (Exception e) {
            stringReader.setCursor(pos);
            var locArg = LocationPosArgument.parse(stringReader);
            cir.setReturnValue(locArg);
        }
    }
}