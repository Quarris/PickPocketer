package quarris.pickpocketer.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import quarris.pickpocketer.Helper;
import quarris.pickpocketer.ModConfig;
import quarris.pickpocketer.PickPocketer;
import quarris.pickpocketer.StealingManager;

@SuppressWarnings("ALL")
@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(value = Side.CLIENT, modid = PickPocketer.MODID)
public class ClientEvents {

    public static final ResourceLocation HIDDEN_STATUS = new ResourceLocation(PickPocketer.MODID, "textures/hidden_status.png");

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void renderHiddenIcon(RenderGameOverlayEvent.Pre event) {
        if (event.getType() == RenderGameOverlayEvent.ElementType.CROSSHAIRS) {

            EntityPlayer player = Minecraft.getMinecraft().player;

            EntityLivingBase target = Helper.getLookingAtEntity(player, player.world, player.getPositionEyes(event.getPartialTicks()), player.getLook(event.getPartialTicks()), 3);

            if (target instanceof EntityPlayer && ((EntityPlayer) target).isCreative())
                return;

            if (target != null) {
                for (String entityName : ModConfig.blacklist) {
                    if (EntityRegistry.getEntry(target.getClass()).getRegistryName().toString().equals(entityName)) {
                        return;
                    }
                }

                int x = event.getResolution().getScaledWidth() / 2 - 10;
                int y = event.getResolution().getScaledHeight() / 2 + 7;

                int u = 0;
                int v = StealingManager.isHiddenFrom(player, target) ? 5 : 0;

                GlStateManager.enableBlend();
                Minecraft.getMinecraft().getTextureManager().bindTexture(HIDDEN_STATUS);
                Gui.drawModalRectWithCustomSizedTexture(x, y, u, v, 21, 5, 21, 10);
            }
        }
    }
}
