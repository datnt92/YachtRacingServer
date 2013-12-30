package com.duduto.util;

import com.duduto.yacht.enums.ErrorCode;
import com.duduto.yacht.model.GamePlayer;
import com.duduto.yacht.model.Player;
import com.electrotank.electroserver5.extensions.api.PluginApi;
import com.electrotank.electroserver5.extensions.api.value.EsObject;
import com.netgame.lobby.enums.Field;
import java.util.List;

public class MessagingHelper {

    public static void sendMessageToPlayer(String username, EsObject message, PluginApi api) {
        api.sendPluginMessageToUser(username, message);
        //api.getLogger().info(message.toString());
    }

    public static EsObject buildErrorMessage(String username, ErrorCode errorCode, String mes) {
        EsObject message = new EsObject();
        message.setString(Field.Command.getName(), "error");
        message.setString("mess", mes);
        message.setInteger(Field.ErrorCode.getName(), errorCode.getCode());
        return message;
    }

    public static void sendMessageGameResultToPlayer(String username, boolean isWinner, int bonusMoney, PluginApi api) {
        EsObject message = new EsObject();
        message.setString(Field.Command.getName(), Field.GameResult.getName());
        message.setString(Field.UserName.getName(), username);
        message.setBoolean(Field.IsWinner.getName(), isWinner);
        message.setInteger(Field.Money.getName(), bonusMoney);
        sendMessageToPlayer(username, message, api);
        api.getLogger().info(message.toString());
    }
    
    public static void sendMessageEndGame(int bestHorse,int secondHost,int thirdHorse, PluginApi api) {
        EsObject message = new EsObject();
        message.setString(Field.Command.getName(), Field.EndGame.getName());
        message.setInteger(Field.BestYacht.getName(), bestHorse);
        message.setInteger(Field.secondHorse.getName(), secondHost);
        message.setInteger(Field.thirdHorse.getName(), thirdHorse);
        api.getLogger().warn(message.toString());
        sendGlobalMessage(message, api);
        //sendMessageToMiddleWare(message, api);
    }

    public static void sendMessageTrackRace(EsObject esHorse, int horseid, PluginApi api) {

        EsObject message = new EsObject();
        message.setString(Field.Command.getName(), Field.TrackRace.getName());
        message.setEsObject(Field.ListYacht.getName(), esHorse);
        message.setInteger(Field.horseId.getName(), horseid);
        List<Player> lstPlayerTrackRace = GamePlayer.getInstance().getListPlayerTrackRace();
        for (int i = 0; i < lstPlayerTrackRace.size(); i++) {
//            api.getLogger().info(message.toString());
            sendMessageToPlayer(lstPlayerTrackRace.get(i).getUserName(), message, api);
        }
    }

    public static void sendGlobalMessage(EsObject message, PluginApi api) {
        api.sendGlobalPluginMessage(message);
    }
}
