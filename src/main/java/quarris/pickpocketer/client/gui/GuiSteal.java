package quarris.pickpocketer.client.gui;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import quarris.pickpocketer.PickPocketer;
import quarris.pickpocketer.container.ContainerSteal;

public class GuiSteal extends GuiContainer {

    public static final ResourceLocation PLAYER_STEAL = new ResourceLocation(PickPocketer.MODID, "textures/gui/player_steal.png");
    public static final ResourceLocation MOB_STEAL = new ResourceLocation(PickPocketer.MODID, "textures/gui/mob_steal.png");

    public final ContainerSteal container;

    public GuiSteal(ContainerSteal container) {
        super(container);
        this.container = container;
    }

    @Override
    public void initGui() {
        this.xSize = 176;
        this.ySize = this.container.isPlayerSteal() ? 227 : 194;
        super.initGui();
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        this.drawDefaultBackground();
        this.mc.getTextureManager().bindTexture(this.container.isPlayerSteal() ? PLAYER_STEAL : MOB_STEAL);
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        if (container.isPlayerSteal()) {
            // Armor text
            this.drawCenteredString(I18n.format("gui.steal.armor_slots"), 60, 8, 0x404040);

            // Offhand text
            this.drawCenteredString(I18n.format("gui.steal.offhand_slot"), 141, 9, 0x404040);

            // Target name
            this.fontRenderer.drawString(this.container.target.getDisplayName().getFormattedText(), 7, 44, 0x404040);

            // Thief name
            this.fontRenderer.drawString(this.container.player.getDisplayName().getFormattedText(), 7, 134, 0x404040);
        } else {

            // Mainhand text
            this.drawCenteredString(I18n.format("gui.steal.mainhand_slot"), 34, 20, 0x404040);

            // Armor text
            this.drawCenteredString(I18n.format("gui.steal.armor_slots"), 89, 13, 0x404040);

            // Offhand text
            this.drawCenteredString(I18n.format("gui.steal.offhand_slot"), 142, 20, 0x404040);

            // Target name
            this.drawCenteredString(this.container.target.getDisplayName().getFormattedText(), 87, 66, 0x404040);

            // Thief name
            this.fontRenderer.drawString(this.container.player.getDisplayName().getFormattedText(), 7, 101, 0x404040);

        }
    }

    private void drawCenteredString(String text, int x, int y, int color) {
        fontRenderer.drawString(text, x - fontRenderer.getStringWidth(text) / 2, y, color);
    }
}
