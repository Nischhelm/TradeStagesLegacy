package tradestages.mixins.vanilla;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraftforge.fml.common.registry.VillagerRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tradestages.mixinwrapper.IMerchantRecipeWrapper;

import javax.annotation.Nullable;

@Mixin(EntityVillager.class)
public abstract class EntityVillagerMixin {

    @Shadow public abstract VillagerRegistry.VillagerProfession getProfessionForge();
    @Shadow private int careerId;
    @Shadow private int careerLevel;
    @Shadow @Nullable private MerchantRecipeList buyingList;

    @Inject(method = "populateBuyingList", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/passive/EntityVillager$ITradeList;addMerchantRecipe(Lnet/minecraft/entity/IMerchant;Lnet/minecraft/village/MerchantRecipeList;Ljava/util/Random;)V"))
    public void tsl_countTrades(CallbackInfo ci, @Share("tradeCount")LocalIntRef tradeCount) {
        tradeCount.set(this.buyingList != null ? this.buyingList.size() : -1);
    }

    @Inject(method = "populateBuyingList", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/passive/EntityVillager$ITradeList;addMerchantRecipe(Lnet/minecraft/entity/IMerchant;Lnet/minecraft/village/MerchantRecipeList;Ljava/util/Random;)V", shift = At.Shift.AFTER))
    public void tsl_addAdditionalDataToNewRecipes(CallbackInfo ci, @Share("tradeCount")LocalIntRef tradeCount) {
        MerchantRecipeList recipes = this.buyingList;
        if (recipes == null || recipes.isEmpty()) return;

        String careerName = tsl$getCareerName();
        String professionName = tsl$getProfessionName();

        //addMerchantRecipe turns some ITradeList (=trade entry) into usually a single MerchantRecipe (but not guaranteed for modded trades!)
        for (int i = tradeCount.get(); i < this.buyingList.size(); i++) {
            MerchantRecipe latestRecipe = this.buyingList.get(i);
            if (!(latestRecipe instanceof IMerchantRecipeWrapper)) return;

            IMerchantRecipeWrapper wrappedRecipe = (IMerchantRecipeWrapper) latestRecipe;
            wrappedRecipe.tsl$setTradeLevel(this.careerLevel);
            wrappedRecipe.tsl$setCareer(careerName);
            wrappedRecipe.tsl$setProfession(professionName);
        }
    }

    @Unique
    private String tsl$getCareerName() {
        VillagerRegistry.VillagerProfession profession = this.getProfessionForge();
        if (profession == null) return "unknown";

        // vanilla careerIds start with 1, forge getCareer() expects starting with 0
        VillagerRegistry.VillagerCareer career = profession.getCareer(this.careerId - 1);
        if (career == null) return "unknown";

        // ex "cartographer"
        return career.getName();
    }

    @Unique
    private String tsl$getProfessionName() {
        VillagerRegistry.VillagerProfession profession = this.getProfessionForge();
        if (profession == null || profession.getRegistryName() == null)
            return "minecraft:villager";

        // ex "minecraft:farmer"
        return profession.getRegistryName().toString();
    }
}
