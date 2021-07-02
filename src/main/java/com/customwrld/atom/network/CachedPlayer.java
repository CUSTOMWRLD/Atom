package com.customwrld.atom.network;

import com.customwrld.atom.Atom;
import com.customwrld.atom.pigeon.payloads.MessagePlayerPayload;
import com.customwrld.atom.pigeon.payloads.conversation.ConversationFromPayload;
import com.customwrld.atom.pigeon.payloads.conversation.ConversationToPayload;
import com.customwrld.atom.timers.PartyPlayerOfflineTimer;
import com.customwrld.commonlib.modules.profile.Profile;
import com.customwrld.commonlib.util.MC;
import com.customwrld.pigeon.Pigeon;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;

import java.util.*;

@Getter
public class CachedPlayer {

    @Getter private static final Map<UUID, CachedPlayer> cachedPlayerList = new HashMap<>();

    private final String name;
    private final UUID uuid;
    @Setter private Set<UUID> partyInvites;
    @Setter private String currentServer;
    @Setter private CachedPlayer lastMessaged;

    public CachedPlayer(UUID uuid, String name) {
        this.name = name;
        this.uuid = uuid;
        this.partyInvites = new HashSet<>();
    }

    public CachedPlayer(UUID uuid, String name, String currentServer) {
        this.name = name;
        this.uuid = uuid;
        this.currentServer = currentServer;
        this.partyInvites = new HashSet<>();
    }

    public boolean isInParty() {
        for(Party party : Party.getPartyList()) {
            if(party.getMembers().contains(this.getUuid())) {
                return true;
            }
        }

        return false;
    }

    public void addPartyInvite(CachedPlayer from) {
        if(this.partyInvites.contains(from.getUuid())) {
            return;
        }
        // TODO: Check if they have receiving party invites toggled

        Profile toProfile = Profile.getProfile(from.getUuid());

        if(toProfile != null) {
            this.partyInvites.add(from.getUuid());
            this.sendMessage("Party", Component.text()
                    .append(toProfile.api().getChatFormat())
                    .append(Component.text(" has invited you to join their party.", MC.CC.GRAY.getTextColor()))
                    .build());
        }

        Profile fromProfile = Profile.getProfile(this.getUuid());

        if(fromProfile != null) {
            from.sendMessage("Party", Component.text()
                    .append(Component.text("You've invited ", MC.CC.GRAY.getTextColor()))
                    .append(fromProfile.api().getChatFormat())
                    .append(Component.text(" to your party", MC.CC.GRAY.getTextColor()))
                    .build());
        }
    }

    public void sendMessage(String prefix, Component message) {
        Pigeon pigeon = Atom.getAtom().getPigeon();

        pigeon.broadcast(new MessagePlayerPayload(this.uuid, MC.Chat.notificationMessage(prefix, message)));
    }

    public void sendConversationMessage(CachedPlayer target, String message) {
        Pigeon pigeon = Atom.getAtom().getPigeon();

        pigeon.broadcast(new ConversationToPayload(this.uuid, target.getUuid(), message));
        pigeon.broadcast(new ConversationFromPayload(this.uuid, target.getUuid(), message));

        this.lastMessaged = target;
        target.setLastMessaged(this);
    }

    public void reply(String message) {
        Pigeon pigeon = Atom.getAtom().getPigeon();

        pigeon.broadcast(new ConversationToPayload(this.uuid, this.lastMessaged.getUuid(), message));
        pigeon.broadcast(new ConversationFromPayload(this.uuid, this.lastMessaged.getUuid(), message));

        this.lastMessaged.setLastMessaged(this);
    }

    public PartyPlayerOfflineTimer getPartyPlayerOfflineTimer() {
        return (PartyPlayerOfflineTimer) Atom.getAtom().getManager().getTimers().stream().filter(timer -> timer instanceof PartyPlayerOfflineTimer && ((PartyPlayerOfflineTimer) timer).getCachedPlayer().getUuid().equals(this.uuid)).findFirst().orElse(null);
    }

    public static CachedPlayer getPlayerByUuid(UUID uuid) {
        return cachedPlayerList.get(uuid);
    }

    public boolean isOnline() {
        return cachedPlayerList.containsKey(this.uuid);
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof CachedPlayer && ((CachedPlayer) object).getUuid().equals(this.uuid);
    }

}
