package tradestages;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.village.MerchantRecipe;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.VillagerRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tradestages.mixins.vanilla.VillagerProfessionAccessor;
import tradestages.rules.TradeData;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

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

   @Mod.EventHandler
   public void preInit(FMLPreInitializationEvent event) {
      loadTradeData(event.getModConfigurationDirectory());
   }

   @Mod.EventHandler
   public void postInit(FMLPostInitializationEvent event) {
      if (ModConfig.doDebugDump) {
         LOGGER.info("=== Dumping all registered villagers ===");

         for (VillagerRegistry.VillagerProfession profession : ForgeRegistries.VILLAGER_PROFESSIONS) {
            String professionName = profession.getRegistryName() != null ? profession.getRegistryName().toString() : "unknown";

            List<String> careerNames = new ArrayList<>();
            for (VillagerRegistry.VillagerCareer career : ((VillagerProfessionAccessor) profession).getCareers())
               careerNames.add(career.getName());

            LOGGER.info("Profession: {} - Careers: {}", professionName, String.join(", ", careerNames));
         }

         LOGGER.info("=== End of villager dump ===");
      }
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
               writer.write("  \"trades\": [\n");
               writer.write("  ]\n");
               writer.write("}\n");
            }
            return;
         }

         reader = new BufferedReader(new FileReader(configFile));
         JsonParser parser = new JsonParser();
         JsonObject configJson = parser.parse(reader).getAsJsonObject();
         if (!configJson.has("trades")) {
            LOGGER.warn("Config file must contain 'trades' key");
            return;
         }

         TradeData.load(configJson.get("trades"));
      } catch (Exception var4) {
         LOGGER.error("Failed to read config", var4);
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
      return TradeData.canTrade(player, recipe);
   }
}
