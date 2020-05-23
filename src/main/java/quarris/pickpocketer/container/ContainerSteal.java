package quarris.pickpocketer.container;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import quarris.pickpocketer.InventoryMob;
import quarris.pickpocketer.ModConfig;
import quarris.pickpocketer.StealingManager;
import quarris.pickpocketer.network.PacketHandler;
import quarris.pickpocketer.network.PacketSyncPlayer;

import javax.annotation.Nullable;

@SuppressWarnings("NullableProblems")
public class ContainerSteal extends Container {

    private static final EntityEquipmentSlot[] VALID_EQUIPMENT_SLOTS =
            new EntityEquipmentSlot[]{
                    EntityEquipmentSlot.HEAD,
                    EntityEquipmentSlot.CHEST,
                    EntityEquipmentSlot.LEGS,
                    EntityEquipmentSlot.FEET
            };

    public final EntityPlayer player;
    public final EntityLivingBase target;

    public ContainerSteal(EntityPlayer player, EntityLivingBase target) {
        this.target = target;
        this.player = player;

        int yPlayerInv = this.isPlayerSteal() ? 145 : 112;

        // Player Inventory
        for (int j = 0; j < 3; ++j) {
            for (int i = 0; i < 9; ++i) {
                this.addSlotToContainer(new Slot(player.inventory, i + (j + 1) * 9, 8 + i * 18, yPlayerInv + j * 18));
            }
        }

        // Player Hotbar
        for (int i = 0; i < 9; ++i) {
            this.addSlotToContainer(new Slot(player.inventory, i, 8 + i * 18, yPlayerInv + 58));
        }

        if (this.isPlayerSteal()) {
            this.addPlayerStealInventory((EntityPlayer) target);
        } else {
            this.addMobStealInventory((EntityLiving) target);
        }
    }

    public boolean isPlayerSteal() {
        return this.target instanceof EntityPlayer;
    }

    @Override
    public ItemStack slotClick(int slotId, int dragType, ClickType clickType, EntityPlayer player) {
        if (slotId > 35) {
            if (clickType == ClickType.SWAP)
                return ItemStack.EMPTY;

            if (this.isPlayerSteal()) {
                if (!player.world.isRemote) {
                    if (player.world.rand.nextFloat() > ModConfig.notifyChance) {
                        player.sendStatusMessage(new TextComponentTranslation("stealing.notify.player")
                                .setStyle(new Style().setColor(TextFormatting.RED)), true);
                        ((EntityPlayer) this.target).sendStatusMessage(new TextComponentTranslation("stealing.notify.target")
                                .setStyle(new Style().setColor(TextFormatting.RED)), true);
                    }
                    player.getEntityData().setLong("PP:StolenTime", player.world.getTotalWorldTime());
                    PacketHandler.sendTo(new PacketSyncPlayer(player.world.getTotalWorldTime()), (EntityPlayerMP) player);
                }
            }

            ItemStack stack = transferStackInSlot(player, slotId);
            if (this.getSlot(slotId).inventory instanceof InventoryMob) {
                ((InventoryMob) this.getSlot(slotId).inventory).save();
            }
            return stack;
        }

        return super.slotClick(slotId, dragType, clickType, player);
    }


    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return player.getDistance(this.target) <= 3 &&
                StealingManager.isHiddenFrom(player, this.target) &&
                (!this.isPlayerSteal() ||
                        !player.getEntityData().hasKey("PP:StolenTime") ||
                        player.world.getTotalWorldTime() > player.getEntityData().getLong("PP:StolenTime") + ModConfig.cooldown);
    }


    /**
     * Handle when the stack in slot {@code index} is shift-clicked. Normally this moves the stack between the player
     * inventory and the other inventory(s).
     */
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack()) {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();

            if (index < 27) {
                if (!this.mergeItemStack(itemstack1, 27, 36, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index < 36) {
                if (!this.mergeItemStack(itemstack1, 0, 27, false)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (!this.mergeItemStack(itemstack1, 0, 36, false)) {
                    return ItemStack.EMPTY;
                }
            }
        }

        return itemstack;
    }


    private void addMobStealInventory(EntityLiving entity) {
        InventoryMob inventory = new InventoryMob(entity);
        if (!entity.world.isRemote)
            inventory.populateMobInventory(this.player);

        // Mob Loot
        for (int i = 0; i < 5; i++) {
            this.addSlotToContainer(new Slot(inventory, i, 44 + i * 18, 78));
        }

        // Mob Left Hand
        this.addSlotToContainer(new Slot(inventory, 5, 26, 33) {
            @SideOnly(Side.CLIENT)
            public String getSlotTexture() {
                return "minecraft:items/empty_armor_slot_shield";
            }
        });

        // Mob Right Hand
        this.addSlotToContainer(new Slot(inventory, 6, 134, 33));

        // Mob Armor
        for (int i = 7; i < 11; i++) {
            int x = (i - 7) % 2;
            int y = (i - 7) / 2;
            final EntityEquipmentSlot entityequipmentslot = VALID_EQUIPMENT_SLOTS[i - 7];
            this.addSlotToContainer(new Slot(inventory, i, 71 + x * 18, 24 + y * 18) {

                public int getSlotStackLimit() {
                    return 1;
                }

                public boolean isItemValid(ItemStack stack) {
                    return stack.getItem().isValidArmor(stack, entityequipmentslot, player);
                }

                public boolean canTakeStack(EntityPlayer playerIn) {
                    ItemStack itemstack = this.getStack();
                    return (itemstack.isEmpty() || playerIn.isCreative() || !EnchantmentHelper.hasBindingCurse(itemstack)) && super.canTakeStack(playerIn);
                }

                @Nullable
                @SideOnly(Side.CLIENT)
                public String getSlotTexture() {
                    return ItemArmor.EMPTY_SLOT_NAMES[entityequipmentslot.getIndex()];
                }
            });
        }
    }

    private void addPlayerStealInventory(EntityPlayer player) {
        // Target equipment slots
        for (int i = 0; i < VALID_EQUIPMENT_SLOTS.length; ++i) {
            final EntityEquipmentSlot entityequipmentslot = VALID_EQUIPMENT_SLOTS[i];
            this.addSlotToContainer(new Slot(player.inventory, 36 + (3 - i), 26 + i * 18, 21) {
                /**
                 * Returns the maximum stack size for a given slot (usually the same as getInventoryStackLimit(), but 1
                 * in the case of armor slots)
                 */
                public int getSlotStackLimit() {
                    return 1;
                }

                /**
                 * Check if the stack is allowed to be placed in this slot, used for armor slots as well as furnace
                 * fuel.
                 */
                public boolean isItemValid(ItemStack stack) {
                    return stack.getItem().isValidArmor(stack, entityequipmentslot, player);
                }

                /**
                 * Return whether this slot's stack can be taken from this slot.
                 */
                public boolean canTakeStack(EntityPlayer playerIn) {
                    ItemStack itemstack = this.getStack();
                    return (itemstack.isEmpty() || playerIn.isCreative() || !EnchantmentHelper.hasBindingCurse(itemstack)) && super.canTakeStack(playerIn);
                }

                @Nullable
                @SideOnly(Side.CLIENT)
                public String getSlotTexture() {
                    return ItemArmor.EMPTY_SLOT_NAMES[entityequipmentslot.getIndex()];
                }
            });
        }

        // Target Inventory
        for (int j = 0; j < 3; ++j) {
            for (int i = 0; i < 9; ++i) {
                this.addSlotToContainer(new Slot(player.inventory, i + (j + 1) * 9, 8 + i * 18, 55 + j * 18));
            }
        }

        // Target Hotbar
        for (int i = 0; i < 9; ++i) {
            this.addSlotToContainer(new Slot(player.inventory, i, 8 + i * 18, 111));
        }

        // Target Offhand
        this.addSlotToContainer(new Slot(player.inventory, 40, 134, 21) {
            @Nullable
            @SideOnly(Side.CLIENT)
            public String getSlotTexture() {
                return "minecraft:items/empty_armor_slot_shield";
            }
        });
    }
}
