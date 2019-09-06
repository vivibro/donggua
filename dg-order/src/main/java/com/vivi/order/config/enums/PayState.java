package com.vivi.order.config.enums;

public enum PayState {
    NOT_PAY(0),SUCCESS(1),FAIL(2);
    PayState(int code){
        this.code=code;
    }
    private int code;
    public int getCode(){
        return code;
    }
}
