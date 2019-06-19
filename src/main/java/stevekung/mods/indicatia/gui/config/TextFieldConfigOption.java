package stevekung.mods.indicatia.gui.config;

import java.util.function.BiConsumer;
import java.util.function.Function;

import net.minecraft.client.gui.widget.Widget;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import stevekung.mods.indicatia.config.ExtendedConfig;

@OnlyIn(Dist.CLIENT)
public class TextFieldConfigOption extends ExtendedConfigOption
{
    private final Function<ExtendedConfig, String> getter;
    private final BiConsumer<ExtendedConfig, String> setter;

    public TextFieldConfigOption(String key, Function<ExtendedConfig, String> getter, BiConsumer<ExtendedConfig, String> setter)
    {
        super(key);
        this.getter = getter;
        this.setter = setter;
    }

    @Override
    public Widget createOptionButton(ExtendedConfig config, int x, int y, int width)
    {
        GuiTextFieldExtended textField = new GuiTextFieldExtended(x, y, width, config, this);
        this.set(config, textField.getText());
        textField.setText(this.get(config));
        textField.setDisplayName(this.getDisplayName());
        textField.setDisplayPrefix(this.getDisplayPrefix());
        return textField;
    }

    public void set(ExtendedConfig config, String value)
    {
        this.setter.accept(config, value);
    }

    public String get(ExtendedConfig config)
    {
        return this.getter.apply(config);
    }
}