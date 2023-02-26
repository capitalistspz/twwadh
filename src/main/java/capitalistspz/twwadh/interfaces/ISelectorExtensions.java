package capitalistspz.twwadh.interfaces;

import capitalistspz.twwadh.command.argument.RelationOption;
import net.minecraft.predicate.NumberRange;

public interface ISelectorExtensions {

    void setExtraArgs(RelationOption relationOption, NumberRange.IntRange attackerTimeRange, NumberRange.IntRange index);
}
