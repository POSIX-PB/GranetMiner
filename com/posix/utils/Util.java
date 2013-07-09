package com.posix.utils;

import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;


import com.posix.utils.*;
import org.powerbot.core.event.listeners.PaintListener;
import org.powerbot.core.script.job.Task;
import org.powerbot.game.api.methods.Calculations;
import org.powerbot.game.api.methods.Settings;
import org.powerbot.game.api.methods.Tabs;
import org.powerbot.game.api.methods.input.Mouse;
import org.powerbot.game.api.methods.interactive.NPCs;
import org.powerbot.game.api.methods.interactive.Players;
import org.powerbot.game.api.methods.node.Menu;
import org.powerbot.game.api.methods.tab.Inventory;
import org.powerbot.game.api.methods.widget.Camera;
import org.powerbot.game.api.util.Filter;
import org.powerbot.game.api.util.Random;
import org.powerbot.game.api.util.Timer;
import org.powerbot.game.api.wrappers.Area;
import org.powerbot.game.api.wrappers.Locatable;
import org.powerbot.game.api.wrappers.Tile;
import org.powerbot.game.api.wrappers.interactive.NPC;
import org.powerbot.game.api.wrappers.node.Item;
import org.powerbot.game.api.wrappers.node.SceneObject;
import org.powerbot.game.api.wrappers.widget.Widget;
import org.powerbot.game.api.wrappers.widget.WidgetChild;

import javax.swing.*;

public class Util implements PaintListener {

	private static WidgetChild hpLevel = new WidgetChild(new Widget(320), 144);
	private static double hpLvl = -1,curHp;
	private static WidgetChild lpNumb = new WidgetChild(new Widget(748), 8);
	private static WidgetChild skillsTab = new WidgetChild(new Widget(548), 109);
    public static double getDist(Tile t1, Tile t2){
        return t1.distance(t2);
    }
    public static double getDist(Tile t){
        return Players.getLocal().getLocation().distance(t);
    }
	public static int getXpTillNextLvl(int curXp, int curLvl){
		double xpRemain = 0, dif;
		for(double i = 1; i<=curLvl;i++){
			dif = i + 300 * Math.pow(2, (i/7));
			xpRemain += dif;
		}
        xpRemain=xpRemain/4;
		return (int)((xpRemain)- (curXp) );
//        int total = 0;
//        for (int i = 1; i < curLvl; i++)
//        {
//            total += i + 300 * Math.pow(2, i / 7.0);
//        }
//
//        return (int)(total * 2.5);
	}
	public static int getHpPercent(){
		double percent = -1;
		if((hpLevel != null) &&(lpNumb != null)){
			if(hpLvl == -1){
				if(!hpLevel.visible())
					skillsTab.click(true);
				hpLvl = Integer.parseInt(hpLevel.getText());
				hpLvl = hpLvl * 40;
			}			
			curHp =Settings.get(659)/2;
			percent =(curHp/hpLvl)*100;	

			return (int)percent;
		}
		return -1;
	}
	public static int getHp(){
		if(lpNumb != null)
			return Integer.parseInt(lpNumb.getText());
		return -1;
	}
	public static void turnTo(NPC npc, Camera camera){
		if(!npc.isOnScreen()){
			camera.turnTo(npc);			
		}		
	}
	public static void turnTo(SceneObject scObj, Camera camera){
        if(!scObj.isOnScreen()){
			camera.turnTo(scObj);
            while(!scObj.isOnScreen()){
                Camera.setPitch(Camera.getPitch()-5);
            }
		}

	}
	public static void turnTo(Tile tile, Camera camera)
	{
		if(!tile.isOnScreen())
		{
			camera.turnTo(tile);			
		}		
	}
    public static void turnTo(Locatable loc) {
        if(loc == null)
            return;
        int cAngle = Camera.getYaw();
        int angle = 180+Camera.getMobileAngle(loc);
        int dir = cAngle-angle;
        Camera.setAngle(Camera.getYaw()+((dir > 0 ^ Math.abs(dir) > 180) ? 10 : -10));
    }
	public static boolean isIn(Area a)
	{
		Tile meTile = Players.getLocal().getLocation();
		if(meTile != null)
			return (a.contains(meTile.getLocation()));
		return false;


	}
    public static Timer walkTimer = null;
    public static Tile[] finalPath = null;
	public static boolean walkPath(Tile[] path, boolean reverse, int time){
        finalPath = path.clone();
        for (int i = 0; i < path.length; i++) {

            if(reverse){
                finalPath[i] = path[path.length - i - 1];
            }
            else{
                finalPath = path.clone();
            }
        }

        System.out.println("Distance to end: "+Calculations.distanceTo(finalPath[finalPath.length - 1]) );

        walkTimer = new Timer(time*1000);
        while(walkTimer.isRunning()){
            if (Calculations.distanceTo(finalPath[finalPath.length - 1]) > 4){
                Tile n = getNext(finalPath);
                if (n != null) {
                    n.randomize(2, 2).clickOnMap();
                    Task.sleep(600,800);
                }
            }
            else{
                return true;
            }
        }
		return false;
	}

	public static Tile getNext(Tile[] path) {
		boolean found = false;
        Point nextPoint = null;
		for (int a = 0; (a < path.length) && !found; a++) {
//            System.out.println("Calc: "+Calculations.worldToMap(path[path.length-1-a].getX(), path[path.length-1-a].getY()));
            nextPoint = Calculations.worldToMap(path[path.length-1-a].getX(), path[path.length-1-a].getY());
			if (nextPoint.getX() != -1 && nextPoint.getY() != -1) {
                //point.setLocation(Calculations.worldToMap(path[path.length-1-a].getX(),path[path.length-1-a].getY()));
//                System.out.println("Next point: "+Calculations.worldToMap(path[path.length-1-a].getX(), path[path.length-1-a].getY()));
				found = true;
				return path[path.length - 1 - a];
			}
		}
		return null;
	}
	public static Item edible() {
		Item[] inventory = Inventory.getItems();
		for (Item i : inventory) {
			String[] s = i.getWidgetChild().getActions();
			if (s != null) {
				List<String> l = Arrays.asList(s);
				if (l.contains("Eat")
                        || i.getId() == 6685
                        || i.getId() == 6687
                        || i.getId() == 6689
                        || i.getId() == 6691
                        || i.getId() == 23351
                        || i.getId() == 23353
                        || i.getId() == 23355
                        || i.getId() == 23357
                        || i.getId() == 23359
                        || i.getId() == 23361) {
					return i;
				}
			}
		}
		return null;
	}
    public static int foodCount() {
        int count = 0;
        Item[] inventory = Inventory.getItems();
        for (Item i : inventory) {
            String[] s = i.getWidgetChild().getActions();
            if (s != null) {
                List<String> l = Arrays.asList(s);
                if (l.contains("Eat")) {
                    count++;
                }
            }
        }
        return count;
    }
	public static void logout(){
		Tabs.LOGOUT.open();
		Widget logout = new Widget(182);
		if(logout != null){
			logout.getChild(13).click(true);
		}
	}
	public static boolean interact(int tries, Tile target, String action){
		if(tries > 0){
			if(Mouse.move(target.getCentralPoint()) && target.contains((Mouse.getLocation()))){
				return Menu.select(action);
			}
			else{
				return interact(--tries,target,action);
			}
		}
		return false;
	}
	public static Tile[] getRandTileArray(Area a)
	{
		Tile t[] =  new Tile[3];
		int x = a.getBounds().x;
		int y = a.getBounds().y;
		t[0] = new Tile(Random.nextInt(x,x+a.getBounds().width),Random.nextInt(y,y+a.getBounds().height),0);
        t[1] = new Tile(t[0].getX()-1,t[0].getY(),0);
        t[2] = new Tile(t[0].getX()-2,t[0].getY(),0);
        for(int i=0;i< t.length;i++){
        System.out.println("T"+i+" X"+ t[i].getX()+" T"+i+" y"+ t[i].getY());
        }
        return t;
	}
    public static Tile getRandTile(Area a)
    {
        Tile t;
        int x = a.getBounds().x;
        int y = a.getBounds().y;
        t = new Tile(Random.nextInt(x,x+a.getBounds().width),Random.nextInt(y,y+a.getBounds().height),0);
        return t;
    }

	public static NPC getNthClosestNPC(final int n, final Filter<NPC> filter) {
		final NPC[] loaded = NPCs.getLoaded(filter);
		if (loaded.length < 1) return null;
		if (loaded.length - 1 < n) return n > 0 ? getNthClosestNPC(n - 1, filter) : null;
		Arrays.sort(loaded, new Comparator<NPC>() {
			@Override
			public int compare(NPC o1, NPC o2) {
				return (int) (Calculations.distanceTo(o1) - Calculations.distanceTo(o2));
			}
		});
		return loaded[n];
	}
    Polygon[] p = null;
    public static Point point = null;

    public static boolean waitFor(int time,final Cond condition) {
        final Timer t = new Timer(Random.nextInt(time * 1000,(time+2)*100));
        while (t.isRunning()) {
            if (Players.getLocal().isMoving())
                t.reset();
            if (condition.accept())
                return true;
            Task.sleep(100, 150);
        }
        return false;
    }
    public interface Cond {//Condition
        public boolean accept();
    }
    public static int getInvDoseCnt(ArrayList<Potion> pots){
        int invDoseCnt = 0;
        for(Potion p : pots){
                System.out.println("Add ammout: "+Inventory.getCount(p.id) * p.doseAmount);
              invDoseCnt += Inventory.getCount(p.id) * p.doseAmount;
        }
        return invDoseCnt;
    }

    @Override
    public void onRepaint(Graphics g) {
        g.setColor(Color.green);
        if(point!=null){
            p[0].addPoint(point.x,point.y);
            p[0].addPoint(point.x+1,point.y);
            p[0].addPoint(point.x+1,point.y+1);
            p[0].addPoint(point.x,point.y+1);
            if(p.length > 0){
                g.drawPolygon(p[0]);
                g.fillPolygon(p[0]);
            }
        }
    }

    public static boolean  onTile(Tile[] tiles){
        for(Tile t: tiles){
            if(getDist(t)==0){
                return true;
            }
        }
        return false;
    }
    public static int getPrice(final int id) {
        final String add = "http://scriptwith.us/api/?return=text&item=" + id;
        try  {

            final BufferedReader in = new BufferedReader(new InputStreamReader(
                    new URL(add).openConnection().getInputStream()));
            final String line = in.readLine();
            return Integer.parseInt(line.split("[:]")[1]);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }
    public static String getUsername() {
        final Frame frame = JFrame.getFrames()[0];
        final Component component = frame.getComponent(0);

        if (component instanceof JRootPane) {
            final JMenuBar menuBar = ((JRootPane) component).getJMenuBar();
            for (final Component item : menuBar.getComponents()) {
                if (item instanceof JMenu) {
                    final JMenu menu = (JMenu) item;
                    if (menu.getText().equals("Edit")) {
                        JMenuItem element = (JMenuItem) menu.getPopupMenu().getSubElements()[0];
                        String text = element.getText();
                        if (!text.equals("Sign in")) {
                            return text.substring(0, text.length() - 3);
                        }
                    }
                }
            }
        }

        return null;
    }



}
