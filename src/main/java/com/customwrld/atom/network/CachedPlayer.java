package com.customwrld.atom.network;

import com.customwrld.atom.Atom;
import com.customwrld.atom.timers.PartyPlayerOfflineTimer;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
public class CachedPlayer {

    public static List<CachedPlayer> cachedPlayerList = new ArrayList<>();

    // TODO: USE LATEST CONVERSATION
    private final String name;
    private final UUID uuid;
    @Setter
    private Server currentServer;
    @Setter
    private Conversation latestConversation;

    public CachedPlayer(UUID uuid, String name) {
        this.name = name;
        this.uuid = uuid;
    }

    public CachedPlayer(UUID uuid, String name, Server currentServer) {
        this.name = name;
        this.uuid = uuid;
        this.currentServer = currentServer;
    }

    public void playerJoin(CachedPlayer cachedPlayer) {
        PartyPlayerOfflineTimer playerOfflineTimer = this.getPartyPlayerOfflineTimer();

        if (playerOfflineTimer != null) {
            playerOfflineTimer.stop();
        }
    }

    public void serverSwitch() {

    }

    public void playerDisconnect(CachedPlayer cachedPlayer) {
        this.endConversations();

        Party party = Party.getPartyByPlayer(cachedPlayer);

        if (party != null) {
            new PartyPlayerOfflineTimer(cachedPlayer, party);
        }
    }

    public void endConversations() {
        for (Conversation conversation : Conversation.conversationList) {
            if (conversation.getMembers().contains(this.uuid)) {
                conversation.endConversation();
            }
        }
    }

    public PartyPlayerOfflineTimer getPartyPlayerOfflineTimer() {
        return (PartyPlayerOfflineTimer) Atom.getAtom().getManager().getTimers().stream().filter(timer -> timer instanceof PartyPlayerOfflineTimer && ((PartyPlayerOfflineTimer) timer).cachedPlayer.getUuid().equals(this.uuid)).findFirst().orElse(null);
    }

    public static CachedPlayer getPlayerByUuid(UUID uuid) {
        return cachedPlayerList.stream().filter(player -> player.getUuid().equals(uuid)).findFirst().orElse(null);
    }

    public boolean isOnline() {
        return cachedPlayerList.contains(this);
    }

}
