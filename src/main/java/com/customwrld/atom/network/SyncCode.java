package com.customwrld.atom.network;

import com.customwrld.atom.timers.SyncCodeTimer;
import com.customwrld.atom.util.Util;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class SyncCode {

    public static List<SyncCode> syncCodeList = new ArrayList<>();

    private final CachedPlayer cachedPlayer;
    private final int code;
    private final SyncCodeTimer timer;
    private final long generatedAt;

    public SyncCode(CachedPlayer cachedPlayer) {
        this.cachedPlayer = cachedPlayer;
        this.code = Util.getRandomCode();
        this.timer = new SyncCodeTimer(this);
        this.generatedAt = System.currentTimeMillis();
    }

}