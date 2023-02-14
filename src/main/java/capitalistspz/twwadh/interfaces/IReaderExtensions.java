package capitalistspz.twwadh.interfaces;

import capitalistspz.twwadh.command.RelationOption;
import net.minecraft.predicate.NumberRange;

public interface IReaderExtensions {

    RelationOption getRelation();

    void setAttackerTimeRange(NumberRange.IntRange range);

    NumberRange.IntRange getAttackerTimeRange();

    void setIndex(NumberRange.IntRange j);

    boolean hasIndex();
}
