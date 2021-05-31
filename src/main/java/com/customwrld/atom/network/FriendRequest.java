package com.customwrld.atom.network;

import com.customwrld.atom.Atom;
import com.customwrld.atom.pigeon.payloads.MessagePlayerPayload;
import com.customwrld.atom.timers.FriendRequestTimer;
import com.customwrld.atom.util.Util;
import com.customwrld.pigeon.Pigeon;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

@Getter
public class FriendRequest {

    public static List<FriendRequest> friendRequestList = new ArrayList<>();

    // TODO: Handle ignoring, check if a player has an active friend request then remove it if they ignored them.

    private final UUID initiator;
    private final UUID target;
    private final Long created;
    private final FriendRequestTimer timer;

    public FriendRequest(UUID initiator, UUID target) {
        this.initiator = initiator;
        this.target = target;
        this.created = System.currentTimeMillis();
        this.timer = new FriendRequestTimer(this);
    }

    public static FriendRequest getRequestByUuid(UUID initiator, UUID target) {
        return friendRequestList.stream().filter(request -> request.getInitiator().equals(initiator) && request.getTarget().equals(target)).findFirst().orElse(null);
    }

    public boolean isActiveRequest() {
        return friendRequestList.stream().filter(request -> request.getInitiator() == this.initiator && request.getTarget() == this.target).findFirst().orElse(null) != null;
    }

    public static void removeRequests(UUID player, UUID otherPlayer) {

        Iterator<FriendRequest> iterator = friendRequestList.iterator();

        while (iterator.hasNext()) {
            FriendRequest request = iterator.next();

            if (request.getInitiator().equals(player) && request.getTarget().equals(otherPlayer) || request.getInitiator().equals(otherPlayer) && request.getTarget().equals(player)) {
                request.getTimer().stop();
                iterator.remove();
            }
        }
    }

    public void accept() {
        Pigeon pigeon = Atom.getAtom().getPigeon();

        Profile initiatorProfile = new Profile(initiator);
        Profile targetProfile = new Profile(target);

        pigeon.broadcast(new MessagePlayerPayload(target, "&aYou are now friends with " + Util.getColoredName(initiatorProfile)));
        pigeon.broadcast(new MessagePlayerPayload(initiator, "&aYou are now friends with " + Util.getColoredName(targetProfile)));

        initiatorProfile.getFriends().add(target);
        targetProfile.getFriends().add(initiator);

        initiatorProfile.save();
        targetProfile.save();

        // TODO: This
//        new RecalculateProfilePayload();
//        new RecalculateProfilePayload();
    }

}
