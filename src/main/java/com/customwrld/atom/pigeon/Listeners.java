package com.customwrld.atom.pigeon;

import com.customwrld.atom.Atom;
import com.customwrld.atom.network.CachedPlayer;
import com.customwrld.atom.network.FriendRequest;
import com.customwrld.atom.network.Server;
import com.customwrld.atom.pigeon.payloads.AtomStatsRequestPayload;
import com.customwrld.atom.network.*;
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
import com.customwrld.atom.pigeon.payloads.rank.ReloadRankPayload;
import com.customwrld.atom.pigeon.payloads.rank.RemoveRankPayload;
import com.customwrld.atom.timers.PartyPlayerOfflineTimer;
import com.customwrld.commonlib.modules.profile.Profile;
import com.customwrld.atom.pigeon.payloads.network.GamemodeRequestPayload;
import com.customwrld.atom.pigeon.payloads.network.RegisterGamemodePayload;
import com.customwrld.atom.util.Logger;
import com.customwrld.atom.util.Util;
import com.customwrld.commonlib.modules.rank.Rank;
import com.customwrld.commonlib.util.MC;
import com.customwrld.pigeon.Pigeon;
import com.customwrld.pigeon.annotations.PayloadHandler;
import com.customwrld.pigeon.annotations.PayloadListener;
import com.customwrld.pigeon.feedback.RequiredState;
import com.customwrld.pigeon.listeners.Listener;
import net.kyori.adventure.text.Component;

import java.util.UUID;

@PayloadListener
public class Listeners implements Listener {

    @PayloadHandler
    public void onReloadRank(ReloadRankPayload payload) {
        Rank rank = Rank.getRank(payload.getRank(), false);

        Rank.getRanks().remove(payload.getRank());

        if (rank != null) {
            Rank.getRanks().put(rank.getUuid(), rank);
        }
    }

    @PayloadHandler
    public void onRemoveRank(RemoveRankPayload payload) {
        UUID rank = payload.getRank();

        Rank.getRanks().remove(rank);
    }

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

                if (profile.api().getAllPermissions().contains("flame.staff")) {
                    Atom.getAtom().getPigeon().broadcast(new StaffConnectPayload(uuid, payload.getServerTo()));
                }
            } else {
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
        CachedPlayer player = CachedPlayer.getPlayerByUuid(payload.getUuid());
        CachedPlayer.getCachedPlayerList().remove(payload.getUuid());

        if(player != null) {
            Party party = Party.getPartyByMember(payload.getUuid());

            if(party != null) {
                PartyPlayerOfflineTimer timer = new PartyPlayerOfflineTimer(payload.getUuid());
                timer.start();
            }
        }

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
            pigeon.broadcast(new MessagePlayerPayload(payload.getInitiator(), Component.text("An error has occurred, please try rejoining the network.", MC.CC.RED.getTextColor())));
            return;
        }

        if (target == null || !target.isOnline()) {
            pigeon.broadcast(new MessagePlayerPayload(payload.getInitiator(), Component.text("&cThat player is currently not online.", MC.CC.RED.getTextColor())));
            return;
        }

        initiator.sendConversationMessage(target, message);
    }

    @PayloadHandler
    public void onConversationReply(ConversationReplyPayload payload) {
        Pigeon pigeon = Atom.getAtom().getPigeon();

        CachedPlayer initiator = CachedPlayer.getPlayerByUuid(payload.getInitiator());
        String message = payload.getMessage();

        if (initiator == null || !initiator.isOnline()) {
            pigeon.broadcast(new MessagePlayerPayload(payload.getInitiator(), Component.text("An error has occurred, please try rejoining the network.", MC.CC.RED.getTextColor())));
            return;
        }

        CachedPlayer target = initiator.getLastMessaged();

        if (target == null) {
            pigeon.broadcast(new MessagePlayerPayload(payload.getInitiator(), Component.text("You haven't started a conversation with anybody.", MC.CC.RED.getTextColor())));
            return;
        }

        if (!target.isOnline()) {
            pigeon.broadcast(new MessagePlayerPayload(payload.getInitiator(), Component.text("That player is currently not online.", MC.CC.RED.getTextColor())));
            return;
        }

        Profile targetProfile = Profile.getProfile(target.getUuid());

        if (targetProfile == null) {
            pigeon.broadcast(new MessagePlayerPayload(payload.getInitiator(), Component.text("Could not resolve player information.", MC.CC.RED.getTextColor())));
            return;
        }

        Profile initiatorProfile = Profile.getProfile(initiator.getUuid());

        if (initiatorProfile == null) {
            pigeon.broadcast(new MessagePlayerPayload(payload.getInitiator(), Component.text("Failed to load your profile. Try again later.", MC.CC.RED.getTextColor())));
            return;
        }

        if (targetProfile.api().isIgnoring(initiatorProfile) || !targetProfile.getOptionsProfile().isReceivingConversations()) {
            pigeon.broadcast(new MessagePlayerPayload(payload.getInitiator(), Component.text("That player is not receiving new conversations right now.", MC.CC.RED.getTextColor())));
            return;
        }

        initiator.reply(message);
    }

    @PayloadHandler
    public void onServerUpdate(ServerUpdatePayload payload) {
        new Server(payload.getName(), payload.getGamemode(), payload.getUptime(), payload.getTps(), payload.getPlayers(), payload.getMaxPlayers()).updateServer();
    }

    @PayloadHandler
    public void onCreateFriendRequest(CreateFriendRequestPayload payload) {
        Pigeon pigeon = Atom.getAtom().getPigeon();

        FriendRequest friendRequest = new FriendRequest(payload.getInitiator(), payload.getTarget());

        UUID initiator = payload.getInitiator();

        if (friendRequest.isActiveRequest()) {
            pigeon.broadcast(new MessagePlayerPayload(initiator, Component.text("Please wait before sending this person another request.", MC.CC.RED.getTextColor())));
            return;
        }

        friendRequest.getTimer().start();
        FriendRequest.friendRequestList.add(friendRequest);

        // TODO: Might throw NPE
        pigeon.broadcast(new MessagePlayerPayload(initiator, Component.text("You sent a friend request to " + Profile.getProfile(payload.getTarget()).getName() + ", they have 5 minutes to accept it!", MC.CC.YELLOW.getTextColor())));
    }

    @PayloadHandler
    public void onAcceptFriendRequest(AcceptFriendRequestPayload payload) {
        Pigeon pigeon = Atom.getAtom().getPigeon();
        UUID initiator = payload.getInitiator();
        UUID target = payload.getTarget();

        FriendRequest friendRequest = FriendRequest.getRequestByUuid(target, initiator);

        if (friendRequest == null) {
            pigeon.broadcast(new MessagePlayerPayload(initiator, Component.text("That person hasn't invited you to be friends!", MC.CC.RED.getTextColor())));
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
            pigeon.broadcast(new MessagePlayerPayload(initiator, Component.text("That player hasn't invite you to be friends.", MC.CC.RED.getTextColor())));
            return;
        }

        Profile initiatorProfile = Profile.getProfile(initiator);
        Profile targetProfile = Profile.getProfile(target);

        pigeon.broadcast(new MessagePlayerPayload(target, Component.text(initiatorProfile.getName() + " denied your friend request.", MC.CC.RED.getTextColor())));
        pigeon.broadcast(new MessagePlayerPayload(initiator, Component.text("You denied " + targetProfile.getName() + "'s friend request.", MC.CC.RED.getTextColor())));

        friendRequest.getTimer().stop();
        FriendRequest.removeRequests(initiator, target);
    }

    @PayloadHandler(requiredState = RequiredState.REQUEST)
    public void onGamemodeRequest(GamemodeRequestPayload payload) {
        String name = payload.getName();

        Gamemode gamemode = Gamemode.getByName(name);

        payload.sendResponse(new GamemodeRequestPayload(gamemode));
    }

    @PayloadHandler
    public void onRegisterGamemode(RegisterGamemodePayload payload) {
        var gamemode = payload.getGamemode();
        Gamemode.save(gamemode);
    }

}
