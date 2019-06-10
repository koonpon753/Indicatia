package stevekung.mods.indicatia.gui;

import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.entity.player.PlayerModelPart;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import stevekung.mods.indicatia.config.ExtendedConfig;
import stevekung.mods.indicatia.utils.CapeUtils;
import stevekung.mods.indicatia.utils.ThreadDownloadedCustomCape;
import stevekung.mods.stevekungslib.utils.JsonUtils;
import stevekung.mods.stevekungslib.utils.LangUtils;

@OnlyIn(Dist.CLIENT)
public class GuiCustomCape extends Screen
{
    private TextFieldWidget inputField;
    private Button doneBtn;
    private Button resetBtn;
    private Button capeBtn;
    private int capeOption;
    private int prevCapeOption;

    public GuiCustomCape()
    {
        super(JsonUtils.create("Custom Cape"));
    }

    @Override
    public void init()
    {
        this.minecraft.keyboardListener.enableRepeatEvents(true);
        this.inputField = new TextFieldWidget(this.font, this.width / 2 - 150, this.height / 4 + 85, 300, 20, "Cape Input");
        this.inputField.setMaxStringLength(32767);
        this.inputField.setFocused(true);
        this.inputField.setCanLoseFocus(true);
        this.doneBtn = this.addButton(new Button(this.width / 2 - 50 - 100 - 4, this.height / 4 + 100 + 12, 100, 20, LangUtils.translate("gui.done"), button ->
        {
            if (!GuiCustomCape.this.inputField.getText().isEmpty())
            {
                ThreadDownloadedCustomCape thread = new ThreadDownloadedCustomCape(GuiCustomCape.this.inputField.getText());
                thread.start();
                GuiCustomCape.this.minecraft.player.sendMessage(JsonUtils.create("Start downloading cape texture from " + GuiCustomCape.this.inputField.getText()));
            }
            GuiCustomCape.this.minecraft.displayGuiScreen(null);
        }));
        this.doneBtn.active = !this.inputField.getText().isEmpty();
        this.addButton(new Button(this.width / 2 + 50 + 4, this.height / 4 + 100 + 12, 100, 20, LangUtils.translate("gui.cancel"), button ->
        {
            GuiCustomCape.this.capeOption = GuiCustomCape.this.prevCapeOption;
            GuiCustomCape.this.saveCapeOption();
            GuiCustomCape.this.minecraft.displayGuiScreen(null);
        }));
        this.resetBtn = this.addButton(new Button(this.width / 2 - 50, this.height / 4 + 100 + 12, 100, 20, LangUtils.translate("menu.reset_cape"), button ->
        {
            CapeUtils.CAPE_TEXTURE = null;
            GuiCustomCape.this.minecraft.player.sendMessage(JsonUtils.create(LangUtils.translate("menu.reset_current_cape")));
            CapeUtils.texture.delete();
            GuiCustomCape.this.minecraft.displayGuiScreen(null);
        }));
        this.resetBtn.active = CapeUtils.texture.exists();

        if (!this.minecraft.gameSettings.getModelParts().contains(PlayerModelPart.CAPE) && !ExtendedConfig.instance.showCustomCape)
        {
            this.capeOption = 0;
        }
        if (ExtendedConfig.instance.showCustomCape)
        {
            this.capeOption = 1;
        }
        if (this.minecraft.gameSettings.getModelParts().contains(PlayerModelPart.CAPE))
        {
            this.capeOption = 2;
        }
        this.prevCapeOption = this.capeOption;
        this.capeBtn = this.addButton(new Button(this.width / 2 + 50 + 4, this.height / 4 + 50, 100, 20, "", button ->
        {
            int i = 0;
            i++;
            GuiCustomCape.this.capeOption = (GuiCustomCape.this.capeOption + i) % 3;
            GuiCustomCape.this.saveCapeOption();
        }));
        this.children.add(this.inputField);
        this.setTextForCapeOption();
    }

    @Override
    public void tick()
    {
        this.doneBtn.active = !this.inputField.getText().isEmpty() || this.prevCapeOption != this.capeOption;
        this.resetBtn.active = CapeUtils.texture.exists();
        this.setTextForCapeOption();
        this.inputField.tick();
    }

    @Override
    public void onClose()
    {
        this.minecraft.keyboardListener.enableRepeatEvents(false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton)
    {
        this.inputField.mouseClicked(mouseX, mouseY, mouseButton);
        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks)
    {
        GuiCustomCape.renderPlayer(this.minecraft, this.width, this.height);
        this.renderBackground();
        this.drawCenteredString(this.font, "Custom Cape Downloader", this.width / 2, 20, 16777215);
        this.drawCenteredString(this.font, "Put your Cape URL (Must be .png or image format)", this.width / 2, 37, 10526880);
        this.inputField.render(mouseX, mouseY, partialTicks);
        super.render(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean isPauseScreen()
    {
        return false;
    }

    private void setTextForCapeOption()
    {
        switch (this.capeOption)
        {
        case 0:
            this.capeBtn.setMessage("Cape: OFF");
            break;
        case 1:
            this.capeBtn.setMessage("Cape: Custom");
            break;
        case 2:
            this.capeBtn.setMessage("Cape: OptiFine");
            break;
        }
    }

    private void saveCapeOption()
    {
        if (this.capeOption == 0)
        {
            ExtendedConfig.instance.showCustomCape = false;
            this.minecraft.gameSettings.setModelPartEnabled(PlayerModelPart.CAPE, false);
        }
        if (this.capeOption == 1)
        {
            ExtendedConfig.instance.showCustomCape = true;
            this.minecraft.gameSettings.setModelPartEnabled(PlayerModelPart.CAPE, false);
        }
        if (this.capeOption == 2)
        {
            ExtendedConfig.instance.showCustomCape = false;
            this.minecraft.gameSettings.setModelPartEnabled(PlayerModelPart.CAPE, true);
        }
        this.minecraft.gameSettings.sendSettingsToServer();
        this.minecraft.gameSettings.saveOptions();
        ExtendedConfig.instance.save();
    }

    private static void renderPlayer(Minecraft mc, int width, int height)
    {
        float yawOffset = mc.player.renderYawOffset;
        float yaw = mc.player.rotationYaw;
        float pitch = mc.player.rotationPitch;
        float yawHead = mc.player.rotationYawHead;
        float scale = 40.0F + height / 8 - 28;
        RenderHelper.enableStandardItemLighting();
        GlStateManager.pushMatrix();
        GlStateManager.translatef(width / 2 - 50, height / 6 + 85, 256.0F);
        GlStateManager.scalef(-scale, scale, scale);
        GlStateManager.rotatef(180.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.rotatef(180.0F, 0.0F, 1.0F, 0.0F);
        mc.player.renderYawOffset = 0.0F;
        mc.player.rotationYaw = 0.0F;
        mc.player.rotationYawHead = mc.player.rotationYaw;
        GlStateManager.translated(0.0D, mc.player.getYOffset(), 0.0D);
        EntityRendererManager manager = mc.getRenderManager();
        manager.setPlayerViewY(180.0F);
        manager.setRenderShadow(false);
        manager.renderEntity(mc.player, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, false);
        manager.setRenderShadow(true);
        mc.player.renderYawOffset = yawOffset;
        mc.player.rotationYaw = yaw;
        mc.player.rotationPitch = pitch;
        mc.player.rotationYawHead = yawHead;
        GlStateManager.popMatrix();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
        GlStateManager.activeTexture(OpenGlHelper.GL_TEXTURE1);
        GlStateManager.disableTexture();
        GlStateManager.activeTexture(OpenGlHelper.GL_TEXTURE0);
    }
}