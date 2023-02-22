package capitalistspz.twwadh.advancement.criterion;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.advancement.criterion.AbstractCriterionConditions;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.loot.context.LootContext;
import net.minecraft.predicate.StatePredicate;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.AdvancementEntityPredicateSerializer;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.LocationPredicate;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class EnderPearlLandCriterion
        extends AbstractCriterion<EnderPearlLandCriterion.Conditions> {

    static final Identifier ID = new Identifier("ender_pearl_land");
    @Override
    public Identifier getId() {
        return ID;
    }
    @Override
    protected Conditions conditionsFromJson(JsonObject obj, EntityPredicate.Extended playerPredicate, AdvancementEntityPredicateDeserializer predicateDeserializer) {
        var enderPearl = EntityPredicate.Extended.getInJson(obj, "ender_pearl", predicateDeserializer);
        var collidedEntity = EntityPredicate.Extended.getInJson(obj, "entity", predicateDeserializer);
        var block = EnderPearlLandCriterion.getBlock(obj);
        StatePredicate statePredicate = StatePredicate.fromJson(obj.get("state"));
        if (block != null) {
            statePredicate.check(block.getStateManager(), name -> {
                throw new JsonSyntaxException("Block " + block + " has no property " + name + ":");
            });
        }
        LocationPredicate locationPredicate = LocationPredicate.fromJson(obj.get("location"));
        return new Conditions(playerPredicate, enderPearl, block, statePredicate, locationPredicate, collidedEntity);
    }


    private static Block getBlock(JsonObject obj) {
        if (obj.has("block")) {
            Identifier identifier = new Identifier(JsonHelper.getString(obj, "block"));
            return Registries.BLOCK.getOrEmpty(identifier).orElseThrow(() -> new JsonSyntaxException("Unknown block type '" + identifier + "'"));
        }
        return null;
    }

    public void trigger(ServerPlayerEntity player, EnderPearlEntity enderPearl, BlockPos blockPos, @Nullable Entity collidedEntity){
        LootContext enderPearlContext = EntityPredicate.createAdvancementEntityLootContext(player, enderPearl);
        LootContext collidedEntityContext = collidedEntity != null ? EntityPredicate.createAdvancementEntityLootContext(player, collidedEntity) : null;

        BlockState blockState = player.getWorld().getBlockState(blockPos);
        this.trigger(player, conditions -> conditions.matches(enderPearlContext, blockState, blockPos, player.getWorld(), collidedEntityContext));
    }

    public static class Conditions extends AbstractCriterionConditions {
        private final EntityPredicate.Extended enderPearl;
        @Nullable
        private final Block block;
        private final LocationPredicate location;

        private final EntityPredicate.Extended collidedEntity;
        private final StatePredicate state;


        public Conditions(EntityPredicate.Extended player, EntityPredicate.Extended enderPearl, @Nullable Block block, StatePredicate state, LocationPredicate location, EntityPredicate.Extended entity) {
            super(ID, player);
            this.enderPearl = enderPearl;
            this.block = block;
            this.state = state;
            this.location = location;
            this.collidedEntity = entity;
        }

        public static Conditions createEnderPearlLand(EntityPredicate.Extended player, EntityPredicate.Extended enderPearl, Block block, StatePredicate blockState, LocationPredicate location, EntityPredicate.Extended entity){
            return new Conditions(player, enderPearl, block, blockState, location, entity);
        }

        public boolean matches(LootContext enderPearl, BlockState state, BlockPos pos, ServerWorld world, LootContext entityContext) {
            if (!(this.collidedEntity == EntityPredicate.Extended.EMPTY || entityContext != null && this.collidedEntity.test(entityContext))) {
                return false;
            }
            if (this.block != null && !state.isOf(this.block)){
                return false;
            }
            if (!this.state.test(state)){
                return false;
            }
            if (!this.location.test(world, pos.getX(), pos.getY(), pos.getZ())){
                return false;
            }
            return this.enderPearl.test(enderPearl);
        }
        @Override
        public JsonObject toJson(AdvancementEntityPredicateSerializer predicateSerializer){
            JsonObject obj = super.toJson(predicateSerializer);

            obj.add("ender_pearl", this.enderPearl.toJson(predicateSerializer));
            if (this.block != null){
                obj.addProperty("block", Registries.BLOCK.getId(this.block).toString());
            }
            obj.add("state", this.state.toJson());
            obj.add("location", this.location.toJson());
            obj.add("entity", this.collidedEntity.toJson(predicateSerializer));
            return obj;
        }
    }
}
