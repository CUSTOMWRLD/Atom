package com.customwrld.atom.timers;

import com.customwrld.atom.Atom;
import com.customwrld.atom.network.FriendRequest;
import com.customwrld.atom.network.Profile;
import com.customwrld.atom.pigeon.payloads.MessagePlayerPayload;
import com.customwrld.atom.util.Util;
import com.customwrld.atom.util.timer.api.Timer;
import com.customwrld.atom.util.timer.api.TimerType;
import com.customwrld.pigeon.Pigeon;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class FriendRequestTimer extends Timer {

    public final FriendRequest friendRequest;

    public FriendRequestTimer(FriendRequest friendRequest) {
        super(TimerType.COUNTDOWN, Atom.getAtom().getManager());
        setDuration(TimeUnit.MINUTES.toMillis(5));
        this.friendRequest = friendRequest;
    }

    @Override
    public void onSecond() {
    }

    @Override
    public void onStart() {
    }

    @Override
    public void onComplete() {
        List<FriendRequest> friendRequestList = FriendRequest.friendRequestList;
        Pigeon pigeon = Atom.getAtom().getPigeon();

        pigeon.broadcast(new MessagePlayerPayload(this.friendRequest.getInitiator(), "&eYour friend request to " + Util.getColoredName(new Profile(this.friendRequest.getTarget())) + " &ehas expired."));
        pigeon.broadcast(new MessagePlayerPayload(this.friendRequest.getTarget(), "&eThe friend request from " + Util.getColoredName(new Profile(this.friendRequest.getInitiator())) + " &ehas expired."));

        friendRequestList.remove(this.friendRequest);
    }

    @Override
    public void onCancel() {

    }

}
