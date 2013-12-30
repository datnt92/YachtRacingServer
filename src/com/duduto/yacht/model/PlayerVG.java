/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.duduto.yacht.model;

/**
 *
 * @author Dark
 */
public class PlayerVG {

   private String email;
   private String access_token;
   private int amount;
   private String order_id;
   private int gameid;

    public int getGameid() {
        return gameid;
    }

    public void setGameid(int gameid) {
        this.gameid = gameid;
    }
    public String getEmail() {
        return email;
    }

    public PlayerVG(String email, String access_token, int amount, String order_id,int gameid) {
        this.email = email;
        this.access_token = access_token;
        this.amount = amount;
        this.order_id = order_id;
        this.gameid=gameid;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAccess_token() {
        return access_token;
    }

    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getOrder_id() {
        return order_id;
    }

    public void setOrder_id(String order_id) {
        this.order_id = order_id;
    }
}
