package quarris.pickpocketer.config;


import net.minecraftforge.common.config.Config;
import quarris.pickpocketer.PickPocketer;

import java.util.HashMap;
import java.util.Map;

@Config(modid = PickPocketer.MODID)
public class ModConfig {

    @Config.Comment("The cooldown after a steal from another player (in ticks)")
    @Config.RangeInt(min = 0, max = 12000)
    public static int cooldown = 400;

    @Config.Comment("The chance of notifying the victim when their item was stolen")
    @Config.RangeDouble(min = 0, max = 1)
    public static float notifyChance = 0.5f;

    @Config.RequiresWorldRestart
    @Config.Comment({
            "Blacklist for entities which should not be able to be stolen from.",
            "Layout: <modid>:<entity>"
    })
    public static String[] blacklist = new String[]{
            "minecraft:wither",
            "minecraft:ender_dragon"
    };

    @Config.Comment({
            "This config allows to modify loot table for specified mobs",
            "Each of the entries require the same amount of values"
    })


    public static MobLootOverrides lootOverrides = new MobLootOverrides();

    @Config.Ignore
    public static Map<String, MobLootEntry> lootEntries = new HashMap<>();

    public static void sync() {
        PickPocketer.LOGGER.info("Syncing configs for Pick Pocketer");
        for (Map.Entry<String, String[]> ovItems : lootOverrides.lootItemOverrides.entrySet()) {
            String mobName = ovItems.getKey();
            Integer[] minSize = lootOverrides.lootMinOverrides.get(mobName);
            Integer[] maxSize = lootOverrides.lootMaxOverrides.get(mobName);

            if (minSize == null || maxSize == null) {
                PickPocketer.LOGGER.warn("Could not sync item override entry. Could not find min or max entries for {}", mobName);
                continue;
            }

            try {
                lootEntries.put(ovItems.getKey(), new MobLootEntry(mobName, ovItems.getValue(), minSize, maxSize));
            } catch (RuntimeException e) {
                PickPocketer.LOGGER.warn("Error syncing loot entry.", e);
            }
        }
    }

    public static class MobLootOverrides {

        @Config.Comment({
                "Loot override items. Format: ",
                "S:\"modid:entityName\" <",
                "   modid:itemName",
                "   modid:item2Name",
                ">"
        })
        public Map<String, String[]> lootItemOverrides = new HashMap<>();

        @Config.Comment({
                "Loot override min amount. Format: ",
                "S:\"modid:entityName\" <",
                "   itemMinAmount",
                "   item2MinAmount",
                ">"
        })
        public Map<String, Integer[]> lootMinOverrides = new HashMap<>();

        @Config.Comment({
                "Loot override max amount. Format: ",
                "S:\"modid:entityName\" <",
                "   itemMaxAmount",
                "   item2MaxAmount",
                ">"
        })
        public Map<String, Integer[]> lootMaxOverrides = new HashMap<>();

        public MobLootOverrides() {
        }
    }
}
