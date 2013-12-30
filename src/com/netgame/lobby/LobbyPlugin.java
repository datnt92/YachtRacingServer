package com.netgame.lobby;

import com.duduto.Global;
import com.duduto.yacht.model.GamePlayer;
import com.duduto.util.MessagingHelper;
import com.duduto.util.RequestVGUtil;
import com.duduto.yacht.enums.BlogGameCode;
import com.duduto.yacht.enums.ErrorCode;
import com.duduto.yacht.model.Player;
import com.electrotank.electroserver5.client.ElectroServer;
import com.electrotank.electroserver5.extensions.BasePlugin;
import com.electrotank.electroserver5.extensions.ChainAction;
import com.electrotank.electroserver5.extensions.api.value.EsObject;
import com.electrotank.electroserver5.extensions.api.value.EsObjectRO;
import com.electrotank.electroserver5.extensions.api.value.UserPublicMessageContext;
import com.netgame.database.DatabaseController;
import com.netgame.lobby.controllers.PublicMessageController;
import com.netgame.lobby.controllers.RequestController;
import com.netgame.lobby.enums.Field;
import com.netgame.lobby.enums.Message;
import com.netgame.lobby.model.GameControlTimer;
import com.netgame.lobby.model.LobbyModel;
import com.netgame.lobby.processors.request.GetPlayerList;
import com.netgame.lobby.processors.request.GetRoomList;
import it.sauronsoftware.cron4j.Scheduler;
import it.sauronsoftware.cron4j.SchedulingPattern;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LobbyPlugin extends BasePlugin {

    private LobbyModel model;
    private PublicMessageController publicMessageController;
    private RequestController requestController;
    public DatabaseController dbController;
    private Scheduler schGamePlayTimer_;
    ElectroServer els;
    private static final Logger logger = LoggerFactory.getLogger(LobbyPlugin.class);

    //<editor-fold defaultstate="collapsed" desc="init">
    @Override
    public void init(EsObjectRO parameters) {
        els = new ElectroServer();
        this.model = new LobbyModel(getApi());
        this.publicMessageController = new PublicMessageController(model);
        this.requestController = new RequestController(model);
        this.dbController = (DatabaseController) getApi().acquireManagedObject("DatabaseControllerFactory", null);
        dbController.writeLogServerStart();
        //init game player - reference PluginApi to GamePlayer
        //this game just only require 1 GamePlayer
        int firstGameId = dbController.prepareStartGame();
        if (firstGameId > 0) {
            logger.info("First game id " + firstGameId);
            GamePlayer.getInstance().initGamePlayer(parameters, getApi(), firstGameId);
        } else {
            logger.warn("Game id not exist .Cant start game play");
        }
        this.initRequestProcessors();
        this.scheduleAutoGame();
        logger.info("LobbyPlugin initialized...");
    }

    private void scheduleAutoGame() {
        SchedulingPattern pattern = new SchedulingPattern(Global.DURATION_PLAY_GAME);
        GameControlTimer gameControlTimer = new GameControlTimer();
        schGamePlayTimer_ = new Scheduler();
        schGamePlayTimer_.schedule(pattern, gameControlTimer);
        schGamePlayTimer_.start();
        logger.info("Timer started...");
    }

    /**
     * resister process on server
     */
    private void initRequestProcessors() {
        this.requestController.register(new GetPlayerList());
        this.requestController.register(new GetRoomList());
    }

    @Override
    public void userDidEnter(String userName) {
        getApi().getLogger().info("User " + userName + " is logged");
    }
//</editor-fold>

    @Override
    public void request(String username, EsObjectRO requestParameters) {
        if (requestParameters.getString(Field.Command.getName()).equals(Field.TimeStartGame.getName())) {
            this.timeStartGame(username);
        } else if (requestParameters.getString(Field.Command.getName()).equals(Field.UserData.getName())) {
            this.getUserData(username);
        } else if (requestParameters.getString(Field.Command.getName()).equals(Field.Betting.getName())) {
            this.betting(username, requestParameters);
        } else if (requestParameters.getString(Field.Command.getName()).equals(Field.TradeGift.getName())) {
            this.tradeGift(username, requestParameters);
        } else if (requestParameters.getString(Field.Command.getName()).equals(Field.TradeKhoai.getName())) {
            this.tradeXu(username, requestParameters);
        } else if (requestParameters.getString(Field.Command.getName()).equals(Field.UpdateEmail.getName())) {
            this.rqUpdateVGID(username, requestParameters);
        } else if (requestParameters.getString(Field.Command.getName()).equals(Field.GetLogBetting.getName())) {
            this.getLogBetting(username);
        } else if (requestParameters.getString(Field.Command.getName()).equals(Field.GetLogTradeGift.getName())) {
            this.getLogTradeGift(username);
        } else if (requestParameters.getString(Field.Command.getName()).equals(Field.GetLogTradeXu.getName())) {
            this.getLogTradeXu(username);
        } else if (requestParameters.getString(Field.Command.getName()).equals(Field.UpdateDeviceToken.getName())) {
            this.updateToken(username,requestParameters);
        }
    }

    @Override
    public ChainAction userSendPublicMessage(UserPublicMessageContext message) {
        if (message.getEsObject().variableExists(Field.Command.getName())) {
            String commandName = message.getEsObject().getString(Field.Command.getName());
            return publicMessageController.processCommand(commandName, message.getUserName(), message.getEsObject());
        } else {
            return ChainAction.OkAndContinue;
        }
    }

    private void updateToken(String username, EsObjectRO request) {
        if (!request.variableExists(Field.DeviceToken.getName())) {
            logger.warn("no parameter");
        } else {
            EsObject esObject = new EsObject();
            esObject.setString(Field.Command.getName(), Field.UpdateDeviceToken.getName());
            String token = request.getString(Field.DeviceToken.getName());
            dbController.updateDeviceToken(username, token);
            esObject.setInteger(Field.ErrorCode.getName(), ErrorCode.IsSuccess.getCode());
            esObject.setString(Field.Message.getName(), Message.DeviceToken.getMessage());
            Player p = dbController.getPlayerData(username);
            esObject.setEsObject(Field.UserData.getName(), p.toEsObject());
            MessagingHelper.sendMessageToPlayer(username, esObject, getApi());
        }
    }

    private void getLogBetting(String username) {
        EsObject esObject = new EsObject();
        esObject.setString(Field.Command.getName(), Field.GetLogBetting.getName());
        List<EsObject> lstBetting = dbController.getListBetting(username);
        if (lstBetting != null) {
            esObject.setEsObjectArray(Field.ListBetting.getName(), lstBetting.toArray(new EsObject[0]));
        } else {
            esObject.setInteger(Field.ErrorCode.getName(), ErrorCode.LogBettingEmpty.getCode());
            esObject.setString(Field.Message.getName(), Message.LogBettingEmpty.getMessage());
        }
        MessagingHelper.sendMessageToPlayer(username, esObject, getApi());
    }

    private void getUserData(String username) {
        Player player = null;
        player = dbController.getPlayerData(username);
        EsObject es = player.toEsObject();
        es.setString(Field.Command.getName(), Field.UserData.getName());
        MessagingHelper.sendMessageToPlayer(username, es, getApi());
    }

    //<editor-fold defaultstate="collapsed" desc="rq update">
    private void rqUpdateVGID(String username, EsObjectRO request) {
        EsObject esObject = new EsObject();
        esObject.setString(Field.Command.getName(), Field.UpdateEmail.getName());
        if (!request.variableExists(Field.Email.getName())) {
            getApi().getLogger().info("Message 'Update email' isn't enought prameter !!!");
            esObject.setString(Field.Message.getName(), Message.EmailEmpty.getMessage());
        } else {
            try {
                String newEmail = request.getString(Field.Email.getName()).trim();
                if (dbController.checkExistEmail(newEmail) == false && dbController.updateEmail(username, newEmail)) {
                    Player p = dbController.getPlayerData(username);
                    dbController.setMoney(username, p.getMoney(), -p.getMoney(), Global.SERVER_NAME, "Update Blog Game Account");
                    p.setMoney(0);
                    esObject.setInteger(Field.ErrorCode.getName(), ErrorCode.IsSuccess.getCode());
                    esObject.setString(Field.Message.getName(), Message.UpdateSuccess.getMessage());
                    esObject.setEsObject(Field.UserData.getName(), p.toEsObject());
                } else {
                    esObject.setInteger(Field.ErrorCode.getName(), ErrorCode.EmailExist.getCode());
                    esObject.setString(Field.Message.getName(), Message.EmailExist.getMessage());
                }
            } catch (Exception e) {
                esObject.setInteger(Field.ErrorCode.getName(), ErrorCode.SystemError.getCode());
                esObject.setString(Field.Message.getName(), Message.SystemError.getMessage());
                getApi().getLogger().warn(e.getMessage());
            }
        }
        MessagingHelper.sendMessageToPlayer(username, esObject, getApi());
    }
//</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="betting">
    private void betting(String username, EsObjectRO requestParameters) {
        Player player = dbController.getPlayerData(username);
        int arr[] = GamePlayer.getInstance().getArrRate();
        int choice = requestParameters.getInteger("choice");
        int state;
        int chargeBetting = requestParameters.getInteger(Field.chargeBetting.getName());
        //get rate of yacht_id
        int rate = arr[choice - 1];
        if (player.getMoney() < chargeBetting) {
            state = -2;
        } else {
            dbController.writeYachtChoice(username,
                    GamePlayer.getInstance().getGameID(), choice, chargeBetting, rate, player.isDemo());
            dbController.setMoney(username, player.getMoney(), -chargeBetting,
                    Global.SERVER_NAME, "Betting " + choice + " GameID: " + GamePlayer.getInstance().getGameID());
            player.updateMoney(-chargeBetting);
            state = 1;
        }
        EsObject es = new EsObject();
        es.setString(Field.Command.getName(), Field.Betting.getName());
        es.setInteger(Field.Choice.getName(), choice);
        es.setInteger(Field.OkChoice.getName(), state);
        es.setInteger(Field.Money.getName(), player.getMoney());
        MessagingHelper.sendMessageToPlayer(username, es, getApi());
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="tradexu">
    private void getLogTradeXu(String username) {
        EsObject es = new EsObject();
        es.setString(Field.Command.getName(), Field.GetLogTradeXu.getName());
        List<EsObject> lst = dbController.getLogTradeXu(username);
        if (lst == null || lst.isEmpty()) {
            es.setInteger(Field.ErrorCode.getName(), ErrorCode.LogEmpty.getCode());
            es.setString(Field.Message.getName(), Message.LogXuEmpty.getMessage());
        } else {
            es.setEsObjectArray(Field.LogTradeGift.getName(), lst.toArray(new EsObject[0]));
        }
        MessagingHelper.sendMessageToPlayer(username, es, getApi());
    }

    private void tradeXu(String username, EsObjectRO request) {
        EsObject es = new EsObject();

        es.setString(Field.Command.getName(), Field.TradeKhoai.getName());
        if (!request.variableExists(Field.Money.getName())
                || !request.variableExists(Field.Transaction.getName()) || !request.variableExists(Field.BlogGameCoin.getName())) {//send error
            getApi().getLogger().info("Message 'Trade khoai' isn't enought prameter !!!");
        } else {
            int code = BlogGameCode.Unknown.getCode();
            //   int khoai = request.getInteger(Field.BlogGameCoin.getName());
            int xu = request.getInteger(Field.Money.getName());
            int khoai = xu / 10;
            String transaction = request.getString(Field.Transaction.getName());
            try {
                Player p = dbController.getPlayerData(username);
                if (!dbController.checkExistTransaction(username, transaction)) {
                    //get code trade from vatgia_id
                    String email = p.getEmail();
                    getApi().getLogger().debug("money=" + request.getString(Field.Money.getName()) + "coin = "
                            + request.getString(Field.BlogGameCoin.getName())
                            + "tras=" + request.getString(Field.Transaction.getName()) + "email=" + email);
                    code = RequestVGUtil.getPaymentXuCode(email, khoai, transaction, xu);
                    if (code == BlogGameCode.TradeSuccess.getCode()) {
                        dbController.setMoney(username, p.getMoney(),
                                xu, Global.SERVER_NAME, "Trade " + khoai + " Khoai to " + xu + " xu");
                        es.setInteger(Field.ErrorCode.getName(), BlogGameCode.TradeSuccess.getCode());
                        es.setString(Field.TradeXu.getName(), "Giao dịch thành công.Tài khoản được cộng thêm " + xu + " xu");
                        p.setMoney(p.getMoney() + xu);
                        es.setEsObject(Field.UserData.getName(), p.toEsObject());
                    }
                } else {
                    //transaction đã tồn tại trong db.
                    code = -1;
                    es.setInteger(Field.ErrorCode.getName(), BlogGameCode.TransactionExist.getCode());
                    es.setString(Field.Message.getName(), Message.TransactionExist.getMessage());
                }
            } catch (Exception e) {
                logger.error(e.getMessage());
                es.setInteger(Field.ErrorCode.getName(), BlogGameCode.Unknown.getCode());
                es.setString(Field.TradeXu.getName(), Message.TradeUnknown.getMessage());
            } finally {
                dbController.writeLogTradeXu(username, code, khoai, transaction);
            }
            MessagingHelper.sendMessageToPlayer(username, es, getApi());
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="trade gift">
    private void tradeGift(String username, EsObjectRO requestParameters) {
        if (!requestParameters.variableExists(Field.ItemId.getName())) {
            //send error
            getApi().getLogger().info("Message 'trade gift' isn't enought prameter !!!");
        } else {
            EsObject es = new EsObject();
            es.setString(Field.Command.getName(), Field.TradeGift.getName());
            try {
                int itemId = (Integer) requestParameters.getInteger(Field.ItemId.getName());
                Map<String, Object> map = dbController.getItemByID(itemId);
                Player player = dbController.getPlayerData(username);
                if (map != null) {
                    String itemName = map.get("item_name").toString();
                    int price = (Integer) map.get("item_price");
                    if (price <= player.getMoney()) {
                        dbController.writeLogTradeGift(username, itemName, price);
                        dbController.setMoney(username, player.getMoney(), -price, Global.SERVER_NAME, "Trade gift " + itemName);
                        player.setMoney(player.getMoney() - price);
                        es.setInteger(Field.ErrorCode.getName(), ErrorCode.IsSuccess.getCode());
                        es.setString(Field.Message.getName(), Message.RegisterGift.getMessage() + Message.ProcessWaiting.getMessage());
                        es.setEsObject(Field.UserData.getName(), player.toEsObject());
                    } else {
                        es.setInteger(Field.ErrorCode.getName(), BlogGameCode.LackMoney.getCode());
                        es.setString(Field.Message.getName(), Message.LackXu.getMessage());
                    }
                }
            } catch (Exception e) {
                logger.error(e.getMessage());
                es.setInteger(Field.ErrorCode.getName(), BlogGameCode.Unknown.getCode());
                es.setString(Field.Message.getName(), Message.TradeUnknown.getMessage());
            }
            MessagingHelper.sendMessageToPlayer(username, es, getApi());
        }
    }

    private void getLogTradeGift(String email) {
        EsObject es = new EsObject();
        es.setString(Field.Command.getName(), Field.GetLogTradeGift.getName());
        List<EsObject> lst = dbController.getLogTradeGift(email);
        if (lst == null || lst.isEmpty()) {
            es.setInteger(Field.ErrorCode.getName(), ErrorCode.LogEmpty.getCode());
            es.setString(Field.Message.getName(), Message.LogEmpty.getMessage());
        } else {
            es.setEsObjectArray(Field.LogTradeGift.getName(), lst.toArray(new EsObject[0]));
        }
        MessagingHelper.sendMessageToPlayer(email, es, getApi());
    }

    //</editor-fold>
    // <editor-fold defaultstate="collapsed" desc=" not running ">
    private void timeStartGame(String email) {
        long tmp = GamePlayer.getInstance().getTimeNextGame();
        EsObject obj = new EsObject();
        obj.setString(Field.Command.getName(), Field.TimeStartGame.getName());
        if (tmp > 0) {
            tmp = tmp / 1000;
        } else {
            tmp = 0;
        }
        obj.setLong(Field.TimeStartGame.getName(), tmp);
        MessagingHelper.sendMessageToPlayer(email, obj, getApi());
    }
    // </editor-fold>
}
