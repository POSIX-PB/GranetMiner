package com.posix.jobs.miner;
import com.posix.GraniteMiner.GraniteMiner;
import com.posix.utils.Util;
import org.powerbot.core.script.job.Task;
import org.powerbot.core.script.job.state.Node;
import org.powerbot.game.api.methods.interactive.Players;
import org.powerbot.game.api.methods.node.Menu;
import org.powerbot.game.api.methods.node.SceneEntities;
import org.powerbot.game.api.methods.tab.Inventory;
import org.powerbot.game.api.methods.tab.Summoning;
import org.powerbot.game.api.methods.widget.Camera;
import org.powerbot.game.api.util.Random;
import org.powerbot.game.api.wrappers.Tile;
import org.powerbot.game.api.wrappers.interactive.Player;
import org.powerbot.game.api.wrappers.node.SceneObject;


public class MineOres  extends Node{
    public static int EAST =275;
    public static int oreToMine = 10947;
    Tile miningSpot = new Tile(3167, 2907, 0);
    public static Tile[] ROCK_SPOTS = {new Tile(3167, 2911, 0),new Tile(3165, 2910, 0),new Tile(3165, 2909, 0),new Tile(3165, 2908, 0)
            ,new Tile(3168, 2904, 0),new Tile(3167, 2904, 0)};
    static GraniteMiner.Status THIS_STATE = GraniteMiner.Status.MINING;
    @Override
    public boolean activate() {
        // Fam is !null, Has urns, and inv not full
        final boolean valid = workLeft();

        if ((valid && (GraniteMiner.curState == GraniteMiner.Status.NONE))) {
            GraniteMiner.curState = THIS_STATE;
        } else if (!valid && (GraniteMiner.curState == THIS_STATE)) {
            GraniteMiner.curState = GraniteMiner.Status.NONE;
        }
        return ((GraniteMiner.curState == THIS_STATE) && valid);

    }
    private boolean workLeft() {
        return (!Inventory.isFull()&& Summoning.getFamiliar()!=null && Util.getDist(miningSpot)<10
                && (Inventory.contains(20406)||Inventory.contains(20407)));
    }

    @Override
    public void execute() {
        if(Inventory.contains(20408)){
            Inventory.getItem(20408).getWidgetChild().click(true);
            Task.sleep(100,150);
        }
        if(!(Camera.getYaw() > EAST-15 && Camera.getYaw() < EAST+15)){
            Camera.setAngle(Random.nextInt(260, 280));
            Task.sleep(60,100);
            Camera.setPitch(99);
            Task.sleep(150,200);
        }
            SceneObject ore = getClosestOre(ROCK_SPOTS);
            final Player p = Players.getLocal();

            if(p != null){
                if(ore != null){
                    if(ore.isOnScreen()){
                        if(!p.isMoving() && p.isIdle()){
                            if(Random.nextInt(0, 7) % 2 == 0){
                                if(ore.click(false) && Menu.isOpen() && Menu.contains("Mine")){
                                    Menu.select("Mine");
                                    Task.sleep(650, 1000);
                                }
                            } else {
                                ore.interact("Mine", "Granite Rock");
                                Task.sleep(650, 1000);
                            }
                        }
                    } else {
                        Camera.turnTo(ore);
                    }
                }
            }
        }
    public static SceneObject getClosestOre(Tile[] ores){
        SceneObject ore = null, finalOre = null;
        for(Tile t : ores){
          ore =  SceneEntities.getAt(t);
            if(ore.getId() == oreToMine){
                if(finalOre== null){
                    finalOre = ore;
                }
                else{
                    if(Util.getDist(finalOre.getLocation())>Util.getDist(ore.getLocation())){
                        finalOre = ore;
                    }
                }
            }
        }
       return finalOre;
    }

}
