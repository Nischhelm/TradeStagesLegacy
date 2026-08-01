package tradestages.trades;

import net.minecraft.util.ResourceLocation;

public interface IStagedOffer {
   int getTradeLevel();

   void setTradeLevel(int var1);

   ResourceLocation getProfessionId();

   void setProfessionId(ResourceLocation var1);
}
