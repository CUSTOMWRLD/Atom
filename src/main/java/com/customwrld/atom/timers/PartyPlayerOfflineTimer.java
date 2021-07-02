package com.customwrld.atom.timers;

import com.customwrld.atom.Atom;
import com.customwrld.atom.network.CachedPlayer;
import com.customwrld.atom.network.Party;
import com.customwrld.atom.pigeon.payloads.MessagePlayerPayload;
import com.customwrld.atom.util.Util;
import com.customwrld.atom.util.timer.api.Timer;
import com.customwrld.atom.util.timer.api.TimerType;
import com.customwrld.pigeon.Pigeon;
import lombok.Getter;

import java.util.concurrent.TimeUnit;

@Getter
public class PartyPlayerOfflineTimer extends Timer {

    private final CachedPlayer cachedPlayer;
    private final Party party;

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
        // TODO

        //                    case OFFLINE -> {
//        this.members.remove(player);
//        this.sendPartyMessage(Component.text()
//                .append(profile.api().getChatFormat())
//                .append(Component.text(" has been removed from the party for being offline.", MC.CC.RED.getTextColor()))
//                .build());
//    }


//        Pigeon pigeon = Atom.getAtom().getPigeon();
//
//        this.party.getMembers().remove(this.cachedPlayer);
//
//        String name = "PLAYER";// Util.getColoredName(new Profile(this.cachedPlayer.getUuid()));
//
//        for(CachedPlayer player : this.party.getMembers()) {
//            pigeon.broadcast(new MessagePlayerPayload(player.getUuid(), name + " &chas left the party due to being offline."));
//        }
    }

    @Override
    public void onCancel() {
    }

}
