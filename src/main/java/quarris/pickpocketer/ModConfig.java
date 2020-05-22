package quarris.pickpocketer;


import net.minecraftforge.common.config.Config;

@Config(modid = PickPocketer.MODID)
public class ModConfig {

    @Config.Comment("The cooldown after a steal from another player (in ticks)")
    @Config.RangeInt(min = 0, max = 12000)
    public static int cooldown = 400;

    @Config.Comment("The chance of notifying the victim when their item was stolen")
    @Config.RangeDouble(min = 0, max = 1)
    public static float notifyChance = 0.5f;

    @Config.Comment({
            "Blacklist for entities which should not be able to be stolen from.",
            "Layout: <modid>:<entity>"
    })
    public static String[] blacklist = new String[] {
            "minecraft:wither",
            "minecraft:ender_dragon"
    };
}
