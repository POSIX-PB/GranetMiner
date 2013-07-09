package com.posix.jobs.miner;
import com.posix.GraniteMiner.GraniteMiner;
import org.powerbot.core.script.job.Task;
import org.powerbot.core.script.job.state.Node;
import org.powerbot.game.api.methods.Widgets;
import org.powerbot.game.api.methods.input.Mouse;
import org.powerbot.game.api.methods.node.Menu;
import org.powerbot.game.api.methods.tab.Inventory;
import org.powerbot.game.api.util.Random;
import org.powerbot.game.api.wrappers.interactive.NPC;
import org.powerbot.game.api.wrappers.widget.WidgetChild;

public class DropOres extends Node{
    static GraniteMiner.Status THIS_STATE = GraniteMiner.Status.DROPING_ORES;
    boolean notDone;
    NPC familiar;
    @Override
    public boolean activate() {
        //Inv full
        final boolean valid = workLeft();

        if ((valid && (GraniteMiner.curState == GraniteMiner.Status.NONE))) {
            GraniteMiner.curState = THIS_STATE;
        } else if (!valid && (GraniteMiner.curState == THIS_STATE)) {
            GraniteMiner.curState = GraniteMiner.Status.NONE;
        }
        return ((GraniteMiner.curState == THIS_STATE) && valid);
    }
    private boolean workLeft() {
        return (Inventory.isFull());
    }

    @Override
    public void execute() {
        drop(6979, 6981, 6983);
        if(Inventory.contains(20408)){
            Inventory.getItem(20408).getWidgetChild().click(true);
            Task.sleep(100,150);
        }
    }
    public static void dropInventory() {
        dropInventory(false);
    }

    public static void dropInventory(boolean hop) {
        dropDown(hop, true);
    }

    public static void dropAllExcept(int... id) {
        dropAllExcept(false, id);
    }

    public static void dropAllExcept(boolean hop, int... id) {
        dropDown(hop, true, id);
    }

    public static void drop(int... id) {
        drop(true, id);
    }

    public static void drop(boolean hop, int... id) {
        dropDown(hop, false, id);
    }

    private static void dropDown(boolean hop, boolean skip, int... id) {
        WidgetChild inv = Widgets.get(679, 0);
        for (int x = 0; x < 4; x++)
            for (int y = x; y < 28; y += 4){
                WidgetChild spot = inv.getChild(y);
                if (spot != null && checkID(spot.getChildId(), skip, id))
                    clickItem(spot, hop);
            }
    }

    private static boolean checkID(int ID, boolean Skip, int... id) {
        if (ID == -1)
            return false;

        for (int curID : id)
            if (curID == ID)
                return !Skip;

        return Skip;
    }

    private static void clickItem(WidgetChild item, boolean hop) {
        if (!item.getBoundingRectangle().contains(Mouse.getLocation()))
            move(item.getCentralPoint().x, item.getAbsoluteY() + 5, hop);

        Mouse.click(false);
        move(Mouse.getX(), getDropLocation(), hop);
        Mouse.click(true);
    }

    private static void move(int x, int y, boolean hop) {
        if (hop)
            Mouse.hop(x, y, 3, 3);
        else
            Mouse.move(x, y, 3, 3);
    }

    private static int getDropLocation() {
        String[] actions = Menu.getItems();
        for (int i = 0; i < actions.length; i++)
            if (actions[i].contains("Drop "))
                return Menu.getLocation().y + 21 + 16 * i + Random.nextInt(3, 6);
        return Menu.getLocation().y + 40;
    }
}
