/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.netgame.lobby.enums;

/**
 *
 * @author PhongTn
 */
public enum Constant {
    isDemo(0),
    MaxHorseRace(10);
    private final int num;

    private Constant(int n) {
        this.num = n;
    }

    public int getNumber() {
        return num;
    }
}
