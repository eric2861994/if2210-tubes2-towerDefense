/**
 *
 * @author wira gotama
 * this is a singleton model class
 */
import java.util.List;
import java.util.ArrayList;
import java.lang.AssertionError;
import java.io.*;
import java.util.*;
import java.util.Collections;

public class TowerList {
    private  List<Tower> ListOfTower = Collections.synchronizedList(new ArrayList());
    private  int maximumTower; //should be final
    private static TowerList instance;
    
    private TowerList() {}
    
    /** create new tower at position (pos_x, pos_y) if have enough money. return true if the tower could be created */
    public boolean createNewTower(int pos_x, int pos_y, int money) {
    /* ?Precondition, there should not exist any tower at position (pos_x, pos_y) */
        int idx = getTowerIdx(pos_x, pos_y);
        assert(idx==-1) : "There's already tower in there";
        if (idx==-1 && money>=1) {
            Tower temp = new Tower(pos_x,pos_y);
            ListOfTower.add(temp);
            return true;
        }
        else return false;
    }
    
    /** for testing purpose */
    public void getTower() {
        if (ListOfTower.size() > 0 ) {
            System.out.println("ada");
        }
    }
    
    /** sell (destroy) the tower at (pos_x, pos_y) to get pay back money */
    public int sellTower(int pos_x, int pos_y) {
    /* ?Precondition, there should exist a tower at position (pos_x, pos_y) */
        int idx = getTowerIdx(pos_x, pos_y);
        int payBack=0;
        assert(idx!=-1) : "Tower not found";
        if (idx!=-1) {
            payBack = ListOfTower.get(idx).sellTower();
            ListOfTower.remove(idx);
        }
        return payBack;
    }
    
    /** upgrade tower at position (pos_x, pos_y) if player's money is sufficient, return remaining money */
    public int upgradeTower(int pos_x, int pos_y, int money) {
    /* ?Precondition, there should exist a tower at position (pos_x, pos_y) */
        int idx = getTowerIdx(pos_x,pos_y);
        assert(idx!=-1) : "Tower not found";
        if (idx!=-1 && money >= ListOfTower.get(idx).getUpgradeCost()) {
            int retval = money - ListOfTower.get(idx).getUpgradeCost();
            ListOfTower.get(idx).upgradeTower();
            return retval;
        }
        else return 0;
    }
    
    /** Enemies are being attacked by tower(s) */
    public void Attack() {
        //this requires map and MonsterListInstance
        int m = 5; //m adalah jumlah instans monster
        for (int i=0; i<ListOfTower.size(); i++) {
            boolean Attacking = false;
            for (int j=0; j<m && !Attacking; j++) {
                /*if (ListOfTower.elementAt(i).rangeCheck(pos_x musuh, pos_y musuh)) {
                    Musuh kena tembakan...
                    ListOfTower.elementAt(i).resetCoolingDownTime();
                }
                */
            }
            /* if (there's ')enemy in tower's sight, then attack those enemy
            */
        }
    }
    
    /** CoolDown tower after attacking */
    public void CoolDownAllTower() {
         for (int i=0; i<ListOfTower.size(); i++)
             ListOfTower.get(i).coolingDown();
    }
    
    /** get this class instance
     * @return  */
    public static TowerList getInstance() {
        if (instance == null)
            instance = new TowerList();
        return instance;
    }
    
    /** Save ListOfTower to file */
    public void saveToFile(PrintWriter out) {
        out.println(ListOfTower.size());
        for (int i=0; i<ListOfTower.size(); i++) {
           writeToFile(i,out); 
        }
        out.flush();
    }
    
    /** read from file, then create new tower
     * @param in */
    public void readFromFile(Scanner in) {
        int total = in.nextInt();
        for (int i=0; i<total; i++) {
            int x = in.nextInt();
            int y = in.nextInt();
            Tower temp = new Tower(x,y);
            ListOfTower.add(temp);
            readAttributes(i,in);
        }
    }
    
    /** Get tower (pos_x, pos_y) index in the list */
    private int getTowerIdx(int pos_x, int pos_y) {
        boolean stop = false;
        int simpan=-1;
        for (int i=0; i<ListOfTower.size(); i++) {
            if (ListOfTower.get(i).getPositionX()==pos_x && ListOfTower.get(i).getPositionY()==pos_y) {
                stop = true;
                simpan = i;
            }
        }
        return simpan;
    }
    
    /** write LisOfTower[idx] to file */
    private void writeToFile(int idx, PrintWriter out) {
        out.println(ListOfTower.get(idx).getPositionX() + " " +
                    ListOfTower.get(idx).getPositionY() + " " +
                    ListOfTower.get(idx).getUpgradeCost()+ " " +
                    ListOfTower.get(idx).getAttack()+ " " +
                    ListOfTower.get(idx).getRange()+ " " +
                    ListOfTower.get(idx).getCoolDown()+ " " +
                    ListOfTower.get(idx).getCurrentLevel()+ " ");
    }
    
    private void readAttributes(int idx, Scanner in) {
        int cost = in.nextInt(); ListOfTower.get(idx).setUpgradeCost(cost);
        int att = in.nextInt(); ListOfTower.get(idx).setAttack(att);
        int r = in.nextInt(); ListOfTower.get(idx).setRange(r);
        int CD = in.nextInt(); ListOfTower.get(idx).setCoolDown(CD);
        int level = in.nextInt(); ListOfTower.get(idx).setCurrentLevel(level);
    }
}
