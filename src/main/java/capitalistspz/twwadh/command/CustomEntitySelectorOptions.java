package capitalistspz.twwadh.command;

import capitalistspz.twwadh.interfaces.IReaderExtensions;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.command.EntitySelectorReader;
import net.minecraft.predicate.NumberRange;
import net.minecraft.text.Text;

public class CustomEntitySelectorOptions {
    private static final SimpleCommandExceptionType INVALID_ATTACKER_TIME_RANGE_EXCEPTION =
            new SimpleCommandExceptionType(Text.translatableWithFallback("twaddh.argument.entity.options.attack_time.invalid", "Attack time should be between 0 and 200 ticks inclusive"));

    public static void timeOption(EntitySelectorReader reader) throws CommandSyntaxException {
        var stringReader = reader.getReader();
        var oldCursor = stringReader.getCursor();
        NumberRange.IntRange intRange = NumberRange.IntRange.parse(stringReader);
        if ((intRange.getMax() != null && intRange.getMax() > 200) || (intRange.getMin() != null && intRange.getMin() < 0)) {
            reader.getReader().setCursor(oldCursor);
            throw INVALID_ATTACKER_TIME_RANGE_EXCEPTION.createWithContext(reader.getReader());
        }
        ((IReaderExtensions) reader).setAttackerTimeRange(intRange);
    }

    public static void indexOption(EntitySelectorReader reader) throws CommandSyntaxException {
        var range = NumberRange.IntRange.parse(reader.getReader());
        ((IReaderExtensions) reader).setIndex(range);
    }
}
