package com.sc.easyaccessmodule.event;

/**
 * Created by Peter on 27.04.2017.
 */

public class FingerPrintConfirmedEvent {
    private long verificationCode;

    public final long getVerificationCode() {
        return verificationCode;
    }

    public FingerPrintConfirmedEvent(long verificationCode) {
        this.verificationCode = verificationCode;
    }
}
