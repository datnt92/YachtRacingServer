package com.netgame.lobby.model;

import com.duduto.Global;
import com.duduto.util.MessagingHelper;
import com.duduto.util.RandomUtil;
import com.electrotank.electroserver5.extensions.api.PluginApi;
import com.electrotank.electroserver5.extensions.api.value.EsObject;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author PhongTN
 */
public class TrackRace {

    private List<Horse> lstHorse_;
    int firstHorse;
    int secondHorse;
    int thirdHorse;
    private PluginApi api_;

    public TrackRace(PluginApi api, int fHorse, int sHorse, int tHorse) {
        api_ = api;
        firstHorse = fHorse - 1;
        secondHorse = sHorse - 1;
        thirdHorse = tHorse - 1;
        makeHorseForRance();
    }

    private void makeHorseForRance() {
        lstHorse_ = new ArrayList<Horse>(10);
        for (int i = 0; i < 10; i++) {
            Horse horse = new Horse(i + 1, 1.0F);
            lstHorse_.add(horse);
        }
    }

    public void startGame() {
        EsObject gameResult = new EsObject();
        for (int i = 0; i < lstHorse_.size(); i++) {
            Horse h = lstHorse_.get(i);
            if (h.equals(lstHorse_.get(firstHorse))) {
                gameResult.setFloatArray("horseId:" + String.valueOf(h.getId()),
                        this.toArraySpeed(6));
            } else if (h.equals(lstHorse_.get(secondHorse))) {
                gameResult.setFloatArray("horseId:" + String.valueOf(h.getId()),
                        this.toArraySpeed(5.65f));
            } else if (h.equals(lstHorse_.get(thirdHorse))) {
                gameResult.setFloatArray("horseId:" + String.valueOf(h.getId()),
                        this.toArraySpeed(5.3f));
            } else {
                float rd = RandomUtil.getNumMaxRandom(4.5f, 5.0f);
                gameResult.setFloatArray("horseId:" + String.valueOf(h.getId()),
                        this.toArraySpeed(rd));
            }
        }
        MessagingHelper.sendMessageTrackRace(gameResult, lstHorse_.size(), api_);
    }

    private float[] toArraySpeed(float avg) {
        return RandomUtil.genSpeedBlock(Global.SPEED_BLOCK, avg);
    }
}
