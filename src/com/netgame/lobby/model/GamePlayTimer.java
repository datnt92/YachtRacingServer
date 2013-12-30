/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.netgame.lobby.model;

import com.duduto.util.Log;
import com.electrotank.electroserver5.extensions.api.EventApi;
import com.electrotank.electroserver5.extensions.api.PluginApi;
import com.netgame.lobby.enums.Constant;
import java.util.*;

/**
 * when game start
 *
 * @author PhongTn
 * @since Nov 21, 2012 8:20:14 AM
 * @version 1.0
 */
public class GamePlayTimer implements Runnable {

    private PluginApi api_;
    private List<Horse> lstHorses_;
    private final long TIME_RACING = 20000; //2s
    private final long BLOCK = 1000;
    private int nguaDich = 0;

    public GamePlayTimer(PluginApi api, int nguavedich) {
        this.api_ = api;
        this.nguaDich = nguavedich;
    }

    public GamePlayTimer() {
    }

    private void makeHorseForRance() {
        lstHorses_ = new ArrayList<Horse>(Constant.MaxHorseRace.getNumber());
        for (int i = 0; i <= Constant.MaxHorseRace.getNumber(); i++) {
            Horse horse = new Horse(i, 1);
            lstHorses_.add(horse);
        }
    }

    @Override
    public void run() {
        try {
            this.makeHorseForRance();
            Timer timer = new Timer("Race Track");
            TimerTask racing = new RaceTrack(lstHorses_, api_, nguaDich);
            timer.schedule(racing, 0, BLOCK);
            Thread.sleep(20000L);
            timer.cancel();
            timer.purge();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class RaceTrack extends TimerTask {

    private List<Horse> lstHorse_;
    final long MAX_TARDINESS = 20000;
    static int count = 0;
    int HORSEWIN = 4;
    static int currentFirst = 0;
    static float MAX_TRACK = 0;
    private PluginApi api_;

    public RaceTrack(List<Horse> lst, PluginApi api, int horseWin) {
        this.lstHorse_ = lst;
        this.api_ = api;
        this.HORSEWIN = horseWin;
    }

    @Override
    public void run() {
        int horseId = getFirstHorse();

        if (count == 17) {
            lstHorse_.get(HORSEWIN).setTrack(lstHorse_.get(currentFirst).getTrack_());
            lstHorse_.get(HORSEWIN).setSpeed(lstHorse_.get(currentFirst).getSpeed_());
        }
        for (int i = 0; i < Constant.MaxHorseRace.getNumber(); i++) {
            if (count == 17) {
                horseId = HORSEWIN;
            }
            if (i == horseId) {
                lstHorse_.get(i).updateSpeed();
            }
            lstHorse_.get(i).updateTrack();
            if (MAX_TRACK < lstHorse_.get(i).getTrack_()) {
                MAX_TRACK = lstHorse_.get(i).getTrack_();
                currentFirst = i;
            }
        }
        showRacing();
        count++;
        if (count == 20) {
            cancel();
        }
    }

    private void showRacing() {
        for (int i = 0; i < Constant.MaxHorseRace.getNumber(); i++) {
            api_.getLogger().debug("Yacht: " + i + " Speed: " + lstHorse_.get(i).getSpeed_() + " Track: " + lstHorse_.get(i).getTrack_());
        }
        api_.getLogger().debug("=============================================================");
    }

    public int getFirstHorse() {
        int rnd = new Random().nextInt(Constant.MaxHorseRace.getNumber());
        return rnd;
    }
}
