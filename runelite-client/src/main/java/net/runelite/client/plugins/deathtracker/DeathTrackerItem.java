package net.runelite.client.plugins.deathtracker;

import lombok.Value;

@Value
class DeathTrackerItem {
    private final int id;
    private final String name;
    private final int quantity;
    private final long price;
}
