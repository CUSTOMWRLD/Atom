package com.customwrld.atom.timers;

import com.customwrld.atom.Atom;
import com.customwrld.atom.network.SyncCode;
import com.customwrld.atom.util.timer.api.Timer;
import com.customwrld.atom.util.timer.api.TimerType;

import java.util.concurrent.TimeUnit;

public class SyncCodeTimer extends Timer {

    public final SyncCode syncCode;

    public SyncCodeTimer(SyncCode syncCode) {
        super(TimerType.COUNTDOWN, Atom.getAtom().getManager());
        setDuration(TimeUnit.MINUTES.toMillis(5));
        this.syncCode = syncCode;
    }

    @Override
    public void onSecond() {
    }

    @Override
    public void onStart() {
    }

    @Override
    public void onComplete() {
        SyncCode.syncCodeList.remove(this.syncCode);
    }

    @Override
    public void onCancel() {
    }

}
