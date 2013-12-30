package com.netgame.database;

import com.duduto.Global;
import com.duduto.yacht.model.Player;
import com.duduto.util.DateUtil;
import com.duduto.yacht.model.GamePlayer;
import com.electrotank.electroserver5.extensions.api.value.EsObject;
import com.netgame.lobby.enums.Field;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.dbcp.BasicDataSourceFactory;
import org.skife.jdbi.v2.*;
import org.skife.jdbi.v2.exceptions.ResultSetException;
import org.skife.jdbi.v2.tweak.HandleCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main controller for all application logic
 *
 */
public class DatabaseController {

    //<editor-fold defaultstate="collapsed" desc="init">
    private static final Logger logger = LoggerFactory.getLogger(DatabaseController.class);
    private final BasicDataSource dataSource;
    private final DBI dbi;

    //public static boolean usingWebService;
    public DatabaseController(Properties properties) throws Exception {
        this.dataSource = newDataSource(properties);
        this.dbi = new DBI(dataSource);
        logger.debug("DatabaseController init");
    }
    public static int percentMoney = 10;

    /**
     * OTHER POSSIBLY USEFUL METHODS
     */
    /**
     * Executes any SQL command. WARNING!!!!!!!! Only use canned SQL here,
     * because there is no binding, so this is wide open to SQL injection
     * attacks if a user provides any part of it other than integers!
     */
    public boolean executeSQL(final String sqlCommand) {
        try {
            getDbi().withHandle(new HandleCallback<Object>() {
                @Override
                public Object withHandle(Handle handle)
                        throws Exception {
                    handle.createStatement(sqlCommand).execute();
                    return null;
                }
            });
            return true;
        } catch (Exception exception) {
            //logger.error(exception.getStackTrace().toString());
            logger.error("Error attempting to execute SQL: {} ", sqlCommand);
            logger.error(exception.getMessage());
            return false;
        }
    }

    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="register">
    public boolean playerRegister(Player player) {
        Handle handle = getDbi().open();
        try {
            Update insertNewPlayer = handle.createStatement("sql/WriteNewPlayer.sql").
                    bind("username", player.getUserName())
                    .bind("email", player.getEmail()).
                    bind("flag", booleanToInt(player.isDemo())).
                    bind("status", player.getStatus());
            insertNewPlayer.execute();
//            setMoney(player.getUserName(), 0, Global.DEFAULT_MONEY_NEW_USER, Global.SERVER_NAME, "Register new user");
            logger.info("Insert new player: " + player.getUserName() + " success...");
        } catch (Exception e) {
            logger.warn("error register" + e.getMessage(), e);
            return false;
        } finally {
            if (handle != null) {
                handle.close();
            }
        }
        return true;
    }

    public void updateDeviceToken(String username, String token) {
        Handle handle = getDbi().open();
        try {
            handle.createStatement("sql/UpdateDeviceToken.sql")
                    .bind("username", username)
                    .bind("device_token", token).execute();
        } catch (Exception e) {
            logger.warn("error update token " + e.getMessage());
        } finally {
            handle.close();
        }
    }

    public boolean updateUsername(String email, String username) {
        Handle handle = getDbi().open();
        try {
            handle.createStatement("sql/UpdateUsername.sql").
                    bind("username", username).
                    bind("email", email).execute();
            setMoney(username, 0, Global.DEFAULT_MONEY_NEW_USER, Global.SERVER_NAME, "Register new user");
            logger.info("Update: " + username + " success...");
        } catch (Exception e) {
            logger.warn("error update username" + e.getMessage(), e);
            return false;
        } finally {
            if (handle != null) {
                handle.close();
            }
        }
        return true;
    }

    public boolean checkUsernameEmpty(String email) {
        Handle handle = getDbi().open();
        try {
            Map<String, Object> map = handle.createQuery("sql/CheckUsernameEmpty.sql")
                    .bind("email", email).first();
            int count = Integer.valueOf(map.get("count").toString());
            if (count == 0) {
                return true;
            }
        } catch (Exception e) {
            logger.error("error check exist email " + e.getMessage(), e);
        } finally {
            handle.close();
        }
        return false;
    }

    public boolean playerDemo(Player player) {
        Handle handle = getDbi().open();
        try {
            Update insertNewPlayer = handle.createStatement("sql/WritePlayerDemo.sql")
                    .bind("uid", player.getUid()).
                    bind("username", player.getUserName()).
                    bind("status", player.getStatus())
                    .bind("email", player.getEmail())
                    .bind("device_token", "")
                    .bind("flag", booleanToInt(player.isDemo()));
            insertNewPlayer.execute();
            setMoney(player.getUserName(), 0, Global.DEFAULT_MONEY_DEMO_USER, Global.SERVER_NAME, "Register new demo user");
            logger.info("Insert demo player: " + player.getUserName() + " success...");
        } catch (Exception e) {
            logger.warn("error write demo " + e.getMessage(), e);
            return false;
        } finally {
            if (handle != null) {
                handle.close();
            }
        }
        return true;
    }

    public boolean updateEmail(String username, String newEmail) {
        Handle handle = getDbi().open();
        try {
            handle.createStatement("sql/UpdateEmail.sql").bind("username", username)
                    .bind("newEmail", newEmail).execute();
            logger.info("Update: " + newEmail + " success...");
            return true;
        } catch (Exception e) {
            logger.warn("error update email " + e.getMessage(), e);
            return false;
        } finally {
            handle.close();
        }
    }

    public long getCloneAccount(String uid) {
        Handle handle = getDbi().open();
        try {
            Map<String, Object> map = handle.createQuery("sql/GetAccountClone.sql").bind("uid", uid).first();
            long total = (Long) map.get("total");
            return total;
        } catch (Exception e) {
            logger.warn("error clone account" + e.getMessage(), e);
            return -1;
        }
    }
//</editor-fold>

    public int getMoneyOfPlayer(String username) {
        Handle handle = getDbi().open();;
        int pMoney = -1;
        try {
            Map<String, Object> map = handle.createQuery("sql/GetMoneyOfPlayer.sql")
                    .bind("username", username).first();
            pMoney = Integer.valueOf(map.get("sumMoney").toString());
        } catch (Exception e) {
            logger.warn("error get money of player " + e.getMessage());
        } finally {
            handle.close();
        }
        return pMoney;
    }

    public boolean checkExistEmail(String email) {
        Handle handle = getDbi().open();
        try {
            Map<String, Object> map = handle.createQuery("sql/CheckExistEmail.sql")
                    .bind("email", email).first();
            logger.debug("count =" + map.get("count").toString());
            int count = Integer.valueOf(map.get("count").toString());

            if (count == 0 || map.isEmpty()) {
                return false;
            }
        } catch (Exception e) {
            logger.error("error check exist email " + e.getMessage(), e);
        } finally {
            handle.close();
        }
        return true;
    }

    public Player getPlayerData(String username) {
        Player player = null;
        Handle handle = getDbi().open();
        try {
            Map<String, Object> data = handle.createQuery("sql/GetUserData.sql").bind("username", username).first();
            if (data != null) {
                player = new Player();
                player.setEmail(data.get("email").toString());
                player.setIsDemo(intToBoolean((Integer) data.get("flag")));
                player.setMoney(getMoneyOfPlayer(username));
                player.setUserName(username);

                if (data.get("Uid") != null) {
                    player.setUid(data.get("Uid").toString());
                }
                if (!player.isDemo()) {

                    if (data.get("device_token") != null) {
                        logger.debug("device_token = " + data.get("device_token").toString());
                        player.setDevice_token(data.get("device_token").toString());
                    } else {
                        player.setDevice_token("");
                    }
                }
                List<Map<String, Object>> lstDbPlayerBetting = handle.createQuery("sql/GetPlayerBetting.sql").
                        bind("game_id", GamePlayer.getInstance().getGameID()).
                        bind("username", username).list();
                if (!lstDbPlayerBetting.isEmpty()) {
                    for (Map<String, Object> map : lstDbPlayerBetting) {
                        int choice = (Integer) map.get("bet_choice");
                        int money = (Integer) map.get("money_betting");
                        player.putYachtMap(choice, money);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("error player data " + e.getMessage(), e);
        } finally {
            handle.close();
        }
        return player;
    }

    public Player getPlayerByUserName(String username) {
        Player player = null;
        Handle handle = getDbi().open();
        try {
            Map<String, Object> data = handle.createQuery("sql/CheckExistUserName.sql").bind("username", username).first();
            if (data != null) {
                player = new Player();
                player.setUid(String.valueOf(data.get("uid")));
                player.setUserName(username);
                player.setEmail(String.valueOf(data.get("email")));
                player.setIsDemo(intToBoolean((Integer) data.get("flag")));
                player.setMoney(getMoneyOfPlayer(username));
            }
        } catch (Exception e) {
            logger.error("error get player by username" + e.getMessage(), e);
        } finally {
            handle.close();
        }
        return player;
    }

    public void setNewGame(String game_name, String time_start,
            String time_end, int win_count, int status) {
        Handle handle = null;
        try {
            handle = getDbi().open();
            Update iNewGame = handle.createStatement("sql/SetNewGame.sql").bind("game_name", game_name).bind("time_start", time_start).bind("time_end", time_end).bind("win_count", win_count).bind("status", status);
            iNewGame.execute();
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        } finally {
            if (handle != null) {
                handle.close();
            }
        }
    }

    /**
     * close game is waiting state
     */
//    public void closeGameWaiting() {
//        Handle handle = null;
//        try {
//            handle = getDbi().open();
//            Map<String, Object> gameResult = this.getGameWaiting(handle);
//            int gameId = (Integer) gameResult.get("game_id");
//            this.updateGameState(handle, gameId, 1);
//        } catch (Exception ex) {
//            logger.error(ex.getMessage(), ex);
//        } finally {
//            if (handle != null) {
//                handle.close();
//            }
//        }
//    }
    /**
     * update game to started state
     */
    public int prepareStartGame() {
        Handle handle = null;
        int gameId = -1;
        try {
            handle = getDbi().open();
            Map<String, Object> gameResult = handle.inTransaction(new TransactionCallback<Map<String, Object>>() {
                @Override
                public Map<String, Object> inTransaction(Handle handle, TransactionStatus ts) throws Exception {
                    return getGameWaiting(handle);
                }
            });
            gameId = (Integer) gameResult.get("game_id");
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        } finally {
            if (handle != null) {
                handle.close();
            }
        }
        return gameId;
    }

    public int updateEndGame(int gameId, int wincount, int win2nd, int win3rd, int winYacht, int second, int third) {
        Handle handle = null;
        int u = -1;
        try {
            handle = getDbi().open();
            u = handle.createStatement("sql/UpdateWinCount.sql").bind("win_count", wincount)
                    .bind("win2nd_count", win2nd)
                    .bind("win3rd_count", win3rd)
                    .bind("win_horse", winYacht)
                    .bind("second_horse", second)
                    .bind("third_horse", third)
                    .bind("game_id", gameId).execute();
            return u;
        } catch (Exception ex) {
            logger.warn(ex.getMessage(), ex);
        } finally {
            if (handle != null) {
                handle.close();
            }
        }
        return u;
    }

    public List<Player> getListPlayerPlay(int gameId) {
        List<Player> lstPlayerBetting = new ArrayList<Player>();
        Handle handle = null;
        try {
            handle = getDbi().open();
            List<Map<String, Object>> lstDbPlayerBetting = handle.createQuery("sql/GetPlayerListBetting.sql").
                    bind("game_id", gameId).list();
            if (!lstDbPlayerBetting.isEmpty()) {
                // logger.info("COUNT BETTING GAME " + gameId + " : " + lstDbPlayerBetting.size());
                for (int i = 0; i < lstDbPlayerBetting.size(); i++) {
                    boolean isPlayerExist = false;
                    Map<String, Object> tmp = lstDbPlayerBetting.get(i);
                    String username = tmp.get("username").toString();
                    int yachtId = (Integer) tmp.get("bet_choice");
                    int money = (Integer) tmp.get("money_betting");
                    for (int j = 0; j < lstPlayerBetting.size(); j++) {
                        if (username.equals(lstPlayerBetting.get(j).getUserName())) {
                            Player playerExist = lstPlayerBetting.get(j);
                            playerExist.putYachtMap(yachtId, money);
                            isPlayerExist = true;
                        }
                    }
                    //add new player to list player
                    if (!isPlayerExist) {
                        Player p = new Player();
                        p.setUserName(username);
                        p.putYachtMap(yachtId, money);
                        lstPlayerBetting.add(p);
                    }
                }
                logger.warn("GAME SESSION SIZE: " + gameId + " " + lstDbPlayerBetting.size());
            } else {
                logger.warn("GAME SESSION EMPTY: " + gameId);
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        } finally {
            if (handle != null) {
                handle.close();
            }
        }
        return lstPlayerBetting;
    }

    public long getTimeRemain() {
        Map<String, Object> gameResult = null;
        long timeCloseGame = 0;
        Handle handle = null;
        try {
            handle = getDbi().open();
            gameResult = getGameWaiting(handle);
            if (gameResult != null) {
                String strTimeClose = gameResult.get("time_end").toString();
                timeCloseGame = DateUtil.stringToDate(strTimeClose, "yyyy-MM-dd HH:mm:ss").getTime();
            }
        } catch (ResultSetException e) {
            logger.error("getTimeRemain()", e);
        } finally {
            if (handle != null) {
                handle.close();
            }
        }
        return timeCloseGame;
    }

    private Map<String, Object> getGameWaiting(Handle handle) {
        List<Map<String, Object>> gameResult = null;
        try {
            gameResult = handle.createQuery("sql/LoadGameWaiting.sql").list();
            if (gameResult != null && gameResult.size() > 0) {
                return gameResult.get(0);
            } else {
                throw new NullPointerException();
            }
        } catch (ResultSetException e) {
            logger.error("Exception loading game waiting...");
            return null;
        }
    }

    public int updateGameState(int gameId, int status) {
        Handle handle = getDbi().open();
        try {
            Update u = handle.createStatement("sql/UpdateGamePlay.sql").bind("status", status).bind("game_id", gameId);
            return u.execute();
        } catch (Exception e) {
            return -1;
        } finally {
            if (handle != null) {
                handle.close();
            }

        }
    }

    public Boolean writeYachtChoice(final String userName,
            final int gameId, final int choice, final int money, final int rate, final boolean isDemo) {
        try {
            return getDbi().inTransaction(new TransactionCallback<Boolean>() {
                @Override
                public Boolean inTransaction(Handle handle, TransactionStatus status) throws Exception {
                    return writeYachtChoice(handle, userName, gameId, choice, money, rate, isDemo);
                }
            });
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
            return false;
        }
    }

    private boolean writeYachtChoice(Handle handle, String username, int gameId, int choice, int money, int rate, boolean isDemo) {
        try {
            handle.createStatement("sql/WritePlayerChoice.sql").
                    bind("username", username)
                    .bind("game_id", gameId).bind("bet_choice", choice)
                    .bind("money_betting", money).bind("rate", rate).
                    bind("isDemo", booleanToInt(isDemo)).
                    bind("money_result", 0 - money)
                    .execute();
        } catch (Exception e) {
            logger.error("error choice " + e.getMessage(), e);
            return false;
        }
        return true;
    }

    public boolean setMoney(final String username, final long old_money, final long money,
            final String src, final String description) {
        getDbi().inTransaction(new TransactionCallback<Boolean>() {
            @Override
            public Boolean inTransaction(Handle handle, TransactionStatus ts) throws Exception {
                return changeMoney(handle, username, old_money, money, src, description);
            }
        });
        return true;
    }

    private boolean changeMoney(Handle handle, String username, long old_money, long money, String src, String description) {
        try {
            handle.createStatement("sql/WriteLogMoney.sql").
                    bind("username", username).
                    bind("old_money", old_money).
                    bind("money", money).
                    bind("src", src).
                    bind("description", description).execute();

        } catch (Exception e) {
            logger.debug("error change money " + e.getMessage(), e);
            return false;
        }
        return true;
    }

    public List<Integer> getTopBettingDESC(int gameid) {
        Handle handle = getDbi().open();
        List<Integer> lst = new LinkedList<Integer>();
        try {
            List<Map<String, Object>> listBestYach = handle.createQuery("sql/GetBestYach.sql")
                    .bind("game_id", gameid).list();

            for (Map<String, Object> mapYach : listBestYach) {
                lst.add((Integer) mapYach.get("bet_choice"));
            }
        } catch (Exception e) {
            logger.warn("error get best yach " + e.getMessage());
        } finally {
            handle.close();
        }
        return lst;
    }

    public List<EsObject> getListBetting(String username) {
        Handle handle = null;
        List<Map<String, Object>> lstBetting = null;
        List<EsObject> lstEs = null;
        try {
            handle = getDbi().open();
            lstBetting = handle.createQuery("sql/GetLogBetting.sql").bind("username", username).list();
            lstEs = new ArrayList<EsObject>();
            if (!lstBetting.isEmpty()) {
                for (Map<String, Object> map : lstBetting) {
                    EsObject es = new EsObject();
                    es.setInteger(Field.Choice.getName(), (Integer) map.get("bet_choice"));
                    es.setInteger(Field.chargeBetting.getName(), (Integer) map.get("money_betting"));
                    es.setString(Field.TimeBetting.getName(), map.get("log_time").toString());
                    es.setInteger(Field.Money.getName(), (Integer) map.get("money_result"));
                    lstEs.add(es);
                }
            }
        } catch (Exception e) {
            logger.error("error get log betting " + e.getMessage());
        } finally {
            handle.close();
        }
        return lstEs;
    }

    public void updateMoneyResult(int gameid, String username, int yachID, int money) {
        Handle handle = null;
        try {
            handle = getDbi().open();
            handle.createStatement("sql/UpdateMoneyResult.sql").bind("username", username).bind("game_id", gameid)
                    .bind("bet_choice", yachID).bind("money_result", money).execute();
        } catch (Exception e) {
            logger.error("Error update money result " + e.getMessage());
        } finally {
            handle.close();
        }
    }

    public List<Map<String, Object>> getListWinner(int gameid, int betChoice) {
        Handle handle = null;
        List<Map<String, Object>> lstWin = null;
        try {
            handle = getDbi().open();
            lstWin = handle.createQuery("sql/GetListWin")
                    .bind("game_id", gameid)
                    .bind("bet_choice", betChoice).list();
        } catch (Exception e) {
            logger.warn("error get list winner " + e.getMessage());
        } finally {
            handle.close();
        }
        return lstWin;
    }

    public int getMoneyBettingOfPlayer(int gameid, String username, int choice) {
        Handle handle = null;
        int money = 0;
        try {
            handle = getDbi().open();
            Map<String, Object> map = handle.createQuery("sql/GetMoneyBettingOfPlayer.sql")
                    .bind("game_id", gameid)
                    .bind("bet_choice", choice)
                    .bind("username", username).first();
            money = Integer.valueOf(map.get("money").toString());
        } catch (Exception e) {
            logger.warn("error get money betting of player " + e.getMessage());
        } finally {
            handle.close();
        }
        return money;
    }

    public void writeLogTradeXu(String username, int code, int amount, String trans) {
        Handle handle = getDbi().open();
        try {
            handle.createStatement("sql/WriteLogTradeXu.sql")
                    .bind("username", username).bind("trade_code", code).bind("amount", amount)
                    .bind("transaction", trans)
                    .execute();

        } catch (Exception e) {
            logger.error("Error write log trade xu " + e.getMessage());
        } finally {
            handle.close();
        }
    }

    public boolean checkExistTransaction(String username, String transaction) {
        Handle handle = getDbi().open();
        try {
            Map<String, Object> map = handle.createQuery("sql/CheckExistTransaction.sql")
                    .bind("username", username)
                    .bind("transaction", transaction)
                    .first();

            if (!map.isEmpty() && Integer.valueOf(map.get("count").toString()) == 0) {

                return false;
            }
        } catch (Exception e) {
            logger.error("Error write log trade xu " + e.getMessage());
        } finally {
            handle.close();
        }
        return true;
    }

    public void writeLogLogin(String username, int state) {
        Handle handle = null;
        try {
            handle = getDbi().open();
            handle.createStatement("sql/WriteLogLogin.sql").bind("username", username).bind("login_state", state).execute();
        } catch (Exception e) {
            logger.error("Error write log login " + e.getMessage());
        } finally {
            handle.close();
        }
    }

    public void writeLogWin(int gameid, String username, int win_1st, int win_2st, int win_3rd, int money) {
        Handle handle = null;
        try {
            handle = getDbi().open();
            handle.createStatement("sql/WriteLogWin.sql")
                    .bind("username", username)
                    .bind("win_1st", win_1st)
                    .bind("win_2nd", win_2st)
                    .bind("win_3rd", win_3rd)
                    .bind("win_money", money)
                    .bind("game_id", gameid).execute();
        } catch (Exception e) {
            logger.error("Error write log win " + e.getMessage());
        } finally {
            handle.close();
        }
    }

    public List<Map<String, Object>> getMoneyBonus(int gameid) {
        Handle handle = null;
        List<Map<String, Object>> lst = null;
        try {
            handle = getDbi().open();
            lst = handle.createQuery("sql/GetBonusMoney.sql")
                    .bind("game_id", gameid)
                    .list();
        } catch (Exception e) {
            logger.warn("error get money betting of player " + e.getMessage());
        } finally {
            handle.close();
        }
        return lst;
    }

    public Map<String, Object> getItemByID(int itemId) {
        Handle handle = null;
        Map<String, Object> map = null;
        try {
            handle = getDbi().open();
            map = handle.createQuery("sql/GetItemById.sql")
                    .bind("item_id", itemId)
                    .first();
        } catch (Exception e) {
            logger.warn("error get item  " + e.getMessage());
        } finally {
            handle.close();
        }
        return map;
    }

    public void writeLogTradeGift(String username, String item_name, int price) {
        Handle handle = null;
        try {
            handle = getDbi().open();
            handle.createStatement("sql/WriteLogTradeGift.sql")
                    .bind("username", username)
                    .bind("item_name", item_name)
                    .bind("price", price)
                    .execute();
        } catch (Exception e) {
            logger.error("Error write log trade gift " + e.getMessage());
        } finally {
            handle.close();
        }
    }

    public List<EsObject> getLogTradeGift(String username) {
        Handle handle = null;
        List<Map<String, Object>> lstGift = null;
        List<EsObject> lstEs = null;
        try {
            handle = getDbi().open();
            lstGift = handle.createQuery("sql/GetLogTradeGift.sql").bind("username", username).list();
            lstEs = new ArrayList<EsObject>();
            if (!lstGift.isEmpty()) {
                for (Map<String, Object> map : lstGift) {
                    EsObject es = new EsObject();
                    es.setString(Field.ItemName.getName(), map.get("item_name").toString());
                    es.setInteger(Field.Status.getName(), Integer.valueOf(map.get("status").toString()));
                    es.setString(Field.TimeTrade.getName(), map.get("log_time").toString());
                    es.setString(Field.Info.getName(), map.get("info").toString());
                    es.setInteger(Field.ItemPrice.getName(), Integer.valueOf(map.get("price").toString()));
                    lstEs.add(es);
                }
            }
        } catch (Exception e) {
            logger.error("error get shop " + e.getMessage());
        } finally {
            handle.close();
        }
        return lstEs;
    }

    public List<EsObject> getLogTradeXu(String username) {
        Handle handle = null;
        List<Map<String, Object>> lstLogXu = null;
        List<EsObject> lstEs = null;
        try {
            handle = getDbi().open();
            lstLogXu = handle.createQuery("sql/GetLogTradeXu.sql").bind("username", username).list();
            lstEs = new ArrayList<EsObject>();
            if (!lstLogXu.isEmpty()) {
                for (Map<String, Object> map : lstLogXu) {
                    EsObject es = new EsObject();
                    es.setString(Field.TimeTrade.getName(), map.get("time_trade").toString());
                    es.setString(Field.Xu.getName(), map.get("amount").toString());
                    lstEs.add(es);
                }
            }
        } catch (Exception e) {
            logger.error("error get shop " + e.getMessage());
        } finally {
            handle.close();
        }
        return lstEs;
    }

    //<editor-fold defaultstate="collapsed" desc="don't remove">
    public void writeLogServerStart() {
        try {
            getDbi().inTransaction(new TransactionCallback<Object>() {
                @Override
                public Object inTransaction(Handle handle, TransactionStatus status) throws Exception {
                    writeServerStartDb(handle);
                    return null;
                }
            });
        } catch (Throwable t) {
            logger.error("writeLogServerStart error: ");
            logger.error(t.getMessage());
        }
    }

    private void writeServerStartDb(Handle handle) {
        handle.createStatement("sql/WriteLogServerStart.sql").execute();
    }

    private BasicDataSource newDataSource(Properties properties) throws Exception {
        Properties databaseProperties = new Properties();
        databaseProperties.setProperty("charset", "utf8");
        databaseProperties.setProperty("characterEncoding", "utf8");
        for (String key : properties.stringPropertyNames()) {
            if (key.startsWith("database.")) {
                databaseProperties.setProperty(key.substring(9), properties.getProperty(key));
            }
        }

        return (BasicDataSource) BasicDataSourceFactory.createDataSource(databaseProperties);
    }
//</editor-fold>

    /**
     * OTHER PUBLIC METHODS
     *
     */
    //<editor-fold defaultstate="collapsed" desc="dispose">
    public void dispose() throws Exception {
        logger.warn("Controller.dispose invoked");
        logger.warn("Now attempting to close the dataSource");
        dataSource.getConnection().close();
        dataSource.close();
    }

    public DBI getDbi() {
        return dbi;
    }

    public boolean intToBoolean(int flag) {
        if (flag == 0) {
            return true;
        }
        return false;
    }

    public int booleanToInt(boolean isDemo) {
        if (isDemo) {
            return 0;
        }
        return 1;
    }
    //</editor-fold>
}
