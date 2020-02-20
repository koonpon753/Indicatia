package stevekung.mods.indicatia.gui.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

public class PlayerStatsBonus
{
    public static PlayerStatsBonus.Farming[] FARMING;
    public static PlayerStatsBonus.Foraging[] FORAGING;
    public static PlayerStatsBonus.Mining[] MINING;
    public static PlayerStatsBonus.Fishing[] FISHING;
    public static PlayerStatsBonus.Combat[] COMBAT;
    public static PlayerStatsBonus.Enchanting[] ENCHANTING;
    public static PlayerStatsBonus.Alchemy[] ALCHEMY;
    public static PlayerStatsBonus.ZombieSlayer[] ZOMBIE_SLAYER;
    public static PlayerStatsBonus.SpiderSlayer[] SPIDER_SLAYER;
    public static PlayerStatsBonus.WolfSlayer[] WOLF_SLAYER;
    public static PlayerStatsBonus.FairySouls[] FAIRY_SOULS;
    private static final Gson GSON = new Gson();

    public static void getBonusFromRemote(Type type) throws IOException
    {
        URL url = new URL("https://raw.githubusercontent.com/SteveKunG/Indicatia/1.8.9_skyblock/api/bonus/" + type.name().toLowerCase() + ".json");
        URLConnection connection = url.openConnection();
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));

        switch (type)
        {
        case FARMING:
            FARMING = GSON.fromJson(in, PlayerStatsBonus.Farming[].class);
            break;
        case FORAGING:
            FORAGING = GSON.fromJson(in, PlayerStatsBonus.Foraging[].class);
            break;
        case MINING:
            MINING = GSON.fromJson(in, PlayerStatsBonus.Mining[].class);
            break;
        case FISHING:
            FISHING = GSON.fromJson(in, PlayerStatsBonus.Fishing[].class);
            break;
        case COMBAT:
            COMBAT = GSON.fromJson(in, PlayerStatsBonus.Combat[].class);
            break;
        case ENCHANTING:
            ENCHANTING = GSON.fromJson(in, PlayerStatsBonus.Enchanting[].class);
            break;
        case ALCHEMY:
            ALCHEMY = GSON.fromJson(in, PlayerStatsBonus.Alchemy[].class);
            break;
        case ZOMBIE_SLAYER:
            ZOMBIE_SLAYER = GSON.fromJson(in, PlayerStatsBonus.ZombieSlayer[].class);
            break;
        case SPIDER_SLAYER:
            SPIDER_SLAYER = GSON.fromJson(in, PlayerStatsBonus.SpiderSlayer[].class);
            break;
        case WOLF_SLAYER:
            WOLF_SLAYER = GSON.fromJson(in, PlayerStatsBonus.WolfSlayer[].class);
            break;
        case FAIRY_SOULS:
            FAIRY_SOULS = GSON.fromJson(in, PlayerStatsBonus.FairySouls[].class);
            break;
        }
    }

    public class Farming implements IBonusTemplate
    {
        private final int level;
        private final int health;

        public Farming(int level, int health)
        {
            this.level = level;
            this.health = health;
        }

        @Override
        public int getLevel()
        {
            return this.level;
        }

        @Override
        public int getHealth()
        {
            return this.health;
        }
    }

    public class Foraging implements IBonusTemplate
    {
        private final int level;
        private final int strength;

        public Foraging(int level, int strength)
        {
            this.level = level;
            this.strength = strength;
        }

        @Override
        public int getLevel()
        {
            return this.level;
        }

        @Override
        public int getStrength()
        {
            return this.strength;
        }
    }

    public class Mining implements IBonusTemplate
    {
        private final int level;
        private final int defense;

        public Mining(int level, int defense)
        {
            this.level = level;
            this.defense = defense;
        }

        @Override
        public int getLevel()
        {
            return this.level;
        }

        @Override
        public int getDefense()
        {
            return this.defense;
        }
    }

    public class Fishing implements IBonusTemplate
    {
        private final int level;
        private final int health;

        public Fishing(int level, int health)
        {
            this.level = level;
            this.health = health;
        }

        @Override
        public int getLevel()
        {
            return this.level;
        }

        @Override
        public int getHealth()
        {
            return this.health;
        }
    }

    public class Combat implements IBonusTemplate
    {
        private final int level;
        @SerializedName("crit_chance")
        private final int critChance;

        public Combat(int level, int critChance)
        {
            this.level = level;
            this.critChance = critChance;
        }

        @Override
        public int getLevel()
        {
            return this.level;
        }

        @Override
        public int getCritChance()
        {
            return this.critChance;
        }
    }

    public class Enchanting implements IBonusTemplate
    {
        private final int level;
        private final int intelligence;

        public Enchanting(int level, int intelligence)
        {
            this.level = level;
            this.intelligence = intelligence;
        }

        @Override
        public int getLevel()
        {
            return this.level;
        }

        @Override
        public int getIntelligence()
        {
            return this.intelligence;
        }
    }

    public class Alchemy implements IBonusTemplate
    {
        private final int level;
        private final int intelligence;

        public Alchemy(int level, int intelligence)
        {
            this.level = level;
            this.intelligence = intelligence;
        }

        @Override
        public int getLevel()
        {
            return this.level;
        }

        @Override
        public int getIntelligence()
        {
            return this.intelligence;
        }
    }

    public class ZombieSlayer implements IBonusTemplate
    {
        private final int level;
        private final int health;

        public ZombieSlayer(int level, int health)
        {
            this.level = level;
            this.health = health;
        }

        @Override
        public int getLevel()
        {
            return this.level;
        }

        @Override
        public int getHealth()
        {
            return this.health;
        }
    }

    public class SpiderSlayer implements IBonusTemplate
    {
        private final int level;
        @SerializedName("crit_chance")
        private final int critChance;
        @SerializedName("crit_damage")
        private final int critDamage;

        public SpiderSlayer(int level, int critChance, int critDamage)
        {
            this.level = level;
            this.critChance = critChance;
            this.critDamage = critDamage;
        }

        @Override
        public int getLevel()
        {
            return this.level;
        }

        @Override
        public int getCritChance()
        {
            return this.critChance;
        }

        @Override
        public int getCritDamage()
        {
            return this.critDamage;
        }
    }

    public class WolfSlayer implements IBonusTemplate
    {
        private final int level;
        private final int health;
        private final int speed;
        @SerializedName("crit_damage")
        private final int critDamage;

        public WolfSlayer(int level, int health, int speed, int critDamage)
        {
            this.level = level;
            this.health = health;
            this.speed = speed;
            this.critDamage = critDamage;
        }

        @Override
        public int getLevel()
        {
            return this.level;
        }

        @Override
        public int getHealth()
        {
            return this.health;
        }

        @Override
        public int getSpeed()
        {
            return this.speed;
        }

        @Override
        public int getCritDamage()
        {
            return this.critDamage;
        }
    }

    public class FairySouls implements IBonusTemplate
    {
        private final int count;
        private final int health;
        private final int defense;
        private final int strength;
        private final int speed;

        public FairySouls(int count, int health, int defense, int strength, int speed)
        {
            this.count = count;
            this.health = health;
            this.defense = defense;
            this.strength = strength;
            this.speed = speed;
        }

        public int getCount()
        {
            return this.count;
        }

        @Override
        public int getHealth()
        {
            return this.health;
        }

        @Override
        public int getDefense()
        {
            return this.defense;
        }

        @Override
        public int getStrength()
        {
            return this.strength;
        }

        @Override
        public int getSpeed()
        {
            return this.speed;
        }
    }

    public interface IBonusTemplate
    {
        default int getLevel()
        {
            return 0;
        }

        default int getHealth()
        {
            return 0;
        }

        default int getDefense()
        {
            return 0;
        }

        default int getTrueDefense()
        {
            return 0;
        }

        default int getStrength()
        {
            return 0;
        }

        default int getSpeed()
        {
            return 0;
        }

        default int getCritChance()
        {
            return 0;
        }

        default int getCritDamage()
        {
            return 0;
        }

        default int getIntelligence()
        {
            return 0;
        }
    }

    public enum Type
    {
        FARMING,
        FORAGING,
        MINING,
        FISHING,
        COMBAT,
        ENCHANTING,
        ALCHEMY,
        ZOMBIE_SLAYER,
        SPIDER_SLAYER,
        WOLF_SLAYER,
        FAIRY_SOULS;

        public static final Type[] VALUES = values();
    }
}