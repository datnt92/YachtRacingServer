package com.duduto.yacht.enums;

public enum ErrorCode {

    IsSuccess(999),
    SystemError(-1),
    UserNameEmpty(200),
    UserNameExist(201),
    UsernameSpace(208),
    LoginFaild(202),
    EmailExist(203),
    IsDemo(204),
    CloneAccount(205),
    GameEmpty(206),
    LogBettingEmpty(207),
    LogEmpty(1005);
    private final int code;

    private ErrorCode(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
