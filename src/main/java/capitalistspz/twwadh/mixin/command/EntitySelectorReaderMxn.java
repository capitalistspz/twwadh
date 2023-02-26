package capitalistspz.twwadh.mixin.command;

import capitalistspz.twwadh.command.argument.RelationOption;
import capitalistspz.twwadh.interfaces.IReaderExtensions;
import capitalistspz.twwadh.interfaces.ISelectorExtensions;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.EntitySelectorReader;
import net.minecraft.entity.Entity;
import net.minecraft.predicate.NumberRange;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;

@Mixin(EntitySelectorReader.class)
public class EntitySelectorReaderMxn implements IReaderExtensions {

    RelationOption relation = RelationOption.UNSET;
    NumberRange.IntRange index = NumberRange.IntRange.ANY;

    NumberRange.IntRange attackerTimeRange = NumberRange.IntRange.ANY;

    // If only I could inject a new branch
    @Inject(method = "readAtVariable",
            at = @At(value = "HEAD"),
            cancellable = true)
    void readAtRelation(CallbackInfo ci) {
        this.usesAt = true;
        this.suggestionProvider = this::suggestSelectorRest;
        if (reader.canRead()) {
            var cursorPos = reader.getCursor();
            var string = reader.readUnquotedString();

            switch (string) {
                case "attacker" -> this.relation = RelationOption.ATTACKER;
                case "controller" -> this.relation = RelationOption.CONTROLLER;
                case "leasher" -> this.relation = RelationOption.LEASHER;
                case "origin" -> this.relation = RelationOption.ORIGIN;
                case "owner" -> this.relation = RelationOption.OWNER;
                case "passenger" -> this.relation = RelationOption.PASSENGERS;
                case "target" -> this.relation = RelationOption.TARGET;
                case "vehicle" -> this.relation = RelationOption.VEHICLE;
                default -> {
                    this.relation = RelationOption.UNSET;
                    reader.setCursor(cursorPos);
                    return;
                }
            }
            this.sorter = EntitySelector.ARBITRARY;
            this.limit = Integer.MAX_VALUE;
            this.includesNonPlayers = true;
            this.suggestionProvider = this::suggestOpen;

            if (this.reader.canRead() && this.reader.peek() == '[') {
                this.reader.skip();
                this.suggestionProvider = this::suggestOptionOrEnd;
                this.readArguments();
            }
            ci.cancel();
        }
    }

    @Inject(method = "build",
            at = @At(value = "RETURN"),
            cancellable = true)
    void buildWithNewComponents(CallbackInfoReturnable<EntitySelector> cir) {
        var selector = cir.getReturnValue();
        ((ISelectorExtensions) selector).setExtraArgs(this.relation, this.attackerTimeRange, this.index);
        cir.setReturnValue(selector);
    }

    @Inject(method = "suggestSelector(Lcom/mojang/brigadier/suggestion/SuggestionsBuilder;)V",
            at = @At(value = "TAIL"))
    private static void suggestRelationalSelector(SuggestionsBuilder builder, CallbackInfo ci) {
        builder.suggest("@attacker", Text.translatableWithFallback("twwadh.argument.entity.selector.attacker", "The last entity to attack in the last 5 seconds"));
        builder.suggest("@controller", Text.translatableWithFallback("twwadh.argument.entity.selector.controller", "The passenger controlling this entity"));
        builder.suggest("@leasher", Text.translatableWithFallback("twwadh.argument.entity.selector.leasher", "The entity leashing this entity"));
        builder.suggest("@origin", Text.translatableWithFallback("twwadh.argument.entity.selector.origin", "The originator of this entity"));
        builder.suggest("@owner", Text.translatableWithFallback("twwadh.argument.entity.selector.owner", "The owner of this pet"));
        builder.suggest("@passenger", Text.translatableWithFallback("twwadh.argument.entity.selector.passenger", "The entities riding this entity"));
        builder.suggest("@target", Text.translatableWithFallback("twwadh.argument.entity.selector.target", "The attack target of this entity"));
        builder.suggest("@vehicle", Text.translatableWithFallback("twwadh.argument.entity.selector.vehicle", "The entity currently being ridden"));
    }

    @Override
    public RelationOption getRelation() {
        return this.relation;
    }

    @Override
    public void setAttackerTimeRange(NumberRange.IntRange range) {
        this.attackerTimeRange = range;
    }

    @Override
    public NumberRange.IntRange getAttackerTimeRange() {
        return this.attackerTimeRange;
    }

    @Override
    public void setIndex(NumberRange.IntRange index) {
        this.index = index;
    }

    @Override
    public boolean hasIndex() {
        return index != NumberRange.IntRange.ANY;
    }

    // Shadowed Members
    @Shadow
    private int limit;
    @Shadow
    private boolean includesNonPlayers;
    @Shadow
    private boolean usesAt;

    @Shadow
    @Final
    private StringReader reader;

    @Shadow
    private BiFunction<SuggestionsBuilder, Consumer<SuggestionsBuilder>, CompletableFuture<Suggestions>> suggestionProvider;

    @Shadow
    private BiConsumer<Vec3d, List<? extends Entity>> sorter;

    @Shadow
    protected void readArguments() {
    }

    @Shadow
    private CompletableFuture<Suggestions> suggestOpen(SuggestionsBuilder builder, Consumer<SuggestionsBuilder> consumer) {
        return null;
    }

    @Shadow
    private CompletableFuture<Suggestions> suggestOptionOrEnd(SuggestionsBuilder builder, Consumer<SuggestionsBuilder> consumer) {
        return null;
    }

    @Shadow
    private CompletableFuture<Suggestions> suggestSelectorRest(SuggestionsBuilder builder, Consumer<SuggestionsBuilder> consumer) {
        return null;
    }
}
