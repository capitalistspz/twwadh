package capitalistspz.twwadh.mixin.command;

import capitalistspz.twwadh.Helper;
import capitalistspz.twwadh.Twwadh;
import capitalistspz.twwadh.command.argument.RelationOption;
import capitalistspz.twwadh.interfaces.ISelectorExtensions;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.EntitySelector;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Ownable;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.predicate.NumberRange;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.apache.logging.log4j.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

@Mixin(EntitySelector.class)
public abstract class EntitySelectorMxn implements ISelectorExtensions {
    RelationOption relationOption = RelationOption.UNSET;
    NumberRange.IntRange attackerTimeRange = NumberRange.IntRange.ANY;

    NumberRange.IntRange index = NumberRange.IntRange.ANY;

    @Override
    public void setExtraArgs(RelationOption relationOption,
                             NumberRange.IntRange attackerTimeRange,
                             NumberRange.IntRange index) {
        this.relationOption = relationOption;
        this.attackerTimeRange = attackerTimeRange;
        this.index = index;
    }

    @Inject(method = "getUnfilteredEntities",
            at = @At(value = "HEAD"),
            cancellable = true)
    public void getUnfilteredEntitiesExtra(ServerCommandSource source, CallbackInfoReturnable<List<? extends Entity>> cir) throws CommandSyntaxException {
        this.checkSourcePermission(source);
        if (this.relationOption != RelationOption.UNSET && this.uuid == null && this.playerName == null) {
            if (source.getEntity() != null) {
                Vec3d vec3d = this.positionOffset.apply(source.getPosition());
                Predicate<Entity> predicate = this.getPositionPredicate(vec3d);

                if (this.includesNonPlayers) {
                    List<Entity> entities = new ArrayList<>();
                    appendEntitiesFromRelations(source.getEntity(), entities, predicate);

                    cir.setReturnValue(this.indexing(this.getEntities(vec3d, entities)));
                } else {
                    cir.setReturnValue(this.indexing(this.getEntities(vec3d, this.getPlayers(source, predicate, relationOption))));
                }
            } else {
                cir.setReturnValue(Collections.emptyList());
            }
        }
    }

    @Inject(method = "getUnfilteredEntities",
            at = @At(value = "RETURN"),
            cancellable = true)
    public void doIndex(ServerCommandSource source, CallbackInfoReturnable<List<? extends Entity>> cir) {
        cir.setReturnValue(this.indexing(cir.getReturnValue()));
    }

    private void appendEntitiesFromRelations(Entity sourceEntity, List<Entity> relations, Predicate<Entity> predicate) {
        int i = this.getAppendLimit();
        if (relations.size() >= i) {
            return;
        }
        for (var entity : getRelations(sourceEntity, relationOption, attackerTimeRange)) {
            if (this.sorter == ARBITRARY && i <= 0)
                break;
            i -= 1;
            if (predicate.test(entity))
                relations.add(entity);
        }
    }

    private static boolean checkRelation(Entity entity, Entity relation, RelationOption relationOption, NumberRange.IntRange attackerTimeRange) {
        switch (relationOption) {
            case OWNER -> {
                return entity instanceof TameableEntity tameableEntity && tameableEntity.getOwnerUuid() == relation.getUuid();
            }
            case LEASHER -> {
                if (entity instanceof MobEntity mob) {
                    return mob.getHoldingEntity() == relation;
                }
            }
            case TARGET -> {
                if (entity instanceof MobEntity mob) {
                    return mob.getTarget() == relation;
                }
            }
            case ATTACKER -> {
                if (entity instanceof LivingEntity ent) {
                    return ent.getAttacker() == relation && attackerTimeRange.test(ent.getLastAttackedTime() - ent.age);
                }
            }
            case VEHICLE -> {
                return entity.getVehicle() == relation;
            }
            case CONTROLLER -> {
                return entity.getPrimaryPassenger() == relation;
            }
            case PASSENGERS -> {
                return entity.hasPassenger(relation);
            }
            case ORIGIN -> {
                return entity instanceof Ownable ownable && ownable.getOwner() != null;
            }
            default -> {
                return false;
            }
        }
        return false;
    }

    private static List<Entity> getRelations(Entity entity, RelationOption relationOption, NumberRange.IntRange attackerTimeRange) {
        Twwadh.log.log(Level.INFO, "Checking relation {}", relationOption.name());
        if (relationOption != RelationOption.PASSENGERS) {
            Entity relation = switch (relationOption) {
                case OWNER -> entity instanceof TameableEntity tameableEntity ? tameableEntity.getOwner() : null;
                case LEASHER -> entity instanceof MobEntity mob ? mob.getHoldingEntity() : null;
                case TARGET -> entity instanceof MobEntity mob ? mob.getTarget() : null;
                case ATTACKER ->
                        entity instanceof LivingEntity ent && attackerTimeRange.test(ent.getLastAttackedTime() - ent.age) ? ent.getAttacker() : null;
                case VEHICLE -> entity.getVehicle();
                case CONTROLLER -> entity.getPrimaryPassenger();
                case ORIGIN -> entity instanceof Ownable ownable ? ownable.getOwner() : null;
                default -> null;
            };
            if (relation != null) {
                return List.of(relation);
            } else {
                return Collections.emptyList();
            }
        } else {
            return entity.getPassengerList();
        }
    }

    List<ServerPlayerEntity> getPlayers(ServerCommandSource source, Predicate<Entity> predicate, RelationOption relationOption) {
        return source.getServer().getPlayerManager().getPlayerList().stream().filter((player) -> predicate.test(player) && checkRelation(source.getEntity(), player, relationOption, attackerTimeRange)).toList();
    }

    List<? extends Entity> indexing(List<? extends Entity> entities) {
        var min = index.getMin();
        var max = index.getMax();
        final var size = entities.size();
        if (min == null && max == null) {
            return entities;
        }
        if (min != null && min.equals(max)) {
            var index = (min < 0) ? min + size : min;
            return index >= 0 && index < size ? List.of(entities.get(index)) : Collections.emptyList();
        }
        min = (min == null) ? 0 : min;
        max = (max == null) ? size - 1 : max;
        if (min >= 0) {
            var newMax = (max < 0) ? max + size : max;

            return (newMax < size && min < size) ? entities.subList(min, newMax + 1) : Collections.emptyList();
        } else if (max >= 0) {
            final int newMin = max;
            var newMax = Math.min(size + min, size);
            if (newMin > newMax)
                return Collections.emptyList();
            var sublist = entities.subList(newMin, newMax + 1);
            Helper.Reverse(sublist);
            return sublist;
        } else {
            var newMin = min + size;
            var newMax = max + size;
            if (newMin < 0)
                return Collections.emptyList();
            var sublist = entities.subList(newMin, newMax + 1);
            Helper.Reverse(sublist);
            return sublist;
        }
    }

    @Shadow
    protected abstract void checkSourcePermission(ServerCommandSource source) throws CommandSyntaxException;

    @Shadow
    @Final
    private boolean includesNonPlayers;
    @Shadow
    @Final
    private @Nullable String playerName;
    @Shadow
    @Final
    private @Nullable UUID uuid;

    @Shadow
    protected abstract int getAppendLimit();

    @Shadow
    protected abstract Predicate<Entity> getPositionPredicate(Vec3d pos);

    @Shadow
    @Final
    private Function<Vec3d, Vec3d> positionOffset;

    @Shadow
    protected abstract <T extends Entity> List<T> getEntities(Vec3d pos, List<T> entities);

    @Shadow
    @Final
    public static BiConsumer<Vec3d, List<? extends Entity>> ARBITRARY;
    @Shadow
    @Final
    private BiConsumer<Vec3d, List<? extends Entity>> sorter;


}
