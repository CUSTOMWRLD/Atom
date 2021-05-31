package com.customwrld.atom.network;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
public class Party {

    public static List<Party> partyList = new ArrayList<>();

    private final UUID uuid;
    @Setter
    private CachedPlayer leader;
    private final List<CachedPlayer> members;

    public Party(UUID uuid, CachedPlayer leader) {
        this.uuid = uuid;
        this.leader = leader;
        this.members = new ArrayList<>();
    }

    public static Party getPartyByPlayer(CachedPlayer cachedPlayer) {
        return partyList.stream().filter(party -> party.getMembers().contains(cachedPlayer)).findFirst().orElse(null);
    }

    public boolean addMember(CachedPlayer cachedPlayer) {
        if (members.contains(cachedPlayer))
            return false;

        members.add(cachedPlayer);
        return true;
    }

    public boolean removeMember(CachedPlayer cachedPlayer) {
        if (!members.contains(cachedPlayer))
            return false;

        members.remove(cachedPlayer);
        return true;
    }

    public void sendPartyMessage(String message) {
        // PartyMessagePayload
    }

    public void disbandParty() {
        // TODO: Include a reason
        // PartyMessagePayload

        partyList.remove(this);
    }

}
