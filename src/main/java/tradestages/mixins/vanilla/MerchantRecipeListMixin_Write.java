package tradestages.mixins.vanilla;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.network.PacketBuffer;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import tradestages.mixinwrapper.IMerchantRecipeWrapper;

import java.util.ArrayList;

@Mixin(MerchantRecipeList.class)
public abstract class MerchantRecipeListMixin_Write extends ArrayList<MerchantRecipe> {

    // Marks the end of the trade stages trailer, so the client can tell if the server actually wrote one
    @Unique private static final int TSL_MAGIC = 0x54534C31; // "TSL1"

    // Data is appended after everything else instead of per entry, since other mods (e.g. Raids Backport) replace the vanilla writing
    // Layout: [original list data][per recipe: varint level, career, profession][int trailer length][int magic]
    @WrapMethod(method = "writeToBuf")
    public void tsl_writeTradeLevel(PacketBuffer buffer, Operation<Void> original) {
        original.call(buffer);

        int start = buffer.writerIndex();
        for (MerchantRecipe recipe : this) {
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
        buffer.writeInt(buffer.writerIndex() - start);
        buffer.writeInt(TSL_MAGIC);
    }
}
