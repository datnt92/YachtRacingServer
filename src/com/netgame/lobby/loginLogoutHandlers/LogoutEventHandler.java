package com.netgame.lobby.loginLogoutHandlers;

import com.duduto.yacht.model.GamePlayer;
import com.electrotank.electroserver5.extensions.BaseLogoutEventHandler;
import com.electrotank.electroserver5.extensions.api.value.EsObjectRO;

public class LogoutEventHandler extends BaseLogoutEventHandler {

    @Override
    public void executeLogout(String username) {
        getApi().getLogger().info("Execute logout for user '" + username + "'");
        super.executeLogout(username);
        GamePlayer.getInstance().removePlayerTrackRace(username);
    }

    @Override
    public void init(EsObjectRO parameters) {
        getApi().getLogger().info("Init logout event handler");
        super.init(parameters);
    }
}
