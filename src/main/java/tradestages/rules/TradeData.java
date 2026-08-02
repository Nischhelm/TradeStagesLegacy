package tradestages.rules;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.darkhax.gamestages.GameStageHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.village.MerchantRecipe;
import tradestages.TradeStagesLegacy;

import java.util.*;
import java.util.Map.Entry;

public class TradeData {
    private static final List<TradeRule> rules = new ArrayList<>();

    // Client-side cache for performance as its otherwise ran once per frame (cleared when GUI closes)
    private static final Map<MerchantRecipe, Boolean> clientTradeCache = new HashMap<>();

    /**
     * Check if a player can make a trade.
     * Returns false if ANY rule matches and the player lacks the required stages of that rule.
     */
    public static boolean canTrade(EntityPlayer player, MerchantRecipe recipe) {
        // Use cache on client-side only (GUI rendering)
        if (player.world.isRemote && clientTradeCache.containsKey(recipe))
            return clientTradeCache.get(recipe);

        // Perform actual check
        boolean isAllowed = true;
        for (TradeRule rule : rules) {
            if (rule.matches(recipe)) {
                // Rule matches - check if player has required stages
                List<String> stages = rule.getStages();
                if (stages == null) continue;

                boolean hasStages = rule.areAllStagesRequired()
                    ? GameStageHelper.hasAllOf(player, stages)
                    : GameStageHelper.hasAnyOf(player, stages);

                if (!hasStages) {
                    isAllowed = false;
                    break;
                }
            }
        }

        // Cache result on client-side
        if (player.world.isRemote)
            clientTradeCache.put(recipe, isAllowed);

        return isAllowed;
    }

    public static void clearClientCache() {
        clientTradeCache.clear();
    }

    /**
     * Get all stages from rules that match the given recipe.
     * This is only used by the GUI to display the tooltip.
     */
    public static Set<String> getMatchingStages(MerchantRecipe recipe) {
        Set<String> allStages = null;
        for (TradeRule rule : rules) {
            if (!rule.matches(recipe)) continue;
            if (allStages == null) allStages = new LinkedHashSet<>(); //lazy init to save a cycle or two, set to remove dupes, linked so order stays the same

            allStages.addAll(rule.getStages());
        }
        return allStages;
    }

    public static void load(JsonElement tradesElement) {
        rules.clear();

        // Compat for original mod
        if (tradesElement.isJsonObject()) {
            TradeStagesLegacy.LOGGER.warn("JSON format of the original mod detected. Consider migrating to array-based format. See documentation for examples.");
            loadOldFormat(tradesElement.getAsJsonObject());
            return;
        }

        if (!tradesElement.isJsonArray()) {
            TradeStagesLegacy.LOGGER.error("'trades' must be a JSON array. Use \"trades\": [...]");
            return;
        }

        JsonArray rulesArray = tradesElement.getAsJsonArray();

        for (JsonElement ruleElement : rulesArray) {
            if (!ruleElement.isJsonObject()) {
                TradeStagesLegacy.LOGGER.warn("Trade rule must be a JSON object, skipping: {}", ruleElement);
                continue;
            }

            try {
                TradeRule rule = TradeRule.parseRule(ruleElement.getAsJsonObject());
                if (rule != null) rules.add(rule);
            } catch (Exception e) {
                TradeStagesLegacy.LOGGER.error("Failed to parse trade rule: {}", ruleElement, e);
            }
        }

        TradeStagesLegacy.LOGGER.info("Loaded {} trade restriction rules", rules.size());
    }

    /**
     * Load data from old JSON format for backward compatibility.
     * Old format: { "armorer": { "1": ["stage1", "stage2"] } }
     */
    private static void loadOldFormat(JsonObject json) {
        int loadedCount = 0;

        for (Entry<String, JsonElement> careerEntry : json.entrySet()) {
            String career = careerEntry.getKey();
            if (!careerEntry.getValue().isJsonObject()) {
                TradeStagesLegacy.LOGGER.warn("Career entry '{}' must be a JSON object in old format", career);
                continue;
            }

            JsonObject careerObj = careerEntry.getValue().getAsJsonObject();

            for (Entry<String, JsonElement> tradeLevelEntry : careerObj.entrySet()) {
                int tradeLevel;
                try {
                    tradeLevel = Integer.parseInt(tradeLevelEntry.getKey());
                } catch (NumberFormatException e) {
                    TradeStagesLegacy.LOGGER.warn("Trade level must be a number: {}", tradeLevelEntry.getKey());
                    continue;
                }

                if (!tradeLevelEntry.getValue().isJsonArray()) {
                    TradeStagesLegacy.LOGGER.warn("Trade level entry '{}' must be a JSON array", tradeLevelEntry.getKey());
                    continue;
                }

                List<String> stages = new ArrayList<>();
                for (JsonElement stageElement : tradeLevelEntry.getValue().getAsJsonArray()) {
                    if (stageElement.isJsonPrimitive() && stageElement.getAsJsonPrimitive().isString()) {
                        stages.add(stageElement.getAsString());
                    }
                }

                if (!stages.isEmpty()) {
                    // Convert to new format rule: profession=null, career=career, tradeLevel=tradeLevel, item=null
                    TradeRule rule = new TradeRule(null, career, tradeLevel, null, stages, false);
                    rules.add(rule);
                    loadedCount++;
                }
            }
        }

        TradeStagesLegacy.LOGGER.info("Converted {} rules from old format", loadedCount);
    }
}
