package tradestages.wrapper;

import javax.annotation.Nullable;

public interface IMerchantRecipeWrapper {
   int tsl$getTradeLevel();
   void tsl$setTradeLevel(int var1);

   // Career name e.g. "fisherman", "cartographer", "cleric"
   @Nullable String tsl$getCareer();
   void tsl$setCareer(@Nullable String careerName);

   // Profession name e.g. "minecraft:farmer", "minecraft:librarian"
   @Nullable String tsl$getProfession();
   void tsl$setProfession(@Nullable String profession);
}
