package tradestages;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.village.MerchantRecipe;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

@Mod(
        modid = TradeStagesLegacy.MODID,
        name = TradeStagesLegacy.NAME,
        version = TradeStagesLegacy.VERSION,
        acceptableRemoteVersions = "*",
        acceptedMinecraftVersions = "1.12.2"
)
public class TradeStagesLegacy {
   public static final String MODID = "tradestages";
   public static final String NAME = "Trade Stages Legacy";
   public static final String VERSION = "1.0.0";
   public static final Logger LOGGER = LogManager.getLogger(NAME);

   public static EntityVillager lastInteractedVillager;
   public static StagedTradeData stagedTrades;

   @Mod.EventHandler
   public void preInit(FMLPreInitializationEvent event) {
      loadTradeData(event.getModConfigurationDirectory());
      LOGGER.info(stagedTrades);
   }

   public static void loadTradeData(File configDir) {
      BufferedReader reader = null;

      try {
         File configFile = new File(configDir, "tradestages.json");

         // Create default config file if it doesn't exist
         if (!configFile.exists()) {
            LOGGER.info("Config file not found, creating default tradestages.json");
            try (FileWriter writer = new FileWriter(configFile)) {
               writer.write("{\n");
               writer.write("  \"trades\": {\n");
               writer.write("  }\n");
               writer.write("}\n");
            }
            stagedTrades = new StagedTradeData();
            return;
         }

         reader = new BufferedReader(new FileReader(configFile));
         JsonParser parser = new JsonParser();
         JsonObject configJson = parser.parse(reader).getAsJsonObject();
         if (!configJson.has("trades") || !configJson.get("trades").isJsonObject()) {
            LOGGER.warn("Config file must contain 'trades' as Json Object");
            stagedTrades = new StagedTradeData();
            return;
         }

         stagedTrades = StagedTradeData.load(configJson.getAsJsonObject("trades"));
      } catch (Exception var4) {
         LOGGER.error("Failed to read config", var4);
         stagedTrades = new StagedTradeData();
      } finally {
         if (reader != null) {
            try {
               reader.close();
            } catch (IOException e) {
               LOGGER.error("Failed to close config reader", e);
            }
         }
      }
   }

   public static boolean canTrade(EntityPlayer player, MerchantRecipe recipe) {
      return TradeStagesLegacy.stagedTrades.canTrade(player, recipe);
   }
}
