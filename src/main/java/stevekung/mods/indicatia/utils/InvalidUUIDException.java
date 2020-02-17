package stevekung.mods.indicatia.utils;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiErrorScreen;
import net.minecraftforge.fml.client.CustomModLoadingErrorDisplayException;

public class InvalidUUIDException extends CustomModLoadingErrorDisplayException
{
    @Override
    public void initGui(GuiErrorScreen errorScreen, FontRenderer fontRenderer) {}

    @Override
    public void drawScreen(GuiErrorScreen errorScreen, FontRenderer fontRenderer, int mouseX, int mouseY, float partialTicks)
    {
        errorScreen.drawDefaultBackground();
        int offset = 75;

        errorScreen.drawCenteredString(fontRenderer, "Indicatia SkyBlock couldn't find a matched UUID in our database", errorScreen.width / 2, offset, 0xFFFFFF);
        offset += 20;
        errorScreen.drawCenteredString(fontRenderer, "Make sure you have already tell your IGN in #ign-verify! :)", errorScreen.width / 2, offset, 0xFFFFFF);
    }
}