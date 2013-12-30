/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.duduto.yacht.model;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 *
 * @author PhongTn
 * @since Nov 22, 2012 12:07:05 PM
 * @version 1.0
 */
public class BettingYacht {

    private LinkedList<Player> lstPlayerPlay;
    private int number;
    private int _yachtId;

    public BettingYacht(int yacht) {
        this._yachtId = yacht;
        this.lstPlayerPlay = new LinkedList<Player>();
        this.number = 0;
    }

    public int getCountSelect() {
        return number;
    }

    public void addPlayerPlay(Player player) {
        lstPlayerPlay.add(player);
        number++;
    }

    public void removePlayerPlay(Player player) {
        lstPlayerPlay.remove(player);
        number--;
    }

    public LinkedList<Player> getListPlayerPlay() {
        return lstPlayerPlay;
    }

}
