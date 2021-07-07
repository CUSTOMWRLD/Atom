package com.customwrld.atom.listeners;

import com.customwrld.atom.network.Player;
import com.customwrld.commonlib.modules.pigeon.payloads.conversation.ConversationMessagePayload;
import com.customwrld.commonlib.modules.pigeon.payloads.conversation.ConversationReplyPayload;
import com.customwrld.commonlib.modules.profile.Profile;
import com.customwrld.commonlib.util.MC;
import com.customwrld.pigeon.annotations.PayloadHandler;
import com.customwrld.pigeon.annotations.PayloadListener;
import com.customwrld.pigeon.listeners.Listener;
import net.kyori.adventure.text.Component;

import java.util.UUID;

@PayloadListener
public class ConversationListener implements Listener {

    @PayloadHandler
    public void onConversationMessage(ConversationMessagePayload payload) {
        UUID initiatorUUID = payload.getInitiator();
        Player initiator = Player.getPlayerByUuid(initiatorUUID);
        Player target = Player.getPlayerByUuid(payload.getTarget());

        String message = payload.getMessage();

        if (initiator == null) {
            Player.sendMessage(initiatorUUID,
                    Component.text("An error has occurred, please try rejoining the network.", MC.CC.RED.getTextColor()));
            return;
        }

        if (target == null) {
            Player.sendMessage(initiatorUUID,
                    Component.text("That player is currently not online.", MC.CC.RED.getTextColor()));
            return;
        }

        initiator.sendConversationMessage(target, message);
    }

    @PayloadHandler
    public void onConversationReply(ConversationReplyPayload payload) {
        UUID initiatorUUID = payload.getInitiator();
        Player initiator = Player.getPlayerByUuid(initiatorUUID);
        String message = payload.getMessage();

        if (initiator == null) {
            Player.sendMessage(initiatorUUID,
                    Component.text("An error has occurred, please try rejoining the network.", MC.CC.RED.getTextColor()));
            return;
        }

        Player lastMessaged = initiator.getLastMessaged();

        if (lastMessaged == null) {
            Player.sendMessage(initiatorUUID,
                    Component.text("You haven't started a conversation with anybody.", MC.CC.RED.getTextColor()));
            return;
        }

        if (Player.getPlayerByUuid(lastMessaged.getUuid()) == null) {
            Player.sendMessage(initiatorUUID,
                    Component.text("That player is currently not online.", MC.CC.RED.getTextColor()));
            return;
        }

        Profile targetProfile = Profile.getProfile(lastMessaged.getUuid());

        if (targetProfile == null) {
            Player.sendMessage(initiatorUUID,
                    Component.text("Could not resolve player information.", MC.CC.RED.getTextColor()));
            return;
        }

        Profile initiatorProfile = Profile.getProfile(initiator.getUuid());

        if (initiatorProfile == null) {
            Player.sendMessage(initiatorUUID,
                    Component.text("Failed to load your profile. Try again later.", MC.CC.RED.getTextColor()));
            return;
        }

        if (targetProfile.api().isIgnoring(initiatorProfile) || !targetProfile.getOptionsProfile().isReceivingConversations()) {
            Player.sendMessage(initiatorUUID,
                    Component.text("That player is not receiving new conversations right now.", MC.CC.RED.getTextColor()));
            return;
        }

        initiator.reply(message);
    }

}
