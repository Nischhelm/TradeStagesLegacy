package tradestages.mixins;

import net.minecraft.entity.IMerchant;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryMerchant;
import net.minecraft.item.ItemStack;
import net.minecraft.village.MerchantRecipe;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tradestages.TradeStagesLegacy;

@Mixin(InventoryMerchant.class)
public abstract class InventoryMerchantMixin_DenyTrade {

    @Shadow public abstract void setInventorySlotContents(int index, ItemStack stack);
    @Shadow @Final private IMerchant merchant;
    @Shadow private MerchantRecipe currentRecipe;

    @Inject(method = "resetRecipeAndSlots", at = @At("TAIL"))
    public void tsl_denyTrade(CallbackInfo ci) {
        EntityPlayer player = this.merchant.getCustomer();

        if (player == null || this.currentRecipe == null) return;
        if (!TradeStagesLegacy.canTrade(player, this.currentRecipe)) {
            // Clear the current recipe and output slot
            this.currentRecipe = null;
            this.setInventorySlotContents(2, ItemStack.EMPTY);
        }
    }
}
