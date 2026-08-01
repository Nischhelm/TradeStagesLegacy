package tradestages;

import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;

@Mod(modid = "tradestages")
public class ModRoot {
   public static AbstractVillager lastInteractedVillager;
   public static StagedTradeData stagedTrades;

   public ModRoot() {
      loadTradeData();
      System.out.println(stagedTrades);
   }

   public static void loadTradeData() {
      BufferedReader reader = null;

      try {
         reader = new BufferedReader(new FileReader(FMLPaths.CONFIGDIR.get().resolve("tradestages.json").toFile()));
         JsonObject configJson = GsonHelper.parse(reader);
         if (!configJson.has("trades") || !configJson.get("trades").isJsonObject()) {
            ModRef.LOGGER.warn("Config file must contain 'trades' as Json Object");
            stagedTrades = new StagedTradeData();
            return;
         }

         stagedTrades = StagedTradeData.load(configJson.getAsJsonObject("trades"));
      } catch (Exception var4) {
         ModRef.LOGGER.error("Failed to read config", var4);
         if (reader != null) {
            try {
               reader.close();
            } catch (IOException var3) {
               ModRef.LOGGER.error("Failed to close config reader", var3);
            }
         }
      }
   }
}
