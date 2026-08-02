package tradestages.rules;

public enum ItemMatchMode {
    ALL,         // Match any of: itemToBuy, secondItemToBuy, itemToSell (default)
    BUY,         // Match any of: itemToBuy, secondItemToBuy
    BUY_FIRST,   // Match only: itemToBuy
    BUY_SECOND,  // Match only: secondItemToBuy
    SELL;        // Match only: itemToSell

    public static ItemMatchMode fromString(String mode) {
        if (mode == null) return ALL;

        try {
            return ItemMatchMode.valueOf(mode.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ALL;
        }
    }
}
