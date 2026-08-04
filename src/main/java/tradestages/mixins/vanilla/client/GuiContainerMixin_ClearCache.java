package tradestages.mixins.vanilla.client;

import net.minecraft.client.gui.GuiMerchant;
import net.minecraft.client.gui.inventory.GuiContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tradestages.rules.TradeData;

@Mixin(GuiContainer.class)
public class GuiContainerMixin_ClearCache {

    // Clear client-side trade cache when merchant GUI closes
    @Inject(method = "onGuiClosed", at = @At("HEAD"))
    public void tsl_clearTradeCache(CallbackInfo ci) {
        // Only clear cache if this is actually a merchant GUI
        if ((Object) this instanceof GuiMerchant) {
            TradeData.clearClientCache();
        }
    }
}