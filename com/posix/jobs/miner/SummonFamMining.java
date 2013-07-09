package com.posix.jobs.miner;
import com.posix.GraniteMiner.GraniteMiner;
import com.posix.utils.Util;
import org.powerbot.core.script.job.Task;
import org.powerbot.core.script.job.state.Node;
import org.powerbot.game.api.methods.tab.Inventory;
import org.powerbot.game.api.methods.tab.Summoning;
import org.powerbot.game.api.wrappers.interactive.NPC;


public class SummonFamMining extends Node{
    static GraniteMiner.Status THIS_STATE = GraniteMiner.Status.SUMMON_FAM;
    boolean notDone;
    NPC familiar;
    public static int LAVA_TITAN = 12788;
    @Override
    public boolean activate() {
        //Fam is null and pouches and Sum pot in inventory
        final boolean valid = workLeft();

        if ((valid && (GraniteMiner.curState == GraniteMiner.Status.NONE))) {
            GraniteMiner.curState = THIS_STATE;
        } else if (!valid && (GraniteMiner.curState == THIS_STATE)) {
            GraniteMiner.curState = GraniteMiner.Status.NONE;
        }
        return ((GraniteMiner.curState == THIS_STATE) && valid);
    }
    private boolean workLeft() {
        notDone = false;
        familiar = Summoning.getFamiliar();
        if((familiar == null )){
            if(Inventory.getCount(LAVA_TITAN)>0 //Lava titan pouch
                    && Summoning.getPoints()>=7){
                notDone = true;
            }
        }
        return (notDone);
    }

    @Override
    public void execute() {
        if(Summoning.getPoints()>=7 && familiar == null ){
            Summoning.summonFamiliar(Summoning.Familiar.LAVA_TITAN);
            Util.waitFor(3, new Util.Cond() {
                @Override
                public boolean accept() {
                    Task.sleep(300, 500);
                    familiar = Summoning.getFamiliar();
                    return (familiar != null);
                }
            });
        }
    }
}
