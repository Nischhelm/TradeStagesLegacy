package tradestages.mixins.easiervillagertrading;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import de.guntram.mcmod.easiervillagertrading.BetterGuiMerchant;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.village.MerchantRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import tradestages.TradeStagesLegacy;

@Mixin(value = BetterGuiMerchant.class, remap = false)
public abstract class BetterGuiMerchantMixin_FixShiftClick extends GuiScreen {

    @WrapOperation(
        method = "mouseClicked",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/village/MerchantRecipe;isRecipeDisabled()Z")
    )
    private boolean tsl_checkTradeStageBeforeTransact(MerchantRecipe recipe, Operation<Boolean> original) {
        if (!TradeStagesLegacy.canTrade(this.mc.player, recipe)) return true;
        return original.call(recipe);
    }
}
