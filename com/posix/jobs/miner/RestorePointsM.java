package com.posix.jobs.miner;
import com.posix.GraniteMiner.GraniteMiner;
import com.posix.utils.Util;
import org.powerbot.core.script.job.Task;
import org.powerbot.core.script.job.state.Node;
import org.powerbot.game.api.methods.tab.Inventory;
import org.powerbot.game.api.methods.tab.Summoning;
import org.powerbot.game.api.util.Filter;
import org.powerbot.game.api.wrappers.node.Item;

public class RestorePointsM  extends Node{
    static GraniteMiner.Status THIS_STATE = GraniteMiner.Status.RESTPRE_POINTS;
    Item summPot;
    final public static Filter<Item> sumFilter = new Filter<Item>() {
        @Override
        public boolean accept(Item item) {

            return (item.getId() == GraniteMiner.SUM_POT1
                    || item.getId() == GraniteMiner.SUM_POT2
                    || item.getId() == GraniteMiner.SUM_POT3
                    || item.getId() == GraniteMiner.SUM_POT4);
        }
    };
    @Override
    public boolean activate() {
        final boolean valid = workLeft();

        if ((valid && (GraniteMiner.curState == GraniteMiner.Status.NONE))) {
            GraniteMiner.curState = THIS_STATE;
        } else if (!valid && (GraniteMiner.curState == THIS_STATE)) {
            GraniteMiner.curState = GraniteMiner.Status.NONE;
        }
        return ((GraniteMiner.curState == THIS_STATE) && valid);
    }
    private boolean workLeft() {
        return(Summoning.getFamiliar() == null &&Summoning.getPoints() <7);
    }

    @Override
    public void execute() {
        System.out.println("Restore points");
        summPot = Inventory.getItem(sumFilter);
        if(summPot != null && Summoning.getPoints() <7){
            summPot.getWidgetChild().interact("Drink");
            Util.waitFor(3, new Util.Cond() {
                @Override
                public boolean accept() {
                    Task.sleep(200, 400);
                    return (Summoning.getPoints() > 7);
                }
            });
        }
    }
}
