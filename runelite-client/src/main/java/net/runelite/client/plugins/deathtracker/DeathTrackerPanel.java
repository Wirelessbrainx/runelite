package net.runelite.client.plugins.deathtracker;

import net.runelite.api.InventoryID;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.PluginErrorPanel;
import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.border.EmptyBorder;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.PluginErrorPanel;
import net.runelite.client.util.ColorUtil;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.StackFormatter;

public class DeathTrackerPanel extends PluginPanel {
//    private static final ImageIcon SINGLE_LOOT_VIEW;
//    private static final ImageIcon SINGLE_LOOT_VIEW_FADED;
//    private static final ImageIcon SINGLE_LOOT_VIEW_HOVER;
//    private static final ImageIcon GROUPED_LOOT_VIEW;
//    private static final ImageIcon GROUPED_LOOT_VIEW_FADED;
//    private static final ImageIcon GROUPED_LOOT_VIEW_HOVER;
//    private static final ImageIcon BACK_ARROW_ICON;
//    private static final ImageIcon BACK_ARROW_ICON_HOVER;

    private static final String HTML_LABEL_TEMPLATE =
            "<html><body style='color:%s'>%s<span style='color:white'>%s</span></body></html>";

    // When there is no loot, display this
    private final PluginErrorPanel errorPanel = new PluginErrorPanel();

    // Handle loot boxes
    private final JPanel logsContainer = new JPanel();

    private final JPanel overallPanel = new JPanel();
    private final JLabel overallIcon = new JLabel();
    private final JLabel overallGpLabel = new JLabel();



    private final JPanel actionsContainer = new JPanel();
    private final JLabel detailsTitle = new JLabel();
    private final JLabel backBtn = new JLabel();
//    private final JLabel singleLootBtn = new JLabel();
//    private final JLabel groupedLootBtn = new JLabel();

    private final List<DeathTrackerRecord> record = new ArrayList<>();
    private final List<DeathTrackerBox> boxes = new ArrayList<>();

    private final ItemManager itemManager;
    //    private boolean groupLoot;
    private String currentView;

    DeathTrackerPanel(final ItemManager itemManager) {

        this.itemManager = itemManager;

        setBorder(new EmptyBorder(6, 6, 6, 6));
        setBackground(ColorScheme.DARK_GRAY_COLOR);
        setLayout(new BorderLayout());

        // Create layout panel for wrapping
        final JPanel layoutPanel = new JPanel();
        layoutPanel.setLayout(new BoxLayout(layoutPanel, BoxLayout.Y_AXIS));
        add(layoutPanel, BorderLayout.NORTH);

        actionsContainer.setLayout(new BorderLayout());
        actionsContainer.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        actionsContainer.setPreferredSize(new Dimension(0, 30));
        actionsContainer.setBorder(new EmptyBorder(5, 5, 5, 10));
        actionsContainer.setVisible(false);

        final JPanel viewControls = new JPanel(new GridLayout(1, 2, 10, 0));
        viewControls.setBackground(ColorScheme.DARKER_GRAY_COLOR);

        final JPanel leftTitleContainer = new JPanel(new BorderLayout(5, 0));
        leftTitleContainer.setBackground(ColorScheme.DARKER_GRAY_COLOR);

        detailsTitle.setForeground(Color.WHITE);

        actionsContainer.add(leftTitleContainer, BorderLayout.WEST);
        actionsContainer.add(viewControls, BorderLayout.EAST);

        // Add icon and contents
        final JPanel overallInfo = new JPanel();
        overallInfo.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        overallInfo.setLayout(new GridLayout(2, 1));
        overallInfo.setBorder(new EmptyBorder(2, 10, 2, 0));
        overallGpLabel.setFont(FontManager.getRunescapeSmallFont());
        overallInfo.add(overallGpLabel);
        overallPanel.add(overallIcon, BorderLayout.WEST);
        overallPanel.add(overallInfo, BorderLayout.CENTER);

        final JMenuItem reset = new JMenuItem("Reset All");
        reset.addActionListener(e ->
        {
            record.removeIf(r -> r.matches(InventoryID.INVENTORY));
            boxes.removeIf(b -> b.matches(InventoryID.INVENTORY));
            updateOverallCost();
            logsContainer.removeAll();
            logsContainer.repaint();
        });

        final JPopupMenu popupMenu = new JPopupMenu();
        popupMenu.setBorder(new EmptyBorder(5, 5, 5, 5));
        popupMenu.add(reset);
        overallPanel.setComponentPopupMenu(popupMenu);

        logsContainer.setLayout(new BoxLayout(logsContainer, BoxLayout.Y_AXIS));
        layoutPanel.add(actionsContainer);
        layoutPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        layoutPanel.add(overallPanel);
        layoutPanel.add(logsContainer);

        errorPanel.setContent("Death Tracker", "You have not died yet.");
        add(errorPanel);
    }

    void loadHeaderIcon(BufferedImage img) {
        overallIcon.setIcon(new ImageIcon(img));
    }

    void add(ArrayList<DeathTrackerRecord> records) {
        record.clear();
        //System.out.println("Record added to pannels -> " + records.toString());
        record.addAll(records);
        //System.out.println("Record added to pannels -> " + record.toString());
        rebuild();
        buildBox(records);
        updateOverallCost();
    }

    /**
     * This method decides what to do with a new record, if a similar log exists, it will
     * add its items to it, updating the log's overall price and kills. If not, a new log will be created
     * to hold this entry's information.
     */
    private void buildBox(ArrayList<DeathTrackerRecord> records) {

        //
// If this record is not part of current view, return

//
//        // Group all similar loot together
//        if (groupLoot)
//        {
//            for (DeathTrackerBox box : boxes)
//            {
//////                if (box.matches(records))
//////                {
////                    box.combine(records);
////                    //updateOverall();
////                    return;
//////                }
//            }
////        }

        // Show main view
        remove(errorPanel);
        actionsContainer.setVisible(true);
        overallPanel.setVisible(true);


        // Create box
        final DeathTrackerBox box0 = new DeathTrackerBox(itemManager, record.get(0).getTitle());
        final DeathTrackerBox box1 = new DeathTrackerBox(itemManager, record.get(1).getTitle());
        box0.combine(record.get(0));
        box1.combine(record.get(1));

        // Create popup menu
        final JPopupMenu popupMenu = new JPopupMenu();
        popupMenu.setBorder(new EmptyBorder(5, 5, 5, 5));
        box0.setComponentPopupMenu(popupMenu);
        box1.setComponentPopupMenu(popupMenu);

        // Create reset menu
        final JMenuItem reset = new JMenuItem("Reset");
        reset.addActionListener(e ->
        {
            //records.removeAll(box.getRecords());
            boxes.remove(box0);
            boxes.remove(box1);
            logsContainer.remove(box0);
            logsContainer.remove(box1);
            logsContainer.repaint();
        });

        popupMenu.add(reset);

        // Create details menu
        final JMenuItem details = new JMenuItem("View details");
        details.addActionListener(e ->
        {
            //currentView = record.getTitle();
            detailsTitle.setText(currentView);
            backBtn.setVisible(true);
            rebuild();
        });

        popupMenu.add(details);

        // Add box to panel
        boxes.add(box0);
        boxes.add(box1);
        logsContainer.add(box0, 0);
        logsContainer.add(box1, 1);

    }

    /**
     * Rebuilds all the boxes from scratch using existing listed records, depending on the grouping mode.
     */
    private void rebuild() {
        logsContainer.removeAll();
        boxes.clear();
        //buildBox(record);
        logsContainer.revalidate();
        logsContainer.repaint();
    }

    private void updateOverallCost() {
        long overallGp = 0;

        for (DeathTrackerRecord records : record) {
            for (DeathTrackerItem item : records.getItems()) {
                overallGp += item.getPrice();
            }
        }

        overallGpLabel.setText(htmlLabel("Total Value: ", overallGp));
    }

    private static String htmlLabel(String key, long value)
    {
        final String valueStr = StackFormatter.quantityToStackSize(value);
        return String.format(HTML_LABEL_TEMPLATE, ColorUtil.toHexColor(ColorScheme.LIGHT_GRAY_COLOR), key, valueStr);

    }

}














