package capitalistspz.twwadh.mixin.entity.player;

import capitalistspz.twwadh.interfaces.IForcedMount;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMxn extends LivingEntity implements IForcedMount {
    boolean forciblyMounted = false;
    protected PlayerEntityMxn(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "shouldDismount", at=@At("RETURN"), cancellable = true)
    public void disallowDismountIfForced(CallbackInfoReturnable<Boolean> cir){
        cir.setReturnValue(cir.getReturnValue() && !this.forciblyMounted);
    }

    @Inject(method="dismountVehicle", at=@At("HEAD"))
    public void resetForced(CallbackInfo ci){
        this.setForciblyMounted(false);
    }

    public void setForciblyMounted(boolean value){
        this.forciblyMounted = value;
    }

}
