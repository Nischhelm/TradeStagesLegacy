package tradestages.mixins.vanilla.client;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.network.PacketBuffer;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import tradestages.mixinwrapper.IMerchantRecipeWrapper;

@Mixin(MerchantRecipeList.class)
public class MerchantRecipeListMixin_Read {

    @Unique private static final int TSL_MAGIC = 0x54534C31; // "TSL1", see MerchantRecipeListMixin_Write

    @WrapMethod(method = "readFromBuf")
    private static MerchantRecipeList tsl_readTradeLevel(PacketBuffer buffer, Operation<MerchantRecipeList> original) {
        int end = buffer.writerIndex();
        int start = -1;
        if (end - buffer.readerIndex() >= 8 && buffer.getInt(end - 4) == TSL_MAGIC) {
            int length = buffer.getInt(end - 8);
            if (length >= 0 && end - 8 - length >= buffer.readerIndex()) start = end - 8 - length;
        }
        // No trailer, e.g. the list was written by a subclass overriding writeToBuf
        if (start < 0) return original.call(buffer);

        // Hide the trailer from the original read, other mods (e.g. Raids Backport) read extra data until the buffer is empty
        MerchantRecipeList list;
        buffer.writerIndex(start);
        try {
            list = original.call(buffer);
        } finally {
            buffer.writerIndex(end);
        }

        buffer.readerIndex(start);
        for (MerchantRecipe recipe : list) {
            if (buffer.readerIndex() >= end - 8) break;
            int tradeLvl = buffer.readVarInt();
            String careerName = buffer.readString(32767);
            String professionName = buffer.readString(32767);
            if (recipe instanceof IMerchantRecipeWrapper) {
                IMerchantRecipeWrapper wrappedRecipe = (IMerchantRecipeWrapper) recipe;
                wrappedRecipe.tsl$setTradeLevel(tradeLvl);
                wrappedRecipe.tsl$setCareer(careerName);
                wrappedRecipe.tsl$setProfession(professionName);
            }
        }
        buffer.readerIndex(end);
        return list;
    }
}
