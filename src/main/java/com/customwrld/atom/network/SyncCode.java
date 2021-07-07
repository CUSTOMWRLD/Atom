package com.customwrld.atom.network;

import com.customwrld.atom.timers.SyncCodeTimer;
import com.customwrld.atom.util.Util;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class SyncCode {

    public static List<SyncCode> syncCodeList = new ArrayList<>();

    private final Player player;
    private final int code;
    private final SyncCodeTimer timer;
    private final long generatedAt;

    public SyncCode(Player player) {
        this.player = player;
        this.code = Util.getRandomCode();
        this.timer = new SyncCodeTimer(this);
        this.generatedAt = System.currentTimeMillis();
    }

}