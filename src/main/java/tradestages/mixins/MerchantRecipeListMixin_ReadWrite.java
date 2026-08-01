package tradestages.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.network.PacketBuffer;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tradestages.wrapper.IMerchantRecipeWrapper;

@Mixin(MerchantRecipeList.class)
public class MerchantRecipeListMixin_ReadWrite {

    @Inject(
        method = "writeToBuf",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/network/PacketBuffer;writeInt(I)Lio/netty/buffer/ByteBuf;", ordinal = 1, shift = At.Shift.AFTER)
            // = after last write per entry
    )
    public void tsl_writeTradeLevel(PacketBuffer buffer, CallbackInfo ci, @Local(name = "merchantrecipe") MerchantRecipe recipe) {
        if (recipe instanceof IMerchantRecipeWrapper) {
            IMerchantRecipeWrapper wrappedRecipe = (IMerchantRecipeWrapper) recipe;
            String careerIdStr = wrappedRecipe.tsl$getCareer();
            if(careerIdStr == null) careerIdStr = "minecraft:villager";

            buffer.writeVarInt(wrappedRecipe.tsl$getTradeLevel());
            buffer.writeString(careerIdStr);
        } else {
            //This shouldnt happen
            buffer.writeVarInt(-1);
            buffer.writeString("minecraft:villager");
        }
    }

    @Inject(
        method = "readFromBuf",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/network/PacketBuffer;readInt()I", ordinal = 1, shift = At.Shift.AFTER)
    )
    private static void tsl_readTradeLevel(
            PacketBuffer buffer, CallbackInfoReturnable<MerchantRecipeList> cir,
            @Share("tradeLvl")LocalIntRef tradeLvl,
            @Share("careerName")LocalRef<String> careerName
    ) {
        tradeLvl.set(buffer.readVarInt());
        careerName.set(buffer.readString(32767));
    }

    @ModifyArg(
            method = "readFromBuf",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/village/MerchantRecipeList;add(Ljava/lang/Object;)Z")
    )
    private static Object tsl_readTradeLevel(
            Object recipe,
            @Share("tradeLvl")LocalIntRef tradeLvl,
            @Share("careerName")LocalRef<String> careerName
    ) {
        if (recipe instanceof IMerchantRecipeWrapper) {
            IMerchantRecipeWrapper wrappedRecipe = (IMerchantRecipeWrapper) recipe;
            wrappedRecipe.tsl$setTradeLevel(tradeLvl.get());
            wrappedRecipe.tsl$setCareer(careerName.get());
        }
        return recipe;
    }
}
