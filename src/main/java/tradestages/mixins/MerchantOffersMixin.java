package tradestages.mixins;

import tradestages.trades.IStagedOffer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(MerchantOffers.class)
public class MerchantOffersMixin {
   @Inject(
      method = "writeToStream",
      at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/world/item/trading/MerchantOffer;getDemand()I", shift = Shift.AFTER),
      locals = LocalCapture.CAPTURE_FAILEXCEPTION
   )
   public void writeTradeLevelToStream(FriendlyByteBuf buf, CallbackInfo ci, int i, MerchantOffer offer, ItemStack unused) {
      if (offer instanceof IStagedOffer stagedOffer) {
         buf.writeVarInt(stagedOffer.getTradeLevel());
         buf.writeResourceLocation(stagedOffer.getProfessionId());
      }
   }

   @Inject(
      method = "createFromStream",
      at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/world/item/trading/MerchantOffers;add(Ljava/lang/Object;)Z", shift = Shift.AFTER),
      locals = LocalCapture.CAPTURE_FAILEXCEPTION
   )
   private static void writeTradeLevelToStream(
      FriendlyByteBuf buf,
      CallbackInfoReturnable<MerchantOffers> cir,
      MerchantOffers merchantoffers,
      int i,
      int j,
      ItemStack itemstack,
      ItemStack itemstack1,
      ItemStack itemstack2,
      boolean flag,
      int k,
      int l,
      int i1,
      int j1,
      float f,
      int k1,
      MerchantOffer offer
   ) {
      if (offer instanceof IStagedOffer stagedOffer) {
         stagedOffer.setTradeLevel(buf.readVarInt());
         stagedOffer.setProfessionId(buf.readResourceLocation());
      }
   }
}
