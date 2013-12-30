package com.netgame.lobby.loginLogoutHandlers;

import com.duduto.Global;
import com.duduto.yacht.model.Player;
import com.duduto.util.Console;
import com.duduto.util.MessagingHelper;
import com.duduto.util.RequestVGUtil;
import com.duduto.yacht.enums.BlogGameCode;
import com.duduto.yacht.enums.ErrorCode;
import com.duduto.yacht.model.GamePlayer;
import com.electrotank.electroserver5.extensions.BaseLoginEventHandler;
import com.electrotank.electroserver5.extensions.ChainAction;
import com.electrotank.electroserver5.extensions.LoginContext;
import com.electrotank.electroserver5.extensions.api.value.EsObject;
import com.electrotank.electroserver5.extensions.api.value.EsObjectRO;
import com.netgame.database.DatabaseController;
import com.netgame.lobby.enums.Field;
import com.netgame.lobby.enums.Message;

public class LoginEventHandler extends BaseLoginEventHandler {

    DatabaseController dbController;

    @Override
    public void init(EsObjectRO parameters) {
        super.init(parameters);
        dbController = (DatabaseController) getApi().acquireManagedObject("DatabaseControllerFactory", null);
    }

    @Override
    public ChainAction executeLogin(final LoginContext context) {
        EsObjectRO esLogin = context.getRequestParameters();
        Player player = null;
        int login_state = -999;
        String username = context.getUserName().trim();
        String password = context.getPassword();
        boolean isDemo = esLogin.getBoolean(Field.IsDemo.getName());
        if (username.isEmpty() || username.equals("")) {
            EsObject es = MessagingHelper.buildErrorMessage(username,
                    ErrorCode.UsernameSpace, Message.UsernameSpace.getMessage());
            context.setResponseParameters(es);
            login_state = -3;
            getApi().getLogger().warn(es.toString());
            return ChainAction.Fail;
        }
        
        //<editor-fold defaultstate="collapsed" desc="clone account ">


        //clone account
//        if (dbController.getCloneAccount(password) > Global.TOTAL_CLONE_REGISTER_ACCOUNT) {
//            EsObject es = MessagingHelper.buildErrorMessage(username,
//                    ErrorCode.CloneAccount, Message.CloneAccount.getMessage());
//            es.setInteger(Field.ErrorCode.getName(), ErrorCode.CloneAccount.getCode());
//            context.setResponseParameters(es);
//            login_state = -2;
//            getApi().getLogger().warn(es.toString());
//            dbController.writeLogLogin(username, login_state);
//            return ChainAction.Fail;
//        }
//</editor-fold>

        try {
            if (isDemo) {
                username = context.getUserName().toLowerCase().trim();
                //login with demo account/
                player = dbController.getPlayerData(username);
                if (player == null) {
                    //register demo account
                    player = registerDemoPlayer(username, password);
                } else if (!password.equals(player.getUid())) {
                    EsObject es = MessagingHelper.buildErrorMessage(username,
                            ErrorCode.UserNameExist, Message.UserNameExist.getMessage());
                    es.setInteger(Field.ErrorCode.getName(), ErrorCode.UserNameExist.getCode());
                    context.setResponseParameters(es);
                    login_state = -3;
                    getApi().getLogger().warn(es.toString());
                    return ChainAction.Fail;
                }
            } else {
                //login with bloggame account
                player = dbController.getPlayerData(username);
                getApi().getLogger().debug("token = " + player.getDevice_token());
            }

            this.evictUserLogged(context);

            //login success add user to list player
            context.setResponseParameters(player.toEsObject());
            GamePlayer.getInstance().addPlayerTrackRace(player);

        } catch (Exception e) {
            EsObject es = MessagingHelper.buildErrorMessage(username,
                    ErrorCode.SystemError, Message.SystemError.getMessage());
            es.setInteger(Field.ErrorCode.getName(), ErrorCode.SystemError.getCode());
            context.setResponseParameters(es);
            login_state = -1;
            getApi().getLogger().warn(es.toString());
            getApi().getLogger().warn(e.toString());
            dbController.writeLogLogin(username, login_state);
            return ChainAction.Fail;
        }
        context.setUserName(username);
        login_state = 1;
        dbController.writeLogLogin(player.getEmail(), login_state);
        return ChainAction.OkAndContinue;
    }

    private void evictUserLogged(LoginContext context) {
        if (getApi().isUserLoggedIn(context.getUserName())) {
            Console.debug(getApi(), "User duplicate");
            EsObject esObject = new EsObject();
            esObject.setString(Field.Action.getName(), "evict");
            esObject.setString("evictReason", "Evict user '" + context.getUserName()
                    + "' from previous session 'cause duplicate login");
            getApi().evictUserFromServer(context.getUserName(), esObject);
        }
    }

    /*
     * write player to database
     * param : demo = true ==> write demo account,false => write with vatgia_id
     */
    private Player registerDemoPlayer(String username, String password) {
        Player player = new Player();

        player.setUserName(username);
        player.setUid(password);
        player.setStatus(1);
        player.setIsDemo(true);
        player.setEmail(username + Global.DEFAULT_EMAIL_DEMO_USER);
        player.setMoney(Global.DEFAULT_MONEY_DEMO_USER);
        dbController.playerDemo(player);

        return player;
    }
}
