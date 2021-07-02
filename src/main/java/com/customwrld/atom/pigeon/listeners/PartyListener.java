package com.customwrld.atom.pigeon.listeners;

import com.customwrld.atom.Atom;
import com.customwrld.atom.network.CachedPlayer;
import com.customwrld.atom.network.Party;
import com.customwrld.atom.pigeon.payloads.MessagePlayerPayload;
import com.customwrld.atom.pigeon.payloads.party.*;
import com.customwrld.commonlib.modules.profile.Profile;
import com.customwrld.commonlib.util.MC;
import com.customwrld.pigeon.Pigeon;
import com.customwrld.pigeon.annotations.PayloadHandler;
import com.customwrld.pigeon.annotations.PayloadListener;
import com.customwrld.pigeon.listeners.Listener;
import net.kyori.adventure.text.Component;

import java.util.UUID;
import java.util.stream.Collectors;

@PayloadListener
public class PartyListener implements Listener {

    @PayloadHandler
    public void onPartyChat(PartyChatPayload payload) {
        Pigeon pigeon = Atom.getAtom().getPigeon();
        UUID initiatorUUID = payload.getInitiator();
        CachedPlayer initiator = CachedPlayer.getPlayerByUuid(initiatorUUID);

        if (initiator == null) {
            pigeon.broadcast(new MessagePlayerPayload(payload.getInitiator(),
                    Component.text("An error has occurred, please try rejoining the network.", MC.CC.RED.getTextColor())));
            return;
        }

        Party party = Party.getPartyByMember(initiatorUUID);

        if(party == null) {
            initiator.sendMessage("Party", Component.text("You are not in a party. Consider creating one.", MC.CC.RED.getTextColor()));
        } else {
            party.sendPartyMessage(initiatorUUID, payload.getMessage());
        }
    }

    @PayloadHandler
    public void onPartyDebug(PartyDebugPayload payload) {
        System.out.println("---------------------------------------------");

        for(Party party : Party.getPartyList()) {
            System.out.println("Leader: " + CachedPlayer.getPlayerByUuid(party.getLeader()).getName() + " | Members: " + party.getMembers().stream().map(CachedPlayer::getPlayerByUuid).map(CachedPlayer::getName).collect(Collectors.toList()));
        }

        for(CachedPlayer player : CachedPlayer.getCachedPlayerList().values()) {
            System.out.println("Cached Player: " + player.getName() + " | Party Invites: " + player.getPartyInvites());
        }

        System.out.println("---------------------------------------------");
    }

    @PayloadHandler
    public void onPartyDisband(PartyDisbandPayload payload) {
        Pigeon pigeon = Atom.getAtom().getPigeon();
        UUID initiatorUUID = payload.getInitiator();
        CachedPlayer initiator = CachedPlayer.getPlayerByUuid(initiatorUUID);

        if (initiator == null) {
            pigeon.broadcast(new MessagePlayerPayload(payload.getInitiator(),
                    Component.text("An error has occurred, please try rejoining the network.", MC.CC.RED.getTextColor())));
            return;
        }

        Party party = Party.getPartyByMember(initiatorUUID);

        if(party == null) {
            initiator.sendMessage("Party", Component.text("You are not in a party. Consider creating one.", MC.CC.RED.getTextColor()));
            return;
        } else {
            if(!party.getLeader().equals(initiator.getUuid())) {
                initiator.sendMessage("Party", Component.text("You are not the leader of the party.", MC.CC.RED.getTextColor()));
                return;
            }
        }

        party.disbandParty();
    }

    @PayloadHandler
    public void onPartyInvite(PartyInvitePayload payload) {
        Pigeon pigeon = Atom.getAtom().getPigeon();
        UUID initiatorUUID = payload.getInitiator();
        UUID targetUUID = payload.getTarget();
        CachedPlayer initiator = CachedPlayer.getPlayerByUuid(initiatorUUID);
        CachedPlayer target = CachedPlayer.getPlayerByUuid(targetUUID);

        if (initiator == null) {
            pigeon.broadcast(new MessagePlayerPayload(initiatorUUID,
                    Component.text("An error has occurred, please try rejoining the network.", MC.CC.RED.getTextColor())));
            return;
        }

        if (target == null) {
            initiator.sendMessage("Party", Component.text("That player is currently not online.", MC.CC.RED.getTextColor()));
            return;
        }

        Party party = Party.getPartyByMember(initiatorUUID);

        if(party == null) {
            initiator.sendMessage("Party", Component.text("You've created a new party!", MC.CC.GRAY.getTextColor()));
            new Party(initiatorUUID);
        } else {
            if(!party.getLeader().equals(initiator.getUuid())) {
                initiator.sendMessage("Party", Component.text("You must be the leader to invite players.", MC.CC.RED.getTextColor()));
                return;
            }

            if(party.getMembers().contains(targetUUID)) {
                initiator.sendMessage("Party", Component.text("That player is already in the party.", MC.CC.RED.getTextColor()));
                return;
            }
        }

        if(target.getPartyInvites().contains(payload.getInitiator())) {
            initiator.sendMessage("Party", Component.text("You've already invited that player to your party.", MC.CC.RED.getTextColor()));
            return;
        }

        target.addPartyInvite(initiator);
    }

    @PayloadHandler
    public void onPartyJoin(PartyJoinPayload payload) {
        Pigeon pigeon = Atom.getAtom().getPigeon();
        UUID initiatorUUID = payload.getInitiator();
        UUID targetUUID = payload.getTarget();
        CachedPlayer initiator = CachedPlayer.getPlayerByUuid(initiatorUUID);

        if (initiator == null) {
            pigeon.broadcast(new MessagePlayerPayload(initiatorUUID,
                    Component.text("An error has occurred, please try rejoining the network.", MC.CC.RED.getTextColor())));
            return;
        }

        if(initiator.isInParty()) {
            initiator.sendMessage("Party", Component.text("You are already in a party. Try leaving it first.", MC.CC.RED.getTextColor()));
            return;
        }

        Party party = Party.getPartyByMember(targetUUID);

        if(party != null && initiator.getPartyInvites().contains(targetUUID) && party.getLeader().equals(targetUUID)) {
            initiator.getPartyInvites().remove(targetUUID);
            party.addMember(initiator);
        } else {
            initiator.sendMessage("Party", Component.text("That player hasn't invited you to join their party.", MC.CC.RED.getTextColor()));
            initiator.getPartyInvites().remove(targetUUID);
        }
    }

    @PayloadHandler
    public void onPartyKick(PartyKickPayload payload) {
        Pigeon pigeon = Atom.getAtom().getPigeon();
        UUID initiatorUUID = payload.getInitiator();
        UUID targetUUID = payload.getTarget();
        CachedPlayer initiator = CachedPlayer.getPlayerByUuid(initiatorUUID);

        if (initiator == null) {
            pigeon.broadcast(new MessagePlayerPayload(payload.getInitiator(),
                    Component.text("An error has occurred, please try rejoining the network.", MC.CC.RED.getTextColor())));
            return;
        }

        Party party = Party.getPartyByMember(initiatorUUID);

        if (party != null) {
            party.removeMember(targetUUID);

            Profile profile = Profile.getProfile(targetUUID);

            if(profile != null) {
                party.sendPartyMessage(Component.text()
                        .append(profile.api().getChatFormat())
                        .append(Component.text(" has been kicked from the party.", MC.CC.RED.getTextColor()))
                        .build());

            }

            CachedPlayer target = CachedPlayer.getPlayerByUuid(targetUUID);

            if(target != null) {
                target.sendMessage("Party", Component.text("You were kicked from the party.", MC.CC.RED.getTextColor()));
            }
        } else {
            initiator.sendMessage("Party", Component.text("You are not in a party. Consider creating one.", MC.CC.RED.getTextColor()));
        }
    }

    @PayloadHandler
    public void onPartyLeave(PartyLeavePayload payload) {
        Pigeon pigeon = Atom.getAtom().getPigeon();
        UUID initiatorUUID = payload.getInitiator();
        Profile profile = Profile.getProfile(initiatorUUID);
        CachedPlayer initiator = CachedPlayer.getPlayerByUuid(initiatorUUID);

        if (initiator == null) {
            pigeon.broadcast(new MessagePlayerPayload(payload.getInitiator(),
                    Component.text("An error has occurred, please try rejoining the network.", MC.CC.RED.getTextColor())));
            return;
        }

        Party party = Party.getPartyByMember(initiatorUUID);

        if(party != null) {

            if(party.getLeader().equals(initiatorUUID)) {
                initiator.sendMessage("Party", Component.text("You cannot leave your own party. Try disbanding it or promoting someone.", MC.CC.RED.getTextColor()));
                return;
            }

            party.removeMember(initiatorUUID);

            if(profile != null) {
                party.sendPartyMessage(Component.text()
                        .append(profile.api().getChatFormat())
                        .append(Component.text(" has left the party.", MC.CC.RED.getTextColor()))
                        .build());
            }

            initiator.sendMessage("Party", Component.text("You left the party.", MC.CC.RED.getTextColor()));
        } else {
            initiator.sendMessage("Party", Component.text("You are not in a party. Consider creating one.", MC.CC.RED.getTextColor()));
        }

    }

    @PayloadHandler
    public void onPartyRequest(PartyRequestPayload payload) {
        // TODO: WIP
    }

    @PayloadHandler
    public void onPartySummon(PartySummonPayload payload) {
        // TODO: WIP
        // TODO: ProxySendAllPayload -> You are being sent to your party leaders server.
        // TODO: Message initiator   -> You have warped your party to your current server.
    }

    @PayloadHandler
    public void onPartyTransfer(PartyTransferPayload payload) {
        // TODO: WIP
    }

}
