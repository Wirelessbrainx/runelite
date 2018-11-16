package net.runelite.client.plugins.deathtracker;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.Value;

@AllArgsConstructor
class DeathTrackerItem {
    @Getter
    private int id;
    @Getter
    private String name;
    @Getter
    @Setter
    private int quantity;
    @Getter
    private long price;
}
