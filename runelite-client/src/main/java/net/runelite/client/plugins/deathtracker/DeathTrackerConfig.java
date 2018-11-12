package net.runelite.client.plugins.deathtracker;

import java.time.Instant;
import java.util.ArrayList;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;


@ConfigGroup("deathTracker")
public interface DeathTrackerConfig extends Config
{
    @ConfigItem(
            position = 1,
            keyName = "Show Death Items",
            name = "",
            description = ""
    )
    default boolean showDeathItems()
    {
        return true;
    }

    @ConfigItem(
            position = 2,
            keyName = "Show Death World",
            name ="",
            description = ""
    )
    default boolean showDeathWorld(){return true;}

    @ConfigItem(
            position = 3,
            keyName = "Show Death Time",
            name = "",
            description = ""
    )
    default boolean showDeathTime(){return true;}

    @ConfigItem(
            keyName = "deathItems",
            name ="",
            description = "",
            hidden = true
    )
    default ArrayList<DeathTrackerItem> deathItem(){return null;}

//    @ConfigItem(
//            keyName = "deathItems",
//            name ="",
//            description = ""
//    )
//    void deathItems(ArrayList<DeathTrackerRecord> deathItems);
//
//    @ConfigItem(
//            keyName = "deathWorld",
//            name = "",
//            description = "",
//            hidden = true
//    )
//    default int deathWorld()
//    {
//        return -1;
//    }
//
//    @ConfigItem(
//            keyName = "deathWorld",
//            name = "",
//            description = ""
//    )
//    void deathWorld(int deathWorld);
//
//    @ConfigItem(
//            keyName = "timeOfDeath",
//            name = "",
//            description = "",
//            hidden = true
//    )
//    Instant timeOfDeath();
//
//    @ConfigItem(
//            keyName = "timeOfDeath",
//            name = "",
//            description = ""
//    )
//    void timeOfDeath(Instant timeOfDeath);



}
