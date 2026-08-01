package tradestages;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.darkhax.gamestages.GameStageHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.village.MerchantRecipe;
import tradestages.wrapper.IMerchantRecipeWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

public class StagedTradeData {
   private final Table<Integer, String, List<String>> stagedTrades = HashBasedTable.create();

   public List<String> getStages(int tradeLevel, String career) {
      return this.stagedTrades.get(tradeLevel, career);
   }

   public boolean canTrade(EntityPlayer player, MerchantRecipe recipe) {
      if (recipe instanceof IMerchantRecipeWrapper) {
         IMerchantRecipeWrapper wrappedRecipe = (IMerchantRecipeWrapper) recipe;
         List<String> stages = this.stagedTrades.get(wrappedRecipe.tsl$getTradeLevel(), wrappedRecipe.tsl$getCareer());
         //TODO: item condition instead of tradelvl
         return stages == null || stages.isEmpty() || GameStageHelper.hasAnyOf(player, stages); //TODO: hasAnyOf vs hasAllOf ? implement both?
      } else {
         return true;
      }
   }

   public static StagedTradeData load(JsonObject json) {
      StagedTradeData data = new StagedTradeData();

      for (Entry<String, JsonElement> careerEntry : json.entrySet()) {
         String career = careerEntry.getKey();
         if (!careerEntry.getValue().isJsonObject()) {
            TradeStagesLegacy.LOGGER.warn("Career Entry '{}' has to be a Json Object, was {}", career, careerEntry.getValue());
            continue;
         }

         JsonObject careerObj = careerEntry.getValue().getAsJsonObject();

         for (Entry<String, JsonElement> tradeLevelEntry : careerObj.entrySet()) {
            int tradeLevel;
            try {
               tradeLevel = Integer.parseInt(tradeLevelEntry.getKey());
            } catch (NumberFormatException e) {
               TradeStagesLegacy.LOGGER.warn("Trade level has to be a whole number, was {}", tradeLevelEntry.getKey());
               continue;
            }

            if (!tradeLevelEntry.getValue().isJsonArray()) {
               TradeStagesLegacy.LOGGER.warn("Trade Level Entry '{}' has to be a Json Array, was {}", tradeLevelEntry.getKey(), tradeLevelEntry.getValue());
               continue;
            }

            List<String> stages = new ArrayList<>();

            for (JsonElement stageJson : tradeLevelEntry.getValue().getAsJsonArray()) {
               if (stageJson.isJsonPrimitive() && stageJson.getAsJsonPrimitive().isString()) {
                  stages.add(stageJson.getAsString());
               } else {
                  TradeStagesLegacy.LOGGER.warn("Stage has to be a String, was {}", stageJson);
               }
            }

            data.stagedTrades.put(tradeLevel, career, stages);
         }
      }

      return data;
   }

   @Override
   public String toString() {
      return this.stagedTrades.toString();
   }
}
