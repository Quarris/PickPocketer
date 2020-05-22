package quarris.pickpocketer;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Random;

public class InventoryMob implements IInventory, INBTSerializable<NBTTagCompound> {

    private static final Field rand = ObfuscationReflectionHelper.findField(Entity.class, "field_70146_Z");
    private static final Field deathLootTable = ObfuscationReflectionHelper.findField(EntityLiving.class, "field_184659_bA");
    private static final Field deathLootTableSeed = ObfuscationReflectionHelper.findField(EntityLiving.class, "field_184653_bB");
    private static final Method getLootTable = ObfuscationReflectionHelper.findMethod(EntityLiving.class, "func_184647_J", ResourceLocation.class);

    public final EntityLiving mob;

    public NonNullList<ItemStack> inventory;

    public InventoryMob(EntityLiving mob) {
        this.mob = mob;
        this.inventory = NonNullList.withSize(5, ItemStack.EMPTY);
    }

    public void populateMobInventory(EntityPlayer player) {
        if (player.world.isRemote)
            return;

        if (this.mob.getEntityData().hasKey("PP:Inventory")) {
            this.deserializeNBT(this.mob.getEntityData().getCompoundTag("PP:Inventory"));
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
    public int getSizeInventory() {
        return 11;
    }

    @Override
    public boolean isEmpty() {
        if (!this.inventory.isEmpty()) {
            for (ItemStack stack : this.mob.getEquipmentAndArmor()) {
                if (!stack.isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        this.validateIndex(index);

        if (index < 5) {
            return this.inventory.get(index);
        }

        return this.mob.getItemStackFromSlot(EntityEquipmentSlot.values()[index - 5]);
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        ItemStack stack = this.getStackInSlot(index);

        if (stack.getCount() < count) {
            return stack.copy();
        }

        int remain = stack.getCount() - count;
        stack.setCount(remain);

        ItemStack ret = stack.copy();
        ret.setCount(count);
        return ret;
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        ItemStack stack = getStackInSlot(index);

        if (index < 5) {
            this.inventory.set(index, ItemStack.EMPTY);
        } else {
            EntityEquipmentSlot slot = EntityEquipmentSlot.values()[index - 5];
            this.mob.setItemStackToSlot(slot, ItemStack.EMPTY);
        }

        this.save();

        return stack;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        if (index < 5) {
            this.inventory.set(index, stack);
        } else {
            EntityEquipmentSlot slot = EntityEquipmentSlot.values()[index - 5];
            this.mob.setItemStackToSlot(slot, stack);
        }
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public void markDirty() {

    }

    public void save() {
        this.mob.getEntityData().setTag("PP:Inventory", this.serializeNBT());
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        return this.mob.getDistance(player) <= 3.0D;
    }

    @Override
    public void openInventory(EntityPlayer player) {

    }

    @Override
    public void closeInventory(EntityPlayer player) {

    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return false;
    }

    @Override
    public int getField(int id) {
        return 0;
    }

    @Override
    public void setField(int id, int value) {

    }

    @Override
    public int getFieldCount() {
        return 0;
    }

    @Override
    public void clear() {

    }

    @Override
    public String getName() {
        return this.getDisplayName().getFormattedText();
    }

    @Override
    public boolean hasCustomName() {
        return true;
    }

    @Override
    public ITextComponent getDisplayName() {
        return this.mob.getDisplayName();
    }

    private void validateIndex(int index) {
        if (index >= this.getSizeInventory()) {
            throw new IllegalArgumentException("Invalid slot index " + index + " for mob " + this.mob);
        }
    }

    @Override
    public NBTTagCompound serializeNBT() {
        return ItemStackHelper.saveAllItems(new NBTTagCompound(), this.inventory);
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        ItemStackHelper.loadAllItems(nbt, this.inventory);
    }
}
