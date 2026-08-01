package tradestages.helper;

import net.minecraft.entity.player.EntityPlayer;
import tradestages.ModRoot;
import net.minecraft.world.item.trading.MerchantOffer;

public class StageHelper {
   public static boolean canTrade(EntityPlayer player, MerchantOffer offer) {
      return ModRoot.stagedTrades.canTrade(player, offer);
   }
}
