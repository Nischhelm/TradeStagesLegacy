package tradestages.mixins.client;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiMerchant;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.IMerchant;
import net.minecraft.inventory.ContainerMerchant;
import net.minecraft.util.ResourceLocation;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tradestages.TradeStagesLegacy;
import tradestages.rules.TradeData;
import tradestages.mixinwrapper.IMerchantRecipeWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Mixin(GuiMerchant.class)
public abstract class GuiMerchantMixin extends GuiContainer {
    @Shadow @Final private IMerchant merchant;
    @Shadow private int selectedMerchantRecipe;

    @Unique private static final ResourceLocation INVALID_STAGE_ICON = new ResourceLocation(TradeStagesLegacy.MODID, "textures/gui/invalid_stage_icon.png");

    public GuiMerchantMixin(ContainerMerchant inventorySlotsIn) {
        super(inventorySlotsIn);
    }

    @Inject(method = "drawScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiMerchant;isPointInRegion(IIIIII)Z", ordinal = 0))
    public void tsl_renderInvalidTradeInfo(int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        MerchantRecipeList recipes = this.merchant.getRecipes(this.mc.player);
        if (recipes == null || recipes.isEmpty()) return;

        int index = this.selectedMerchantRecipe;
        if(index < 0 || index >= recipes.size()) return;

        MerchantRecipe recipe = recipes.get(index);
        if(!(recipe instanceof IMerchantRecipeWrapper)) return;
        if (TradeStagesLegacy.canTrade(this.mc.player, recipe)) return;
        IMerchantRecipeWrapper wrappedRecipe = (IMerchantRecipeWrapper) recipe;

        // Draw invalid stage icon near the output slot
        int iconX = this.guiLeft + 89;
        int iconY = this.guiTop + 24;

        GlStateManager.disableDepth();
        this.mc.getTextureManager().bindTexture(INVALID_STAGE_ICON);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        Gui.drawModalRectWithCustomSizedTexture(iconX, iconY, 0, 0, 14, 14, 32, 32);
        GlStateManager.enableDepth();

        // Hovering over icon
        if (mouseX >= iconX && mouseX <= iconX + 14 && mouseY >= iconY && mouseY <= iconY + 14)
            this.tsl$drawStageTooltip(mouseX, mouseY, recipe);
    }

    @Unique
    private void tsl$drawStageTooltip(int mouseX, int mouseY, MerchantRecipe recipe) {
        Set<String> stages = TradeData.getMatchingStages(recipe);
        if (stages == null || stages.isEmpty()) return;

        List<String> tooltip = new ArrayList<>();
        tooltip.add(I18n.format("merchant.invalid_stage"));

        for (String stage : stages) {
            // Check if translation exists, otherwise use stage name directly
            String key = "stage." + stage;
            if (I18n.hasKey(key)) tooltip.add(I18n.format(key));
            else tooltip.add(stage);
        }

        this.drawHoveringText(tooltip, mouseX, mouseY);
    }
}