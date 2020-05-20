package quarris.pickpocketer;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import quarris.pickpocketer.client.gui.GuiSteal;
import quarris.pickpocketer.container.ContainerSteal;

import javax.annotation.Nullable;

public class ModGuiHandler implements IGuiHandler {
    @Nullable
    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        if (ID == 0) {
            EntityLivingBase target = (EntityLivingBase) world.getEntityByID(x); // 'x' is used for entity id data
            return new ContainerSteal(player, target);
        }
        return null;
    }

    @Nullable
    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        if (ID == 0) {
            EntityLivingBase target = (EntityLivingBase) world.getEntityByID(x); // 'x' is used for entity id data
            return new GuiSteal(new ContainerSteal(player, target));
        }
        return null;
    }
}

