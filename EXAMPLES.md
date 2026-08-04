## Examples

The following examples are all entries in the main list `"trades": [...]` 

### Block all Farmer level 1 trades
```json
{
  "conditions": {
    "profession": "minecraft:farmer",
    "tradeLevel": 1
  },
  "stages": ["basic_farming"]
}
```

### Block trades with specific item + metadata
```json
{
  "conditions": {
    "item": {
      "item": "minecraft:wool",
      "meta": 14
    }
  },
  "stages": ["red_dye_unlock"]
}
```

### Block trades with NBT matching
```json
{
  "conditions": {
    "item": {
      "item": "minecraft:diamond_sword",
      "nbt": {
        "display": {
          "Name": "Legendary Blade"
        }
      }
    }
  },
  "stages": ["legendary_weapons"]
}
```

### Block trades selling specific item
```json
{
  "conditions": {
    "item": {
      "item": "minecraft:enchanted_book",
      "matchMode": "SELL"
    }
  },
  "stages": ["unlock_buying_enchantments"]
}
```
Blocks only trades where the villager **sells** enchanted books (ignores trades that buy them, which don't exist in vanilla).

### Global trade lock
```json
{
  "stages": ["unlock_trading"]
}
```
Blocks **all** villager trades until the player has the `unlock_trading` stage.