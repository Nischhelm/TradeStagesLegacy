package tradestages.mixins.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import tradestages.ModRef;
import tradestages.ModRoot;
import tradestages.helper.StageHelper;
import tradestages.trades.IStagedOffer;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.BaseComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(MerchantScreen.class)
public abstract class MerchantScreenMixin extends AbstractContainerScreen<MerchantMenu> {
   @Shadow
   private int shopItem;
   private static final ResourceLocation INVALID_STAGE_ICON = ModRef.res("textures/gui/invalid_stage_icon.png");
   private boolean validShopItem = false;

   public MerchantScreenMixin(MerchantMenu menu, Inventory inv, Component title) {
      super(menu, inv, title);
   }

   @Inject(
      method = "render",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/gui/screens/inventory/MerchantScreen;renderButtonArrows(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/item/trading/MerchantOffer;II)V",
         shift = Shift.BY,
         by = 3
      ),
      locals = LocalCapture.CAPTURE_FAILEXCEPTION
   )
   public void renderInvalidStageIcon(
      PoseStack matrix,
      int mouseX,
      int mouseY,
      float delta,
      CallbackInfo ci,
      MerchantOffers merchantoffers,
      int i,
      int j,
      int entryY,
      int entryX,
      int i1,
      Iterator<?> var11,
      MerchantOffer merchantOffer
   ) {
      if (!StageHelper.canTrade(Minecraft.getInstance().player, merchantOffer)) {
         RenderSystem.setShaderTexture(0, INVALID_STAGE_ICON);
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
         GuiComponent.blit(matrix, entryX + 51, entryY + 6, 7, 7, 0.0F, 0.0F, 7, 7, 16, 16);
      }
   }

   @Inject(method = "render", at = @At("TAIL"))
   public void renderInvalidTradeTexts(PoseStack matrix, int mouseX, int mouseY, float delta, CallbackInfo ci) {
      int left = this.getGuiLeft();
      int top = this.getGuiTop();
      Component invalidStageText = new TranslatableComponent("merchant.invalid_stage");
      int textWidth = this.font.width(invalidStageText);
      if (this.validShopItem && this.shopItem >= 0 && this.shopItem <= ((MerchantMenu)this.menu).getOffers().size()) {
         MerchantOffer offer = (MerchantOffer)((MerchantMenu)this.menu).getOffers().get(this.shopItem);
         if (offer instanceof IStagedOffer stagedOffer) {
            if (!StageHelper.canTrade(Minecraft.getInstance().player, offer)) {
               this.font.draw(matrix, invalidStageText, left + 156 - textWidth / 2.0F, top + 60, -12566464);
               if (!(mouseX < left + 153 - textWidth / 2.0F) && !(mouseX > left + 159 + textWidth / 2.0F) && mouseY >= top + 57 && mouseY <= top + 63 + 9) {
                  List<Component> tooltip = ModRoot.stagedTrades
                     .getStages(stagedOffer.getTradeLevel(), stagedOffer.getProfessionId())
                     .stream()
                     .map(stage -> (BaseComponent)(I18n.exists("stage." + stage) ? new TranslatableComponent("stage." + stage) : new TextComponent(stage)))
                     .collect(Collectors.toList());
                  this.renderTooltip(matrix, tooltip, Optional.empty(), mouseX, mouseY);
               }
            }
         }
      }
   }

   @Inject(method = "postButtonClick", at = @At("HEAD"))
   public void setShopItemValid(CallbackInfo ci) {
      this.validShopItem = true;
   }
}
