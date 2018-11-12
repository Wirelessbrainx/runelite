package net.runelite.client.plugins.deathtracker;

import com.google.common.base.Strings;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nullable;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import lombok.Getter;
import net.runelite.api.InventoryID;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.util.StackFormatter;

public class DeathTrackerBox extends JPanel {

    private static final int ITEMS_PER_ROW = 5;
    private final JPanel itemContainer = new JPanel();
    private final JLabel priceLabel = new JLabel();
    private final JLabel subTitleLabel = new JLabel();
    private final ItemManager itemManager;
    private final InventoryID id;

    @Getter
    private final ArrayList<DeathTrackerRecord> records = new ArrayList<>();

    DeathTrackerBox(final ItemManager itemManager, final InventoryID id) {
        this.id = id;
        this.itemManager = itemManager;

        setLayout(new BorderLayout(0, 1));
        setBorder(new EmptyBorder(5, 0, 0, 0));

        final JPanel logTitle = new JPanel(new BorderLayout(5, 0));
        logTitle.setBorder(new EmptyBorder(7, 7, 7, 7));
        logTitle.setBackground(ColorScheme.DARKER_GRAY_COLOR.darker());

        final JLabel titleLabel = new JLabel(id.toString());
        titleLabel.setFont(FontManager.getRunescapeSmallFont());
        titleLabel.setForeground(Color.WHITE);

        logTitle.add(titleLabel, BorderLayout.WEST);

        subTitleLabel.setFont(FontManager.getRunescapeSmallFont());
        subTitleLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
        logTitle.add(subTitleLabel, BorderLayout.CENTER);


        priceLabel.setFont(FontManager.getRunescapeSmallFont());
        priceLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
        logTitle.add(priceLabel, BorderLayout.EAST);

        add(logTitle, BorderLayout.NORTH);
        add(itemContainer, BorderLayout.CENTER);
    }



    /**
     * Checks if this box matches specified record
     * @param record loot record
     * @return true if match is made
     */
    boolean matches(final DeathTrackerRecord record)
    {
        return record.getTitle().equals(id);
    }

    /**
     * Checks if this box matches specified id
     * @param id other record id
     * @return true if match is made
     */
    boolean matches(final InventoryID id)
    {
        if (id == null)
        {
            return true;
        }

        return this.id.equals(id);
    }

    /**
     * Adds an record's data into a loot box.
     * This will add new items to the list, re-calculating price and kill count.
     */
    void combine(DeathTrackerRecord record)
    {
        if (!matches(record))
        {
            throw new IllegalArgumentException(record.toString());
        }
//
        records.add(record);
        buildItems();
//
//        priceLabel.setText(StackFormatter.quantityToStackSize(totalPrice) + " gp");
//        if (records.size() > 1)
//        {
//            subTitleLabel.setText("x " + records.size());
//        }
//
        repaint();
    }

    /**
     * This method creates stacked items from the item list, calculates total price and then
     * displays all the items in the UI.
     */
    private void buildItems()
    {
        final List<DeathTrackerItem> allItems = new ArrayList<>();
        final List<DeathTrackerItem> items = new ArrayList<>();


        for (DeathTrackerRecord record : records)
        {
            allItems.addAll(record.getItems());
        }

        for (final DeathTrackerItem entry : allItems)
        {
            //totalPrice += entry.getPrice();

            int quantity = 0;
            for (final DeathTrackerItem i : items)
            {
                if (i.getId() == entry.getId())
                {
                    quantity = i.getQuantity();
                    items.remove(i);
                    break;
                }
            }
            if (quantity > 0)
            {
                int newQuantity = entry.getQuantity() + quantity;
                long pricePerItem = entry.getPrice() == 0 ? 0 : (entry.getPrice() / entry.getQuantity());

                items.add(new DeathTrackerItem(entry.getId(), entry.getName(), newQuantity, pricePerItem * newQuantity));
            }
            else
            {
                items.add(entry);
            }
        }

        items.sort((i1, i2) -> Long.compare(i2.getPrice(), i1.getPrice()));

        // Calculates how many rows need to be display to fit all items
        final int rowSize = ((items.size() % ITEMS_PER_ROW == 0) ? 0 : 1) + items.size() / ITEMS_PER_ROW;

        itemContainer.removeAll();
        itemContainer.setLayout(new GridLayout(rowSize, ITEMS_PER_ROW, 1, 1));

        for (int i = 0; i < rowSize * ITEMS_PER_ROW; i++)
        {
            final JPanel slotContainer = new JPanel();
            slotContainer.setBackground(ColorScheme.DARKER_GRAY_COLOR);

            if (i < items.size())
            {
                final DeathTrackerItem item = items.get(i);
                final JLabel imageLabel = new JLabel();
                imageLabel.setToolTipText(buildToolTip(item));
                imageLabel.setVerticalAlignment(SwingConstants.CENTER);
                imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
                itemManager.getImage(item.getId(), item.getQuantity(), item.getQuantity() > 1).addTo(imageLabel);
                slotContainer.add(imageLabel);
            }

            itemContainer.add(slotContainer);
        }

        itemContainer.repaint();
    }

    private static String buildToolTip(DeathTrackerItem item)
    {
        final String name = item.getName();
        final int quantity = item.getQuantity();
        final long price = item.getPrice();
        return name + " x " + quantity + " (" + StackFormatter.quantityToStackSize(price) + ")";
    }
}
