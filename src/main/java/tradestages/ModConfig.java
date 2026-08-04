package tradestages;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Config(modid = TradeStagesLegacy.MODID)
public class ModConfig {
    @Config.Comment({
            "Will write all available names of (vanilla+modded) villager professions & careers to log (latest.log) during postInit (startup).",
            "This can be needed cause some professions and careers have unexpected internal names."
    })
    @Config.Name("Dump Villager Professions+Careers")
    @Config.RequiresMcRestart
    public static boolean doDebugDump = false;

    @Mod.EventBusSubscriber
    public static class EventHandler {
        @SubscribeEvent
        public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
            if (event.getModID().equals(TradeStagesLegacy.MODID)) {
                ConfigManager.sync(TradeStagesLegacy.MODID, Config.Type.INSTANCE);
            }
        }
    }
}
