package tradestages.mixins.easiervillagertrading;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import de.guntram.mcmod.easiervillagertrading.BetterGuiMerchant;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiMerchant;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.world.World;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tradestages.TradeStagesLegacy;

@Mixin(BetterGuiMerchant.class)
public abstract class BetterGuiMerchantMixin_RenderIcon extends GuiMerchant {
    public BetterGuiMerchantMixin_RenderIcon(InventoryPlayer playerInventoryIn, IMerchant merchantIn, World worldIn) {
        super(playerInventoryIn, merchantIn, worldIn);
    }

    @Unique private static final ResourceLocation INVALID_STAGE_ICON = new ResourceLocation(TradeStagesLegacy.MODID, "textures/gui/invalid_stage_icon.png");

    @Shadow(remap = false) @Final private int okNokXpos;
    @Shadow(remap = false) @Final private int lineHeight;
    @Shadow(remap = false) @Final private int titleDistance;
    @Shadow(remap = false) private int xBase;
    @Shadow(remap = false) private int scrollCount;
    @Shadow(remap = false) protected abstract int getTopAdjust(int numTrades);

    @Inject(method = "drawGuiContainerForegroundLayer", at = @At("TAIL"))
    private void tsl_renderInvalidIcons(int mouseX, int mouseY, CallbackInfo ci) {
        MerchantRecipeList trades = getMerchant().getRecipes(this.mc.player);
        if (trades == null) return;

        int topAdjust = getTopAdjust(trades.size());

        GlStateManager.pushMatrix();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(INVALID_STAGE_ICON);
        GlStateManager.disableLighting();

        // Loop through visible trades
        for (int i = 0; i < trades.size() - scrollCount; i++) {
            MerchantRecipe recipe = trades.get(i + scrollCount);

            if (!TradeStagesLegacy.canTrade(this.mc.player, recipe)) {
                int iconX = xBase + okNokXpos + 2; // Offset slightly to overlay on existing arrow
                int iconY = i * lineHeight - topAdjust + titleDistance + 3;

                Gui.drawModalRectWithCustomSizedTexture(iconX, iconY, 0, 0, 14, 14, 32, 32);
            }
        }

        GlStateManager.popMatrix();
    }

    @WrapWithCondition(
            method = "drawGuiContainerForegroundLayer",
            at = @At(value = "INVOKE", target = "Lde/guntram/mcmod/easiervillagertrading/BetterGuiMerchant;drawTexturedModalRect(IIIIII)V"),
            slice = @Slice(to = @At(value = "FIELD", target = "Lde/guntram/mcmod/easiervillagertrading/BetterGuiMerchant;scrollCount:I", ordinal = 4, opcode = Opcodes.GETFIELD))
    )
    private boolean tsl_dontDrawOtherArrows(BetterGuiMerchant instance, int x, int y, int textureX, int textureY, int width, int height, @Local(name = "trade") MerchantRecipe recipe){
        return TradeStagesLegacy.canTrade(this.mc.player, recipe);
    }
}
