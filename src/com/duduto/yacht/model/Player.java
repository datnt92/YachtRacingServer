package com.duduto.yacht.model;

import com.duduto.util.StringUtil;
import com.electrotank.electroserver5.extensions.api.value.EsObject;
import com.netgame.lobby.enums.Field;
import java.util.*;

/**
 *
 * @author PhongTn
 * @since Oct 22, 2012 4:30:43 PM
 * @version 1.0
 */
public class Player {

    private String device_token;
    private int _countSms;
    private Map<Integer, Integer> _mapYacht;
    private int _playerId;
    private String _userName;
    private String _passWord;
    private int _money;
    private String _email;
    private boolean isDemo;
    private int _status;
    private String _timeRegister;
    private String uid;

    public Player() {
    }

    public void updateMoney(int money) {
        _money = _money + money;
        if (_money < 0) {
            _money = 0;
        }
    }

    public void putYachtMap(int idYacht, int money) {
        if (_mapYacht == null) {
            _mapYacht = new HashMap<Integer, Integer>();
        }
        _mapYacht.put(idYacht, money);
        _countSms++;
    }

    public Map<Integer, Integer> getMapYacht() {
        return _mapYacht;
    }

    public int getCountChoiceByYachtId(int yachtId) {
        if (_mapYacht.containsKey(yachtId)) {
            return _mapYacht.get(yachtId);
        } else {
            return 0;
        }
    }

    public void setMapYacht(Map<Integer, Integer> _mapYacht) {
        this._mapYacht = _mapYacht;
    }

    public int getCountSms() {
        return _countSms;
    }

    public EsObject toEsObject() {
        int[] arryacht = new int[11];
        EsObject es = new EsObject();
        es.setString(Field.UserName.getName(), StringUtil.nvl(_userName));
        es.setString(Field.Email.getName(), StringUtil.nvl(_email));
        es.setBoolean(Field.IsDemo.getName(), isDemo);
        es.setInteger(Field.Money.getName(), _money);
        if(!isDemo){
        es.setString(Field.DeviceToken.getName(), device_token);
        }
        if (_mapYacht != null) {
            for (Map.Entry<Integer, Integer> entry : _mapYacht.entrySet()) {
                Integer yachtId = entry.getKey();
                Integer money = entry.getValue();
                arryacht[yachtId] = money;
            }
        }
        es.setIntegerArray("arrYachtCount", arryacht);
        es.setIntegerArray(Field.arrRate.getName(), GamePlayer.getInstance().getArrRate());
        return es;
    }

//    public EsObject toEsObject() {
//        EsObject es = new EsObject();
//        es.setString(Field.PhoneNumberId.getName(), phoneNumber);
//        if (mapHorse != null) {
//
//            es.setInteger(Field.NumHorseSelect.getName(), mapHorse.size());
//            EsObject[] arrHorseSelect = null;
//            arrHorseSelect = new EsObject[mapHorse.size()];
//            if (mapHorse.size() > 0) {
//                int i = 0;
//                for (Map.Entry<Integer, Integer> mapEntry : mapHorse.entrySet()) {
//                    Integer horseId = mapEntry.getKey();
//                    Integer count = mapEntry.getValue();
//                    EsObject tmp = new EsObject();
//                    tmp.setInteger(Field.HorseId.getName(), horseId);
//                    tmp.setInteger(Field.HorseCount.getName(), count);
//                    arrHorseSelect[i] = tmp;
//                    i++;
//                }
//                es.setEsObjectArray(Field.ArrayHorseSelect.getName(), arrHorseSelect);
//            }
//
//        } else {
//            es.setInteger(Field.NumHorseSelect.getName(), 0);
//        }
//        return es;
//    }
    ////////////////////////////////
    public int getPlayerId() {
        return _playerId;
    }

    public void setPlayerId(int _playerId) {
        this._playerId = _playerId;
    }

    public String getUserName() {
        return _userName;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setUserName(String _userName) {
        this._userName = _userName;
    }

    public String getPassWord() {
        return _passWord;
    }

    public void setPassWord(String _passWord) {
        this._passWord = _passWord;
    }

    public int getMoney() {
        return _money;
    }

    public void setMoney(int _money) {
        this._money = _money;
    }

    public String getEmail() {
        return _email;
    }

    public void setEmail(String _email) {
        this._email = _email;
    }

    public boolean isDemo() {
        return isDemo;
    }

    public void setIsDemo(boolean isDemo) {
        this.isDemo = isDemo;
    }

    public int getStatus() {
        return _status;
    }

    public void setStatus(int _status) {
        this._status = _status;
    }

    public String getTimeRegister() {
        return _timeRegister;
    }

    public void setTimeRegister(String _timeRegister) {
        this._timeRegister = _timeRegister;
    }

    public String getDevice_token() {
        return device_token;
    }

    public void setDevice_token(String device_token) {
        this.device_token = device_token;
    }
}
