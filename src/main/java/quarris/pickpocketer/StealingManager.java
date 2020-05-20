package quarris.pickpocketer;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPacketEntity;
import net.minecraft.network.play.server.SPacketEntityMetadata;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.server.FMLServerHandler;

@SuppressWarnings("unused")
@Mod.EventBusSubscriber(modid = PickPocketer.MODID)
public class StealingManager {

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteractSpecific event) {
        if (event.getWorld().isRemote)
            return;

        if (event.getTarget() instanceof EntityLivingBase) {
            EntityPlayer player = event.getEntityPlayer();

            if (!player.isSneaking())
                return;

            EntityLivingBase target = (EntityLivingBase) event.getTarget();

            StealingAttemptResult result = canStealFrom(player, target);

            if (result == StealingAttemptResult.INV_FULL)
                player.sendStatusMessage(new TextComponentTranslation("stealing.attempt.inv_full"), true);
            else if (result == StealingAttemptResult.NOT_HIDDEN)
                player.sendStatusMessage(new TextComponentTranslation("stealing.attempt.not_hidden"), true);
            else if (result == StealingAttemptResult.CAN_STEAL) {
                openStealingContainer(player, target);
            }
        }
    }

    @SubscribeEvent
    public static void setTarget(LivingSetAttackTargetEvent event) {
        if (event.getTarget() instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP) event.getTarget();
            player.connection.sendPacket(new SPacketEntityMetadata());
        }
    }

    private static void openStealingContainer(EntityPlayer thief, EntityLivingBase target) {
        thief.openGui(PickPocketer.MODID, 0, thief.world, target.getEntityId(), 0, 0);
    }

    public static StealingAttemptResult canStealFrom(EntityPlayer thief, EntityLivingBase entity) {
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
        return entity.getLookVec().dotProduct(directionToPlayer) > 0;
    }

    public enum StealingAttemptResult {
        CAN_STEAL, INV_FULL, NOT_HIDDEN
    }

}
