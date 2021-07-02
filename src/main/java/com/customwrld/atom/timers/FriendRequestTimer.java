package com.customwrld.atom.timers;

import com.customwrld.atom.Atom;
import com.customwrld.atom.network.FriendRequest;
import com.customwrld.atom.pigeon.payloads.MessagePlayerPayload;
import com.customwrld.atom.util.Util;
import com.customwrld.atom.util.timer.api.Timer;
import com.customwrld.atom.util.timer.api.TimerType;
import com.customwrld.commonlib.modules.profile.Profile;
import com.customwrld.commonlib.util.MC;
import com.customwrld.pigeon.Pigeon;
import net.kyori.adventure.text.Component;

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

        // TODO: Might throw NPE
        pigeon.broadcast(new MessagePlayerPayload(this.friendRequest.getInitiator(), Component.text("Your friend request to " + Profile.getProfile(this.friendRequest.getTarget()).getName() + " has expired.", MC.CC.RED.getTextColor())));
        pigeon.broadcast(new MessagePlayerPayload(this.friendRequest.getTarget(), Component.text("The friend request from " + Profile.getProfile(this.friendRequest.getInitiator()).getName() + " has expired.", MC.CC.RED.getTextColor())));

        friendRequestList.remove(this.friendRequest);
    }

    @Override
    public void onCancel() {

    }

}
