/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.duduto.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Dark
 */
public class RequestVGUtil {

    private HttpURLConnection getCon(String url, String request) {
        HttpURLConnection con = null;
        try {
            URL obj = new URL(url);
            con = (HttpURLConnection) obj.openConnection();
            con.setDoOutput(true);
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            con.setRequestProperty("Content-length", String.valueOf(request.length()));
            DataOutputStream out = new DataOutputStream(con.getOutputStream());
            out.flush();
            out.writeBytes(request);
            out.close();
        } catch (MalformedURLException ex) {
            Logger.getLogger(RequestVGUtil.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ProtocolException ex) {
            Logger.getLogger(RequestVGUtil.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(RequestVGUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
        return con;
    }

 public static int getPaymentXuCode(String email, int amount,String trans,int money) {
        int code = 1004;
        RequestVGUtil rqvg = new RequestVGUtil();
        try {
            String url = " http://bloggame.vn/mobile/ctransaction";
            String urlParameters = "bloggame_coin="+URLEncoder.encode(String.valueOf(amount), "UTF-8")
                    +"&email=" + URLEncoder.encode(email, "UTF-8")
                    +"&gameid="+URLEncoder.encode("6", "UTF-8")
                    +"&money="+URLEncoder.encode(String.valueOf(money), "UTF-8")
                    +"&transaction="+URLEncoder.encode(trans, "UTF-8");
            System.out.println(""+urlParameters);
            //read response code
            HttpURLConnection con = rqvg.getCon(url, urlParameters);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
                 JsonElement jelement = new JsonParser().parse(in.readLine());
            JsonObject jobject = jelement.getAsJsonObject();
            code = jobject.getAsJsonPrimitive("ecode").getAsInt();
            in.close();
            con.disconnect();
            return code;
        } catch (IOException ex) {
            Logger.getLogger(RequestVGUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
        return code;
    }

    public static int getCodeValidateEmail(String email, String token) {
        int code = 1004;
        RequestVGUtil rqvg = new RequestVGUtil();
        try {
            String url = "http://bloggame.vn/mobile/tkcheck";
            String urlParameters = "email=" + URLEncoder.encode(email, "UTF-8") + "&utoken=" + URLEncoder.encode(token, "UTF-8");
            //read response code
            HttpURLConnection con = rqvg.getCon(url, urlParameters);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            JsonElement jelement = new JsonParser().parse(in.readLine());
            in.close();
            JsonObject jobject = jelement.getAsJsonObject();
            code = jobject.getAsJsonPrimitive("ecode").getAsInt();
            return code;
        } catch (IOException ex) {
            Logger.getLogger(RequestVGUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
        return code;
    }
}
