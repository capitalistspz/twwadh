package capitalistspz.twwadh.mixin.command;

import capitalistspz.twwadh.command.RelationOption;
import capitalistspz.twwadh.interfaces.IReaderExtensions;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
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
    private static void putOption(String id, EntitySelectorOptions.SelectorHandler handler, Predicate<EntitySelectorReader> condition, Text description){}
    private static final SimpleCommandExceptionType INVALID_ATTACKER_TIME_RANGE_EXCEPTION
            = new SimpleCommandExceptionType(Text.translatableWithFallback("twaddh.argument.entity.options.attack_time.invalid", "Attack time should be between 0 and 200 ticks inclusive"));

    @Inject(method="register", at=@At("TAIL"))
    private static void registerNewOptions(CallbackInfo ci){
        putOption("attack_time", EntitySelectorOptionsMxn::timeOption, reader -> {
            var extendedReader = (IReaderExtensions)reader;
            return extendedReader.getRelation() == RelationOption.ATTACKER && extendedReader.getAttackerTimeRange() == NumberRange.IntRange.ANY;
        }, Text.translatableWithFallback("twwadh.argument.entity.options.attack_time.description", "Time since the attack occurred"));

        putOption("index", EntitySelectorOptionsMxn::indexOption, reader -> {
            var extendedReader = (IReaderExtensions)reader;
            return !reader.isSenderOnly() && !extendedReader.hasIndex();
        }, Text.translatableWithFallback("twwadh.argument.entity.options.index.description", "Index of the entity"));
    }

    private static void timeOption(EntitySelectorReader reader) throws CommandSyntaxException {
        var stringReader = reader.getReader();
        var oldCursor = stringReader.getCursor();
        NumberRange.IntRange intRange = NumberRange.IntRange.parse(stringReader);
        if ((intRange.getMax() != null && intRange.getMax() > 200) || (intRange.getMin() != null && intRange.getMin() < 0)){
            reader.getReader().setCursor(oldCursor);
            throw INVALID_ATTACKER_TIME_RANGE_EXCEPTION.createWithContext(reader.getReader());
        }
        ((IReaderExtensions)reader).setAttackerTimeRange(intRange);
    }

    private static void indexOption(EntitySelectorReader reader) throws CommandSyntaxException {
        var range = NumberRange.IntRange.parse(reader.getReader());
        ((IReaderExtensions)reader).setIndex(range);
    }


}
