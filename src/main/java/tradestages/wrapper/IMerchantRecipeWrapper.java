package tradestages.wrapper;

import javax.annotation.Nullable;

public interface IMerchantRecipeWrapper {
   int tsl$getTradeLevel();
   void tsl$setTradeLevel(int var1);

   // Career name e.g. "fisherman", "cartographer", "cleric" TODO check names, might need modid on top
   @Nullable String tsl$getCareer();
   void tsl$setCareer(@Nullable String careerName);
}
