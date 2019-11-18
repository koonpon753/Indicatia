package stevekung.mods.indicatia.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.lwjgl.input.Mouse;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import stevekung.mods.indicatia.config.ExtendedConfig;
import stevekung.mods.indicatia.gui.GuiDropdownMinigames.IDropboxCallback;
import stevekung.mods.indicatia.utils.*;

public class GuiIndicatiaChat implements IGuiChat, IDropboxCallback
{
    private GuiDropdownMinigames dropdown;
    private int prevSelect = -1;
    private ChatMode mode = ChatMode.ALL;

    @Override
    public void initGui(List<GuiButton> buttonList, int width, int height)
    {
        this.updateButton(buttonList, width, height);
        this.mode = ChatMode.VALUES[ExtendedConfig.instance.chatMode];
    }

    @Override
    public void drawScreen(List<GuiButton> buttonList, int mouseX, int mouseY, float partialTicks)
    {
        buttonList.forEach(button ->
        {
            if (button instanceof GuiButtonCustomize)
            {
                GuiButtonCustomize customButton = (GuiButtonCustomize) button;
                customButton.drawRegion(mouseX, mouseY);
                GlStateManager.enableDepth();
            }
        });

        if (InfoUtils.INSTANCE.isHypixel())
        {
            Minecraft mc = Minecraft.getMinecraft();
            ScaledResolution res = new ScaledResolution(mc);
            mc.fontRendererObj.drawStringWithShadow("CHAT MODE: " + this.mode.getDesc(), 2, res.getScaledHeight() - 30, ColorUtils.rgbToDecimal(255, 255, 255));
        }
    }

    @Override
    public void updateScreen(List<GuiButton> buttonList, int width, int height)
    {
        if (InfoUtils.INSTANCE.isHypixel())
        {
            if (this.prevSelect != ExtendedConfig.instance.selectedHypixelMinigame)
            {
                this.updateButton(buttonList, width, height);
                this.prevSelect = ExtendedConfig.instance.selectedHypixelMinigame;
            }

            boolean clicked = !this.dropdown.dropdownClicked;

            buttonList.forEach(button ->
            {
                if (button instanceof GuiButtonCustomize)
                {
                    GuiButtonCustomize buttonCustom = (GuiButtonCustomize) button;
                    buttonCustom.visible = clicked;
                }
            });
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {}

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {}

    @Override
    public void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {}

    @Override
    public void actionPerformed(GuiButton button)
    {
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayerSP player = mc.thePlayer;

        if (player == null || !(mc.currentScreen instanceof GuiChat))
        {
            return;
        }

        if (button instanceof GuiButtonCustomize)
        {
            GuiButtonCustomize buttomCustom = (GuiButtonCustomize) button;

            if (button.id == buttomCustom.id)
            {
                player.sendChatMessage(buttomCustom.command);
            }
        }

        switch (button.id)
        {
        case 200:
            this.mode = ChatMode.ALL;
            player.sendChatMessage("/chat a");
            ExtendedConfig.instance.chatMode = 0;
            break;
        case 201:
            this.mode = ChatMode.PARTY;
            player.sendChatMessage("/chat p");
            ExtendedConfig.instance.chatMode = 1;
            break;
        case 202:
            this.mode = ChatMode.GUILD;
            player.sendChatMessage("/chat g");
            ExtendedConfig.instance.chatMode = 2;
            break;
        case 203:
            this.mode = ChatMode.SKYBLOCK_COOP;
            ExtendedConfig.instance.chatMode = 3;
            break;
        }
    }

    @Override
    public String sendChatMessage(String original)
    {
        if (this.mode == ChatMode.SKYBLOCK_COOP)
        {
            return this.mode.getCommand() + " " + original;
        }
        else
        {
            return original;
        }
    }

    @Override
    public void onGuiClosed()
    {
        ExtendedConfig.instance.save();
    }

    @Override
    public void handleMouseInput(int width, int height)
    {
        Minecraft mc = Minecraft.getMinecraft();
        int mouseX = Mouse.getEventX() * width / mc.displayWidth;
        int mouseY = height - Mouse.getEventY() * height / mc.displayHeight - 1;

        if (this.dropdown != null && this.dropdown.dropdownClicked && this.dropdown.isHoverDropdown(mouseX, mouseY))
        {
            int i = Mouse.getEventDWheel();

            if (i != 0)
            {
                if (i > 1)
                {
                    i = -1;
                }
                if (i < -1)
                {
                    i = 1;
                }
                if (GuiScreen.isCtrlKeyDown())
                {
                    i *= 7;
                }
                this.dropdown.scroll(i);
            }
        }
    }

    @Override
    public void pageUp() {}

    @Override
    public void pageDown() {}

    @Override
    public void getSentHistory(int msgPos) {}

    @Override
    public void onSelectionChanged(GuiDropdownMinigames dropdown, int selection)
    {
        ExtendedConfig.instance.selectedHypixelMinigame = selection;
        ExtendedConfig.instance.save();
    }

    @Override
    public int getInitialSelection(GuiDropdownMinigames dropdown)
    {
        return ExtendedConfig.instance.selectedHypixelMinigame;
    }

    private void updateButton(List<GuiButton> buttonList, int width, int height)
    {
        Minecraft mc = Minecraft.getMinecraft();
        buttonList.clear();

        if (InfoUtils.INSTANCE.isHypixel())
        {
            List<String> list = new ArrayList<>();

            for (MinigameData data : MinigameData.getMinigameData())
            {
                list.add(data.getName());
            }

            String max = Collections.max(list, Comparator.comparing(text -> text.length()));
            int length = mc.fontRendererObj.getStringWidth(max) + 32;

            // hypixel chat
            buttonList.add(new GuiButton(200, width - 23, height - 35, 20, 20, "A"));
            buttonList.add(new GuiButton(201, width - 23, height - 56, 20, 20, "P"));
            buttonList.add(new GuiButton(202, width - 23, height - 77, 20, 20, "G"));
            buttonList.add(new GuiButton(203, width - 31, height - 98, 28, 20, "COOP"));
            buttonList.add(this.dropdown = new GuiDropdownMinigames(this, width - length, 2, list));
            this.dropdown.width = length;
            this.prevSelect = ExtendedConfig.instance.selectedHypixelMinigame;

            List<GuiButtonCustomize> gameBtn = new ArrayList<>();
            int xPos2 = width - 99;

            if (this.prevSelect > list.size())
            {
                this.prevSelect = 0;
                ExtendedConfig.instance.selectedHypixelMinigame = 0;
            }

            for (MinigameData data : MinigameData.getMinigameData())
            {
                for (MinigameCommand command : data.getCommands())
                {
                    if (data.getName().equals(list.get(this.prevSelect)))
                    {
                        gameBtn.add(new GuiButtonCustomize(width, command.getName(), command.getCommand(), command.isMinigame()));
                    }
                }
            }

            for (int i = 0; i < gameBtn.size(); i++)
            {
                GuiButtonCustomize button = gameBtn.get(i);

                if (i >= 6 && i <= 10)
                {
                    button.xPosition = xPos2 - 136;
                    button.yPosition = 41;
                }
                else if (i >= 11 && i <= 15)
                {
                    button.xPosition = xPos2 - 241;
                    button.yPosition = 62;
                }
                else if (i >= 16 && i <= 20)
                {
                    button.xPosition = xPos2 - 346;
                    button.yPosition = 83;
                }
                else if (i >= 21)
                {
                    button.xPosition = xPos2 - 451;
                    button.yPosition = 104;
                }
                button.xPosition += 21 * i;
                buttonList.add(button);
            }
        }

        for (GuiButton button : buttonList)
        {
            if (!button.getClass().equals(GuiDropdownMinigames.class) && !(button.id >= 0 && button.id <= 203))
            {
                button.visible = false;
            }
        }
    }

    public enum ChatMode
    {
        ALL("/achat", "All Chat"),
        PARTY("/pchat", "Party Chat"),
        GUILD("/gchat", "Guild Chat"),
        SKYBLOCK_COOP("/cc", "Coop Chat");

        private final String command;
        private final String desc;
        public static final ChatMode[] VALUES = ChatMode.values();

        private ChatMode(String command, String desc)
        {
            this.command = command;
            this.desc = desc;
        }

        public String getCommand()
        {
            return this.command;
        }

        public String getDesc()
        {
            return this.desc;
        }
    }
}