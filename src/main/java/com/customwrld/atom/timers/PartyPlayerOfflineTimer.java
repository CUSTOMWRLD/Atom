package com.customwrld.atom.timers;

import com.customwrld.atom.Atom;
import com.customwrld.atom.network.CachedPlayer;
import com.customwrld.atom.network.Party;
import com.customwrld.atom.util.timer.api.Timer;
import com.customwrld.atom.util.timer.api.TimerType;

import java.util.concurrent.TimeUnit;

public class PartyPlayerOfflineTimer extends Timer {

    public final CachedPlayer cachedPlayer;
    public final Party party;

    public PartyPlayerOfflineTimer(CachedPlayer cachedPlayer, Party party) {
        super(TimerType.COUNTDOWN, Atom.getAtom().getManager());
        setDuration(TimeUnit.MINUTES.toMillis(5));
        this.cachedPlayer = cachedPlayer;
        this.party = party;
    }

    @Override
    public void onSecond() {
    }

    @Override
    public void onStart() {
    }

    @Override
    public void onComplete() {
        // PartyMessagePayload -> Player has left the party due to being offline

        this.party.getMembers().remove(this.cachedPlayer);
    }

    @Override
    public void onCancel() {
    }

}
