package controller;

/**
 *
 * @author wira gotama this is a singleton model class
 */
import java.util.List;
import java.util.ArrayList;
import java.io.*;
import java.util.*;
import java.util.Collections;
import exception.*;
import model.*;
import towerdefense.gameUI;

public class Controller {

    private List<Tower> listOfTower = Collections.synchronizedList(new ArrayList());
    private List<Monster> listOfMonster = new ArrayList<Monster>();
    private static final int ROW = 15, COL = 20, INITIAL_LIFE = 10;
    private static Controller instance;
    private final int maximumLevel;
    private final int goldRate = 5;
   
    private model.Player player;
    private int currentLevel;
    private int score;
    private int gold;
    private int lives;
    private final int start_x = 2, start_y = 0;
    private final int monsterCount = 10;
    private model.Map map;

    private Controller() {
        maximumLevel = 2; //buat testing dulu
    }

    public int getCurrentLevel() {
        return currentLevel;
    }
    
    public int getMaxLevel() {
        return maximumLevel;
    }
        
    public int getScore() {
        return score;
    }
    
    public int getLives() {
        return lives;
    }
    
    public void showMonsterLive() {
        for (int i=0; i<listOfMonster.size(); i++)
            System.out.println("Monster ke-"+i+" "+listOfMonster.get(i).getHP());
    }   
    
    
    public void newGame(Player x) throws FileNotFoundException, IOException  {
        map.readFile();
        player = x;
        currentLevel = 1;
        score = 0;
        gold = 10;
        lives = INITIAL_LIFE;
    }

    public void loadGame(Player x) throws FileNotFoundException, IOException {
        player = x;
        map.readFile();
        String filename = player.getName() + ".txt";
        Scanner in = new Scanner (new File(filename));
        readFromFile(in);
    }

    public void spawnMonster() {
        for (int i=0; i<monsterCount; i++)
            listOfMonster.add(new Monster(start_x, start_y-i, currentLevel));
    }
    
    /**
     * create new tower at position (pos_x, pos_y) if have enough money. return
     * true if the tower could be created
     *
     * @param pos_x
     * @param pos_y
     * @return
     */
    public boolean createNewTower(int pos_x, int pos_y) {
        /* ?Precondition, there should not exist any tower at position (pos_x, pos_y) */
        int idx = getTowerIdx(pos_x, pos_y);
        assert (idx == -1) : "There's already tower in there";
        if (idx == -1 && gold >= Tower.towerCost) {
            Tower temp = new Tower(pos_x, pos_y);
            listOfTower.add(temp);
            gold -= Tower.towerCost;
            return true;
        } else {
            return false;
        }
    }

    public void moveAllMonster() {
        for (int it = 0; it < listOfMonster.size(); ++it) {
            /*
             int x_new = ambil dari bit map sesuai dengan posisinya sekarang
             int y_new = ambil dari bit map sesuai dengan posisinya sekarang
             */
            Monster M = listOfMonster.get(it);
            if (M.getHP()>0) {
                int x_new = M.getX();
                int y_new = M.getY();
                if (x_new>0 && x_new < map.row && y_new>0 && y_new < map.col) {
                    int arah = map.Peta[x_new][y_new] & 0xF;
                    switch (arah) {
                        case 1: {
                            y_new += 1;
                            break;
                        }
                        case 2: {
                            x_new -= 1;
                            break;
                        }
                        case 4: {
                            y_new -= 1;
                            break;
                        }
                        case 8: {
                            x_new += 1;
                            break;
                        }
                        default:
                            assert (false);
                    }  
                }
                else {
                    y_new += 1;
                }
                M.changePos(x_new, y_new);
                if (y_new==20) { //masuk ke rumah kita
                    decreaseLive();
                            
                }
            }  
        }
    }
    
    public int countSeenMonster() {
        int count = 0;
        for (int i=0; i<listOfMonster.size(); i++) {
            if ((listOfMonster.get(i).getY()<map.col && listOfMonster.get(i).getY()>=0)
                && (listOfMonster.get(i).getX()<map.row && listOfMonster.get(i).getX()>=0))
                if (listOfMonster.get(i).getHP() > 0 ) {
                    ++count;
                }
        }
        return count;
    }

    public void decreaseLive() {
        --lives;
    }

    public void nextLevel() {
        listOfMonster = new ArrayList<Monster>();
        ++currentLevel;
        gold = gold + (goldRate * currentLevel);
    }

    /**
     * for testing purpose
     */
    public void getTower() {
        if (listOfTower.size() > 0) {
            System.out.println("ada");
        }
    }

    /**
     * sell (destroy) the tower at (pos_x, pos_y) to get pay back money
     *
     * @param pos_x
     * @param pos_y
     * @return
     */
    public void sellTower(int pos_x, int pos_y) {
        /* ?Precondition, there should exist a tower at position (pos_x, pos_y) */
        int idx = getTowerIdx(pos_x, pos_y);
        int payBack = 0;
        assert (idx != -1) : "Tower not found";
        if (idx != -1) {
            payBack = listOfTower.get(idx).sellTower();
            listOfTower.remove(idx);
        }
        gold = gold + payBack;
    }

    /**
     * upgrade tower at position (pos_x, pos_y) if player's money is sufficient,
     *
     * @param pos_x
     * @param pos_y
     */
    public void upgradeTower(int pos_x, int pos_y) {
        /* ?Precondition, there should exist a tower at position (pos_x, pos_y) */
        int idx = getTowerIdx(pos_x, pos_y);
        assert (idx != -1) : "Tower not found";
        gold = gold - listOfTower.get(idx).getUpgradeCost();
        listOfTower.get(idx).upgradeTower();
    }

    /**
     * Enemies are being attacked by tower(s)
     */
    public void allTowersAttack() {
        System.out.println("Attacking!");
        for (int i = 0; i < listOfTower.size(); i++) {
            System.out.println("Tower ke-" +i + " Cooldown : "+ listOfTower.get(i).getCoolDownCount());
            for (int j = 0; j < listOfMonster.size(); ++j) {
                /* 
                 if (there's ')enemy in tower's sight, then attack those enemy
                 */
                //System.out.println(listOfTower.get(i).rangeCheck(2, 2,map.row,map.col));
                if (listOfMonster.get(j).getHP() > 0 && listOfMonster.get(j).getY() >=0 
                        && listOfTower.get(i).rangeCheck(listOfMonster.get(j).getX(), listOfMonster.get(j).getY(),map.row,map.col)) {
                    listOfTower.get(i).resetCoolingDownTime();
                    listOfMonster.get(j).decreaseHitPoints(listOfTower.get(i).getAttack());
                    if (listOfMonster.get(j).getHP() <= 0) {
                        ++score;
                    }
                    break;
                }
            }
        }
    }

    /**
     * CoolDown tower after attacking
     */
    public void coolDownAllTower() {
        for (int i = 0; i < listOfTower.size(); i++) {
            listOfTower.get(i).coolingDown();
        }
    }

    /**
     * get this class instance
     *
     * @return
     */
    public static Controller getInstance() {
        if (instance == null) {
            instance = new Controller();
        }
        return instance;
    }

    /**
     * Save ListOfTower to file
     *
     * @param out
     */
    public void saveToFile() throws IOException {
        File file = new File(player.getName() + ".txt");  
        FileWriter writer = new FileWriter(file);
        PrintWriter out = new PrintWriter(writer);
        out.println(score);
        out.println(gold);
        out.println(currentLevel);
        out.println(lives);
        out.println(listOfTower.size());
        for (int i = 0; i < listOfTower.size(); i++) {
            writeTowerToFile(i, out);
        }
        out.println(listOfMonster.size());
        for (int i = 0; i < listOfMonster.size(); i++) {
            writeMonsterToFile(i, out);
        }
        out.flush();
    }

    /**
     * read from file, then create new tower
     *
     * @param in
     */
    public void readFromFile(Scanner in) {
        score   = in.nextInt();
        gold    = in.nextInt();
        currentLevel    = in.nextInt();
        lives   = in.nextInt();
        int total = in.nextInt();
        for (int i = 0; i < total; i++) {
            int x = in.nextInt();
            int y = in.nextInt();
            // Ini ditambahkan di terakhir saja

            Tower temp = new Tower(x, y);
            temp.setCurrentLevel(in.nextInt());
            temp.setUpgradeCost(in.nextInt());
            temp.setAttack(in.nextInt());
            temp.setRange(in.nextInt());
            temp.setCoolDown(in.nextInt());
            listOfTower.add(temp);
        }
        total = in.nextInt();
        for (int i = 0; i < total; i++) {
            int HP = in.nextInt();
            int x = in.nextInt();
            int y = in.nextInt();
            // Ini ditambahkan di terakhir saja

            Monster temp = new Monster(x, y, currentLevel);
            temp.setHP(HP);
            listOfMonster.add(temp);
        }
    }

    /**
     * Get tower (pos_x, pos_y) index in the list
     *
     * @param pos_x
     * @param pos_y
     * @return
     */
    public int getTowerIdx(int pos_x, int pos_y) {
        int simpan = -1;
        for (int i = 0; i < listOfTower.size(); i++) {
            if (listOfTower.get(i).getPositionX() == pos_x && listOfTower.get(i).getPositionY() == pos_y) {
                simpan = i;
                break;
            }
        }
        return simpan;
    }

    /**
     * write LisOfTower[idx] to file
     */
    private void writeTowerToFile(int idx, PrintWriter out) {
        out.println(listOfTower.get(idx).getPositionX() + " "
                + listOfTower.get(idx).getPositionY() + " "
                + listOfTower.get(idx).getUpgradeCost() + " "
                + listOfTower.get(idx).getAttack() + " "
                + listOfTower.get(idx).getRange() + " "
                + listOfTower.get(idx).getCoolDownCount() + " "
                + listOfTower.get(idx).getCurrentLevel() + " ");
    }
    
    private void writeMonsterToFile(int idx, PrintWriter out) {
        out.println(listOfMonster.get(idx).getHP() + " "
                + listOfMonster.get(idx).getX() + " "
                + listOfMonster.get(idx).getY());
    }

    public void showGame(boolean ingame) {
        if (ingame)
            gameUI.showTransition(map, player, score, currentLevel, gold, lives,  listOfTower, listOfMonster);
        else
            gameUI.showMap(map, player, score, ROW, gold, listOfTower, listOfMonster);
    }
}
