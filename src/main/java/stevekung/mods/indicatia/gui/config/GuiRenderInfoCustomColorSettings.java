//package stevekung.mods.indicatia.gui.config;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import net.minecraft.client.Minecraft;
//import net.minecraft.client.gui.screen.Screen;
//import net.minecraft.client.gui.widget.button.Button;
//import net.minecraftforge.api.distmarker.Dist;
//import net.minecraftforge.api.distmarker.OnlyIn;
//import stevekung.mods.indicatia.config.ExtendedConfig;
//import stevekung.mods.indicatia.core.IndicatiaMod;
//import stevekung.mods.stevekungslib.utils.ColorUtils;
//import stevekung.mods.stevekungslib.utils.ColorUtils.RGB;
//import stevekung.mods.stevekungslib.utils.JsonUtils;
//import stevekung.mods.stevekungslib.utils.LangUtils;
//
//@OnlyIn(Dist.CLIENT)
//public class GuiRenderInfoCustomColorSettings extends Screen
//{
//    private final Screen parent;
//    private GuiConfigTextFieldRowList optionsRowList;
//    private static final List<ExtendedConfigOption> OPTIONS = new ArrayList<>();
//
//    static
//    {
//        OPTIONS.add(ExtendedConfig.FPS_COLOR);
//        OPTIONS.add(ExtendedConfig.XYZ_COLOR);
//        OPTIONS.add(ExtendedConfig.BIOME_COLOR);
//        OPTIONS.add(ExtendedConfig.DIRECTION_COLOR);
//        OPTIONS.add(ExtendedConfig.PING_COLOR);
//        OPTIONS.add(ExtendedConfig.PING_TO_SECOND_COLOR);
//        OPTIONS.add(ExtendedConfig.SERVER_IP_COLOR);
//        OPTIONS.add(ExtendedConfig.EQUIPMENT_STATUS_COLOR);
//        OPTIONS.add(ExtendedConfig.ARROW_COUNT_COLOR);
//        OPTIONS.add(ExtendedConfig.CPS_COLOR);
//        OPTIONS.add(ExtendedConfig.RCPS_COLOR);
//        OPTIONS.add(ExtendedConfig.SLIME_CHUNK_COLOR);
//        OPTIONS.add(ExtendedConfig.TOP_DONATOR_NAME_COLOR);
//        OPTIONS.add(ExtendedConfig.RECENT_DONATOR_NAME_COLOR);
//        OPTIONS.add(ExtendedConfig.TPS_COLOR);
//        OPTIONS.add(ExtendedConfig.REAL_TIME_COLOR);
//        OPTIONS.add(ExtendedConfig.GAME_TIME_COLOR);
//        OPTIONS.add(ExtendedConfig.GAME_WEATHER_COLOR);
//        OPTIONS.add(ExtendedConfig.MOON_PHASE_COLOR);
//
//        if (IndicatiaMod.isYoutubeChatLoaded)
//        {
//            OPTIONS.add(ExtendedConfig.YTCHAT_VIEW_COUNT_COLOR);
//        }
//
//        OPTIONS.add(ExtendedConfig.FPS_VALUE_COLOR);
//        OPTIONS.add(ExtendedConfig.FPS_26_AND_40_COLOR);
//        OPTIONS.add(ExtendedConfig.FPS_LOW_25_COLOR);
//        OPTIONS.add(ExtendedConfig.XYZ_VALUE_COLOR);
//        OPTIONS.add(ExtendedConfig.DIRECTION_VALUE_COLOR);
//        OPTIONS.add(ExtendedConfig.BIOME_VALUE_COLOR);
//        OPTIONS.add(ExtendedConfig.PING_VALUE_COLOR);
//        OPTIONS.add(ExtendedConfig.PING_200_AND_300_COLOR);
//        OPTIONS.add(ExtendedConfig.PING_300_AND_500_COLOR);
//        OPTIONS.add(ExtendedConfig.PING_MAX_500_COLOR);
//        OPTIONS.add(ExtendedConfig.SERVER_IP_VALUE_COLOR);
//        OPTIONS.add(ExtendedConfig.CPS_VALUE_COLOR);
//        OPTIONS.add(ExtendedConfig.RCPS_VALUE_COLOR);
//        OPTIONS.add(ExtendedConfig.SLIME_CHUNK_VALUE_COLOR);
//        OPTIONS.add(ExtendedConfig.TOP_DONATOR_VALUE_COLOR);
//        OPTIONS.add(ExtendedConfig.RECENT_DONATOR_VALUE_COLOR);
//        OPTIONS.add(ExtendedConfig.TPS_VALUE_COLOR);
//        OPTIONS.add(ExtendedConfig.REAL_TIME_HHMMSS_VALUE_COLOR);
//        OPTIONS.add(ExtendedConfig.REAL_TIME_DDMMYY_VALUE_COLOR);
//        OPTIONS.add(ExtendedConfig.GAME_TIME_VALUE_COLOR);
//        OPTIONS.add(ExtendedConfig.GAME_WEATHER_VALUE_COLOR);
//        OPTIONS.add(ExtendedConfig.MOON_PHASE_VALUE_COLOR);
//
//        if (IndicatiaMod.isYoutubeChatLoaded)
//        {
//            OPTIONS.add(ExtendedConfig.YTCHAT_VIEW_COUNT_VALUE_COLOR);
//        }
//    }
//
//    GuiRenderInfoCustomColorSettings(Screen parent)
//    {
//        super(JsonUtils.create("Render Info Custom Color Settings"));
//        this.parent = parent;
//    }
//
//    @Override
//    public void init()
//    {
//        this.minecraft.keyboardListener.enableRepeatEvents(true);
//        this.addButton(new Button(this.width / 2 - 105, this.height - 27, 100, 20, LangUtils.translate("gui.done"), button ->
//        {
//            GuiRenderInfoCustomColorSettings.this.optionsRowList.saveCurrentValue();
//            ExtendedConfig.instance.save();
//            GuiRenderInfoCustomColorSettings.this.minecraft.displayGuiScreen(GuiRenderInfoCustomColorSettings.this.parent);
//        }));
//        this.addButton(new Button(this.width / 2 + 5, this.height - 27, 100, 20, LangUtils.translate("menu.preview"), button ->
//        {
//            GuiRenderInfoCustomColorSettings.this.optionsRowList.saveCurrentValue();
//            ExtendedConfig.instance.save();
//            GuiRenderInfoCustomColorSettings.this.minecraft.displayGuiScreen(new GuiRenderPreview(GuiRenderInfoCustomColorSettings.this, "render_info"));
//        }));
//
//        ExtendedConfig.Options[] options = new ExtendedConfig.Options[OPTIONS.size()];
//        options = OPTIONS.toArray(options);
//        this.optionsRowList = new GuiConfigTextFieldRowList(this.width, this.height, 32, this.height - 32, 25, options);
//        this.children.add(this.optionsRowList);
//        this.children.addAll(this.optionsRowList.getTextField());
//    }
//
//    @Override
//    public void onClose()
//    {
//        this.minecraft.keyboardListener.enableRepeatEvents(false);
//    }
//
//    @Override
//    public void tick()
//    {
//        this.optionsRowList.tick();
//    }
//
//    @Override
//    public void resize(Minecraft mc, int width, int height)
//    {
//        this.optionsRowList.onResize();
//        super.resize(mc, width, height);
//    }
//
//    @Override
//    public boolean keyPressed(int keyCode, int scanCode, int modifiers)
//    {
//        ExtendedConfig.instance.save();
//        this.optionsRowList.saveCurrentValue();
//        this.optionsRowList.keyPressedText(keyCode, scanCode, modifiers);
//        return super.keyPressed(keyCode, scanCode, modifiers);
//    }
//
//    @Override
//    public boolean charTyped(char codePoint, int modifiers)
//    {
//        this.optionsRowList.charTypedText(codePoint, modifiers);
//        return super.charTyped(codePoint, modifiers);
//    }
//
//    @Override
//    public void render(int mouseX, int mouseY, float partialTicks)
//    {
//        this.renderBackground();
//        this.optionsRowList.render(mouseX, mouseY, partialTicks);
//        this.drawCenteredString(this.font, LangUtils.translate("extended_config.render_info_custom_color.title"), this.width / 2, 5, 16777215);
//        int index = this.optionsRowList.selected;
//
//        if (index != -1)
//        {
//            ExtendedConfig.Options options = this.optionsRowList.getChildren().get(index).getTextField().getOption();
//            RGB rgb = ColorUtils.stringToRGB(this.optionsRowList.getChildren().get(index).getTextField().getText());
//            this.drawCenteredString(ColorUtils.coloredFontRenderer, LangUtils.translate("menu.example") + ": " + rgb.toColoredFont() + options.getTranslation(), this.width / 2, 15, 16777215);
//        }
//        else
//        {
//            this.drawCenteredString(this.font, "Color Format is '255,255,255'", this.width / 2, 15, 16777215);
//        }
//        super.render(mouseX, mouseY, partialTicks);
//    }
//}