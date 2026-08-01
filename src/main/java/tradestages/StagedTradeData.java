package tradestages;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import tradestages.trades.IStagedOffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import net.darkhax.gamestages.GameStageHelper;
import net.minecraft.world.item.trading.MerchantOffer;

public class StagedTradeData {
   private final Table<Integer, ResourceLocation, List<String>> stagedTrades = HashBasedTable.create();

   public List<String> getStages(int tradeLevel, ResourceLocation profession) {
      return this.stagedTrades.get(tradeLevel, profession);
   }

   public boolean canTrade(EntityPlayer player, MerchantOffer offer) {
      if (offer instanceof IStagedOffer stagedOffer) {
         List<String> stages = this.stagedTrades.get(stagedOffer.getTradeLevel(), stagedOffer.getProfessionId());
         return stages != null && !stages.isEmpty() ? GameStageHelper.hasAnyOf(player, stages) : true;
      } else {
         return true;
      }
   }

   public static StagedTradeData load(JsonObject json) {
      StagedTradeData data = new StagedTradeData();

      label56:
      for (Entry<String, JsonElement> professionEntry : json.entrySet()) {
         ResourceLocation profession = new ResourceLocation(professionEntry.getKey());
         if (!professionEntry.getValue().isJsonObject()) {
            ModRef.LOGGER.warn("Profession Entry '{}' has to be a Json Object, was {}", profession, professionEntry.getValue());
         } else {
            Iterator var5 = professionEntry.getValue().getAsJsonObject().entrySet().iterator();

            while (true) {
               Entry<String, JsonElement> tradeLevelEntry;
               int tradeLevel;
               while (true) {
                  if (!var5.hasNext()) {
                     continue label56;
                  }

                  tradeLevelEntry = (Entry<String, JsonElement>)var5.next();

                  try {
                     tradeLevel = Integer.parseInt(tradeLevelEntry.getKey());
                     break;
                  } catch (NumberFormatException var11) {
                     ModRef.LOGGER.warn("Trade level has to be a whole number, was {}", tradeLevelEntry.getKey());
                  }
               }

               if (!tradeLevelEntry.getValue().isJsonArray()) {
                  ModRef.LOGGER.warn("Trade Level Entry '{}' has to be a Json Object, was {}", profession, professionEntry.getValue());
               } else {
                  List<String> stages = new ArrayList<>();

                  for (JsonElement stageJson : tradeLevelEntry.getValue().getAsJsonArray()) {
                     if (stageJson.isJsonPrimitive() && stageJson.getAsJsonPrimitive().isString()) {
                        stages.add(stageJson.getAsString());
                     } else {
                        ModRef.LOGGER.warn("Stage has to be a String, was {}", stageJson);
                     }
                  }

                  data.stagedTrades.put(tradeLevel, profession, stages);
               }
            }
         }
      }

      return data;
   }

   @Override
   public String toString() {
      return this.stagedTrades.toString();
   }
}
