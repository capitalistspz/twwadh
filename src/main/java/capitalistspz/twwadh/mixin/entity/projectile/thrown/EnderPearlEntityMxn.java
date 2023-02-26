package capitalistspz.twwadh.mixin.entity.projectile.thrown;

import capitalistspz.twwadh.advancement.criterion.CustomCriteria;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EnderPearlEntity.class)
public abstract class EnderPearlEntityMxn extends ThrownItemEntity {
    @Inject(method = "onCollision",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/entity/Entity;hasVehicle()Z"))
    void triggerEnderPearlLandedAdvancementCriteria(HitResult hitResult, CallbackInfo ci) {
        Entity collidedEntity = null;
        if (hitResult.getType() == HitResult.Type.ENTITY) {
            collidedEntity = ((EntityHitResult) hitResult).getEntity();
        }
        CustomCriteria.ENDER_PEARL_LANDED.trigger((ServerPlayerEntity) this.getOwner(), (EnderPearlEntity) (Object) this, this.getBlockPos(), collidedEntity);
    }

    public EnderPearlEntityMxn(EntityType<? extends ThrownItemEntity> entityType, World world) {
        super(entityType, world);
    }
}
