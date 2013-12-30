package com.duduto.yacht.enums;

public enum BlogGameCode {
    EmailCorrect(1010),
    TokenError(1001),
    LackMoney(1002),
    Unknown(1004),
    LogEmpty(1005),
    TradeSuccess(1010),
    KhoaiEmpty(1020),
    NotEnoughXu(1021),
    TransactionExist(1022);
    private final int code;

    private BlogGameCode(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
