package quarris.pickpocketer;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import quarris.pickpocketer.config.ModConfig;

@SuppressWarnings("unused")
@Mod.EventBusSubscriber(modid = PickPocketer.MODID)
public class StealingManager {

    @SubscribeEvent
    public static void attachCaps(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof EntityLiving) {
            if (StealingManager.isStealableFrom((EntityLiving)event.getObject())) {
                event.addCapability(new ResourceLocation(PickPocketer.MODID, "mob_steal"), new CapabilityMobSteal((EntityLiving) event.getObject(), 5));
            }
        }
    }

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteractSpecific event) {
        if (event.getWorld().isRemote)
            return;

        if (event.getTarget() instanceof EntityLivingBase) {
            EntityPlayer player = event.getEntityPlayer();
            EntityLivingBase target = (EntityLivingBase) event.getTarget();

            if (!isStealableFrom(target))
                return;

            if (!player.isSneaking() || (target instanceof EntityPlayer && ((EntityPlayer) target).isCreative())) {
                return;
            }

            StealingAttemptResult result = canStealFrom(player, target);

            if (result == StealingAttemptResult.INV_FULL)
                player.sendStatusMessage(new TextComponentTranslation("stealing.attempt.inv_full"), true);
            else if (result == StealingAttemptResult.NOT_HIDDEN)
                player.sendStatusMessage(new TextComponentTranslation("stealing.attempt.not_hidden"), true);
            else if (result == StealingAttemptResult.COOLDOWN) {
                player.sendStatusMessage(new TextComponentTranslation("stealing.attempt.cooldown"), true);
            } else if (result == StealingAttemptResult.CAN_STEAL) {
                openStealingContainer(player, target);
            }
        }
    }

    private static void openStealingContainer(EntityPlayer thief, EntityLivingBase target) {
        thief.openGui(PickPocketer.MODID, 0, thief.world, target.getEntityId(), 0, 0);
    }

    public static StealingAttemptResult canStealFrom(EntityPlayer thief, EntityLivingBase entity) {
        if (entity instanceof EntityPlayer) {
            if (thief.getEntityData().hasKey("PP:StolenTime") &&
                    thief.world.getTotalWorldTime() <= thief.getEntityData().getLong("PP:StolenTime") + ModConfig.cooldown
            ) {
                return StealingAttemptResult.COOLDOWN;
            }
        }

        if (isHiddenFrom(thief, entity)) {
            if (thief.inventory.mainInventory.stream().anyMatch(ItemStack::isEmpty)) {
                return StealingAttemptResult.CAN_STEAL;
            }
            return StealingAttemptResult.INV_FULL;
        }
        return StealingAttemptResult.NOT_HIDDEN;
    }

    public static boolean isHiddenFrom(EntityPlayer thief, EntityLivingBase entity) {
        return isBehind(thief, entity);
    }

    private static boolean isBehind(EntityPlayer thief, EntityLivingBase entity) {
        Vec3d directionToPlayer = entity.getPositionVector().subtract(thief.getPositionVector());
        Vec3d look = PickPocketer.proxy.getEntityLook(entity);

        return look.dotProduct(directionToPlayer) > 0;
    }

    public static Vec3d getVectorForRotation(float pitch, float yaw) {
        float f = MathHelper.cos(-yaw * 0.017453292F - (float) Math.PI);
        float f1 = MathHelper.sin(-yaw * 0.017453292F - (float) Math.PI);
        float f2 = -MathHelper.cos(-pitch * 0.017453292F);
        return new Vec3d((f1 * f2), 0, (f * f2));
    }

    public static boolean isStealableFrom(EntityLivingBase entity) {
        return !Helper.isEntityInArray(ModConfig.blacklist, entity);
    }

    public enum StealingAttemptResult {
        CAN_STEAL, INV_FULL, NOT_HIDDEN, COOLDOWN
    }

}
