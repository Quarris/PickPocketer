package quarris.pickpocketer;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Random;

public class InventoryMob extends InventoryBasic implements INBTSerializable<NBTTagCompound> {

    private static final Field rand = ObfuscationReflectionHelper.findField(Entity.class, "field_70146_Z");
    private static final Field deathLootTable = ObfuscationReflectionHelper.findField(EntityLiving.class, "field_184659_bA");
    private static final Field deathLootTableSeed = ObfuscationReflectionHelper.findField(EntityLiving.class, "field_184653_bB");
    private static final Method getLootTable = ObfuscationReflectionHelper.findMethod(EntityLiving.class, "func_184647_J", ResourceLocation.class);

    public final EntityLiving mob;

    public InventoryMob(EntityLiving mob) {
        super(mob.getDisplayName().getFormattedText(), false, 5);
        this.mob = mob;
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        if (index < 5) {
            return super.getStackInSlot(index);
        } else if (index < 11) {
            return this.mob.getItemStackFromSlot(this.getSlot(index));
        } else
            return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        if (index < 5) {
            return super.removeStackFromSlot(index);
        } else if (index < 11){
            ItemStack equipment = this.getStackInSlot(index);
            if (!equipment.isEmpty()) {
                this.mob.setItemStackToSlot(this.getSlot(index), ItemStack.EMPTY);
            }
            return equipment;
        } else
            return ItemStack.EMPTY;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        if (index < 5) {
            super.setInventorySlotContents(index, stack);
        }
    }

    private EntityEquipmentSlot getSlot(int index) {
        return EntityEquipmentSlot.values()[index-5];
    }

    @Override
    public boolean isEmpty() {
        if (super.isEmpty()) {
            for (ItemStack stack : this.mob.getEquipmentAndArmor()) {
                if (!stack.isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    public void save() {
        this.mob.getEntityData().setTag("PP:Inventory", this.serializeNBT());
    }

    public void populateMobInventory(EntityPlayer player) {
        if (player.world.isRemote)
            return;

        if (this.mob.getEntityData().hasKey("PP:Inventory")) {
            this.deserializeNBT(this.mob.getEntityData().getCompoundTag("PP:Inventory"));
            return;
        }

        /*
        for (ModConfig.MobLootEntry entry : ModConfig.lootOverrides) {
            if (EntityRegistry.getEntry(this.mob.getClass()).getRegistryName().toString().equals(entry.mob)) {
                int i = 0;
                try {
                    int size = Math.min(5, entry.items.length);
                    for (; i < size; i++) {
                        String item = entry.items[i];
                        int min = entry.min[i];
                        int max = entry.max[i];
                        int amount = min + this.mob.world.rand.nextInt(max-min);
                        ItemStack stack = new ItemStack(Objects.requireNonNull(ForgeRegistries.ITEMS.getValue(new ResourceLocation(item))), amount);
                        this.setInventorySlotContents(i, stack);
                    }
                    this.save();
                    return;
                } catch (ArrayIndexOutOfBoundsException e) {
                    PickPocketer.LOGGER.error("Could not load loot override for entity {}. The items, min and max count does not match.", entry.mob);
                } catch (NullPointerException e) {
                    PickPocketer.LOGGER.error("Could not load loot override for entity {}. The item {} was not found.", entry.mob, entry.items[i]);
                }
            }
        }
         */

        try {
            ResourceLocation resourcelocation = (ResourceLocation) deathLootTable.get(mob);

            if (resourcelocation == null) {
                resourcelocation = (ResourceLocation) getLootTable.invoke(mob);
            }

            if (resourcelocation != null) {
                LootTable loottable = mob.world.getLootTableManager().getLootTableFromLocation(resourcelocation);

                DamageSource source = DamageSource.causePlayerDamage(player);

                LootContext.Builder lootcontext$builder = (new LootContext.Builder((WorldServer) mob.world)).withLootedEntity(mob).withDamageSource(source).withPlayer(player).withLuck(player.getLuck());

                List<ItemStack> loot = loottable.generateLootForPools(deathLootTableSeed.getFloat(mob) == 0L ? (Random) rand.get(mob) : new Random(deathLootTableSeed.getLong(mob)), lootcontext$builder.build());

                int size = Math.min(5, loot.size());
                for (int i = 0; i < size; i++) {
                    this.setInventorySlotContents(i, loot.get(i));
                }

                this.save();
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NonNullList<ItemStack> inv = NonNullList.withSize(5, ItemStack.EMPTY);
        for (int i = 0; i < 5; i++) {
            inv.set(i, this.getStackInSlot(i));
        }
        return ItemStackHelper.saveAllItems(new NBTTagCompound(), inv);
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        NonNullList<ItemStack> inv = NonNullList.withSize(5, ItemStack.EMPTY);
        ItemStackHelper.loadAllItems(nbt, inv);

        for (int i = 0; i < 5; i++) {
            this.setInventorySlotContents(i, inv.get(i));
        }
    }
}
