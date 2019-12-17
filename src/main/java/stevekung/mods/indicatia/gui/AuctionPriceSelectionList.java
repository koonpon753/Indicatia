package stevekung.mods.indicatia.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.gui.inventory.GuiEditSign;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.network.play.client.C12PacketUpdateSign;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ResourceLocation;
import stevekung.mods.indicatia.utils.IEditSign;

public class AuctionPriceSelectionList extends GuiListExtended
{
    private static final List<AuctionPrice> AUCTION_PRICES = new ArrayList<>();
    private int selectedSlotIndex = -1;

    public AuctionPriceSelectionList(Minecraft mc, int width, int height, int top, int bottom)
    {
        super(mc, width, height, top, bottom, 16);

        if (this.getSize() > 5)
        {
            AuctionPriceSelectionList.AUCTION_PRICES.remove(0);
        }
        Collections.reverse(AUCTION_PRICES);
    }

    @Override
    protected void elementClicked(int slotIndex, boolean isDoubleClick, int mouseX, int mouseY)
    {
        this.selectedSlotIndex = slotIndex;
    }

    @Override
    public IGuiListEntry getListEntry(int index)
    {
        return AUCTION_PRICES.stream().distinct().collect(Collectors.toList()).get(index);
    }

    @Override
    protected int getSize()
    {
        return AUCTION_PRICES.stream().distinct().collect(Collectors.toList()).size();
    }

    @Override
    protected boolean isSelected(int index)
    {
        return index == this.selectedSlotIndex;
    }

    @Override
    protected void drawContainerBackground(Tessellator tessellator) {}

    @Override
    protected void overlayBackground(int startY, int endY, int startAlpha, int endAlpha) {}

    @Override
    public int getListWidth()
    {
        if (AUCTION_PRICES.isEmpty())
        {
            return 0;
        }
        AuctionPrice max = Collections.max(AUCTION_PRICES, Comparator.comparing(text -> text.getPrice().length()));
        int length = this.mc.fontRendererObj.getStringWidth(max.getPrice()) + 12;
        return length;
    }

    @Override
    public int getSlotHeight()
    {
        return 10;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        if (this.field_178041_q)
        {
            this.mouseX = mouseX;
            this.mouseY = mouseY;
            this.drawBackground();
            this.bindAmountScrolled();
            GlStateManager.disableLighting();
            GlStateManager.disableFog();
            Tessellator tessellator = Tessellator.getInstance();
            this.drawContainerBackground(tessellator);
            int k = this.left + this.width / 2 - this.getListWidth() / 2 + 2;
            int l = this.top + 4 - (int)this.amountScrolled;

            if (this.hasListHeader)
            {
                this.drawListHeader(k, l, tessellator);
            }

            this.drawSelectionBox(k, l, mouseX, mouseY);
            this.mc.fontRendererObj.drawString("Select price:", k, l - 12, 16777215);
            GlStateManager.disableDepth();
            this.overlayBackground(0, this.top, 255, 255);
            this.overlayBackground(this.bottom, this.height, 255, 255);
            this.func_148142_b(mouseX, mouseY);
        }
        GlStateManager.enableDepth();
    }

    public void add(String price)
    {
        AuctionPriceSelectionList.AUCTION_PRICES.add(new AuctionPrice(price));
    }

    public static List<AuctionPrice> getAuctionPrice()
    {
        return AuctionPriceSelectionList.AUCTION_PRICES;
    }

    private class AuctionPrice implements GuiListExtended.IGuiListEntry
    {
        private final Minecraft mc;
        private final String price;
        private long lastClicked;

        public AuctionPrice(String price)
        {
            this.mc = Minecraft.getMinecraft();
            this.price = price;
        }

        @Override
        public void setSelected(int p_178011_1_, int p_178011_2_, int p_178011_3_) {}

        @Override
        public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected)
        {
            this.mc.fontRendererObj.drawString(this.price, x + 2, y + 2, 16777215);
        }

        @Override
        public boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX, int relativeY)
        {
            TileEntitySign sign = ((GuiEditSign)this.mc.currentScreen).tileSign;
            sign.markDirty();

            if (Minecraft.getSystemTime() - this.lastClicked < 250L)
            {
                NetHandlerPlayClient net = this.mc.getNetHandler();

                if (net != null)
                {
                    net.addToSendQueue(new C12PacketUpdateSign(sign.getPos(), sign.signText));
                }
                sign.setEditable(true);
                this.mc.getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
                this.mc.displayGuiScreen(null);
            }

            sign.signText[0] = new ChatComponentText(this.price);

            if ((IEditSign)(GuiEditSign)this.mc.currentScreen != null)
            {
                ((IEditSign)(GuiEditSign)this.mc.currentScreen).getTextInputUtil().moveCaretToEnd();
            }
            this.lastClicked = Minecraft.getSystemTime();
            return false;
        }

        @Override
        public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY) {}

        @Override
        public boolean equals(Object obj)
        {
            if (!(obj instanceof AuctionPrice))
            {
                return false;
            }
            if (obj == this)
            {
                return true;
            }
            AuctionPrice other = (AuctionPrice) obj;
            return new EqualsBuilder().append(this.price, other.price).isEquals();
        }

        @Override
        public int hashCode()
        {
            return new HashCodeBuilder().append(this.price).toHashCode();
        }

        public String getPrice()
        {
            return this.price;
        }
    }
}