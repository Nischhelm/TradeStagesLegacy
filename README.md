# Trade Stages Legacy

A 1.12.2 mod that allows restricting villager trades using [Game Stages](https://www.curseforge.com/minecraft/mc-mods/game-stages).

Backport and modification of [Trade Stages](https://www.curseforge.com/minecraft/mc-mods/trade-stages) by [Quarris](https://www.curseforge.com/members/quarris/projects).

Note: This mod requires Mixin & MixinExtras. 
Many mods pack those, so i didn't add a specific required dependency for it. 
My suggestion is to use [FermiumBooter](https://www.curseforge.com/minecraft/mc-mods/fermiumbooter), but other mods work too.

## Configuration

The mod uses a rule-based system configured in `/config/tradestages.json`. 
Each rule can specify any of the shown optional conditions and needs at least one specified game stage.

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
          "id": "minecraft:wheat",
          "meta": 0,
          "nbt": { ... },
          "matchMode": "BUY",
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

Note: If zero conditions are specified, the rule will match ALL trades.

#### `profession` (string)
- Available vanilla professions: `minecraft:farmer, minecraft:librarian, minecraft:priest, minecraft:smith, minecraft:butcher (, minecraft:nitwit)"`
- If not specified, matches all professions
- Note: This is just the coat color, not the actual villager type. Both librarians and cartographers (careers) are of profession minecraft:librarian = whitecoats
- Another note: This condition can easily be skipped if there are no custom villager types in the modpack. But Ice&Fire for example has a fisherman career for a different profession, so there you'd need this option to differentiate.

#### `career` (string)
- Available vanilla career names: `"farmer, fisherman, shepherd, fletcher, librarian, cartographer, cleric, armor, weapon, tool, butcher, leather (, nitwit)`
- If not specified, matches all careers

#### `tradeLevel` (integer)
- If not specified, matches all levels
- Note: Levels are unlocked when trading with villagers. Each time there's new trades (green particles) that's a new level. Initial trades are lvl 1, not 0.

#### `item` (object)
Match trades containing a specific item.

**Required field:**
- `id` (string): Item registry name (e.g. `"minecraft:diamond"`)

**Optional fields:**
- `meta` (integer): Item metadata/damage value
  - Example: `14` for red wool
  - If not specified, matches any metadata

- `nbt` (object): NBT data to match
  - Example: `{"display": {"Name": "Special Sword"}}`
  - Supports nested NBT structures. Doesn't support number arrays (byte,int,long arrays)!

- `matchMode` (string): Which items in the trade to check
  - `"ALL"`: Match any of the three items (first input, second input, output)
  - `"BUY"`: Match any input item (first or second input)
  - `"BUY_FIRST"`: Match only first input item
  - `"BUY_SECOND"`: Match only second input item
  - `"SELL"`: Match only output item
  - If not specified, will use ALL
  - Note that this is written from the perspective of the villager who is selling items to you.

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

Note that enchantments are stored as IDs, no way around it. Those IDs can change for modded enchantments if mods (with enchantments) are added or removed. 
To make them have fixed IDs instead, you can use my mod [EnchantmentControl](https://www.curseforge.com/minecraft/mc-mods/enchantmentcontrol).

Also Note: NBT matching doesn't support number array NBT types, to support my sanity instead. 
Number arrays (TAG_INT_ARRAY etc) aren't used in vanilla ItemStack NBT anyway.
If you REALLY need that compat, make sure to write a github issue for it.

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
- This only matters if a rule has multiple stages attached to it.

### Multiple Rules

Multiple rules can match the same trade. 
Each rule will be evaluated independently. 
If **any** of those rules requirements are not met, the trade will be blocked.

### UX / Clientside

Technically this mod can be ran on server only, but it's not gonna be a great experience.

Blocked trades are shown as blocked in the trading GUI, naming required gamestages, marked as green or red if the player has them already or not.
Lang keys can be used here to not have to display the actual gamestage names, use `stage.mystagename=This is displayed in trading GUI.`

### Legacy Format Support

The mod supports the nested object format of original TradeStages for backward (forward i guess) compatibility for the improbable chance that someone already set it up in that format:
```json
{
  "trades": {
    "armor": {
      "1": ["stage1", "stage2"]
    }
  }
}
```

Note that this only allows you to set tradeLevel and career conditions (both required).
Also note that career+profession names have changed throughout Minecraft versions.