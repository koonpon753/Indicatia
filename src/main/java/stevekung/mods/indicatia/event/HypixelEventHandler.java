package stevekung.mods.indicatia.event;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.gui.inventory.GuiEditSign;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import stevekung.mods.indicatia.config.ExtendedConfig;
import stevekung.mods.indicatia.config.RareDropMode;
import stevekung.mods.indicatia.config.VisitIslandMode;
import stevekung.mods.indicatia.gui.toasts.ItemDropsToast;
import stevekung.mods.indicatia.gui.toasts.VisitIslandToast;
import stevekung.mods.indicatia.handler.KeyBindingHandler;
import stevekung.mods.indicatia.utils.*;

public class HypixelEventHandler
{
    private static final Pattern LETTERS_NUMBERS = Pattern.compile("[^a-z A-Z:0-9/']");
    private static final Pattern JOINED_PARTY_PATTERN = Pattern.compile("(?<name>\\w+) joined the party!");
    private static final Pattern VISIT_ISLAND_PATTERN = Pattern.compile("(?:\\[SkyBlock\\]|\\[SkyBlock\\] (?:\\[VIP?\\u002B{0,1}\\]|\\[MVP?\\u002B{0,2}\\]|\\[YOUTUBE\\])) (?<name>\\w+) is visiting Your Island!");
    private static final Pattern NICK_PATTERN = Pattern.compile("^You are now nicked as (?<nick>\\w+)!");

    // Item Drop Stuff
    private static final String DROP_PATTERN = "(?<item>[\\w\\u0027\\u25C6 -]+)";
    public static final Pattern RARE_DROP_PATTERN = Pattern.compile("RARE DROP! " + DROP_PATTERN + " ?(?:\\u0028\\u002B[0-9]+% Magic Find!\\u0029){0,1}");
    public static final Pattern RARE_DROP_WITH_BRACKET_PATTERN = Pattern.compile("(?<type>RARE|VERY RARE|CRAZY RARE) DROP!? {1,2}\\u0028" + DROP_PATTERN + "\\u0029 ?(?:\\u0028\\u002B[0-9]+% Magic Find!\\u0029){0,1}");
    public static final Pattern DRAGON_DROP_PATTERN = Pattern.compile("(?:(?:" + GameProfileUtils.getUsername() + ")|(?:\\[VIP?\\u002B{0,1}\\]|\\[MVP?\\u002B{0,2}\\]|\\[YOUTUBE\\]) " + GameProfileUtils.getUsername() + ") has obtained " + DROP_PATTERN + "!");

    // Fish catch stuff
    public static final Pattern FISH_CATCH_PATTERN = Pattern.compile("(?<type>GOOD|GREAT) CATCH! You found a " + DROP_PATTERN + ".");
    public static final Pattern COINS_CATCH_PATTERN = Pattern.compile("(?<type>GOOD|GREAT) CATCH! You found (?<coin>[0-9,]+) Coins.");

    private static final List<String> LEFT_PARTY_MESSAGE = new ArrayList<>(Arrays.asList("You are not in a party and have been moved to the ALL channel!", "has disbanded the party!", "The party was disbanded because all invites have expired and all members have left."));

    public static boolean isSkyBlock = false;
    public static boolean foundSkyBlockPack;
    public static String skyBlockPackResolution = "16";
    public static SkyBlockLocation SKY_BLOCK_LOCATION = SkyBlockLocation.YOUR_ISLAND;
    private static final List<String> PARTY_LIST = new ArrayList<>();
    public static String SKYBLOCK_AMPM = "";
    public static final List<ItemDrop> ITEM_DROP_LIST = new ArrayList<>();
    private List<ItemStack> previousInventory;
    private Minecraft mc;

    public HypixelEventHandler()
    {
        this.mc = Minecraft.getMinecraft();
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event)
    {
        if (this.mc.thePlayer != null)
        {
            if (event.phase == TickEvent.Phase.START)
            {
                HypixelEventHandler.getHypixelNickedPlayer(this.mc);

                if (this.mc.thePlayer.ticksExisted % 5 == 0)
                {
                    this.getInventoryDifference(this.mc.thePlayer.inventory.mainInventory);
                }
                if (this.mc.theWorld != null)
                {
                    boolean found = false;
                    ScoreObjective scoreObj = this.mc.theWorld.getScoreboard().getObjectiveInDisplaySlot(1);
                    Scoreboard scoreboard = this.mc.theWorld.getScoreboard();
                    Collection<Score> collection = scoreboard.getSortedScores(scoreObj);
                    List<Score> list = Lists.newArrayList(collection.stream().filter(score -> score.getPlayerName() != null && !score.getPlayerName().startsWith("#")).collect(Collectors.toList()));

                    if (list.size() > 15)
                    {
                        collection = Lists.newArrayList(Iterables.skip(list, collection.size() - 15));
                    }
                    else
                    {
                        collection = list;
                    }

                    for (Score score1 : collection)
                    {
                        ScorePlayerTeam scorePlayerTeam = scoreboard.getPlayersTeam(score1.getPlayerName());
                        String scoreText = this.keepLettersAndNumbersOnly(EnumChatFormatting.getTextWithoutFormattingCodes(ScorePlayerTeam.formatPlayerName(scorePlayerTeam, score1.getPlayerName())));

                        if (scoreText.endsWith("am"))
                        {
                            HypixelEventHandler.SKYBLOCK_AMPM = " AM";
                        }
                        else if (scoreText.endsWith("pm"))
                        {
                            HypixelEventHandler.SKYBLOCK_AMPM = " PM";
                        }

                        for (SkyBlockLocation location : CachedEnum.locationValues)
                        {
                            if (scoreText.endsWith(location.getLocation()))
                            {
                                HypixelEventHandler.SKY_BLOCK_LOCATION = location;
                                found = true;
                                break;
                            }
                        }
                    }

                    if (scoreObj != null)
                    {
                        HypixelEventHandler.isSkyBlock = EnumChatFormatting.getTextWithoutFormattingCodes(scoreObj.getDisplayName()).contains("SKYBLOCK");
                    }
                    else
                    {
                        HypixelEventHandler.isSkyBlock = false;
                    }

                    if (!found)
                    {
                        HypixelEventHandler.SKY_BLOCK_LOCATION = SkyBlockLocation.NONE;
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onMouseClick(MouseEvent event)
    {
        if (event.button == 1 && event.buttonstate && InfoUtils.INSTANCE.isHypixel())
        {
            if (this.mc.pointedEntity != null && this.mc.pointedEntity instanceof EntityOtherPlayerMP)
            {
                EntityOtherPlayerMP player = (EntityOtherPlayerMP)this.mc.pointedEntity;

                if (!this.mc.thePlayer.isSneaking() && this.mc.thePlayer.getHeldItem() == null && ExtendedConfig.instance.rightClickToAddParty)
                {
                    if (this.mc.thePlayer.sendQueue.getPlayerInfoMap().stream().anyMatch(info -> info.getGameProfile().getName().equals(player.getName())))
                    {
                        this.mc.thePlayer.sendChatMessage("/p " + player.getName());
                        event.setCanceled(true);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onClientChatReceived(ClientChatReceivedEvent event)
    {
        if (event.message == null)
        {
            return;
        }

        String message = event.message.getUnformattedText();

        if (InfoUtils.INSTANCE.isHypixel())
        {
            // Common matcher
            Matcher nickMatcher = HypixelEventHandler.NICK_PATTERN.matcher(message);
            Matcher visitIslandMatcher = HypixelEventHandler.VISIT_ISLAND_PATTERN.matcher(message);
            Matcher joinedPartyMatcher = HypixelEventHandler.JOINED_PARTY_PATTERN.matcher(message);

            // Item Drop matcher
            Matcher rareDropPattern = HypixelEventHandler.RARE_DROP_PATTERN.matcher(message);
            Matcher dragonDropPattern = HypixelEventHandler.DRAGON_DROP_PATTERN.matcher(message);

            // Fish catch matcher
            Matcher fishCatchPattern = HypixelEventHandler.FISH_CATCH_PATTERN.matcher(message);
            Matcher coinsCatchPattern = HypixelEventHandler.COINS_CATCH_PATTERN.matcher(message);

            // Slayer Drop matcher
            Matcher rareDropBracketPattern = HypixelEventHandler.RARE_DROP_WITH_BRACKET_PATTERN.matcher(message);

            if (event.type == 0)
            {
                if (message.contains("Illegal characters in chat") || message.contains("A kick occurred in your connection"))
                {
                    event.message = null;
                }
                else if (message.contains("You were spawned in Limbo."))
                {
                    event.message = JsonUtils.create("You were spawned in Limbo.").setChatStyle(JsonUtils.green());
                }
                else if (message.contains("Your nick has been reset!"))
                {
                    ExtendedConfig.instance.hypixelNickName = "";
                    ExtendedConfig.instance.save();
                }

                if (HypixelEventHandler.LEFT_PARTY_MESSAGE.stream().anyMatch(pmess -> message.equals(pmess)))
                {
                    ExtendedConfig.instance.chatMode = 0;
                    ExtendedConfig.instance.save();
                }

                if (visitIslandMatcher.matches())
                {
                    String name = visitIslandMatcher.group("name");

                    if (VisitIslandMode.getById(ExtendedConfig.instance.visitIslandMode).equalsIgnoreCase("disabled"))
                    {
                        event.message = null;
                    }
                    else if (VisitIslandMode.getById(ExtendedConfig.instance.visitIslandMode).equalsIgnoreCase("toast"))
                    {
                        HypixelEventHandler.addVisitingToast(name);
                        LoggerIN.logToast(message);
                        event.message = null;
                    }

                    if (ExtendedConfig.instance.addPartyVisitIsland && !HypixelEventHandler.PARTY_LIST.stream().anyMatch(pname -> name.equals(pname)))
                    {
                        this.mc.thePlayer.sendChatMessage("/p " + name);
                    }
                }

                if (joinedPartyMatcher.matches())
                {
                    HypixelEventHandler.PARTY_LIST.add(joinedPartyMatcher.group("name"));
                }
                else if (nickMatcher.matches())
                {
                    ExtendedConfig.instance.hypixelNickName = nickMatcher.group("nick");
                    ExtendedConfig.instance.save();
                }

                if (RareDropMode.getById(ExtendedConfig.instance.itemDropMode).equalsIgnoreCase("toast"))
                {
                    if (message.contains("You destroyed an Ender Crystal!"))
                    {
                        HypixelEventHandler.ITEM_DROP_LIST.add(new ItemDrop("Crystal Fragment", ItemDropsToast.Type.DRAGON_CRYSTAL_FRAGMENT));
                        LoggerIN.logToast(message);
                        event.message = null;
                    }

                    if (rareDropPattern.matches())
                    {
                        String name = rareDropPattern.group("item");
                        HypixelEventHandler.ITEM_DROP_LIST.add(new ItemDrop(EnumChatFormatting.getTextWithoutFormattingCodes(name), ItemDropsToast.Type.RARE_DROP));
                        LoggerIN.logToast(message);
                        event.message = null;
                    }
                    else if (fishCatchPattern.matches())
                    {
                        HypixelEventHandler.addFishLoot(fishCatchPattern);
                        LoggerIN.logToast(message);
                        event.message = null;
                    }
                    else if (dragonDropPattern.matches())
                    {
                        String name = dragonDropPattern.group("item");
                        HypixelEventHandler.ITEM_DROP_LIST.add(new ItemDrop(EnumChatFormatting.getTextWithoutFormattingCodes(name), ItemDropsToast.Type.DRAGON_DROP));
                        LoggerIN.logToast(message);
                        event.message = null;
                    }
                    else if (coinsCatchPattern.matches())
                    {
                        String type = coinsCatchPattern.group("type");
                        String coin = coinsCatchPattern.group("coin");
                        HUDRenderEventHandler.INSTANCE.getToastGui().add(new ItemDropsToast(HypixelEventHandler.getCoinItemStack(coin), type.equals("GOOD") ? ItemDropsToast.Type.GOOD_CATCH_COINS : ItemDropsToast.Type.GREAT_CATCH_COINS));
                        LoggerIN.logToast(message);
                        event.message = null;
                    }
                    else if (rareDropBracketPattern.matches())
                    {
                        String type = rareDropBracketPattern.group("type");
                        String name = rareDropBracketPattern.group("item");
                        ItemDropsToast.Type dropType = type.equals("RARE") ? ItemDropsToast.Type.SLAYER_RARE_DROP : type.equals("VERY RARE") ? ItemDropsToast.Type.SLAYER_VERY_RARE_DROP : ItemDropsToast.Type.SLAYER_CRAZY_RARE_DROP;
                        HypixelEventHandler.ITEM_DROP_LIST.add(new ItemDrop(EnumChatFormatting.getTextWithoutFormattingCodes(name), dropType));
                        LoggerIN.logToast(message);
                        event.message = null;
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onPressKey(InputEvent.KeyInputEvent event)
    {
        if (!HypixelEventHandler.isSkyBlock)
        {
            return;
        }

        if (KeyBindingHandler.KEY_SB_ENDER_CHEST.isKeyDown())
        {
            this.mc.thePlayer.sendChatMessage("/enderchest");
        }
        else if (KeyBindingHandler.KEY_SB_CRAFTED_MINIONS.isKeyDown())
        {
            this.mc.thePlayer.sendChatMessage("/craftedgenerators");
        }
        else if (KeyBindingHandler.KEY_SB_CRAFTING_TABLE.isKeyDown())
        {
            this.mc.thePlayer.sendChatMessage("/viewcraftingtable");
        }
    }

    @SubscribeEvent
    public void onPlaySound(PlaySoundEvent event)
    {
        String name = event.name;

        if (this.mc.theWorld != null)
        {
            if (name.equals("records.13") && HypixelEventHandler.SKY_BLOCK_LOCATION == SkyBlockLocation.BLAZING_FORTRESS)
            {
                this.mc.ingameGUI.displayTitle(JsonUtils.create("Preparing spawn...").setChatStyle(JsonUtils.red()).getFormattedText(), JsonUtils.create("").setChatStyle(JsonUtils.red()).getFormattedText(), 0, 1200, 20);
                this.mc.getSoundHandler().playSound(new PositionedSoundRecord(new ResourceLocation("random.orb"), 0.75F, 1.0F, (float)this.mc.thePlayer.posX + 0.5F, (float)this.mc.thePlayer.posY + 0.5F, (float)this.mc.thePlayer.posZ + 0.5F));
            }
        }
    }

    @SubscribeEvent
    public void onWorldJoin(EntityJoinWorldEvent event)
    {
        if (event.entity == this.mc.thePlayer)
        {
            this.previousInventory = null;
            ITEM_DROP_LIST.clear();
        }
    }

    @SubscribeEvent
    public void onItemTooltip(ItemTooltipEvent event)
    {
        List<String> dates = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();

        try
        {
            for (String tooltip : event.toolTip)
            {
                String startTime = EnumChatFormatting.getTextWithoutFormattingCodes(tooltip);

                if (startTime.startsWith("Starts in:"))
                {
                    startTime = startTime.replace("Starts in: ", "");
                    String[] timeEstimate = Arrays.stream(startTime.split(" ")).map(time -> time.replaceAll("[^0-9]+", "")).toArray(size -> new String[size]);
                    int dayF = Integer.valueOf(timeEstimate[0]);
                    int hourF = Integer.valueOf(timeEstimate[1]);
                    int minuteF = Integer.valueOf(timeEstimate[2]);
                    int secondF = Integer.valueOf(timeEstimate[3]);
                    calendar.add(Calendar.DATE, dayF);
                    calendar.add(Calendar.HOUR, hourF);
                    calendar.add(Calendar.MINUTE, minuteF);
                    calendar.add(Calendar.SECOND, secondF);
                    String startDate1 = new SimpleDateFormat("EEEE HH:mm:ss a").format(calendar.getTime());
                    String startDate2 = new SimpleDateFormat("d MMMMM yyyy").format(calendar.getTime());
                    dates.add("Event starts at: ");
                    dates.add(EnumChatFormatting.YELLOW + startDate1);
                    dates.add(EnumChatFormatting.YELLOW + startDate2);
                    event.toolTip.remove(1);
                    event.toolTip.addAll(1, dates);
                }
                else if (startTime.startsWith("Starting in:"))
                {
                    startTime = startTime.replace("Starting in: ", "");
                    String[] timeEstimate = Arrays.stream(startTime.split(" ")).map(time -> time.replaceAll("[^0-9]+", "")).toArray(size -> new String[size]);
                    int dayF = Integer.valueOf(timeEstimate[0]);
                    int hourF = Integer.valueOf(timeEstimate[1]);
                    int minuteF = Integer.valueOf(timeEstimate[2]);
                    int secondF = Integer.valueOf(timeEstimate[3]);
                    calendar.add(Calendar.DATE, dayF);
                    calendar.add(Calendar.HOUR, hourF);
                    calendar.add(Calendar.MINUTE, minuteF);
                    calendar.add(Calendar.SECOND, secondF);
                    String startDate1 = new SimpleDateFormat("EEEE HH:mm:ss a").format(calendar.getTime());
                    String startDate2 = new SimpleDateFormat("d MMMMM yyyy").format(calendar.getTime());
                    dates.add("Event starts at: ");
                    dates.add(EnumChatFormatting.YELLOW + startDate1);
                    dates.add(EnumChatFormatting.YELLOW + startDate2);
                    event.toolTip.remove(event.toolTip.size() - 3);
                    event.toolTip.addAll(event.toolTip.size() - 2, dates);
                }
            }
        }
        catch (Exception e) {}
    }

    private static void getHypixelNickedPlayer(Minecraft mc)
    {
        if (InfoUtils.INSTANCE.isHypixel() && mc.currentScreen instanceof GuiEditSign)
        {
            GuiEditSign gui = (GuiEditSign) mc.currentScreen;

            if (gui.tileSign != null)
            {
                if (!(gui.tileSign.signText[2].getUnformattedText().contains("Enter your") && gui.tileSign.signText[3].getUnformattedText().contains("username here")))
                {
                    return;
                }

                ExtendedConfig.instance.hypixelNickName = gui.tileSign.signText[0].getUnformattedText();

                if (mc.thePlayer.ticksExisted % 40 == 0)
                {
                    ExtendedConfig.instance.save();
                }
            }
        }
    }

    private String keepLettersAndNumbersOnly(String text)
    {
        return LETTERS_NUMBERS.matcher(text).replaceAll("");
    }

    private static void addFishLoot(Matcher matcher)
    {
        String dropType = matcher.group("type");
        String name = matcher.group("item");
        HypixelEventHandler.ITEM_DROP_LIST.add(new ItemDrop(EnumChatFormatting.getTextWithoutFormattingCodes(name), dropType.equals("GOOD") ? ItemDropsToast.Type.GOOD_CATCH : ItemDropsToast.Type.GREAT_CATCH));
    }

    /**
     * Credit to codes.biscuit.skyblockaddons.utils.InventoryUtils
     */
    private void getInventoryDifference(ItemStack[] currentInventory)
    {
        List<ItemStack> newInventory = this.copyInventory(currentInventory);

        if (this.previousInventory != null)
        {
            for (int i = 0; i < newInventory.size(); i++)
            {
                ItemStack newItem = newInventory.get(i);

                if (newItem != null)
                {
                    String newItemName = EnumChatFormatting.getTextWithoutFormattingCodes(newItem.getDisplayName());

                    for (Iterator<ItemDrop> iterator = HypixelEventHandler.ITEM_DROP_LIST.iterator(); iterator.hasNext();)
                    {
                        ItemDrop drop = iterator.next();

                        if (drop.getName().equals(newItemName))
                        {
                            if (HUDRenderEventHandler.INSTANCE.getToastGui().add(new ItemDropsToast(newItem, drop.getType())))
                            {
                                iterator.remove();
                            }
                        }
                    }
                }
            }
        }
        this.previousInventory = newInventory;
    }

    private static ItemStack getCoinItemStack(String coin)
    {
        ItemStack itemStack = new ItemStack(Items.skull, 1, 3);
        NBTTagCompound compound = new NBTTagCompound();
        NBTTagCompound properties = new NBTTagCompound();
        properties.setString("Id", "2070f6cb-f5db-367a-acd0-64d39a7e5d1b");
        NBTTagCompound texture = new NBTTagCompound();
        NBTTagList list = new NBTTagList();
        NBTTagCompound value = new NBTTagCompound();
        value.setString("Value", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTM4MDcxNzIxY2M1YjRjZDQwNmNlNDMxYTEzZjg2MDgzYTg5NzNlMTA2NGQyZjg4OTc4Njk5MzBlZTZlNTIzNyJ9fX0=");
        list.appendTag(value);
        texture.setTag("textures", list);
        properties.setTag("Properties", texture);
        compound.setTag("SkullOwner", properties);
        itemStack.setTagCompound(compound);
        itemStack.setStackDisplayName(ColorUtils.stringToRGB("255,223,0").toColoredFont() + coin + " Coins");
        return itemStack;
    }

    private List<ItemStack> copyInventory(ItemStack[] inventory)
    {
        List<ItemStack> copy = new ArrayList<>(inventory.length);

        for (ItemStack item : inventory)
        {
            copy.add(item != null ? ItemStack.copyItemStack(item) : null);
        }
        return copy;
    }

    private static void addVisitingToast(String name)
    {
        CommonUtils.POOL.execute(() ->
        {
            try (InputStream input = new URL("https://api.mojang.com/users/profiles/minecraft/" + name).openStream())
            {
                BufferedReader reader = new BufferedReader(new InputStreamReader(input, Charset.forName("UTF-8")));
                String jsonText = readAll(reader);
                JsonObject json = new JsonParser().parse(jsonText).getAsJsonObject();
                String rawName = json.get("name").getAsString();
                String rawUUID = json.get("id").getAsString();
                String uuid = rawUUID.replaceFirst("([0-9a-fA-F]{8})([0-9a-fA-F]{4})([0-9a-fA-F]{4})([0-9a-fA-F]{4})([0-9a-fA-F]+)", "$1-$2-$3-$4-$5");
                HUDRenderEventHandler.INSTANCE.getToastGui().add(new VisitIslandToast(rawName, UUID.fromString(uuid)));
            }
            catch (Exception e) {}
        });
    }

    private static String readAll(Reader rd) throws IOException
    {
        StringBuilder builder = new StringBuilder();
        int cp;

        while ((cp = rd.read()) != -1)
        {
            builder.append((char) cp);
        }
        return builder.toString();
    }

    static class ItemDrop
    {
        private final String name;
        private final ItemDropsToast.Type type;

        private ItemDrop(String name, ItemDropsToast.Type type)
        {
            this.name = name;
            this.type = type;
        }

        public String getName()
        {
            return this.name;
        }

        public ItemDropsToast.Type getType()
        {
            return this.type;
        }
    }
}