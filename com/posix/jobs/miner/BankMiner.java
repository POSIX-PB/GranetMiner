package com.posix.jobs.miner;

import com.posix.GraniteMiner.GraniteMiner;
import com.posix.utils.Util;
import org.powerbot.core.script.job.Task;
import org.powerbot.core.script.job.state.Node;
import org.powerbot.game.api.methods.tab.Inventory;
import org.powerbot.game.api.methods.tab.Summoning;
import org.powerbot.game.api.methods.widget.Bank;
import org.powerbot.game.api.wrappers.Tile;

/**
 * Created with IntelliJ IDEA.
 * User: C0r31N
 * Date: 4/15/13
 * Time: 10:10 PM
 * To change this template use File | Settings | File Templates.
 */
public class BankMiner extends Node{
    static GraniteMiner.Status THIS_STATE = GraniteMiner.Status.BANKING;
    public static boolean bank = false;
    public static int decMiningUrn = 20406;
    public static Tile bankTile = new Tile(3448,3719,0);
    public static final int SMALL_GRANITE = 6979;
    public static final int MED_GRANITE = 6981;
    public static final int LARGE_GRANITE = 6983;
    public static final int[]BANK_ITEMSMiner= {SMALL_GRANITE,MED_GRANITE, LARGE_GRANITE};

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
//        System.out.println("LAva: "+(Inventory.getCount(SummonFamMining.LAVA_TITAN)==0 ));
//        System.out.println("fam: "+(Summoning.getFamiliar() ));
//        System.out.println("urn: "+(Inventory.getCount(Drink) ));
        bank =   ((Inventory.getCount(SummonFamMining.LAVA_TITAN)==0 && Summoning.getFamiliar() == null)
            || Inventory.getCount(decMiningUrn)==0);
        if(!bank){
            if(Bank.isOpen()){
                Bank.close();
                Util.waitFor(1, new Util.Cond() {
                    @Override
                    public boolean accept() {
                        Task.sleep(200, 400);
                        return (!Bank.isOpen());
                    }
                });
            }
        }
        return (bank && Util.getDist(bankTile)<10);
    }

    @Override
    public void execute() {
        if(!Bank.isOpen()){ //Open Bank
            Bank.open();
            Util.waitFor(3,new Util.Cond() {
                @Override
                public boolean accept() {
                    return !Bank.isOpen();
                }
            });
        }
        else if(Bank.isOpen()){
            for(final int item : BANK_ITEMSMiner){//Banking items
                if(Inventory.getCount(item)>0){
                    Bank.deposit(item, Bank.Amount.ALL);
                    Task.sleep(100, 200);
                }
            }
            if(Util.getInvDoseCnt(GraniteMiner.sumPots)<2){
                if(Inventory.getCount(GraniteMiner.SUM_POT4)<1){  //Getting sum pots
                    if(Bank.getItemCount(true, GraniteMiner.SUM_POT4)>1){
                        Bank.withdraw(GraniteMiner.SUM_POT4,1);
                        Util.waitFor(1,new Util.Cond() {
                            @Override
                            public boolean accept() {
                                Task.sleep(200, 300);
                                return (Inventory.getCount(GraniteMiner.SUM_POT4) > 0);
                            }
                        });
                    }
                    else{
                        GraniteMiner.curState = GraniteMiner.Status.OUT_OF_SUPPLIES;
                        System.out.println("Ran out of Sum Pot 4");
                    }
                }

            }
            if(Inventory.getCount(SummonFamMining.LAVA_TITAN)<1){  //Getting Lava pouches
                if(Bank.getItemCount(true,SummonFamMining.LAVA_TITAN)>1){
                    Bank.withdraw(SummonFamMining.LAVA_TITAN,1 - Inventory.getCount(SummonFamMining.LAVA_TITAN));
                    Util.waitFor(2,new Util.Cond() {
                        @Override
                        public boolean accept() {
                            Task.sleep(100, 200);
                            return (Inventory.getCount(SummonFamMining.LAVA_TITAN) > 0);
                        }
                    });
                }
                else{
                    GraniteMiner.curState = GraniteMiner.Status.OUT_OF_SUPPLIES;
                    System.out.println("Ran out of Lava Pouches");
                }
            }
            if(Inventory.getCount(decMiningUrn)<13){  //Getting Urns
                if(Bank.getItemCount(true,decMiningUrn)>13){
                    Bank.withdraw(decMiningUrn,13- Inventory.getCount(decMiningUrn));
                    Util.waitFor(2,new Util.Cond() {
                        @Override
                        public boolean accept() {
                            Task.sleep(100, 200);
                            return (Inventory.getCount(decMiningUrn) > 0);
                        }
                    });
                }
                else{
                    GraniteMiner.curState = GraniteMiner.Status.OUT_OF_SUPPLIES;
                    System.out.println("Ran out of Urns");
                }
            }


        }


    }
}
