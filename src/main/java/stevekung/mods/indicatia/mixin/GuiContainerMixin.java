package stevekung.mods.indicatia.mixin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.lwjgl.input.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.google.common.collect.ObjectArrays;

import net.minecraft.client.gui.*;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.play.client.C14PacketTabComplete;
import net.minecraft.util.*;
import net.minecraftforge.client.ClientCommandHandler;
import stevekung.mods.indicatia.config.ExtendedConfig;
import stevekung.mods.indicatia.event.IndicatiaEventHandler;
import stevekung.mods.indicatia.gui.GuiNumberField;
import stevekung.mods.indicatia.handler.KeyBindingHandler;
import stevekung.mods.indicatia.utils.*;

@Mixin(GuiContainer.class)
public abstract class GuiContainerMixin extends GuiScreen implements ITradeGUI
{
    private final GuiContainer that = (GuiContainer) (Object) this;
    private static final List<String> IGNORE_ITEMS = new ArrayList<>(Arrays.asList(" ", "Recipe Required", "Item To Upgrade", "Rune to Sacrifice", "Runic Pedestal", "Final confirmation"));
    private static final List<String> IGNORE_TOOLTIPS = new ArrayList<>(Arrays.asList(" "));
    private SearchMode mode = SearchMode.SIMPLE;

    @Shadow
    protected int guiLeft;

    @Shadow
    protected int guiTop;

    @Shadow
    protected abstract boolean checkHotbarKeys(int keyCode);

    @Shadow
    protected abstract void handleMouseClick(Slot slotIn, int slotId, int clickedButton, int clickType);

    // GuiChat fields
    private GuiTextField inputField;
    private int sentHistoryCursor = -1;
    private boolean playerNamesFound;
    private boolean waitingOnAutocomplete;
    private int autocompleteIndex;
    private List<String> foundPlayerNames = new ArrayList<>();
    private String historyBuffer = "";

    // Auction
    private GuiNumberField priceSearch = null;

    @Inject(method = "initGui()V", at = @At("RETURN"))
    private void initGui(CallbackInfo info)
    {
        if (this.that instanceof GuiChest)
        {
            GuiChest chest = (GuiChest)this.that;

            if (this.isAuctionBrowser(chest.lowerChestInventory))
            {
                Keyboard.enableRepeatEvents(true);
                this.priceSearch = new GuiNumberField(2, this.fontRendererObj, this.guiLeft + 180, this.guiTop + 40, 100, 20);
                this.priceSearch.setText(IndicatiaEventHandler.auctionPrice);
                this.priceSearch.setCanLoseFocus(true);
            }
            if (this.isChatableGui(chest.lowerChestInventory))
            {
                Keyboard.enableRepeatEvents(true);
                this.sentHistoryCursor = this.mc.ingameGUI.getChatGUI().getSentMessages().size();
                this.inputField = new GuiTextField(0, this.fontRendererObj, 4, this.height - 12, this.width - 4, 12);
                this.inputField.setEnableBackgroundDrawing(false);
                this.inputField.setMaxStringLength(256);
                this.inputField.setFocused(false);
                this.inputField.setCanLoseFocus(true);
            }
        }
    }

    @Inject(method = "mouseClicked(III)V", at = @At("RETURN"))
    private void mouseClicked(int mouseX, int mouseY, int mouseButton, CallbackInfo info)
    {
        if (this.that instanceof GuiChest)
        {
            GuiChest chest = (GuiChest)this.that;

            if (this.isChatableGui(chest.lowerChestInventory))
            {
                this.inputField.mouseClicked(mouseX, mouseY, mouseButton);
            }
            if (this.isAuctionBrowser(chest.lowerChestInventory))
            {
                this.priceSearch.mouseClicked(mouseX, mouseY, mouseButton);
            }
        }
    }

    @Inject(method = "onGuiClosed()V", at = @At("RETURN"))
    private void onGuiClosed(CallbackInfo info)
    {
        if (this.that instanceof GuiChest)
        {
            GuiChest chest = (GuiChest)this.that;

            if (this.isChatableGui(chest.lowerChestInventory) || this.isAuctionBrowser(chest.lowerChestInventory))
            {
                Keyboard.enableRepeatEvents(false);
            }
            if (this.isAuctionBrowser(chest.lowerChestInventory))
            {
                IndicatiaEventHandler.auctionPrice = this.priceSearch.getText();
            }
        }
    }

    @Inject(method = "updateScreen()V", at = @At("RETURN"))
    private void updateScreen(CallbackInfo info)
    {
        if (this.that instanceof GuiChest)
        {
            GuiChest chest = (GuiChest)this.that;

            if (this.isChatableGui(chest.lowerChestInventory))
            {
                this.inputField.updateCursorCounter();
            }
            if (this.priceSearch != null && this.isAuctionBrowser(chest.lowerChestInventory))
            {
                IndicatiaEventHandler.auctionPrice = this.priceSearch.getText();
                this.priceSearch.updateCursorCounter();
            }
        }
    }

    @Inject(method = "drawScreen(IIF)V", at = @At(value = "INVOKE", target = "net/minecraft/client/gui/GuiScreen.drawScreen(IIF)V", shift = Shift.BEFORE))
    private void drawScreen(int mouseX, int mouseY, float partialTicks, CallbackInfo info)
    {
        if (this.that instanceof GuiChest)
        {
            GuiChest chest = (GuiChest)this.that;

            if (this.isChatableGui(chest.lowerChestInventory))
            {
                if (IndicatiaEventHandler.showChat)
                {
                    ScaledResolution scaledresolution = new ScaledResolution(this.mc);
                    int height = scaledresolution.getScaledHeight();
                    GlStateManager.enableBlend();
                    GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
                    GlStateManager.disableAlpha();
                    GlStateManager.pushMatrix();
                    GlStateManager.translate(0.0F, height - 48, 0.0F);
                    this.drawChat();
                    GlStateManager.popMatrix();
                }
                Gui.drawRect(2, this.height - 14, this.width - 2, this.height - 2, Integer.MIN_VALUE);
                this.inputField.drawTextBox();
            }
            if (this.priceSearch != null && this.isAuctionBrowser(chest.lowerChestInventory))
            {
                this.drawString(this.fontRendererObj, "Search for price:", this.guiLeft + 180, this.guiTop + 26, 10526880);
                this.priceSearch.drawTextBox();
            }
        }
    }

    @Inject(method = "keyTyped(CI)V", cancellable = true, at = @At("HEAD"))
    private void keyTyped(char typedChar, int keyCode, CallbackInfo info)
    {
        if (this.that.theSlot != null && keyCode == KeyBindingHandler.KEY_SB_VIEW_RECIPE.getKeyCode())
        {
            SkyBlockRecipeViewer.viewRecipe(this.that.mc.thePlayer, this.that.theSlot, keyCode);
            info.cancel();
        }

        if (this.that instanceof GuiChest)
        {
            GuiChest chest = (GuiChest)this.that;

            if (this.isChatableGui(chest.lowerChestInventory))
            {
                if ((keyCode == 1 || keyCode == this.mc.gameSettings.keyBindInventory.getKeyCode()) && !this.inputField.isFocused())
                {
                    this.mc.thePlayer.closeScreen();
                }
                if (this.inputField.isFocused())
                {
                    if (keyCode == 1)
                    {
                        this.inputField.setFocused(false);
                    }
                }
                else
                {
                    this.checkHotbarKeys(keyCode);

                    if (this.that.theSlot != null && this.that.theSlot.getHasStack())
                    {
                        if (keyCode == this.mc.gameSettings.keyBindPickBlock.getKeyCode())
                        {
                            this.handleMouseClick(this.that.theSlot, this.that.theSlot.slotNumber, 0, 3);
                        }
                        else if (keyCode == this.mc.gameSettings.keyBindDrop.getKeyCode())
                        {
                            this.handleMouseClick(this.that.theSlot, this.that.theSlot.slotNumber, isCtrlKeyDown() ? 1 : 0, 4);
                        }
                    }
                }

                if (keyCode != 28 && keyCode != 156)
                {
                    if (keyCode == 200)
                    {
                        this.getSentHistory(-1);
                    }
                    else if (keyCode == 208)
                    {
                        this.getSentHistory(1);
                    }
                    else
                    {
                        this.inputField.textboxKeyTyped(typedChar, keyCode);
                    }
                }
                else
                {
                    String text = this.inputField.getText().trim();

                    if (!text.isEmpty())
                    {
                        this.sendChatMessage(text);
                    }
                    this.sentHistoryCursor = this.mc.ingameGUI.getChatGUI().getSentMessages().size();
                    this.inputField.setText("");
                    this.mc.ingameGUI.getChatGUI().resetScroll();
                }

                this.waitingOnAutocomplete = false;

                if (keyCode == 15)
                {
                    this.autocompletePlayerNames();
                }
                else
                {
                    this.playerNamesFound = false;
                }
            }
            else if (this.isAuctionBrowser(chest.lowerChestInventory))
            {
                if ((keyCode == 1 || keyCode == this.mc.gameSettings.keyBindInventory.getKeyCode()) && !this.priceSearch.isFocused())
                {
                    this.mc.thePlayer.closeScreen();
                }
                if (this.priceSearch.isFocused())
                {
                    if (keyCode == 1)
                    {
                        this.priceSearch.setFocused(false);
                    }
                }
                else
                {
                    this.checkHotbarKeys(keyCode);

                    if (this.that.theSlot != null && this.that.theSlot.getHasStack())
                    {
                        if (keyCode == this.mc.gameSettings.keyBindPickBlock.getKeyCode())
                        {
                            this.handleMouseClick(this.that.theSlot, this.that.theSlot.slotNumber, 0, 3);
                        }
                        else if (keyCode == this.mc.gameSettings.keyBindDrop.getKeyCode())
                        {
                            this.handleMouseClick(this.that.theSlot, this.that.theSlot.slotNumber, isCtrlKeyDown() ? 1 : 0, 4);
                        }
                    }
                }
                this.priceSearch.textboxKeyTyped(typedChar, keyCode);
            }
        }
    }

    @Inject(method = "handleMouseClick(Lnet/minecraft/inventory/Slot;III)V", cancellable = true, at = @At("HEAD"))
    private void handleMouseClick(Slot slot, int slotId, int clickedButton, int clickType, CallbackInfo info)
    {
        if (slotId != -999 && slotId != -1)
        {
            ItemStack itemStack = this.that.inventorySlots.getSlot(slotId).getStack();

            if (itemStack != null)
            {
                if (this.ignoreNullItem(itemStack, IGNORE_ITEMS))
                {
                    info.cancel();
                }
            }
        }

        if (this.that instanceof GuiChest)
        {
            GuiChest chest = (GuiChest)this.that;
            if (slot != null && clickedButton == 2 && clickType == 3 && this.canViewSeller(chest.lowerChestInventory))
            {
                if (slot.getStack() != null && slot.getStack().hasTagCompound())
                {
                    NBTTagCompound compound = slot.getStack().getTagCompound().getCompoundTag("display");

                    if (compound.getTagId("Lore") == 9)
                    {
                        NBTTagList list = compound.getTagList("Lore", 8);

                        if (list.tagCount() > 0)
                        {
                            for (int j1 = 0; j1 < list.tagCount(); ++j1)
                            {
                                String lore = EnumChatFormatting.getTextWithoutFormattingCodes(list.getStringTagAt(j1));

                                if (lore.startsWith("Seller: "))
                                {
                                    this.mc.thePlayer.sendChatMessage("/ah " + lore.replaceAll("Seller: ?(?:\\[VIP?\\u002B{0,1}\\]|\\[MVP?\\u002B{0,2}\\]|\\[YOUTUBE\\]){0,1} ", ""));
                                }
                            }
                        }
                    }
                }
                info.cancel();
            }
        }
    }

    @Inject(method = "drawSlot", at = @At(value = "INVOKE", target = "net/minecraft/client/renderer/entity/RenderItem.renderItemAndEffectIntoGUI(Lnet/minecraft/item/ItemStack;II)V"))
    private void renderRarity(Slot slot, CallbackInfo info)
    {
        if (ExtendedConfig.instance.showItemRarity)
        {
            if (this.that instanceof GuiChest)
            {
                GuiChest chest = (GuiChest)this.that;
                RenderUtils.drawRarity(slot, chest.lowerChestInventory);
            }
            else
            {
                RenderUtils.drawRarity(slot, null);
            }
        }
    }

    @Inject(method = "drawSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/RenderItem;renderItemOverlayIntoGUI(Lnet/minecraft/client/gui/FontRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V"))
    private void renderBids(Slot slot, CallbackInfo info)
    {
        if (this.that instanceof GuiChest)
        {
            GuiChest chest = (GuiChest)this.that;

            if (this.isRenderBids(chest.lowerChestInventory))
            {
                this.drawBids(slot);
            }
        }
    }

    @Override
    protected void renderToolTip(ItemStack itemStack, int x, int y)
    {
        if (itemStack != null)
        {
            if (this.ignoreNullItem(itemStack, IGNORE_TOOLTIPS))
            {
                return;
            }
        }
        super.renderToolTip(itemStack, x, y);
    }

    @Override
    protected void setText(String newChatText, boolean shouldOverwrite)
    {
        if (this.that instanceof GuiChest)
        {
            GuiChest chest = (GuiChest)this.that;

            if (this.isChatableGui(chest.lowerChestInventory))
            {
                if (shouldOverwrite)
                {
                    this.inputField.setText(newChatText);
                }
                else
                {
                    this.inputField.writeText(newChatText);
                }
            }
        }
    }

    @Override
    public void onAutocompleteResponse(String[] list)
    {
        if (this.waitingOnAutocomplete)
        {
            this.playerNamesFound = false;
            this.foundPlayerNames.clear();

            String[] complete = ClientCommandHandler.instance.latestAutoComplete;

            if (complete != null)
            {
                list = ObjectArrays.concat(complete, list, String.class);
            }

            for (String s : list)
            {
                if (s.length() > 0)
                {
                    this.foundPlayerNames.add(s);
                }
            }

            String s1 = this.inputField.getText().substring(this.inputField.func_146197_a(-1, this.inputField.getCursorPosition(), false));
            String s2 = StringUtils.getCommonPrefix(list);
            s2 = EnumChatFormatting.getTextWithoutFormattingCodes(s2);

            if (s2.length() > 0 && !s1.equalsIgnoreCase(s2))
            {
                this.inputField.deleteFromCursor(this.inputField.func_146197_a(-1, this.inputField.getCursorPosition(), false) - this.inputField.getCursorPosition());
                this.inputField.writeText(s2);
            }
            else if (this.foundPlayerNames.size() > 0)
            {
                this.playerNamesFound = true;
                this.autocompletePlayerNames();
            }
        }
    }

    @Override
    public GuiNumberField getNumberField()
    {
        return this.priceSearch;
    }

    private boolean ignoreNullItem(ItemStack itemStack, List<String> ignores)
    {
        String displayName = EnumChatFormatting.getTextWithoutFormattingCodes(itemStack.getDisplayName());
        return ignores.stream().anyMatch(name -> displayName.equals(name));
    }

    private void drawBids(Slot slot)
    {
        if (slot.getStack() != null && slot.getStack().hasTagCompound())
        {
            NBTTagCompound compound = slot.getStack().getTagCompound().getCompoundTag("display");

            if (compound.getTagId("Lore") == 9)
            {
                NBTTagList list = compound.getTagList("Lore", 8);

                if (list.tagCount() > 0)
                {
                    for (int j1 = 0; j1 < list.tagCount(); ++j1)
                    {
                        int slotLeft = slot.xDisplayPosition;
                        int slotTop = slot.yDisplayPosition;
                        int slotRight = slotLeft + 16;
                        int slotBottom = slotTop + 16;
                        String lore = EnumChatFormatting.getTextWithoutFormattingCodes(list.getStringTagAt(j1));
                        Matcher matcher = Pattern.compile("(?:Top|Starting) bid: (?<coin>[0-9,]+) coins").matcher(lore);
                        int red = ColorUtils.to32BitColor(128, 255, 85, 85);
                        int green = ColorUtils.to32BitColor(128, 85, 255, 85);
                        int yellow = ColorUtils.to32BitColor(128, 255, 255, 85);

                        if (((ITradeGUI)(GuiChest)this.that).getNumberField() == null || ((ITradeGUI)(GuiChest)this.that).getNumberField().getText().isEmpty())
                        {
                            if (lore.startsWith("Starting bid:"))
                            {
                                this.drawGradientRect(slotLeft, slotTop, slotRight, slotBottom, green, green);
                            }
                            else if (lore.startsWith("Bidder:"))
                            {
                                this.drawGradientRect(slotLeft, slotTop, slotRight, slotBottom, red, red);
                            }
                        }
                        else
                        {
                            try
                            {
                                int priceMin = 0;
                                int priceMax = 0;
                                int moneyFromText = 0;

                                if (matcher.matches())
                                {
                                    String[] priceSplit = IndicatiaEventHandler.auctionPrice.split("\\.\\.");
                                    int moneyFromAh = Integer.parseInt(matcher.group("coin").replaceAll("[^\\d.]+", ""));

                                    if (IndicatiaEventHandler.auctionPrice.matches("[\\d]+\\.\\.[\\d]+"))
                                    {
                                        priceMin = Integer.parseInt(priceSplit[0]);
                                        priceMax = Integer.parseInt(priceSplit[1]);
                                        this.mode = SearchMode.RANGED;
                                    }
                                    else if (IndicatiaEventHandler.auctionPrice.matches("[\\d]+\\.\\."))
                                    {
                                        priceMin = Integer.parseInt(IndicatiaEventHandler.auctionPrice.replaceAll("\\.\\.", ""));
                                        this.mode = SearchMode.MIN;
                                    }
                                    else if (IndicatiaEventHandler.auctionPrice.matches("\\.\\.[\\d]+"))
                                    {
                                        priceMax = Integer.parseInt(IndicatiaEventHandler.auctionPrice.replaceAll("\\.\\.", ""));
                                        this.mode = SearchMode.MAX;
                                    }
                                    else
                                    {
                                        moneyFromText = Integer.parseInt(IndicatiaEventHandler.auctionPrice);
                                        this.mode = SearchMode.SIMPLE;
                                    }

                                    if (lore.startsWith("Top bid:"))
                                    {
                                        this.checkCondition(moneyFromText, moneyFromAh, priceMin, priceMax, slotLeft, slotTop, slotRight, slotBottom, yellow, red);
                                    }
                                    else if (lore.startsWith("Starting bid:"))
                                    {
                                        this.checkCondition(moneyFromText, moneyFromAh, priceMin, priceMax, slotLeft, slotTop, slotRight, slotBottom, green, red);
                                    }
                                }
                            }
                            catch (Exception e) {}
                        }
                    }
                }
            }
        }
    }

    private void checkCondition(int moneyFromText, int moneyFromAh, int priceMin, int priceMax, int slotLeft, int slotTop, int slotRight, int slotBottom, int color1, int color2)
    {
        switch (this.mode)
        {
        default:
        case SIMPLE:
            if (moneyFromText == moneyFromAh)
            {
                this.drawGradientRect(slotLeft, slotTop, slotRight, slotBottom, color1, color1);
            }
            else
            {
                this.drawGradientRect(slotLeft, slotTop, slotRight, slotBottom, color2, color2);
            }
            break;
        case MIN:
            if (moneyFromAh >= priceMin)
            {
                this.drawGradientRect(slotLeft, slotTop, slotRight, slotBottom, color1, color1);
            }
            else
            {
                this.drawGradientRect(slotLeft, slotTop, slotRight, slotBottom, color2, color2);
            }
            break;
        case MAX:
            if (moneyFromAh <= priceMax)
            {
                this.drawGradientRect(slotLeft, slotTop, slotRight, slotBottom, color1, color1);
            }
            else
            {
                this.drawGradientRect(slotLeft, slotTop, slotRight, slotBottom, color2, color2);
            }
            break;
        case RANGED:
            if (moneyFromAh >= priceMin && moneyFromAh <= priceMax)
            {
                this.drawGradientRect(slotLeft, slotTop, slotRight, slotBottom, color1, color1);
            }
            else
            {
                this.drawGradientRect(slotLeft, slotTop, slotRight, slotBottom, color2, color2);
            }
            break;
        }
    }

    private boolean canViewSeller(IInventory lowerChestInventory)
    {
        String name = lowerChestInventory.getDisplayName().getUnformattedText();
        return name.equals("Auctions Browser") || name.equals("Your Bids");
    }

    private boolean isAuctionBrowser(IInventory lowerChestInventory)
    {
        String name = lowerChestInventory.getDisplayName().getUnformattedText();
        return name.equals("Auctions Browser") || name.endsWith("'s Auctions");
    }

    private boolean isRenderBids(IInventory lowerChestInventory)
    {
        String name = lowerChestInventory.getDisplayName().getUnformattedText();
        return name.equals("Auctions Browser") || name.equals("Manage Auctions") || name.equals("Your Bids") || name.endsWith("'s Auctions");
    }

    // GuiChat stuff
    private void drawChat()
    {
        if (this.mc.gameSettings.chatVisibility != EntityPlayer.EnumChatVisibility.HIDDEN)
        {
            int i = this.mc.ingameGUI.getChatGUI().getLineCount();
            int k = this.mc.ingameGUI.getChatGUI().field_146253_i.size();
            float f = this.mc.gameSettings.chatOpacity * 0.9F + 0.1F;

            if (k > 0)
            {
                float f1 = this.mc.ingameGUI.getChatGUI().getChatScale();
                GlStateManager.pushMatrix();
                GlStateManager.translate(2.0F, 8.0F, 0.0F);
                GlStateManager.scale(f1, f1, 1.0F);

                for (int i1 = 0; i1 + this.mc.ingameGUI.getChatGUI().scrollPos < this.mc.ingameGUI.getChatGUI().field_146253_i.size() && i1 < i; ++i1)
                {
                    ChatLine chatline = this.mc.ingameGUI.getChatGUI().field_146253_i.get(i1 + this.mc.ingameGUI.getChatGUI().scrollPos);

                    if (chatline != null)
                    {
                        int j1 = this.mc.ingameGUI.getUpdateCounter() - chatline.getUpdatedCounter();

                        if (j1 < 200)
                        {
                            double d0 = j1 / 200.0D;
                            d0 = 1.0D - d0;
                            d0 = d0 * 10.0D;
                            d0 = MathHelper.clamp_double(d0, 0.0D, 1.0D);
                            d0 = d0 * d0;
                            int l1 = (int)(255.0D * d0);
                            l1 = (int)(l1 * f);

                            if (l1 > 3)
                            {
                                int i2 = 0;
                                int j2 = -i1 * 9;
                                String s = chatline.getChatComponent().getFormattedText();
                                GlStateManager.enableBlend();
                                this.mc.fontRendererObj.drawStringWithShadow(s, i2, j2 - 8, 16777215 + (l1 << 24));
                                GlStateManager.disableAlpha();
                                GlStateManager.disableBlend();
                            }
                        }
                    }
                }
                GlStateManager.popMatrix();
            }
        }
    }

    private void getSentHistory(int msgPos)
    {
        int i = this.sentHistoryCursor + msgPos;
        int j = this.mc.ingameGUI.getChatGUI().getSentMessages().size();
        i = MathHelper.clamp_int(i, 0, j);

        if (i != this.sentHistoryCursor)
        {
            if (i == j)
            {
                this.sentHistoryCursor = j;
                this.inputField.setText(this.historyBuffer);
            }
            else
            {
                if (this.sentHistoryCursor == j)
                {
                    this.historyBuffer = this.inputField.getText();
                }
                this.inputField.setText(this.mc.ingameGUI.getChatGUI().getSentMessages().get(i));
                this.sentHistoryCursor = i;
            }
        }
    }

    private void sendAutocompleteRequest(String leftOfCursor, String full)
    {
        if (leftOfCursor.length() >= 1)
        {
            ClientCommandHandler.instance.autoComplete(leftOfCursor, full);
            BlockPos blockpos = null;

            if (this.mc.objectMouseOver != null && this.mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK)
            {
                blockpos = this.mc.objectMouseOver.getBlockPos();
            }
            this.mc.thePlayer.sendQueue.addToSendQueue(new C14PacketTabComplete(leftOfCursor, blockpos));
            this.waitingOnAutocomplete = true;
        }
    }

    private void autocompletePlayerNames()
    {
        if (this.playerNamesFound)
        {
            this.inputField.deleteFromCursor(this.inputField.func_146197_a(-1, this.inputField.getCursorPosition(), false) - this.inputField.getCursorPosition());

            if (this.autocompleteIndex >= this.foundPlayerNames.size())
            {
                this.autocompleteIndex = 0;
            }
        }
        else
        {
            int i = this.inputField.func_146197_a(-1, this.inputField.getCursorPosition(), false);
            this.foundPlayerNames.clear();
            this.autocompleteIndex = 0;
            String s = this.inputField.getText().substring(i).toLowerCase();
            String s1 = this.inputField.getText().substring(0, this.inputField.getCursorPosition());
            this.sendAutocompleteRequest(s1, s);

            if (this.foundPlayerNames.isEmpty())
            {
                return;
            }
            this.playerNamesFound = true;
            this.inputField.deleteFromCursor(i - this.inputField.getCursorPosition());
        }

        if (this.foundPlayerNames.size() > 1)
        {
            StringBuilder stringbuilder = new StringBuilder();

            for (String s2 : this.foundPlayerNames)
            {
                if (stringbuilder.length() > 0)
                {
                    stringbuilder.append(", ");
                }
                stringbuilder.append(s2);
            }
            this.mc.ingameGUI.getChatGUI().printChatMessageWithOptionalDeletion(new ChatComponentText(stringbuilder.toString()), 1);
        }
        this.inputField.writeText(EnumChatFormatting.getTextWithoutFormattingCodes(this.foundPlayerNames.get(this.autocompleteIndex++)));
    }

    private boolean isChatableGui(IInventory lowerChestInventory)
    {
        return IndicatiaEventHandler.CHATABLE_LIST.stream().anyMatch(invName -> lowerChestInventory.getDisplayName().getUnformattedText().equals(invName));
    }
}