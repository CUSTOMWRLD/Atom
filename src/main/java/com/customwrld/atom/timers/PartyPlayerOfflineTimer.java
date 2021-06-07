package com.customwrld.atom.timers;

import com.customwrld.atom.Atom;
import com.customwrld.atom.network.CachedPlayer;
import com.customwrld.atom.network.FriendRequest;
import com.customwrld.atom.network.Party;
import com.customwrld.atom.network.Profile;
import com.customwrld.atom.pigeon.payloads.MessagePlayerPayload;
import com.customwrld.atom.util.Util;
import com.customwrld.atom.util.timer.api.Timer;
import com.customwrld.atom.util.timer.api.TimerType;
import com.customwrld.pigeon.Pigeon;

import java.util.Iterator;
import java.util.UUID;
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
        Pigeon pigeon = Atom.getAtom().getPigeon();

        this.party.getMembers().remove(this.cachedPlayer);

        String name = Util.getColoredName(new Profile(this.cachedPlayer.getUuid()));

        for(CachedPlayer player : this.party.getMembers()) {
            pigeon.broadcast(new MessagePlayerPayload(player.getUuid(), name + " &chas left the party due to being offline."));
        }
    }

    @Override
    public void onCancel() {
    }

}
