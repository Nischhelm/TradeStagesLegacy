package tradestages.mixins.vanilla.client;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.network.PacketBuffer;
import net.minecraft.village.MerchantRecipeList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tradestages.mixinwrapper.IMerchantRecipeWrapper;

@Mixin(MerchantRecipeList.class)
public class MerchantRecipeListMixin_Read {

    @Inject(
        method = "readFromBuf",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/network/PacketBuffer;readInt()I", ordinal = 1, shift = At.Shift.AFTER)
    )
    private static void tsl_readTradeLevel(
            PacketBuffer buffer, CallbackInfoReturnable<MerchantRecipeList> cir,
            @Share("tradeLvl")LocalIntRef tradeLvl,
            @Share("careerName")LocalRef<String> careerName,
            @Share("professionName")LocalRef<String> professionName
    ) {
        tradeLvl.set(buffer.readVarInt());
        careerName.set(buffer.readString(32767));
        professionName.set(buffer.readString(32767));
    }

    @ModifyArg(
            method = "readFromBuf",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/village/MerchantRecipeList;add(Ljava/lang/Object;)Z", remap = false)
    )
    private static Object tsl_readTradeLevel(
            Object recipe,
            @Share("tradeLvl")LocalIntRef tradeLvl,
            @Share("careerName")LocalRef<String> careerName,
            @Share("professionName")LocalRef<String> professionName
    ) {
        if (recipe instanceof IMerchantRecipeWrapper) {
            IMerchantRecipeWrapper wrappedRecipe = (IMerchantRecipeWrapper) recipe;
            wrappedRecipe.tsl$setTradeLevel(tradeLvl.get());
            wrappedRecipe.tsl$setCareer(careerName.get());
            wrappedRecipe.tsl$setProfession(professionName.get());
        }
        return recipe;
    }
}
