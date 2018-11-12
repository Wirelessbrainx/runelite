package net.runelite.client.plugins.deathtracker;


import lombok.Value;
import net.runelite.api.InventoryID;

import java.util.ArrayList;


@Value
public class DeathTrackerRecord {
    private final InventoryID title;
    private final ArrayList<DeathTrackerItem> items;


    /**
     * Checks if this record matches specified id
     *
     * @return true if match is made
     */
    boolean matches(final InventoryID id)
    {
        if(id == this.title) {
            return true;
        }
        return false;
    }

}
