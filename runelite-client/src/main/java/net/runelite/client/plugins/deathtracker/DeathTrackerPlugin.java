package net.runelite.client.plugins.deathtracker;

import com.google.inject.Provides;

import javax.inject.Inject;
import javax.swing.*;

import com.google.common.eventbus.Subscribe;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;

import java.awt.*;
import java.time.Instant;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.ItemStack;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.api.GameState;
import net.runelite.http.api.item.Item;
import sun.security.jca.GetInstance;

import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@PluginDescriptor(
        name = "Death Tracker",
        description = "Show what you lost when you die, and on what world",
        tags = {"overlay"}
)
@Slf4j
public class DeathTrackerPlugin extends Plugin {

    @Inject
    private ClientToolbar clientToolbar;

    @Inject
    private ItemManager itemManager;

    @Inject
    private Client client;

    @Inject
    private DeathTrackerConfig config;

    @Inject
    private SpriteManager spriteManager;

    private DeathTrackerPanel panel;
    private NavigationButton navButton;


    private WorldPoint deathPoint;
    private Instant timeOfLastDeath;
    private int worldNum;

    private ArrayList<DeathTrackerRecord> records = new ArrayList<>();


    @Provides
    DeathTrackerConfig deathTrackerConfig(ConfigManager configManager) {
        return configManager.getConfig(DeathTrackerConfig.class);
    }

    public DeathTrackerPlugin() {
        records.add(0, new DeathTrackerRecord(InventoryID.INVENTORY, new ArrayList<DeathTrackerItem>()));
        records.add(1, new DeathTrackerRecord(InventoryID.EQUIPMENT, new ArrayList<DeathTrackerItem>()));
    }

    @Override
    protected void startUp() {

        panel = new DeathTrackerPanel(itemManager);
        spriteManager.getSpriteAsync(SpriteID.EQUIPMENT_ITEMS_LOST_ON_DEATH, 0, panel::loadHeaderIcon);
        final BufferedImage icon = ImageUtil.getResourceStreamFromClass(getClass(), "ItemsLostOnDeath.png");



        navButton = NavigationButton.builder()
                .tooltip("Death Traker")
                .icon(icon)
                .priority(6)
                .panel(panel)
                .build();

        clientToolbar.addNavigation(navButton);


    }

    @Override
    protected void shutDown() {
        clientToolbar.removeNavigation(navButton);
    }

    @Subscribe
    public void onLocalPlayerDeath(LocalPlayerDeath death) {

        deathPoint = client.getLocalPlayer().getWorldLocation();
        worldNum = client.getWorld();
        timeOfLastDeath = Instant.now();

        config.timeOfDeath(timeOfLastDeath);
        config.deathWorld(worldNum);
    }

    @Subscribe
    public void onItemContainerChanged(ItemContainerChanged event) {

        ItemContainer itemContainer = event.getItemContainer();

        if (hasDied()){
            if (records.toString() != config.deathItems()) {

                DeathTrackerRecord inventoryOnDeath = getInventoryContainer();
                DeathTrackerRecord equipmentOnDeath = getEquipmentContainer();

                if (records.get(0).getTitle() == InventoryID.INVENTORY) {
                    DeathTrackerRecord inventoryBeforeDeath = records.get(0);

                    for (DeathTrackerItem item : inventoryOnDeath.getItems()) {
                        if (inventoryBeforeDeath.getItems().contains(item)) {
                            int itemNotLost = inventoryBeforeDeath.getItems().indexOf(item);

                            if (item.getQuantity() == inventoryBeforeDeath.getItems().get(itemNotLost).getQuantity())
                            {
                                inventoryBeforeDeath.getItems().remove(itemNotLost);
                            }

                            else
                             {
                                inventoryBeforeDeath.getItems().get(itemNotLost).setQuantity(inventoryBeforeDeath.getItems().get(itemNotLost).getQuantity() - inventoryOnDeath.getItems().get(inventoryOnDeath.getItems().indexOf(item)).getQuantity());
                             }

                        }
                    }
                    records.set(0, inventoryBeforeDeath);
                }

                if (records.get(1).getTitle() == InventoryID.EQUIPMENT) {
                    DeathTrackerRecord equipmentBeforeDeath = records.get(1);

                    for (DeathTrackerItem  eItem: equipmentOnDeath.getItems())
                    {
                        if (equipmentBeforeDeath.getItems().contains(eItem)) {
                            int itemNotLost = equipmentBeforeDeath.getItems().indexOf(eItem);

                            if (eItem.getQuantity() == equipmentBeforeDeath.getItems().get(itemNotLost).getQuantity()) {
                                equipmentBeforeDeath.getItems().remove(itemNotLost);
                            }
                            else
                            {
                                equipmentBeforeDeath.getItems().get(itemNotLost).setQuantity(equipmentBeforeDeath.getItems().get(itemNotLost).getQuantity() - equipmentOnDeath.getItems().get(equipmentOnDeath.getItems().indexOf(eItem)).getQuantity());
                            }
                        }
                    }
                    records.set(1, equipmentBeforeDeath);
                }
                config.deathItems(records.toString());

            }
            SwingUtilities.invokeLater(() -> panel.add(records));
        }
        if(!hasDied()) {
            if (itemContainer == client.getItemContainer(InventoryID.INVENTORY)) {
                DeathTrackerRecord inventoryRecord = getInventoryContainer();

                records.set(0, inventoryRecord);
            }

            if (itemContainer == client.getItemContainer(InventoryID.EQUIPMENT)) {
                DeathTrackerRecord equipmentRecord = getEquipmentContainer();

                records.set(1, equipmentRecord);
            }

            SwingUtilities.invokeLater(() -> panel.add(records));
        }

    }

    private DeathTrackerRecord getInventoryContainer()
    {
        ItemContainer InventoryContainer = client.getItemContainer(InventoryID.INVENTORY);
        if (InventoryContainer != null)
        {
            Collection<ItemStack> iItems = Arrays.stream(InventoryContainer.getItems())
                    .filter(item -> item.getId() > 0)
                    .map(item -> new ItemStack(item.getId(), item.getQuantity()))
                    .collect(Collectors.toList());
            ArrayList<DeathTrackerItem> iEntries = buildEntries(stack(iItems));

                //records.set(0, new DeathTrackerRecord(InventoryID.INVENTORY, iEntries));
                return new DeathTrackerRecord(InventoryID.INVENTORY, iEntries);

        }

        //records.set(0 ,new DeathTrackerRecord(InventoryID.INVENTORY, new ArrayList<DeathTrackerItem>()));
        return new DeathTrackerRecord(InventoryID.INVENTORY,new ArrayList<DeathTrackerItem>());

    }

    private DeathTrackerRecord getEquipmentContainer()
    {
        ItemContainer EquipmentContainer = client.getItemContainer(InventoryID.EQUIPMENT);

        if(EquipmentContainer != null){
            Collection<ItemStack> eItems = Arrays.stream(EquipmentContainer.getItems())
                    .filter(item -> item.getId() > 0)
                    .map(item -> new ItemStack(item.getId(), item.getQuantity()))
                    .collect(Collectors.toList());
            ArrayList<DeathTrackerItem> eEntries = buildEntries(stack(eItems));

                //records.set(1,new DeathTrackerRecord(InventoryID.EQUIPMENT, eEntries));
                return new DeathTrackerRecord(InventoryID.EQUIPMENT, eEntries);

        }

            //records.set(1 ,new DeathTrackerRecord(InventoryID.EQUIPMENT,new ArrayList<DeathTrackerItem>()));
            return new DeathTrackerRecord(InventoryID.EQUIPMENT, new ArrayList<DeathTrackerItem>());
    }

    public void onConfigChanged(ConfigChanged event)
    {
        if (event.getGroup().equals("deathTracker"))
        {
            if (!config.showDeathItems())
            {
                clientToolbar.removeNavigation(navButton);
            }

            if (!config.showDeathTime())
            {

            }

            if (!config.showDeathWorld())
            {

            }

        }
    }

    private boolean hasDied()
    {
        return config.timeOfDeath() != null;
    }

    private void resetDeath()
    {
        config.timeOfDeath(null);
        config.deathWorld(0);
        DeathTrackerRecord inventoryRecord = getInventoryContainer();
        DeathTrackerRecord equipmentRecord = getEquipmentContainer();
        records.set(0, inventoryRecord);
        records.set(1, equipmentRecord);
    }

    private ArrayList<DeathTrackerItem> buildEntries(final Collection<ItemStack> itemStacks)
    {
        ArrayList<DeathTrackerItem> Items = new ArrayList<DeathTrackerItem>();
        return itemStacks.stream().map(itemStack ->
        {
            final ItemComposition itemComposition = itemManager.getItemComposition(itemStack.getId());
            final int realItemId = itemComposition.getNote() != -1 ? itemComposition.getLinkedNoteId() : itemStack.getId();
            final long price = (long) itemManager.getItemPrice(realItemId) * (long) itemStack.getQuantity();

            return new DeathTrackerItem(
                    itemStack.getId(),
                    itemComposition.getName(),
                    itemStack.getQuantity(),
                    price);
        }).collect(Collectors.toCollection(ArrayList::new));
    }

    private static Collection<ItemStack> stack(Collection<ItemStack> items)
    {
        final List<ItemStack> list = new ArrayList<>();

        for (final ItemStack item : items)
        {
            int quantity = 0;
            for (final ItemStack i : list)
            {
                if (i.getId() == item.getId())
                {
                    quantity = i.getQuantity();
                    list.remove(i);
                    break;
                }
            }
            if (quantity > 0)
            {
                list.add(new ItemStack(item.getId(), item.getQuantity() + quantity));
            }
            else
            {
                list.add(item);
            }
        }

        return list;
    }



}
