package com.netgame.lobby.enums;

public enum Command {
	GetRoomList("getRoomList"), 
	GetPlayerList("getPlayerList"), 
        MiddlewareRequest("middlewareRequest"),
        MiddlewareUserName("MIDDLEWARE"),
	GetBuddyList("getBuddyList");
	
	private final String command;
    
    private Command(String command) {
        this.command = command;
    }

    public String getCommand() {
        return command;
    }
}
