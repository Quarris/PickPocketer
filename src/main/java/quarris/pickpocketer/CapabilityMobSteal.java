package quarris.pickpocketer;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.items.IItemHandlerModifiable;
import quarris.pickpocketer.config.MobLootEntry;
import quarris.pickpocketer.config.ModConfig;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class CapabilityMobSteal implements IItemHandlerModifiable, ICapabilitySerializable<NBTTagCompound> {

    private static final Field rand = ObfuscationReflectionHelper.findField(Entity.class, "field_70146_Z");
    private static final Field deathLootTable = ObfuscationReflectionHelper.findField(EntityLiving.class, "field_184659_bA");
    private static final Field deathLootTableSeed = ObfuscationReflectionHelper.findField(EntityLiving.class, "field_184653_bB");
    private static final Method getLootTable = ObfuscationReflectionHelper.findMethod(EntityLiving.class, "func_184647_J", ResourceLocation.class);

    private static Random random = new Random();

    @CapabilityInject(CapabilityMobSteal.class)
    public static Capability<CapabilityMobSteal> INSTANCE;

    public final EntityLiving mob;
    public final int size;
    public final NonNullList<ItemStack> inventory;
    public boolean hasGenerated;

    public CapabilityMobSteal(EntityLiving mob, int size) {
        this.mob = mob;
        this.size = size;
        this.inventory = NonNullList.withSize(size, ItemStack.EMPTY);
    }

    public void populateInventory(EntityPlayer player) {
        if (player.world.isRemote || this.hasGenerated)
            return;

        if (this.generateCustomStealLoot()) {
            this.hasGenerated = true;
            return;
        }

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

                int size = Math.min(this.getInternalSize(), loot.size());
                for (int i = 0; i < size; i++) {
                    this.setStackInSlot(i, loot.get(i));
                }
                this.hasGenerated = true;
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * Checks if custom loot exists in the configs and attempts to generate it.
     *
     * @return True if the loot was generated. False otherwise.
     */
    private boolean generateCustomStealLoot() {
        String mobName = EntityRegistry.getEntry(this.mob.getClass()).getRegistryName().toString();
        if (!ModConfig.lootEntries.containsKey(mobName)) {
            return false;
        }

        MobLootEntry entry = ModConfig.lootEntries.get(mobName);

        List<MobLootEntry.ItemEntry> shuffled = new ArrayList<>(entry.loot);
        Collections.shuffle(shuffled, random);

        int size = Math.min(this.getInternalSize(), shuffled.size());
        for (int i = 0; i < size; i++) {
            this.setStackInSlot(i, shuffled.get(i).generate(random));
        }

        return true;
    }

    /* EQUIPMENT METHODS */

    public EntityEquipmentSlot getEquipSlot(int index) {
        return EntityEquipmentSlot.values()[index - this.getInternalSize()];
    }

    /* ITEM HANDLER METHODS */

    @Override
    public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
        if (slot < this.getInternalSize()) {
            this.inventory.set(slot, stack);
            return;
        }

        if (!this.mob.world.isRemote) {
            PickPocketer.LOGGER.warn("Attempted to set stack {} in out of bounds slot {} with max {} for mob steal capability {}. This is NOT critical but should not have happened.", stack, slot, this.getSlots(), this.mob.getName());
        }
    }

    @Override
    public int getSlots() {
        return this.size + 6;
    }

    public int getInternalSize() {
        return this.size;
    }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int slot) {
        if (slot < this.getInternalSize()) {
            return this.inventory.get(slot);
        } else if (slot < this.getSlots()) {
            return this.mob.getItemStackFromSlot(this.getEquipSlot(slot));
        }
        return ItemStack.EMPTY;
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        throw new UnsupportedOperationException("Attempted to insert slot into a steal inventory. This should not happen. Please report this issue.");
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        ItemStack stack = getStackInSlot(slot);
        if (!simulate) {
            if (slot < this.getInternalSize()) {
                this.inventory.set(slot, ItemStack.EMPTY);
            } else if (slot < this.getSlots()) {
                this.mob.setItemStackToSlot(this.getEquipSlot(slot), ItemStack.EMPTY);
            }
        }
        return stack;
    }

    @Override
    public int getSlotLimit(int slot) {
        return 64;
    }

    /* CAPABILITY METHODS */

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == INSTANCE;
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        return capability == INSTANCE ? (T) this : null;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        ItemStackHelper.saveAllItems(nbt, this.inventory);
        nbt.setBoolean("HasGenerated", this.hasGenerated);
        return nbt;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        ItemStackHelper.loadAllItems(nbt, this.inventory);
        this.hasGenerated = nbt.getBoolean("HasGenerated");
    }
}
