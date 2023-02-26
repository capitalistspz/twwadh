package capitalistspz.twwadh.mixin.command.argument;

import capitalistspz.twwadh.command.argument.AxisArgumentType;
import com.mojang.brigadier.arguments.ArgumentType;
import net.minecraft.command.argument.ArgumentTypes;
import net.minecraft.command.argument.serialize.ArgumentSerializer;
import net.minecraft.command.argument.serialize.ConstantArgumentSerializer;
import net.minecraft.registry.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ArgumentTypes.class)
public class ArgumentTypesMxn {


    @Inject(method = "register(Lnet/minecraft/registry/Registry;)Lnet/minecraft/command/argument/serialize/ArgumentSerializer;",
            at = @At(value = "HEAD"))
    private static void registerCustomArgumentTypes(Registry<ArgumentSerializer<?, ?>> registry, CallbackInfoReturnable<ArgumentSerializer<?, ?>> cir) {
        register(registry, "axis", AxisArgumentType.class, ConstantArgumentSerializer.of(AxisArgumentType::axis));
    }

    @Shadow
    private static <A extends ArgumentType<?>, T extends ArgumentSerializer.ArgumentTypeProperties<A>> ArgumentSerializer<A, T> register(Registry<ArgumentSerializer<?, ?>> registry, String id, Class<? extends A> clazz, ArgumentSerializer<A, T> serializer) {
        return null;
    }
}
