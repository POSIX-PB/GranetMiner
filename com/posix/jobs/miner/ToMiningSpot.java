package com.posix.jobs.miner;
import com.posix.GraniteMiner.GraniteMiner;
import com.posix.utils.Util;
import org.powerbot.core.script.job.state.Node;
import org.powerbot.game.api.methods.widget.Bank;
import org.powerbot.game.api.wrappers.Tile;

public class ToMiningSpot  extends Node{
    Tile[] path = new Tile[] { new Tile(3213, 2945, 0), new Tile(3212, 2940, 0), new Tile(3210, 2935, 0),
            new Tile(3207, 2931, 0), new Tile(3203, 2928, 0), new Tile(3199, 2925, 0),
            new Tile(3195, 2922, 0), new Tile(3191, 2919, 0), new Tile(3187, 2916, 0),
            new Tile(3182, 2914, 0), new Tile(3177, 2912, 0), new Tile(3172, 2912, 0),
            new Tile(3167, 2911, 0), new Tile(3162, 2911, 0) };
    Tile miningSpot = new Tile(3167, 2907, 0);
    Tile startPath = new Tile(3213, 2945, 0);
    static GraniteMiner.Status THIS_STATE = GraniteMiner.Status.WALKING_MINE;
    @Override
    public boolean activate() {
        //At bank and have items set up in inventory and not near mining spot
        final boolean valid = workLeft();

        if ((valid && (GraniteMiner.curState == GraniteMiner.Status.NONE))) {
            GraniteMiner.curState = THIS_STATE;
        } else if (!valid && (GraniteMiner.curState == THIS_STATE)) {
            GraniteMiner.curState = GraniteMiner.Status.NONE;
        }
        return ((GraniteMiner.curState == THIS_STATE) && valid);

    }
    private boolean workLeft() {
        return (!BankMiner.bank && !Bank.isOpen() && Util.getDist(miningSpot)>20 );
    }

    @Override
    public void execute() {
        if(Util.getDist(startPath)>50)
            GraniteMiner.teleport(1);
//         GraniteMiner.walkPath(path);
        if(Util.getDist(startPath)<15)
            Util.walkPath(path,false,30);
    }
}
