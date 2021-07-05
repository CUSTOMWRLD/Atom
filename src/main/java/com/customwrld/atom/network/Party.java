package com.customwrld.atom.network;

import com.customwrld.atom.Atom;
import com.customwrld.atom.pigeon.payloads.MessagePlayerPayload;
import com.customwrld.commonlib.modules.profile.Profile;
import com.customwrld.commonlib.util.MC;
import com.customwrld.pigeon.Pigeon;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;

import java.util.*;

@Getter
public class Party {

    @Getter private static final List<Party> partyList = new ArrayList<>();

    private final UUID uuid;
    private final List<UUID> members;
    private UUID leader;
    private final Long createdAt;

    public Party(UUID leader) {
        this.uuid = UUID.randomUUID();
        this.leader = leader;
        this.members = new ArrayList<>(Collections.singletonList(leader));
        this.createdAt = System.currentTimeMillis();
        partyList.add(this);
    }

    public static Party getPartyByUUID(UUID uuid) {
        return partyList.stream().filter(party -> party.getUuid().equals(uuid)).findFirst().orElse(null);
    }

    public static Party getPartyByMember(UUID uuid) {
        return partyList.stream().filter(party -> party.getMembers().contains(uuid)).findFirst().orElse(null);
    }

    public void setLeader(UUID uuid) {
        this.leader = uuid;
    }

//    public boolean inviteMember(CachedPlayer player) {
//        Pigeon pigeon = Atom.getAtom().getPigeon();
//        Profile profile = Profile.getProfile(player.getUuid());
//
//        if (player.getPartyInvites().contains(this.leader.getUuid())) {
//
//        }
//
//        if (profile != null) {
//            sendPartyMessage(Component.text()
//                    .append(profile.api().getChatFormat())
//                    .append(Component.text(" has been invited to the party!", MC.CC.GREEN.getTextColor()))
//                    .build());
//        } else {
//            this.getLeader().sendMessage("Error", Component.text("Something went wrong while loading your profile.", MC.CC.RED.getTextColor()));
//        }
//
//        // TODO Check if they are already invited
//        // TODO: Invite Code
//        return true;
//    }

    public void addMember(CachedPlayer player) {
        UUID uuid = player.getUuid();

        if (this.members.contains(uuid)) {
            player.sendMessage("Party", Component.text("You are already in that party.", MC.CC.RED.getTextColor()));
            return;
        }

        this.members.add(uuid);

        Profile profile = Profile.getProfile(uuid);

        if (profile != null) {
            this.sendPartyMessage(Component.text()
                    .append(profile.api().getChatFormat())
                    .append(Component.text(" has joined the party.", MC.CC.GREEN.getTextColor()))
                    .build());
        }
    }

    public void removeMember(UUID uuid) {
        this.getMembers().remove(uuid);
    }

    public void sendPartyMessage(UUID initiator, String message) {
        Pigeon pigeon = Atom.getAtom().getPigeon();

        Profile profile = Profile.getProfile(initiator);

        if (profile != null) {
            for (UUID uuid : this.getMembers()) {
                pigeon.broadcast(new MessagePlayerPayload(uuid, MC.Chat.notificationMessage("Party",
                        Component.text()
                                .append(profile.api().getChatFormat())
                                .append(Component.text(": ", MC.CC.GRAY.getTextColor()))
                                .append(Component.text(message))
                                .build())));
            }
        }
    }

    public void sendPartyMessage(Component message) {
        Pigeon pigeon = Atom.getAtom().getPigeon();

        for (UUID uuid : this.getMembers()) {
            pigeon.broadcast(new MessagePlayerPayload(uuid, MC.Chat.notificationMessage("Party", message)));
        }
    }

    public void disbandParty(String reason) {
        this.sendPartyMessage(Component.text(reason, MC.CC.RED.getTextColor()));

        partyList.remove(this);
    }

}
