# Trade Stages Legacy

A 1.12.2 mod that allows restricting villager trades using [Game Stages](https://www.curseforge.com/minecraft/mc-mods/game-stages).

Backport and modification of [Trade Stages](https://www.curseforge.com/minecraft/mc-mods/trade-stages) by [Quarris](https://www.curseforge.com/members/quarris/projects)

## Configuration

The mod uses a rule-based system configured in `/config/tradestages.json`. Each rule can specify any of the shown optional conditions and at least one required game stage.

### Rule Structure

```json
{
  "trades": [
    {
      "conditions": {
        "profession": "minecraft:farmer",
        "career": "farmer",
        "tradeLevel": 1,
        "item": {
          "item": "minecraft:wheat",
          "meta": 0,
          "nbt": { ... },
          "matchMode": "ALL",
          "exactNBT": false
        }
      },
      "stages": ["some_gamestage_name"],
      "requireAllStages": false
    },
    {
      ... next rule
    }
  ]
}
```

### Conditions

All conditions are **optional**. Within a rule, all specified conditions must match (AND logic).

Note: If no condition at all is specified, this rule will match ALL trades.

#### `profession` (string)
The villager profession registry name.
- Available vanilla professions: `minecraft:farmer, minecraft:librarian, minecraft:priest, minecraft:smith, minecraft:butcher (, minecraft:nitwit)"`
- If not specified, matches all professions
- Note: This is just the coat color, not the actual villager type. Both librarians and cartographers (careers) are of profession minecraft:librarian = whitecoats
- Another note: This can easily be skipped if there are no custom villager types in the modpack. Ice&Fire for example has a fisherman career for a different profession, there you'd need this option.

#### `career` (string)
The villager career name.
- Available vanilla careers: `"farmer, fisherman, shepherd, fletcher, librarian, cartographer, cleric, armor, weapon, tool, butcher, leather (, nitwit)`
- If not specified, matches all careers

#### `tradeLevel` (integer)
The trade tier/level (1-5).
- If not specified, matches all levels
- Note: Levels are unlocked when trading with villagers. Each time there's new types of trades that's a new level.

#### `item` (object)
Match trades containing a specific item.

**Required field:**
- `item` (string): Item registry name
  - Example: `"minecraft:diamond"`, `"minecraft:enchanted_book"`

**Optional fields:**
- `meta` (integer): Item metadata/damage value
  - Example: `14` for red wool
  - If not specified, matches any metadata

- `nbt` (object): NBT data to match
  - Example: `{"display": {"Name": "Special Sword"}}`
  - Supports nested NBT structures

- `matchMode` (string): Which items in the trade to check
  - `"ALL"`: Match any of the three items (first input, second input, output)
  - `"BUY"`: Match any input item (first or second input)
  - `"BUY_FIRST"`: Match only first input item
  - `"BUY_SECOND"`: Match only second input item
  - `"SELL"`: Match only output item
  - If not specified, will use ALL
  - Note that this is from the perspective of the villager who is selling items to you

### Stages

#### `stages` (array, required)
List of game stages required to access the trade. Cannot be empty.

Example:
```json
"stages": ["basic_trading", "advanced_trading"]
```

#### `requireAllStages` (boolean, optional)
- `false` (default): Player needs at least one stage from the list (OR logic)
- `true`: Player must have all stages in the list (AND logic)
- This only matters if one rule has multiple stages attached to it.

### NBT Matching

#### `exactNBT` (boolean, optional)
Controls how NBT data is compared when using the `item.nbt` condition.

- `false` (default): **Partial matching** - checks if all required NBT tags exist in the item with correct values. Other present tags are ignored.
- `true`: **Exact matching** - item NBT must exactly match the specified NBT (no other tags allowed)

Example:
```json
{
  "conditions": {
    "item": {
      "item": "minecraft:enchanted_book",
      "nbt": {
        "StoredEnchantments": [
          {"id": 0, "lvl": 4}
        ]
      }
    }
  },
  "stages": ["high_level_enchants"]
}
```
This matches any enchanted book with Protection IV, regardless of other enchantments (partial matching is default).

Note that enchantments are stored as IDs. Those IDs can change for modded enchantments if mods (with enchantments) are added or removed. 
To make them have fixed IDs instead, you can use my mod [EnchantmentControl](https://www.curseforge.com/minecraft/mc-mods/enchantmentcontrol).

Also Note: this doesn't support number array NBT types, to support my sanity instead. 
Number arrays (TAG_INT_ARRAY etc) aren't used in vanilla ItemStack NBT. 
If you REALLY need that compat, make sure to write a github issue for it.

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
Blocks only trades where the villager **sells** enchanted books (ignores trades that buy them, which doesn't exist in vanilla).

### Global trade lock
```json
{
  "stages": ["unlock_trading"]
}
```
Blocks **all** villager trades until the player has the `unlock_trading` stage.

## Multiple Rules

If multiple rules (not multiple stages!) match the same trade:
- Each rule is evaluated independently
- If **any** matching rule's stage requirements are not met → trade is **blocked**

## Legacy Format Support

The mod supports the nested object format of original TradeStages for backward/forward compatibility for the improbable chance that someone already set it up in that format:
```json
{
  "trades": {
    "armorer": {
      "1": ["stage1", "stage2"]
    }
  }
}
```
