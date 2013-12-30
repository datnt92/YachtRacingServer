/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.netgame.lobby.model;

/**
 *
 * @author PhongTn
 * @since Nov 21, 2012 8:30:25 AM
 * @version 1.0
 */
public class Horse {

    private float speed_ = 0;
    private int ID_;
    private float track_ = 0;

    private Horse() {
    }

    public Horse(int id) {
        this.ID_ = id;
    }

    public Horse(int id, float speed) {
        this.ID_ = id;
        this.speed_ = speed;
    }

    public int getId() {
        return ID_;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("Horse: ").append(ID_).append(" Speed: ").append(speed_);
        return str.toString();
    }

    public void updateSpeed() {
        this.speed_ += 1;
    }

    public void setSpeed(float newSpeed) {
        //this.speed_ = Math.max(speed_, newSpeed);
        this.speed_ = newSpeed;
    }

    public void updateTrack() {
        track_ += speed_;
    }

    public void setTrack(float track) {
        track_ = track;
    }

    public float getSpeed_() {
        return speed_;
    }

    public float getTrack_() {
        return track_;
    }

 
}
