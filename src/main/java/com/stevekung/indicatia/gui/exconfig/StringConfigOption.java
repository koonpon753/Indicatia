package com.stevekung.indicatia.gui.exconfig;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import com.stevekung.indicatia.config.ExtendedConfig;
import com.stevekung.indicatia.gui.exconfig.screen.widget.ExtendedButton;

import net.minecraft.client.gui.widget.Widget;

public class StringConfigOption extends ExtendedConfigOption
{
    private final BiConsumer<ExtendedConfig, Integer> getter;
    private final BiFunction<ExtendedConfig, StringConfigOption, String> setter;

    public StringConfigOption(String key, BiConsumer<ExtendedConfig, Integer> getter, BiFunction<ExtendedConfig, StringConfigOption, String> setter)
    {
        super(key);
        this.getter = getter;
        this.setter = setter;
    }

    @Override
    public Widget createOptionButton(int x, int y, int width)
    {
        return new ExtendedButton(x, y, width, 20, this.get(ExtendedConfig.INSTANCE), button ->
        {
            this.set(ExtendedConfig.INSTANCE, 1);
            button.setMessage(this.get(ExtendedConfig.INSTANCE));
        });
    }

    public void set(ExtendedConfig config, int value)
    {
        this.getter.accept(config, value);
        config.save();
    }

    public String get(ExtendedConfig config)
    {
        return this.setter.apply(config, this);
    }
}