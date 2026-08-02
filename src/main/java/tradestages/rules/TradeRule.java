package tradestages.rules;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.village.MerchantRecipe;
import tradestages.TradeStagesLegacy;
import tradestages.wrapper.IMerchantRecipeWrapper;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class TradeRule {
    // Conditions (all must match - AND logic)
    @Nullable private final String profession;      // e.g., "minecraft:farmer"
    @Nullable private final String career;          // e.g., "armorer"
    @Nullable private final Integer tradeLevel;     // e.g., 1
    @Nullable private final ItemCondition item;     // Item matching condition

    // Stage requirements
    private final List<String> stages;              // Required stages, at least size 1
    private final boolean requireAllStages;         // If true, use hasAllOf; else hasAnyOf

    public TradeRule(
        @Nullable String profession,
        @Nullable String career,
        @Nullable Integer tradeLevel,
        @Nullable ItemCondition item,
        List<String> stages,
        boolean requireAllStages
    ) {
        this.profession = profession;
        this.career = career;
        this.tradeLevel = tradeLevel;
        this.item = item;
        this.stages = stages;
        this.requireAllStages = requireAllStages;
    }

    /**
     * Check if this rule matches the given recipe.
     * all given conditions must match (AND logic).
     */
    public boolean matches(MerchantRecipe recipe) {
        if (!(recipe instanceof IMerchantRecipeWrapper)) return false;
        IMerchantRecipeWrapper wrapper = (IMerchantRecipeWrapper) recipe;

        if (profession != null) {
            String recipeProfession = wrapper.tsl$getProfession();
            if (!profession.equals(recipeProfession)) return false;
        }

        if (career != null) {
            String recipeCareer = wrapper.tsl$getCareer();
            if (!career.equals(recipeCareer)) return false;
        }

        if (tradeLevel != null && wrapper.tsl$getTradeLevel() != tradeLevel)
            return false;

        if (item != null && !item.matches(recipe)) return false;

        return true;
    }

    // guaranteed to have at least size 1 (from parsed JSON validation)
    public List<String> getStages() {
        return stages;
    }

    public boolean areAllStagesRequired() {
        return requireAllStages;
    }

    static TradeRule parseRule(JsonObject ruleObj) {
        String profession = null;
        String career = null;
        Integer tradeLevel = null;
        ItemCondition itemCondition = null;

        if (ruleObj.has("conditions") && ruleObj.get("conditions").isJsonObject()) {
            JsonObject conditions = ruleObj.getAsJsonObject("conditions");

            if (conditions.has("profession"))
                profession = conditions.get("profession").getAsString();

            if (conditions.has("career"))
                career = conditions.get("career").getAsString();

            if (conditions.has("tradeLevel"))
                tradeLevel = conditions.get("tradeLevel").getAsInt();

            if (conditions.has("item") && conditions.get("item").isJsonObject())
                itemCondition = ItemCondition.parseItemCondition(conditions.getAsJsonObject("item"));
        }

        // Parse stages
        if (!ruleObj.has("stages") || !ruleObj.get("stages").isJsonArray()) {
            TradeStagesLegacy.LOGGER.warn("Trade rule missing 'stages' array, skipping");
            return null;
        }

        List<String> stages = new ArrayList<>();
        for (JsonElement stageElement : ruleObj.getAsJsonArray("stages")) {
            if (stageElement.isJsonPrimitive() && stageElement.getAsJsonPrimitive().isString()) {
                stages.add(stageElement.getAsString());
            } else {
                TradeStagesLegacy.LOGGER.warn("Stage must be a string: {}", stageElement);
            }
        }

        if (stages.isEmpty()) {
            TradeStagesLegacy.LOGGER.warn("Trade rule has empty stages array, skipping");
            return null;
        }

        // Parse requireAllStages (default: false)
        boolean requireAllStages = ruleObj.has("requireAllStages") && ruleObj.get("requireAllStages").getAsBoolean();

        return new TradeRule(profession, career, tradeLevel, itemCondition, stages, requireAllStages);
    }
}
