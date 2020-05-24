package quarris.pickpocketer.config;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.*;

public class MobLootEntry {

    public final String mob;
    public final List<ItemEntry> loot;

    public MobLootEntry(String mob, String[] itemNames, Integer[] minCount, Integer[] maxCount) throws RuntimeException {
        this.mob = mob;
        this.loot = new ArrayList<>();

        List<String> items = Arrays.asList(itemNames);
        List<Integer> minSize = Arrays.asList(minCount);
        List<Integer> maxSize = Arrays.asList(maxCount);

        if (!(items.size() == minSize.size() && minSize.size() == maxSize.size())) {
            throw new RuntimeException("The item, min and max loot entries for mob " + mob + " are not of equal size");
        }

        for (int i = 0; i < items.size(); i++) {
            this.loot.add(new ItemEntry(new ResourceLocation(items.get(i)), minSize.get(i), maxSize.get(i)));
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MobLootEntry{");
        sb.append("mob='").append(mob).append('\'');
        sb.append(", loot=").append(loot);
        sb.append('}');
        return sb.toString();
    }

    public static class ItemEntry {
        public final Item item;
        public final int min;
        public final int max;

        public ItemEntry(ResourceLocation itemName, int min, int max) throws RuntimeException {
            if (!ForgeRegistries.ITEMS.containsKey(itemName)) {
                throw new RuntimeException("Item "+itemName+" not found.");
            }
            this.item = ForgeRegistries.ITEMS.getValue(itemName);
            this.min = min;
            this.max = max;
        }

        public ItemStack generate(Random rand) {
            return new ItemStack(this.item, this.min + rand.nextInt(this.max - this.min));
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("ItemEntry{");
            sb.append("item=").append(item);
            sb.append(", min=").append(min);
            sb.append(", max=").append(max);
            sb.append('}');
            return sb.toString();
        }
    }

}
