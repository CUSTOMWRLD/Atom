package com.customwrld.atom.network;

import com.customwrld.atom.Atom;
import com.customwrld.atom.pigeon.payloads.MessagePlayerPayload;
import com.customwrld.atom.util.Util;
import com.customwrld.pigeon.Pigeon;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
public class Conversation {

    public static List<Conversation> conversationList = new ArrayList<>();

    public List<UUID> members;
    private long lastMessageSentAt;

    public Conversation(List<UUID> members) {
        this.members = members;
        this.lastMessageSentAt = System.currentTimeMillis();

        this.members.forEach(uuid -> CachedPlayer.getPlayerByUuid(uuid).setLatestConversation(this));

        conversationList.add(this);
    }

    public static Conversation getConversation(List<UUID> members) {
        for (Conversation conversation : Conversation.conversationList) {
            if (conversation.getMembers().containsAll(members)) {
                return conversation;
            }
        }
        return null;
    }

    public void sendMessage(UUID initiator, UUID target, String message) {
        Pigeon pigeon = Atom.getAtom().getPigeon();

        pigeon.broadcast(new MessagePlayerPayload(initiator, "&7(To " + Util.getColoredName(new Profile(target)) + "&7) " + message));
        pigeon.broadcast(new MessagePlayerPayload(target, "&7(From " + Util.getColoredName(new Profile(initiator)) + "&7) " + message));

        this.members.forEach(uuid -> CachedPlayer.getPlayerByUuid(uuid).setLatestConversation(this));

        this.lastMessageSentAt = System.currentTimeMillis();
    }

    public boolean hasConversationExpired() {
        return (System.currentTimeMillis() - this.lastMessageSentAt) >= (1000 * 60 * 60);
    }

    public boolean areRecipientsOnline() {
        return CachedPlayer.cachedPlayerList.stream().map(CachedPlayer::getUuid).collect(Collectors.toList()).containsAll(members);
    }

    public boolean validate() {
        return !hasConversationExpired() && areRecipientsOnline();
    }

    public void endConversation() {
        conversationList.remove(this);
    }

}