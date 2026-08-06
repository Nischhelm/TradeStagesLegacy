package tradestages.mixins.vanilla;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.network.PacketBuffer;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tradestages.mixinwrapper.IMerchantRecipeWrapper;

@Mixin(MerchantRecipeList.class)
public class MerchantRecipeListMixin_Write {

    @Inject(
        method = "writeToBuf",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/network/PacketBuffer;writeInt(I)Lio/netty/buffer/ByteBuf;", ordinal = 1, shift = At.Shift.AFTER)
            // = after last write per entry
    )
    public void tsl_writeTradeLevel(PacketBuffer buffer, CallbackInfo ci, @Local MerchantRecipe recipe) {
        if (recipe instanceof IMerchantRecipeWrapper) {
            IMerchantRecipeWrapper wrappedRecipe = (IMerchantRecipeWrapper) recipe;
            String careerIdStr = wrappedRecipe.tsl$getCareer();
            if(careerIdStr == null) careerIdStr = "minecraft:villager";

            String professionIdStr = wrappedRecipe.tsl$getProfession();
            if(professionIdStr == null) professionIdStr = "minecraft:villager";

            buffer.writeVarInt(wrappedRecipe.tsl$getTradeLevel());
            buffer.writeString(careerIdStr);
            buffer.writeString(professionIdStr);
        } else {
            //This shouldnt happen
            buffer.writeVarInt(-1);
            buffer.writeString("minecraft:villager");
            buffer.writeString("minecraft:villager");
        }
    }
}
