package com.posix.GraniteMiner;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.imageio.ImageIO;

import com.posix.jobs.miner.*;
import com.posix.utils.*;

import org.powerbot.core.Bot;
import org.powerbot.core.event.events.MessageEvent;
import org.powerbot.core.event.listeners.MessageListener;
import org.powerbot.core.event.listeners.PaintListener;
import org.powerbot.core.script.ActiveScript;
import org.powerbot.core.script.job.Task;
import org.powerbot.core.script.job.state.Node;
import org.powerbot.core.script.job.state.Tree;

import org.powerbot.game.api.Manifest;
import org.powerbot.game.api.methods.Calculations;
import org.powerbot.game.api.methods.Game;
import org.powerbot.game.api.methods.Tabs;
import org.powerbot.game.api.methods.Widgets;
import org.powerbot.game.api.methods.input.Mouse;
import org.powerbot.game.api.methods.input.Mouse.Speed;
import org.powerbot.game.api.methods.interactive.Players;
import org.powerbot.game.api.methods.tab.Inventory;
import org.powerbot.game.api.methods.tab.Skills;
import org.powerbot.game.api.methods.widget.Lobby;
import org.powerbot.game.api.methods.widget.WidgetCache;
import org.powerbot.game.api.util.Random;
import org.powerbot.game.api.util.Time;
import org.powerbot.game.api.wrappers.Tile;
import org.powerbot.game.api.util.Timer;
import org.powerbot.game.api.wrappers.node.Item;
import org.powerbot.game.api.wrappers.node.SceneObject;
import org.powerbot.game.api.wrappers.widget.Widget;
import org.powerbot.game.api.wrappers.widget.WidgetChild;
import org.powerbot.game.bot.Context;
import org.powerbot.game.client.Client;

@Manifest(
        authors = {"POSIX"},
        name = "Granite Miner",
        description = "V"+GraniteMiner.version+"Mines Granet with summoning",
        version = GraniteMiner.version,
        vip = false,
        hidden = false
)
public class GraniteMiner extends ActiveScript implements PaintListener, MessageListener, MouseListener
{

    public static final double version = 1.05;

    /* Mouse */
    public static Color clickColor = new Color(187, 0, 0);
    public static Color mouseColor = new Color(0, 0, 0, 70);
    public static Color mainColor = new Color(0, 0, 0);
    private Point[] points;
    private int pointIndex;
    private int red;
    private int green;
    private int blue;

    /* Timing */
    public long startTime;
    public long runTime;

    public Timer updateTimer;

    /* Stats */
    private int skill = Skills.MINING;
    private int startExp = Skills.getExperience(skill);
    private int startLv = Skills.getLevel(skill);
    private int mined = 0;
    public static Status curState = Status.SETTING_UP;

    /* Misc checks - Mining */
    public boolean update = false;
    public boolean isShowing = true;

    /* Misc checks - Splitting */
    public boolean isSplitting = false;

    /* Misc chat */
    private Image chat = null;
    private int chatBase = 454;
    private float chatYSpacing = 20.5F;
    private int lChatXSpacing = 20;
    private int rChatXSpacing = 205;

    private List<String> buttons = new ArrayList<String>();

    public Widget friendsChat = null;
    public String world;
    public int worldInt = 0;
    public static final int SUM_POT4 = 12140;
    public static final int SUM_POT3 = 12142;
    public static final int SUM_POT2 = 12144;
    public static final int SUM_POT1 = 12146;
    public static final ArrayList<Potion> sumPots = new ArrayList<Potion>();


    Tile[] path = new Tile[] { new Tile(3175, 2985, 0), new Tile(3178, 2981, 0), new Tile(3182, 2978, 0),
            new Tile(3187, 2976, 0), new Tile(3191, 2973, 0), new Tile(3194, 2969, 0),
            new Tile(3198, 2966, 0), new Tile(3201, 2962, 0), new Tile(3204, 2958, 0),
            new Tile(3207, 2954, 0), new Tile(3211, 2951, 0), new Tile(3213, 2946, 0),
            new Tile(3213, 2941, 0), new Tile(3212, 2936, 0), new Tile(3210, 2931, 0),
            new Tile(3205, 2929, 0), new Tile(3201, 2926, 0), new Tile(3196, 2925, 0),
            new Tile(3191, 2923, 0), new Tile(3188, 2919, 0), new Tile(3184, 2916, 0),
            new Tile(3179, 2914, 0) };

    Tile[] bankPath = {new Tile(3448,3697,0),new Tile(3449,3713,0)};

    static Tile[] destinations = new Tile[] {new Tile(3066, 3505, 0), new Tile(3214, 2954, 0)};
    private Client client = Bot.client();
    private Tree jobContainer = null;
    private final List<Node> jobsCollection = Collections.synchronizedList(new ArrayList<Node>());
    public static enum Status
    {
        SETTING_UP, NONE, MINING,DROPING_ORES,SUMMON_FAM, OUT_OF_SUPPLIES, RESTPRE_POINTS, BUYING, REFILLING, BANKING, WALKING_MINE, WALKING_SHOP, TELEPORTING_MINE, TELEPORTING_REFILL, TELEPORTING_BANK
        ;
    };

    public void onStart(){
        while(!Game.isLoggedIn()){
            Task.sleep(200);
        }
        while(Lobby.isOpen()){
            Task.sleep(200);
        }
        while(Players.getLocal().getLocation() == null){
            Task.sleep(200);
        }
        while(Game.getClientState() != Game.INDEX_MAP_LOADED){
            Task.sleep(200);
        }

        try{
            this.chat = ImageIO.read(new URL("http://dl.dropbox.com/u/26690655/RSBOT/GMiner/Chat.png"));

        } catch(Exception e){ System.out.println("FAILED TO LOAD!"); }

        Mouse.setSpeed(Speed.FAST);
        provide(new DropOres());
        provide(new ToBankMiner());
        provide(new BankMiner());
        provide(new ToMiningSpot());
        provide(new RestorePointsM());
        provide(new SummonFamMining());
        provide(new MineOres());
        curState = Status.NONE;
        sumPots.add(new Potion(SUM_POT1,1));
        sumPots.add(new Potion(SUM_POT2,2));
        sumPots.add(new Potion(SUM_POT3,3));
        sumPots.add(new Potion(SUM_POT4,4));
        startTime = System.currentTimeMillis();
        friendsChat = Widgets.get(548);
        friendsChat.getChild(96).click(true);
        Task.sleep(100,150);
        friendsChat = Widgets.get(550);
        world = friendsChat.getChild(18).getText().substring(26, friendsChat.getChild(18).getText().length());
        worldInt = Integer.parseInt(world);
        Context.setLoginWorld(worldInt);
        friendsChat = Widgets.get(548);
        Inventory.getCount();
        DropOres.drop(6979, 6981, 6983);
    }

    @Override
    public int loop()
    {
        closeXpPopUp();

        if (Game.getClientState() != Game.INDEX_MAP_LOADED) {
            return 1000;
        }
        if (client != Bot.client()) {
            WidgetCache.purge();
            Bot.context().getEventManager().addListener(this);
            client = Bot.client();
        }
        if (jobContainer != null) {
            final Node job = jobContainer.state();
            if (job != null) {
                jobContainer.set(job);
                getContainer().submit(job);
                job.join();
                if(curState == Status.OUT_OF_SUPPLIES)
                {
                    System.out.println("OUT_OF_SUPPLIES");
                    Util.logout();
                    shutdown();
                }
            }
        }
        return Random.nextInt(50, 200);
    }

	/* Useful funcs */
    public final void provide(final Node... jobs) {
        for (final Node job : jobs) 	{
            if(!jobsCollection.contains(job)) {
                jobsCollection.add(job);
            }
        }
        jobContainer = new Tree(jobsCollection.toArray(new Node[jobsCollection.size()]));
    }

    public int getPerHr(int i){
        return (int)(i / (runTime / (double)(1000 * 60 * 60)));
    }

    public double format(double f){
        return format(f, 2);
    }

    public double format(double f, int i){
        String dec = "###.";
        for(int i2 = 0; i2 < i; i2++)
            dec = dec + "#";

        return Double.parseDouble(new DecimalFormat(dec).format(f).replace(",", "."));
    }


    public static WidgetChild getWidget(int parent, int child){
        return new Widget(parent).getChild(child);
    }

    public static void teleport(int i){
        while(!Tabs.ABILITY_BOOK.isOpen())
            Tabs.ABILITY_BOOK.open();

        if(getWidget(275, 41).click(true))
            Task.sleep(350);

        if(getWidget(275, 46).click(true))
            Task.sleep(350);

        Mouse.click(575, 355, true);

        Timer t = new Timer(2000);

        while(t.isRunning() && !(new Widget(1092).validate()))
            Task.sleep(50);

        //Edge
        if(i == 0)
            getWidget(1092, 45).click(true);

        //Bandit
        if(i == 1)
            getWidget(1092, 7).click(true);

        Task.sleep(2000);

        while(Players.getLocal().getAnimation() != -1)
            Task.sleep(50);

        Task.sleep(250, 1000);

        if(Calculations.distanceTo(destinations[i]) >= 5)
            teleport(i);

    }

    public void closeXpPopUp(){
        WidgetChild ab = getWidget(640, 30);

        while(ab != null && ab.isOnScreen())
            ab.click(true);
    }

	/* Drawing */

    double timeTo99 = -1,dayleft, hourLeft, minLeft,secLeft;
    double day,hour,min,sec;
    @Override
    public void onRepaint(Graphics g3d){
        Graphics2D g = (Graphics2D)g3d;


        if(this.chat == null)
            return;

        this.runTime = System.currentTimeMillis() - startTime;
        int expGained = Skills.getExperience(skill) - startExp;
        String ran = startTime != 0 ? Time.format(runTime) : "00:00:00";
        double expHr = getPerHr(expGained);
        int mineHr = getPerHr(mined);

        if(Game.getClientState() == 11){
            if( curState == Status.MINING)
                this.drawClosestGranite(g, MineOres.getClosestOre(MineOres.ROCK_SPOTS));

            if(isShowing){

                g.scale(0.5D, 0.5D);
                g.drawImage(chat, 12, 659, null);
                g.scale(2D, 2D);

                g.setFont(new Font("Myriad Pro", Font.BOLD, 16));
                g.setColor(new Color(125, 0, 255 - 185));
                g.drawString("by Posix " + (update ? "(New Update!)" : "(v" + version + ")"), 220, 394 + 35);
                g.setColor(Color.WHITE);

                g.setFont(new Font("Verdana", Font.TRUETYPE_FONT, 14));

				/*Left-TAB*/
                if(!isSplitting)
                    g.drawString("Status: " + curState , lChatXSpacing, chatBase + (chatYSpacing * 0));
                else
                    g.drawString("Status: Splitting granite", lChatXSpacing, chatBase + (chatYSpacing * 0));
                g.drawString("Time running: " + ran, lChatXSpacing, chatBase + (chatYSpacing * 1));
                g.drawString((isSplitting ? "Granite split: " : "Ores Mined: ") + this.mined, lChatXSpacing, chatBase + (chatYSpacing * 2));
                if(expHr>0){
                    timeTo99 = Skills.getExperienceToLevel(Skills.MINING,99);

                    timeTo99 = timeTo99/(expHr);
                    day = Math.floor( timeTo99/24);
                    hourLeft =  (timeTo99 - (day*24));
                    hour = Math.floor(hourLeft);
                    minLeft =  (hourLeft - hour)*60;
                    min = Math.floor(minLeft);
                    secLeft = (minLeft - min)*60;
                    sec = Math.floor(secLeft);
                }
                g.drawString("Time till 99: " + (int)day+":"+(int)hour+":"+(int)min+":"+(int)sec, lChatXSpacing, chatBase + (chatYSpacing * 3));



				/*Right-TAB*/
                g.drawString("Mining Level: " + Skills.getLevel(skill) + " (+" + (Skills.getLevel(skill) - startLv) + ")", rChatXSpacing, chatBase + (chatYSpacing * 0));
                g.drawString("XP to level: " + (Skills.getExperienceRequired(Skills.getRealLevel(skill) + 1) - Skills.getExperience(skill)), rChatXSpacing, chatBase + (chatYSpacing * 1));
                g.drawString("XP gained: " + expGained, rChatXSpacing, chatBase + (chatYSpacing * 2));
                g.drawString("XP/Hr: " + format(expHr/1000) + "k", rChatXSpacing, chatBase + (chatYSpacing * 3));

            }

            if(Tabs.INVENTORY.isOpen())
                drawSkins(g);
            this.makeButton(g, (isShowing ? "Hide" : "Show"), 1, 440, 395, 55, 25, new Color(255, 255, 255, 0));
            drawMouse(g);
            drawMouseTracer(g);

        }

    }

    public void makeButton(Graphics2D g, String txt, int id, int x, int y, int w, int h, Color c){
        Color c1 = g.getColor();

        g.setColor(c);
        g.fillRect(x, y, w, h);

        g.setFont(new Font("Verdana", Font.BOLD, 14));
        g.setColor(new Color(0, 0, 0));
        g.drawString(txt, x + 10, y + 15);

        g.setColor(c1);

        if(!buttons.contains(id + ":" + x + ":" + y + ":" + w + ":" + h))
            buttons.add(id + ":" + x + ":" + y + ":" + w + ":" + h);
    }

    public void drawClosestGranite(Graphics g, SceneObject obj){
        g.setColor(new Color(0, 255, 0, 60));
        if(obj != null){
            try{
                g.drawPolygon(obj.getLocation().getBounds()[0]);
                g.fillPolygon(obj.getLocation().getBounds()[0]);

            } catch(Exception e){}
        }
    }

    public void drawMouse(final Graphics g) {
        final Dimension game = Game.getDimensions();
        final Point loc = Mouse.getLocation();
        final long mpt = System.currentTimeMillis() - Mouse.getPressTime();

        if (Mouse.getPressTime() == -1 || mpt >= 500) {
            g.setColor(mouseColor);
            g.drawLine(0, loc.y, game.width, loc.y);
            g.drawLine(loc.x, 0, loc.x, game.height);
            g.setColor(mainColor);
            g.drawLine(0, loc.y + 1, game.width, loc.y + 1);
            g.drawLine(0, loc.y - 1, game.width, loc.y - 1);
            g.drawLine(loc.x + 1, 0, loc.x + 1, game.height);
            g.drawLine(loc.x - 1, 0, loc.x - 1, game.height);
        }
        if (mpt < 500) {
            g.setColor(clickColor);
            g.drawLine(0, loc.y, game.width, loc.y);
            g.drawLine(loc.x, 0, loc.x, game.height);
            g.setColor(mainColor);
            g.drawLine(0, loc.y + 1, game.width, loc.y + 1);
            g.drawLine(0, loc.y - 1, game.width, loc.y - 1);
            g.drawLine(loc.x + 1, 0, loc.x + 1, game.height);
            g.drawLine(loc.x - 1, 0, loc.x - 1, game.height);
        }

    }

    public void drawMouseTracer(Graphics g1){
        Graphics2D g = (Graphics2D) g1;
        int rCap = Random.nextInt(5, 20);
        int lCap = rCap;
        int hCap = rCap;
        int lenght = 50;

        red = red == 0 ? Random.nextInt(lCap, 255) : Random.nextInt(red - lCap, red + hCap);
        green = green == 0 ? Random.nextInt(lCap, 255) : Random.nextInt(green - lCap, green + hCap);
        blue = blue == 0 ? Random.nextInt(lCap, 255) : Random.nextInt(blue - lCap, blue + hCap);

        red = red < lCap ? lCap : red > 255 - hCap ? 255 - hCap : red;
        green = green < lCap ? lCap : green > 255 - hCap ? 255 - hCap : green;
        blue = blue < lCap ? lCap : blue > 255 - hCap ? 255 - hCap : blue;

        g.setColor(new Color(red, green, blue, 255));

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        Point p = Mouse.getLocation();

        if(points == null){

            points = new Point[lenght];
            points[0] = p;

        } else {
            if(points[pointIndex == 0 ? 0 : pointIndex - 1] != null && points[pointIndex == 0 ? 0 : pointIndex - 1].distance(p) >= 0){
                points[pointIndex++] = p;
                pointIndex %= lenght;
            }
        }

        for(int i = pointIndex; i != (pointIndex == 0 ? lenght - 1 : pointIndex - 1); i = (i + 1) % lenght)
            if(points[i] != null && points[(i + 1) % lenght] != null)
                g.drawLine(points[i].x, points[i].y, points[(i + 1) % lenght].x, points[(i+1) % lenght].y);

        g.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

    }

    public void drawSkins(Graphics2D g){
        int skin0 = 1831;
        int skin1 = 1829;
        int skin2 = 1827;
        int skin3 = 1825;
        int skin4 = 1823;

        Color c = g.getColor();

        for(Item i : Inventory.getItems()){
            if(i != null){
                int x = i.getWidgetChild().getAbsoluteX() + (i.getWidgetChild().getWidth() / 2) - 8;
                int y = i.getWidgetChild().getAbsoluteY() + (i.getWidgetChild().getHeight() / 2) + 6;

                if(i.getId() == skin0){
                    g.setColor(new Color(255, 0, 0));
                    g.drawString("0", x, y);
                }

                if(i.getId() == skin1){
                    g.setColor(new Color(255, 255/2, 0));
                    g.drawString("1", x, y);
                }

                if(i.getId() == skin2){
                    g.setColor(new Color(255, 255, 0));
                    g.drawString("2", x, y);
                }

                if(i.getId() == skin3){
                    g.setColor(new Color(255/2, 255, 0));
                    g.drawString("3", x, y);
                }

                if(i.getId() == skin4){
                    g.setColor(new Color(0, 255, 0));
                    g.drawString("4", x, y);
                }
            }
        }
        g.setColor(c);
    }

    @Override
    public void messageReceived(MessageEvent e)
    {
        String msg = e.getMessage();
        if(msg.contains("You manage to quarry some"))
            mined++;
    }

    @Override
    public void mousePressed(MouseEvent e)
    {
        for(String s : this.buttons){
            String[] split = s.split(":");

            int id = Integer.parseInt(split[0]);
            int x = Integer.parseInt(split[1]);
            int y = Integer.parseInt(split[2]);
            int w = Integer.parseInt(split[3]);
            int h = Integer.parseInt(split[4]);

            if(e.getX() >= x && e.getY() >= y && e.getX() <= x + w && e.getY() <= y + h){
                if(id == 1)
                    this.isShowing = !this.isShowing;

                e.consume();
            }
        }
    }




    public void mouseClicked(MouseEvent e){}
    public void mouseEntered(MouseEvent e){}
    public void mouseReleased(MouseEvent e){}
    public void mouseExited(MouseEvent e){}

}
