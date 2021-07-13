package com.customwrld.atom.timers;

import com.customwrld.atom.Atom;
import com.customwrld.atom.managers.ProfilesManager;
import com.customwrld.atom.network.Party;
import com.customwrld.atom.network.PartyInvite;
import com.customwrld.atom.network.Player;
import com.customwrld.atom.util.timer.api.Timer;
import com.customwrld.atom.util.timer.api.TimerType;
import com.customwrld.commonlib.modules.profile.Profile;
import com.customwrld.commonlib.util.MC;
import lombok.Getter;
import net.kyori.adventure.text.Component;

import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Getter
public class PartyInviteTimer extends Timer {

    private final UUID partyUUID;
    private final UUID playerUUID;

    public PartyInviteTimer(UUID partyUUID, UUID playerUUID) {
        super(TimerType.COUNTDOWN, Atom.getAtom().getTimerManager());
        setDuration(TimeUnit.MINUTES.toMillis(1));
        this.partyUUID = partyUUID;
        this.playerUUID = playerUUID;
    }

    @Override
    public void onSecond() {}

    @Override
    public void onStart() {}

    @Override
    public void onComplete() {
        new Thread(() -> {
            try {
                final ProfilesManager profilesManager = Atom.getAtom().getProfilesManager();

                Party party = Party.getPartyByUUID(this.partyUUID);
                Player player = Player.getPlayerByUuid(this.playerUUID);

                Profile profile = profilesManager.getProfile(this.playerUUID).get();
                if(profile == null) { return; }
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
                        Profile inviter = profilesManager.getProfile(invite.getInviterUUID()).get();

                        Player.sendNotification(this.playerUUID, "Party", Component.text()
                                .append(Component.text("The party invite from ", MC.CC.GRAY.getTextColor()))
                                .append(inviter.api().getChatFormat())
                                .append(Component.text(" has expired.", MC.CC.GRAY.getTextColor()))
                                .build());

                        player.getPartyInvites().remove(invite.getInviterUUID());
                    }
                }
            } catch(ExecutionException | InterruptedException e) {
                Thread.currentThread().interrupt();
                e.printStackTrace();
            }
        });
    }

    @Override
    public void onCancel() {}

}
