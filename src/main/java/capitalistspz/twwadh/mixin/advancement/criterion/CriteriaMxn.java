package capitalistspz.twwadh.mixin.advancement.criterion;

import capitalistspz.twwadh.advancement.criterion.CustomCriteria;
import capitalistspz.twwadh.advancement.criterion.DestroyedBlockCriterion;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.advancement.criterion.Criterion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Criteria.class)
public abstract class CriteriaMxn {
    @Shadow
    private static <T extends Criterion<?>> T register(T object) {
        return null;
    }

    @Inject(method="<clinit>", at=@At("TAIL"))
    private static void addCriteria(CallbackInfo ci){
        CustomCriteria.DESTROYED_BLOCK = register(new DestroyedBlockCriterion());
    }

}
