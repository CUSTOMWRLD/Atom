package com.customwrld.atom.network;

import com.customwrld.atom.Atom;
import com.customwrld.atom.managers.ProfilesManager;
import com.customwrld.atom.timers.PartyPlayerOfflineTimer;
import com.customwrld.commonlib.modules.pigeon.payloads.conversation.ConversationFromPayload;
import com.customwrld.commonlib.modules.pigeon.payloads.conversation.ConversationToPayload;
import com.customwrld.commonlib.modules.pigeon.payloads.minecraft.MessagePlayerPayload;
import com.customwrld.commonlib.modules.profile.Profile;
import com.customwrld.commonlib.util.MC;
import com.customwrld.commonlib.util.PigeonUtil;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;

import java.util.*;
import java.util.concurrent.ExecutionException;

@Getter
@Setter
public class Player {

    @Getter private static final Map<UUID, Player> cachedPlayerList = new HashMap<>();

    private final String name;
    private final UUID uuid;
    private final Map<UUID, PartyInvite> partyInvites;
    private String currentServer;
    private Player lastMessaged;

    public Player(UUID uuid, String name) {
        this.name = name;
        this.uuid = uuid;
        this.partyInvites = new HashMap<>();
    }

    public Player(UUID uuid, String name, String currentServer) {
        this.name = name;
        this.uuid = uuid;
        this.currentServer = currentServer;
        this.partyInvites = new HashMap<>();
    }

    public PartyInvite getPartyInvite(UUID partyUUID) {
        for(PartyInvite invite : this.partyInvites.values()) {
            if(invite.getPartyUUID().equals(partyUUID)) {
                return invite;
            }
        }

        return null;
    }

    public PartyInvite getPlayerPartyInvite(UUID playerUUID) {
        return this.partyInvites.get(playerUUID);
    }

    public boolean isInParty() {
        for(Party party : Party.getPartyList()) {
            if(party.getMembers().contains(this.getUuid())) {
                return true;
            }
        }

        return false;
    }

    public void addPartyInvite(UUID partyUUID, UUID inviterUUID) {
        // TODO: Check if they have receiving party invites toggled

        new Thread(() -> {
            try {
                final ProfilesManager profilesManager = Atom.getAtom().getProfilesManager();

                Profile initiatorProfile = profilesManager.getProfile(inviterUUID).get();
                Profile targetProfile = profilesManager.getProfile(this.uuid).get();
                Player target = Player.getPlayerByUuid(this.uuid);

                if(initiatorProfile != null || targetProfile != null || target != null) {
                    Atom.getAtom().getPigeon().broadcast(new MessagePlayerPayload(inviterUUID, MC.Chat.notificationMessage("Party",
                        Component.text("Unable to invite that player to the party.", MC.CC.GRAY.getTextColor()))));

                    return;
                }

                Party party = Party.getPartyByUUID(partyUUID);

                if(party != null) {
                    party.sendPartyMessage(Component.text()
                            .append(initiatorProfile.api().getChatFormat())
                            .append(Component.text(" has invited ", MC.CC.GRAY.getTextColor()))
                            .append(targetProfile.api().getChatFormat())
                            .append(Component.text(" to the party!", MC.CC.GRAY.getTextColor()))
                            .build());
                }

                Player.sendNotification(this.uuid, "Party", Component.text()
                        .append(initiatorProfile.api().getChatFormat())
                        .append(Component.text(" has invited you to join their party. You have 60 seconds to accept.", MC.CC.GRAY.getTextColor()))
                        .build());

                this.partyInvites.put(inviterUUID, new PartyInvite(partyUUID, inviterUUID, this.uuid));
            } catch(ExecutionException | InterruptedException e) {
                Thread.currentThread().interrupt();
                e.printStackTrace();
            }
        }).start();

    }

    public void sendConversationMessage(Player target, String message) {
        PigeonUtil.broadcast(new ConversationToPayload(this.uuid, target.getUuid(), message));
        PigeonUtil.broadcast(new ConversationFromPayload(this.uuid, target.getUuid(), message));

        this.lastMessaged = target;
        target.setLastMessaged(this);
    }

    public void reply(String message) {
        PigeonUtil.broadcast(new ConversationToPayload(this.uuid, this.lastMessaged.getUuid(), message));
        PigeonUtil.broadcast(new ConversationFromPayload(this.uuid, this.lastMessaged.getUuid(), message));

        this.lastMessaged.setLastMessaged(this);
    }

    public static void sendNotification(UUID player, String prefix, Component message) {
        PigeonUtil.broadcast(new MessagePlayerPayload(player, MC.Chat.notificationMessage(prefix, message)));
    }

    public static void sendMessage(UUID player, Component message) {
        PigeonUtil.broadcast(new MessagePlayerPayload(player, message));
    }

    public PartyPlayerOfflineTimer getPartyPlayerOfflineTimer() {
        return (PartyPlayerOfflineTimer) Atom.getAtom().getTimerManager().getTimers().stream().filter(timer -> timer instanceof PartyPlayerOfflineTimer && ((PartyPlayerOfflineTimer) timer).getPlayer().equals(this.uuid)).findFirst().orElse(null);
    }

    public static Player getPlayerByUuid(UUID uuid) {
        return cachedPlayerList.get(uuid);
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof Player && ((Player) object).getUuid().equals(this.uuid);
    }

}
