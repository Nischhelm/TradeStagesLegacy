package tradestages.mixins;

import net.minecraft.entity.passive.EntityVillager;
import tradestages.trades.IStagedOffer;
import java.util.Iterator;
import java.util.Set;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.world.entity.npc.VillagerTrades.ItemListing;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(AbstractVillager.class)
public class AbstractVillagerMixin {
   @Inject(
      method = "addOffersFromItemListings",
      at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/world/item/trading/MerchantOffers;add(Ljava/lang/Object;)Z"),
      locals = LocalCapture.CAPTURE_FAILEXCEPTION
   )
   public void addTradeLevelToOffer(
      MerchantOffers offers,
      ItemListing[] listings,
      int amountToAdd,
      CallbackInfo ci,
      Set<Integer> tradeIds,
      Iterator<Integer> tradeIdsIte,
      Integer tradeId,
      ItemListing listing,
      MerchantOffer offer
   ) {
      if (offer instanceof IStagedOffer tradeLevelOffer) {
         if (this instanceof EntityVillager) {
            EntityVillager villager = (EntityVillager) this;
            tradeLevelOffer.setTradeLevel(villager.getVillagerData().getLevel());
            tradeLevelOffer.setProfessionId(ForgeRegistries.PROFESSIONS.getKey(villager.getVillagerData().getProfession()));
         }
      }
   }
}
