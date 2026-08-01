package tradestages;

import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ModRef {
   public static final String ID = "tradestages";
   public static final Logger LOGGER = LogManager.getLogger("Trade Stages");

   public static ResourceLocation res(String name) {
      return new ResourceLocation("tradestages", name);
   }
}
