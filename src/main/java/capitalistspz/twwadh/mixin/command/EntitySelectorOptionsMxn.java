package capitalistspz.twwadh.mixin.command;

import capitalistspz.twwadh.command.CustomEntitySelectorOptions;
import capitalistspz.twwadh.command.argument.RelationOption;
import capitalistspz.twwadh.interfaces.IReaderExtensions;
import net.minecraft.command.EntitySelectorOptions;
import net.minecraft.command.EntitySelectorReader;
import net.minecraft.predicate.NumberRange;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Predicate;

@Mixin(EntitySelectorOptions.class)
public abstract class EntitySelectorOptionsMxn {
    @Shadow
    private static void putOption(String id, EntitySelectorOptions.SelectorHandler handler, Predicate<EntitySelectorReader> condition, Text description) {
    }


    @Inject(method = "register",
            at = @At(value = "TAIL"))
    private static void registerNewOptions(CallbackInfo ci) {
        putOption("attack_time", CustomEntitySelectorOptions::timeOption, reader -> {
            var extendedReader = (IReaderExtensions) reader;
            return extendedReader.getRelation() == RelationOption.ATTACKER && extendedReader.getAttackerTimeRange() == NumberRange.IntRange.ANY;
        }, Text.translatableWithFallback("twwadh.argument.entity.options.attack_time.description", "Time since the attack occurred"));

        putOption("index", CustomEntitySelectorOptions::indexOption, reader -> {
            var extendedReader = (IReaderExtensions) reader;
            return !reader.isSenderOnly() && !extendedReader.hasIndex();
        }, Text.translatableWithFallback("twwadh.argument.entity.options.index.description", "Index of the entity"));
    }


}
