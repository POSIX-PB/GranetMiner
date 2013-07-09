package com.posix.jobs.miner;
import com.posix.GraniteMiner.GraniteMiner;
import com.posix.utils.Util;
import org.powerbot.core.script.job.Task;
import org.powerbot.core.script.job.state.Node;
import org.powerbot.game.api.methods.interactive.Players;
import org.powerbot.game.api.methods.tab.Equipment;
import org.powerbot.game.api.methods.tab.Inventory;
import org.powerbot.game.api.methods.tab.Summoning;
import org.powerbot.game.api.wrappers.Tile;
import org.powerbot.game.api.wrappers.interactive.NPC;

public class ToBankMiner extends Node {
    Tile[] bankPath = {new Tile(3448,3707,0),new Tile(3448,3712,0),new Tile(3449,3720,0)};
    static GraniteMiner.Status THIS_STATE = GraniteMiner.Status.TELEPORTING_BANK;
    NPC familiar;
    int midUrn = 20407;
    public static final Tile T_DUNG = new Tile(3448,3697,0);
    @Override
    public boolean activate() {
        //famular is null, No more sumoning pouches, out of urns,
        final boolean valid = workLeft();

        if ((valid && (GraniteMiner.curState == GraniteMiner.Status.NONE))) {
            GraniteMiner.curState = THIS_STATE;
        } else if (!valid && (GraniteMiner.curState == THIS_STATE)) {
            GraniteMiner.curState = GraniteMiner.Status.NONE;
        }
        return ((GraniteMiner.curState == THIS_STATE) && valid);
    }
    private boolean workLeft() {
//        System.out.println("Dist ot bank: "+Util.getDist(Vars.T_DUNG) );
        familiar = Summoning.getFamiliar();
        return (((familiar == null && Inventory.getCount(SummonFamMining.LAVA_TITAN)==0)
                || (Inventory.getCount(BankMiner.decMiningUrn)==0&& Inventory.getCount(midUrn)==0))
        && Util.getDist(BankMiner.bankTile)>10);
    }

    @Override
    public void execute() {
        DropOres.drop(6979, 6981, 6983);
        Util.getDist(T_DUNG);
        if(Players.getLocal().getLocation().distance(T_DUNG)>30){
            Equipment.getItem(Equipment.Slot.RING).getWidgetChild().interact("Teleport to Daemonheim");
            Util.waitFor(6,new Util.Cond() {
                @Override
                public boolean accept() {
                    Task.sleep(400,500);
                    return Players.getLocal().getLocation().distance(T_DUNG)<20;
                }
            });
        }
        else{
            Util.walkPath(bankPath,false,20);
        }
    }
}
