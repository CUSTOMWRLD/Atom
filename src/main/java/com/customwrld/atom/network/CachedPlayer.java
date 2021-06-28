package com.customwrld.atom.network;

import com.customwrld.atom.Atom;
import com.customwrld.atom.pigeon.payloads.conversation.ConversationFromPayload;
import com.customwrld.atom.pigeon.payloads.conversation.ConversationToPayload;
import com.customwrld.atom.timers.PartyPlayerOfflineTimer;
import com.customwrld.pigeon.Pigeon;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter
public class CachedPlayer {

    @Getter private static final Map<UUID, CachedPlayer> cachedPlayerList = new HashMap<>();

    private final String name;
    private final UUID uuid;
    @Setter private String currentServer;
    @Setter private CachedPlayer lastMessaged;

    public CachedPlayer(UUID uuid, String name) {
        this.name = name;
        this.uuid = uuid;
    }

    public CachedPlayer(UUID uuid, String name, String currentServer) {
        this.name = name;
        this.uuid = uuid;
        this.currentServer = currentServer;
    }

    public void sendMessage(CachedPlayer target, String message) {
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

    public void playerJoin(CachedPlayer cachedPlayer) {

    }

    public void serverSwitch() {

    }

    public void playerDisconnect(CachedPlayer cachedPlayer) {
        Party party = Party.getPartyByPlayer(cachedPlayer);

        if (party != null) {
            new PartyPlayerOfflineTimer(cachedPlayer, party);
        }
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
