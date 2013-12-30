/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.netgame.lobby.model;

import com.duduto.yacht.model.GamePlayer;
import it.sauronsoftware.cron4j.Task;
import it.sauronsoftware.cron4j.TaskExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author PhongTn
 * @since Nov 21, 2012 5:16:02 PM
 * @version 1.0
 */
public class GameControlTimer extends Task {

    private static final Logger logger_ = LoggerFactory.getLogger(GameControlTimer.class);

    @Override
    public void execute(TaskExecutionContext context) throws RuntimeException {        
        GamePlayer.getInstance().startGamePlay();
    }
}
