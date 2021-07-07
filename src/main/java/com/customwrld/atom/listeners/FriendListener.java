package com.customwrld.atom.listeners;

import com.customwrld.atom.network.FriendRequest;
import com.customwrld.atom.network.Player;
import com.customwrld.commonlib.modules.pigeon.payloads.friend.FriendRequestAcceptPayload;
import com.customwrld.commonlib.modules.pigeon.payloads.friend.FriendRequestCreatePayload;
import com.customwrld.commonlib.modules.pigeon.payloads.friend.FriendRequestDenyPayload;
import com.customwrld.commonlib.modules.profile.Profile;
import com.customwrld.commonlib.util.MC;
import com.customwrld.pigeon.annotations.PayloadHandler;
import com.customwrld.pigeon.annotations.PayloadListener;
import com.customwrld.pigeon.listeners.Listener;
import net.kyori.adventure.text.Component;

import java.util.UUID;

@PayloadListener
public class FriendListener implements Listener {

    @PayloadHandler
    public void onFriendRequestCreate(FriendRequestCreatePayload payload) {
        FriendRequest friendRequest = new FriendRequest(payload.getInitiator(), payload.getTarget());

        UUID initiatorUUID = payload.getInitiator();

        if (friendRequest.isActiveRequest()) {
            Player.sendMessage(initiatorUUID,
                    Component.text("Please wait before sending this person another request.", MC.CC.RED.getTextColor()));
            return;
        }

        friendRequest.getTimer().start();
        FriendRequest.friendRequestList.add(friendRequest);

        Player.sendMessage(initiatorUUID,
                Component.text("You sent a friend request to " + Profile.getProfile(payload.getTarget()).getName() + ", they have 5 minutes to accept it!", MC.CC.YELLOW.getTextColor()));
    }

    @PayloadHandler
    public void onFriendRequestAccept(FriendRequestAcceptPayload payload) {
        UUID initiatorUUID = payload.getInitiator();
        UUID targetUUID = payload.getTarget();

        FriendRequest friendRequest = FriendRequest.getRequestByUuid(targetUUID, initiatorUUID);

        if (friendRequest == null) {
            Player.sendMessage(initiatorUUID,
                    Component.text("That person hasn't invited you to be friends!", MC.CC.RED.getTextColor()));
            return;
        }

        friendRequest.getTimer().stop();
        FriendRequest.removeRequests(initiatorUUID, targetUUID);
        friendRequest.accept();
    }

    @PayloadHandler
    public void onFriendRequestDeny(FriendRequestDenyPayload payload) {
        UUID initiatorUUID = payload.getInitiator();
        UUID targetUUID = payload.getTarget();

        FriendRequest friendRequest = FriendRequest.getRequestByUuid(targetUUID, initiatorUUID);

        if (friendRequest == null) {
            Player.sendMessage(initiatorUUID,
                    Component.text("That player hasn't invite you to be friends.", MC.CC.RED.getTextColor()));
            return;
        }

        Profile targetProfile = Profile.getProfile(targetUUID);

        if(targetProfile != null) {
            Player.sendMessage(initiatorUUID,
                    Component.text("You denied " + targetProfile.getName() + "'s friend request.", MC.CC.RED.getTextColor()));
        }

        Profile initiatorProfile = Profile.getProfile(initiatorUUID);

        if(initiatorProfile != null) {
            Player.sendMessage(targetUUID,
                    Component.text(initiatorProfile.getName() + " denied your friend request.", MC.CC.RED.getTextColor()));
        }

        friendRequest.getTimer().stop();
        FriendRequest.removeRequests(initiatorUUID, targetUUID);
    }

}
