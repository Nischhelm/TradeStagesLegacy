package tradestages.mixins;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import tradestages.helper.StageHelper;
import javax.annotation.Nullable;
import net.minecraft.world.inventory.MerchantContainer;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MerchantContainer.class)
public abstract class MerchantContainerMixin {
   @Shadow
   @Nullable
   private MerchantOffer activeOffer;
   @Shadow
   @Final
   private Merchant merchant;
   @Shadow
   private int futureXp;

   @Shadow
   public abstract void setItem(int var1, ItemStack var2);

   @Shadow
   public abstract ItemStack getItem(int var1);

   @Inject(method = "updateSellItem", at = @At("TAIL"))
   public void removeInvalidOffer(CallbackInfo ci) {
      EntityPlayer player = this.merchant.getTradingEntityPlayer();
      if (player != null) {
         if (this.activeOffer != null && !StageHelper.canTrade(this.merchant.getTradingEntityPlayer(), this.activeOffer)) {
            this.activeOffer = null;
            this.setItem(2, ItemStack.EMPTY);
            this.futureXp = 0;
            this.merchant.notifyTradeUpdated(this.getItem(2));
         }
      }
   }
}
