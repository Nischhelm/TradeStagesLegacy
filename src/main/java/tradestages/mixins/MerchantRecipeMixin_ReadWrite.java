package tradestages.mixins;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.village.MerchantRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tradestages.mixinwrapper.IMerchantRecipeWrapper;

@Mixin(MerchantRecipe.class)
public class MerchantRecipeMixin_ReadWrite implements IMerchantRecipeWrapper {
   @Unique private int tsl$tradeLevel;
   @Unique private String tsl$career;
   @Unique private String tsl$profession;

   // Constructor injection for NBT reading
   @Inject(method = "<init>(Lnet/minecraft/nbt/NBTTagCompound;)V", at = @At("TAIL"))
   public void tsl_loadTradeLevelFromTag(NBTTagCompound tag, CallbackInfo ci) {
      if (tag.hasKey("tradeLevel")) this.tsl$tradeLevel = tag.getInteger("tradeLevel");
      if (tag.hasKey("career")) this.tsl$career = tag.getString("career");
      if (tag.hasKey("profession")) this.tsl$profession = tag.getString("profession");
   }

   // NBT writing - inject into writeToTags method
   @Inject(method = "writeToTags", at = @At("TAIL"))
   public void tsl_addTradeLevelToTag(CallbackInfoReturnable<NBTTagCompound> cir) {
      NBTTagCompound tag = cir.getReturnValue();
      tag.setInteger("tradeLevel", this.tsl$tradeLevel);
      if (this.tsl$career != null) tag.setString("career", this.tsl$career);
      if (this.tsl$profession != null) tag.setString("profession", this.tsl$profession);
   }

   // IMerchantRecipeWrapper implementation
   @Override public int tsl$getTradeLevel() {
      return this.tsl$tradeLevel;
   }
   @Override public void tsl$setTradeLevel(int level) {
      this.tsl$tradeLevel = level;
   }
   @Override public String tsl$getCareer() {
      return this.tsl$career;
   }
   @Override public void tsl$setCareer(String career) {
      this.tsl$career = career;
   }
   @Override public String tsl$getProfession() {
      return this.tsl$profession;
   }
   @Override public void tsl$setProfession(String profession) {
      this.tsl$profession = profession;
   }
}
