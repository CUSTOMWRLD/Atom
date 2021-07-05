package com.customwrld.atom.timers;

import com.customwrld.atom.Atom;
import com.customwrld.atom.network.CachedPlayer;
import com.customwrld.atom.network.Party;
import com.customwrld.atom.network.PartyInvite;
import com.customwrld.atom.pigeon.payloads.MessagePlayerPayload;
import com.customwrld.atom.util.Util;
import com.customwrld.atom.util.timer.api.Timer;
import com.customwrld.atom.util.timer.api.TimerType;
import com.customwrld.commonlib.modules.profile.Profile;
import com.customwrld.commonlib.util.MC;
import com.customwrld.pigeon.Pigeon;
import com.google.common.cache.Cache;
import lombok.Getter;
import net.kyori.adventure.text.Component;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Getter
public class PartyInviteTimer extends Timer {

    private final UUID partyUUID;
    private final UUID playerUUID;

    public PartyInviteTimer(UUID partyUUID, UUID playerUUID) {
        super(TimerType.COUNTDOWN, Atom.getAtom().getManager());
        setDuration(TimeUnit.SECONDS.toMillis(10));
        this.partyUUID = partyUUID;
        this.playerUUID = playerUUID;
    }

    @Override
    public void onSecond() {}

    @Override
    public void onStart() {}

    @Override
    public void onComplete() {
        Party party = Party.getPartyByUUID(this.partyUUID);
        Profile profile = Profile.getProfile(this.playerUUID);
        CachedPlayer player = CachedPlayer.getPlayerByUuid(this.playerUUID);

        if(profile != null) {
            if(party != null) {
                party.sendPartyMessage(Component.text()
                        .append(Component.text("The party invite to ", MC.CC.GRAY.getTextColor()))
                        .append(profile.api().getChatFormat())
                        .append(Component.text(" has expired.", MC.CC.GRAY.getTextColor()))
                        .build());
            }

            if(player != null) {
                PartyInvite invite = player.getPartyInvite(this.partyUUID);

                if(invite != null) {
                    Profile inviter = Profile.getProfile(invite.getInviterUUID());

                    if(inviter != null) {
                        player.sendMessage("Party", Component.text()
                                .append(Component.text("The party invite from ", MC.CC.GRAY.getTextColor()))
                                .append(inviter.api().getChatFormat())
                                .append(Component.text(" has expired.", MC.CC.GRAY.getTextColor()))
                                .build());
                    }

                    player.getPartyInvites().remove(invite.getInviterUUID());
                }
            }
        }
    }

    @Override
    public void onCancel() {}

}
