package com.customwrld.atom.pigeon;

import com.customwrld.atom.Atom;
import com.customwrld.atom.network.CachedPlayer;
import com.customwrld.atom.network.Conversation;
import com.customwrld.atom.network.FriendRequest;
import com.customwrld.atom.network.Profile;
import com.customwrld.atom.pigeon.payloads.MessagePlayerPayload;
import com.customwrld.atom.pigeon.payloads.OnlinePlayerRequestPayload;
import com.customwrld.atom.pigeon.payloads.ServerUpdatePayload;
import com.customwrld.atom.pigeon.payloads.conversation.ConversationMessagePayload;
import com.customwrld.atom.pigeon.payloads.conversation.ConversationReplyPayload;
import com.customwrld.atom.pigeon.payloads.friend.AcceptFriendRequestPayload;
import com.customwrld.atom.pigeon.payloads.friend.CreateFriendRequestPayload;
import com.customwrld.atom.pigeon.payloads.friend.DenyFriendRequestPayload;
import com.customwrld.atom.util.Logger;
import com.customwrld.atom.util.Util;
import com.customwrld.pigeon.Pigeon;
import com.customwrld.pigeon.annotations.PayloadHandler;
import com.customwrld.pigeon.annotations.PayloadListener;
import com.customwrld.pigeon.listeners.Listener;

import java.util.Arrays;
import java.util.UUID;

@PayloadListener
public class Listeners implements Listener {

    @PayloadHandler
    public void onOnlinePlayerRequest(OnlinePlayerRequestPayload payload) {
        CachedPlayer cachedPlayer = CachedPlayer.getPlayerByUuid(payload.getPlayer());

        payload.sendResponse(new OnlinePlayerRequestPayload(cachedPlayer != null && cachedPlayer.isOnline()));
    }

    @PayloadHandler
    public void onConversationMessage(ConversationMessagePayload payload) {
        Pigeon pigeon = Atom.getAtom().getPigeon();

        UUID initiator = payload.getInitiator();
        UUID target = payload.getTarget();
        String message = payload.getMessage();

        CachedPlayer cachedPlayer = CachedPlayer.getPlayerByUuid(target);

        if (cachedPlayer == null || !cachedPlayer.isOnline()) {
            pigeon.broadcast(new MessagePlayerPayload(initiator, "&cThat player is currently not online."));
            return;
        }

        Conversation conversation = Conversation.getConversation(Arrays.asList(initiator, target));

        if (conversation == null) {
            conversation = new Conversation(Arrays.asList(initiator, target));
        }

        conversation.sendMessage(initiator, target, message);
    }

    @PayloadHandler
    public void onConversationReply(ConversationReplyPayload payload) {
        Pigeon pigeon = Atom.getAtom().getPigeon();

        UUID initiator = payload.getInitiator();
        String message = payload.getMessage();

        CachedPlayer cachedPlayer = CachedPlayer.getPlayerByUuid(initiator);

        if (cachedPlayer == null || !cachedPlayer.isOnline()) {
            Logger.log(Logger.Level.ERROR, initiator + " is not online but they attempted to reply.");
            return;
        }

        Conversation conversation = cachedPlayer.getLatestConversation();

        if (conversation == null) {
            pigeon.broadcast(new MessagePlayerPayload(initiator, "&cYou are currently not in a conversation with someone."));
            return;
        }

        UUID target = null;

        for (UUID uuid : conversation.getMembers()) {
            if (!uuid.equals(initiator)) {
                target = uuid;
            }
        }

        if (target != null) {
            conversation.sendMessage(initiator, target, message);
        }
    }

    @PayloadHandler
    public void onServerUpdate(ServerUpdatePayload payload) {
        payload.getServer().updateServer();
    }

    @PayloadHandler
    public void onMessagePlayer(MessagePlayerPayload payload) {
        Logger.log(Logger.Level.DEBUG, payload.getPlayer() + " - " + payload.getMessage());
    }

    @PayloadHandler
    public void onCreateFriendRequest(CreateFriendRequestPayload payload) {
        Pigeon pigeon = Atom.getAtom().getPigeon();

        FriendRequest friendRequest = new FriendRequest(payload.getInitiator(), payload.getTarget());

        UUID initiator = payload.getInitiator();

        if (friendRequest.isActiveRequest()) {
            pigeon.broadcast(new MessagePlayerPayload(initiator, "&cPlease wait before sending this person another request."));
            return;
        }

        friendRequest.getTimer().start();
        FriendRequest.friendRequestList.add(friendRequest);

        pigeon.broadcast(new MessagePlayerPayload(initiator, "&eYou sent a friend request to " + Util.getColoredName(new Profile(payload.getTarget())) + "&e, they have 5 minutes to accept it!"));
    }

    @PayloadHandler
    public void onAcceptFriendRequest(AcceptFriendRequestPayload payload) {
        Logger.log(Logger.Level.DEBUG, "AcceptFriendRequestPayload Received");
        Pigeon pigeon = Atom.getAtom().getPigeon();
        UUID initiator = payload.getInitiator();
        UUID target = payload.getTarget();

        FriendRequest friendRequest = FriendRequest.getRequestByUuid(target, initiator);

        if (friendRequest == null) {
            pigeon.broadcast(new MessagePlayerPayload(initiator, "&cThat person hasn't invited you to be friends!"));
            return;
        }

        friendRequest.getTimer().stop();
        FriendRequest.removeRequests(initiator, target);
        friendRequest.accept();
    }

    @PayloadHandler
    public void onDenyFriendRequest(DenyFriendRequestPayload payload) {
        Pigeon pigeon = Atom.getAtom().getPigeon();
        UUID initiator = payload.getInitiator();
        UUID target = payload.getTarget();

        FriendRequest friendRequest = FriendRequest.getRequestByUuid(target, initiator);

        if (friendRequest == null) {
            pigeon.broadcast(new MessagePlayerPayload(initiator, "&cThat player hasn't invite you to be friends."));
            return;
        }

        Profile initiatorProfile = new Profile(initiator);
        Profile targetProfile = new Profile(target);

        pigeon.broadcast(new MessagePlayerPayload(target, Util.getColoredName(initiatorProfile) + " denied your friend request."));
        pigeon.broadcast(new MessagePlayerPayload(initiator, "&cYou denied " + Util.getColoredName(targetProfile) + "'s friend request."));

        friendRequest.getTimer().stop();
        FriendRequest.removeRequests(initiator, target);
    }

}
