package tradestages.rules;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.util.ResourceLocation;
import net.minecraft.village.MerchantRecipe;
import tradestages.TradeStagesLegacy;

import javax.annotation.Nullable;
import java.util.Map;

public class ItemCondition {
    private final ResourceLocation itemId;          // e.g., "minecraft:diamond_sword"
    @Nullable private final Integer meta;           // Item metadata
    @Nullable private final JsonObject data;        // JSON NBT to match against item NBT
    private final boolean exactNBT;                 // Partial vs exact NBT matching
    private final ItemMatchMode matchMode;          // Which items to check

    public ItemCondition(String itemId, @Nullable Integer meta, @Nullable JsonObject data, boolean exactNBT, ItemMatchMode matchMode) {
        this.itemId = new ResourceLocation(itemId);
        this.meta = meta;
        this.data = data;
        this.exactNBT = exactNBT;
        this.matchMode = matchMode != null ? matchMode : ItemMatchMode.ALL;
    }

    public boolean matches(MerchantRecipe recipe) {
        ItemStack itemToBuy = recipe.getItemToBuy();
        ItemStack secondItemToBuy = recipe.getSecondItemToBuy();
        ItemStack itemToSell = recipe.getItemToSell();

        switch (matchMode) {
            case ALL:
                return matchesStack(itemToBuy) ||
                       matchesStack(secondItemToBuy) ||
                       matchesStack(itemToSell);
            case BUY:
                return matchesStack(itemToBuy) ||
                       matchesStack(secondItemToBuy);
            case BUY_FIRST:
                return matchesStack(itemToBuy);
            case BUY_SECOND:
                return matchesStack(secondItemToBuy);
            case SELL:
                return matchesStack(itemToSell);
            default:
                return false;
        }
    }

    private boolean matchesStack(ItemStack stack) {
        if (stack.isEmpty()) return false;

        Item item = stack.getItem();
        ResourceLocation registryName = item.getRegistryName();
        if (!itemId.equals(registryName)) {
            return false;
        }

        if (meta != null && stack.getMetadata() != meta)
            return false;

        if (data != null) {
            NBTTagCompound stackNBT = stack.getTagCompound();
            if (stackNBT == null) return false;

            return containsNBTCompound(stackNBT, data, exactNBT);
        }

        return true;
    }

    /**
     * Recursively check if 'actual' compound contains all tags from 'required' with the same values
     */
    private static boolean containsNBTCompound(NBTTagCompound actual, JsonObject required, boolean exact) {
        // early return for exact matching
        if (exact && actual.getKeySet().size() != required.entrySet().size())
            return false;

        for (Map.Entry<String, JsonElement> entry : required.entrySet()) {
            String key = entry.getKey();
            if (!actual.hasKey(key)) return false;

            NBTBase actualTag = actual.getTag(key);
            if (!matchesElement(actualTag, entry.getValue(), exact))
                return false;
        }
        return true;
    }

    /**
     * Recursively check if 'actual' list contains all elements from 'required' list.
     */
    private static boolean containsNBTList(NBTTagList actual, JsonArray required, boolean exact) {
        if (actual == null || required == null) return false;

        // early return for exact matching
        if (exact && actual.tagCount() != required.size())
            return false;

        // Every element in required must exist in actual, order is ignored. This can blow up for large required NBT
        for (JsonElement requiredElement : required) {
            boolean found = false;

            for (NBTBase actualElement : actual) {
                if (matchesElement(actualElement, requiredElement, exact)) {
                    found = true;
                    break;
                }
            }

            if (!found) return false;
        }

        return true;
    }

    /**
     * Check if an NBT element matches a JSON element.
     * Dispatches to appropriate comparison method based on type.
     */
    private static boolean matchesElement(NBTBase actual, JsonElement required, boolean exact) {
        if (required.isJsonObject()) { // TagCompounds / JSON Objects
            if (!(actual instanceof NBTTagCompound)) return false;
            return containsNBTCompound((NBTTagCompound) actual, required.getAsJsonObject(), exact);
        } else if (required.isJsonArray()) { // TagLists / JSON Arrays
            if (!(actual instanceof NBTTagList)) return false;
            return containsNBTList((NBTTagList) actual, required.getAsJsonArray(), exact);
        } else if (required.isJsonPrimitive()) // Rest
            return matchesPrimitive(actual, required.getAsJsonPrimitive());
        return false;
    }

    /**
     * Check if an NBT primitive matches a JSON primitive.
     * Handles numeric type coercion and boolean→byte conversion.
     */
    private static boolean matchesPrimitive(NBTBase actual, JsonPrimitive required) {
        if (required.isNumber()) {
            if (!(actual instanceof NBTPrimitive)) return false;
            return ((NBTPrimitive) actual).getLong() == required.getAsLong();
        } else if (required.isBoolean()) {
            // booleans are saved as bytes
            if (!(actual instanceof NBTTagByte)) return false;
            byte expectedByte = (byte) (required.getAsBoolean() ? 1 : 0);
            return ((NBTTagByte) actual).getByte() == expectedByte;
        } else {
            if (!(actual instanceof NBTTagString)) return false;
            return ((NBTTagString) actual).getString().equals(required.getAsString());
        }
    }

    static ItemCondition parseItemCondition(JsonObject itemObj) {
        if (!itemObj.has("item")) {
            TradeStagesLegacy.LOGGER.warn("Item condition missing 'item' field, skipping rule");
            return null;
        }

        String itemId = itemObj.get("item").getAsString();
        Integer meta = itemObj.has("meta") ? itemObj.get("meta").getAsInt() : null;

        JsonObject data = null;
        if (itemObj.has("nbt")) {
            JsonElement element = itemObj.get("nbt");
            if (!element.isJsonObject()) {
                TradeStagesLegacy.LOGGER.error("NBT must be a JSON object, got: {}", element);
                return null;
            }
            data = element.getAsJsonObject();
        }

        boolean exactNBT = itemObj.has("exactNBT") && itemObj.get("exactNBT").getAsBoolean();

        String matchModeStr = itemObj.has("matchMode") ? itemObj.get("matchMode").getAsString() : null;
        ItemMatchMode matchMode = ItemMatchMode.fromString(matchModeStr);

        return new ItemCondition(itemId, meta, data, exactNBT, matchMode);
    }
}
