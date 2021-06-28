package com.customwrld.atom.pigeon;

import com.customwrld.atom.Atom;
import com.customwrld.atom.network.CachedPlayer;
import com.customwrld.atom.network.FriendRequest;
import com.customwrld.atom.network.Server;
import com.customwrld.atom.pigeon.payloads.AtomStatsRequestPayload;
import com.customwrld.atom.pigeon.payloads.MessagePlayerPayload;
import com.customwrld.atom.pigeon.payloads.OnlinePlayerRequestPayload;
import com.customwrld.atom.pigeon.payloads.ServerUpdatePayload;
import com.customwrld.atom.pigeon.payloads.conversation.ConversationMessagePayload;
import com.customwrld.atom.pigeon.payloads.conversation.ConversationReplyPayload;
import com.customwrld.atom.pigeon.payloads.friend.AcceptFriendRequestPayload;
import com.customwrld.atom.pigeon.payloads.friend.CreateFriendRequestPayload;
import com.customwrld.atom.pigeon.payloads.friend.DenyFriendRequestPayload;
import com.customwrld.atom.pigeon.payloads.proxy.*;
import com.customwrld.atom.timers.PartyPlayerOfflineTimer;
import com.customwrld.commonlib.modules.profile.Profile;
import com.customwrld.pigeon.Pigeon;
import com.customwrld.pigeon.annotations.PayloadHandler;
import com.customwrld.pigeon.annotations.PayloadListener;
import com.customwrld.pigeon.listeners.Listener;

import java.util.UUID;

@PayloadListener
public class Listeners implements Listener {

    @PayloadHandler
    public void onProxySwitch(ProxySwitchPayload payload) {
        UUID uuid = payload.getUuid();
        Profile profile = Profile.getProfile(uuid);
        CachedPlayer player = CachedPlayer.getPlayerByUuid(payload.getUuid());
        
        if(player == null) {
            player = new CachedPlayer(payload.getUuid(), payload.getPlayer(), payload.getServerTo());
        }

        CachedPlayer.getCachedPlayerList().remove(uuid);

        if (profile != null) {
            if (payload.getServerFrom() == null) {
                PartyPlayerOfflineTimer playerOfflineTimer = player.getPartyPlayerOfflineTimer();

                if (playerOfflineTimer != null) {
                    playerOfflineTimer.stop();
                }

                System.out.println(profile.getName() + " permissions: " + profile.api().getAllPermissions());

                if (profile.api().getAllPermissions().contains("flame.staff")) {
                    Atom.getAtom().getPigeon().broadcast(new StaffConnectPayload(uuid, payload.getServerTo()));
                }
            } else {

                System.out.println(profile.getName() + " permissions: " + profile.api().getAllPermissions());

                if (profile.api().getAllPermissions().contains("flame.staff")) {
                    Atom.getAtom().getPigeon().broadcast(new StaffSwitchPayload(uuid, payload.getServerFrom(), payload.getServerTo()));
                }
            }
        }

        player.setCurrentServer(payload.getServerTo());

        CachedPlayer.getCachedPlayerList().put(uuid, player);
    }

    @PayloadHandler
    public void onProxyDisconnect(ProxyDisconnectPayload payload) {
        CachedPlayer.getCachedPlayerList().remove(payload.getUuid());

        Profile profile = Profile.getProfile(payload.getUuid());

        if (profile != null) {
            if (profile.api().getAllPermissions().contains("flame.staff")) {
                Atom.getAtom().getPigeon().broadcast(new StaffDisconnectPayload(profile.getId(), payload.getServer()));
            }
        }
    }

    @PayloadHandler
    public void onOnlinePlayerRequest(OnlinePlayerRequestPayload payload) {
        CachedPlayer cachedPlayer = CachedPlayer.getPlayerByUuid(payload.getPlayer());

        payload.sendResponse(new OnlinePlayerRequestPayload(cachedPlayer != null && cachedPlayer.isOnline()));
    }

    @PayloadHandler
    public void onAtomStatsRequest(AtomStatsRequestPayload payload) {
        payload.sendResponse(new AtomStatsRequestPayload(System.currentTimeMillis()));
    }

    @PayloadHandler
    public void onConversationMessage(ConversationMessagePayload payload) {
        Pigeon pigeon = Atom.getAtom().getPigeon();

        CachedPlayer initiator = CachedPlayer.getPlayerByUuid(payload.getInitiator());
        CachedPlayer target = CachedPlayer.getPlayerByUuid(payload.getTarget());

        String message = payload.getMessage();

        if (initiator == null || !initiator.isOnline()) {
            System.out.println(CachedPlayer.getCachedPlayerList().keySet());
            pigeon.broadcast(new MessagePlayerPayload(payload.getInitiator(), "&cAn error has occurred, please try rejoining the network."));
            return;
        }

        if (target == null || !target.isOnline()) {
            pigeon.broadcast(new MessagePlayerPayload(payload.getInitiator(), "&cThat player is currently not online."));
            return;
        }

        initiator.sendMessage(target, message);
    }

    @PayloadHandler
    public void onConversationReply(ConversationReplyPayload payload) {
        Pigeon pigeon = Atom.getAtom().getPigeon();

        CachedPlayer initiator = CachedPlayer.getPlayerByUuid(payload.getInitiator());
        String message = payload.getMessage();

        if (initiator == null || !initiator.isOnline()) {
            pigeon.broadcast(new MessagePlayerPayload(payload.getInitiator(), "&cAn error has occurred, please try rejoining the network."));
            return;
        }

        CachedPlayer target = initiator.getLastMessaged();

        if (target == null) {
            pigeon.broadcast(new MessagePlayerPayload(payload.getInitiator(), "&cYou haven't started a conversation with anybody."));
            return;
        }

        if (!target.isOnline()) {
            pigeon.broadcast(new MessagePlayerPayload(payload.getInitiator(), "&cThat player is currently not online."));
            return;
        }

        Profile targetProfile = Profile.getProfile(target.getUuid());

        if (targetProfile == null) {
            pigeon.broadcast(new MessagePlayerPayload(payload.getInitiator(), "&cCould not resolve player information."));
            return;
        }

        Profile initiatorProfile = Profile.getProfile(initiator.getUuid());

        if (initiatorProfile == null) {
            pigeon.broadcast(new MessagePlayerPayload(payload.getInitiator(), "&cFailed to load your profile. Try again later."));
            return;
        }

        if (targetProfile.api().isIgnoring(initiatorProfile) || !targetProfile.getOptionsProfile().isReceivingConversations()) {
            pigeon.broadcast(new MessagePlayerPayload(payload.getInitiator(), "&cThat player is not receiving new conversations right now."));
            return;
        }

        initiator.reply(message);
    }

    @PayloadHandler
    public void onServerUpdate(ServerUpdatePayload payload) {
        payload.getServer().updateServer();
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

        // TODO: Might throw NPE
        pigeon.broadcast(new MessagePlayerPayload(initiator, "&eYou sent a friend request to " + Profile.getProfile(payload.getTarget()).getName() + "&e, they have 5 minutes to accept it!"));
    }

    @PayloadHandler
    public void onAcceptFriendRequest(AcceptFriendRequestPayload payload) {
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

        Profile initiatorProfile = Profile.getProfile(initiator);
        Profile targetProfile = Profile.getProfile(target);

        pigeon.broadcast(new MessagePlayerPayload(target, "&c" + initiatorProfile.getName() + " denied your friend request."));
        pigeon.broadcast(new MessagePlayerPayload(initiator, "&cYou denied " + targetProfile.getName() + "'s friend request."));

        friendRequest.getTimer().stop();
        FriendRequest.removeRequests(initiator, target);
    }

}
