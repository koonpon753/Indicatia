package com.stevekung.indicatia.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import com.stevekung.indicatia.config.Equipments;
import com.stevekung.indicatia.config.ExtendedConfig;
import com.stevekung.stevekungslib.utils.ColorUtils;
import com.stevekung.stevekungslib.utils.client.ClientUtils;

import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;

public class HorizontalEquipmentOverlay extends EquipmentOverlay
{
    private int width;
    private int itemDamageWidth;

    public HorizontalEquipmentOverlay(ItemStack itemStack)
    {
        super(itemStack);
        this.initSize();
    }

    public int getWidth()
    {
        return this.width;
    }

    public void render(int x, int y)
    {
        boolean right = ExtendedConfig.INSTANCE.equipmentPosition == Equipments.Position.RIGHT;
        String arrowInfo = this.renderArrowInfo();
        EquipmentOverlay.renderItem(this.itemStack, right ? x - 18 : x, y);
        this.mc.fontRenderer.drawStringWithShadow(ColorUtils.stringToRGB(ExtendedConfig.INSTANCE.equipmentStatusColor).toColoredFont() + this.renderInfo(), right ? x - 20 - this.itemDamageWidth : x + 18, y + 4, 16777215);

        if (this.itemStack.getItem() instanceof BowItem)
        {
            RenderSystem.disableDepthTest();
            ClientUtils.unicodeFontRenderer.drawStringWithShadow(ColorUtils.stringToRGB(ExtendedConfig.INSTANCE.arrowCountColor).toColoredFont() + arrowInfo, right ? x - ClientUtils.unicodeFontRenderer.getStringWidth(arrowInfo) : x + 6, y + 8, 16777215);
            RenderSystem.enableDepthTest();
        }
    }

    private void initSize()
    {
        this.itemDamageWidth = this.mc.fontRenderer.getStringWidth(this.renderInfo());
        this.width = 20 + this.itemDamageWidth;
    }
}