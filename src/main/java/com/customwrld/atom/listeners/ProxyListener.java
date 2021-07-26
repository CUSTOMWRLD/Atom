package com.customwrld.atom.listeners;

import com.customwrld.atom.Atom;
import com.customwrld.atom.network.Party;
import com.customwrld.atom.network.Player;
import com.customwrld.atom.timers.PartyPlayerOfflineTimer;
import com.customwrld.commonlib.modules.network.Server;
import com.customwrld.commonlib.modules.pigeon.payloads.minecraft.MessagePlayerPayload;
import com.customwrld.commonlib.modules.pigeon.payloads.network.ProxyPlayerConnectPayload;
import com.customwrld.commonlib.modules.pigeon.payloads.network.ProxyPlayerDisconnectPayload;
import com.customwrld.commonlib.modules.pigeon.payloads.network.ProxyPlayerSwitchPayload;
import com.customwrld.commonlib.modules.pigeon.payloads.network.ServerUpdatePayload;
import com.customwrld.commonlib.util.MC;
import com.customwrld.commonlib.util.PigeonUtil;
import com.customwrld.pigeon.annotations.PayloadHandler;
import com.customwrld.pigeon.annotations.PayloadListener;
import com.customwrld.pigeon.listeners.Listener;
import net.kyori.adventure.text.Component;

import java.util.UUID;

@PayloadListener
public class ProxyListener implements Listener {

    @PayloadHandler
    public void onServerUpdate(ServerUpdatePayload payload) {
        new Server(payload.getName(), payload.getGamemode(), payload.getUptime(), payload.getTps(), payload.getPlayers(), payload.getMaxPlayers()).updateServer();
    }

    @PayloadHandler
    public void onProxyPlayerConnect(ProxyPlayerConnectPayload payload) {
        UUID uuid = payload.getUuid();
        Player player = Player.getPlayerByUuid(payload.getUuid());

        if (player == null) {
            player = new Player(payload.getUuid(), payload.getPlayer(), payload.getServer());
        }

        PartyPlayerOfflineTimer playerOfflineTimer = player.getPartyPlayerOfflineTimer();

        if (playerOfflineTimer != null) {
            playerOfflineTimer.stop();
        }

        Atom.getAtom().getProfilesManager()
                .getProfile(uuid)
                .thenAccept(profile -> {
                    if(profile == null) { return; }
                    if (profile.api().getAllPermissions().contains("flame.staff")) {
                        PigeonUtil.broadcast(new MessagePlayerPayload("flame.staff", Component.text()
                                .append(profile.api().getChatFormat())
                                .append(Component.text(" has connected to ", MC.CC.GRAY.getTextColor()))
                                .append(Component.text(payload.getServer(), MC.CC.WHITE.getTextColor()))
                                .build()));
                    }
                });

        Player.getCachedPlayerList().put(uuid, player);
    }

    @PayloadHandler
    public void onProxyPlayerSwitch(ProxyPlayerSwitchPayload payload) {
        UUID uuid = payload.getUuid();
        Player player = Player.getPlayerByUuid(payload.getUuid());

        if(player == null) {
            player = new Player(payload.getUuid(), payload.getPlayer(), payload.getServerTo());
        }

        PartyPlayerOfflineTimer playerOfflineTimer = player.getPartyPlayerOfflineTimer();

        if (playerOfflineTimer != null) {
            playerOfflineTimer.stop();
        }

        Atom.getAtom().getProfilesManager()
                .getProfile(uuid)
                .thenAccept(profile -> {
                    if (profile == null) { return; }
                    if (profile.api().getAllPermissions().contains("flame.staff")) {
                        PigeonUtil.broadcast(new MessagePlayerPayload("flame.staff", Component.text()
                                .append(profile.api().getChatFormat())
                                .append(Component.text(" has switched to ", MC.CC.GRAY.getTextColor()))
                                .append(Component.text(payload.getServerTo(), MC.CC.WHITE.getTextColor()))
                                .append(Component.text(" from ", MC.CC.GRAY.getTextColor()))
                                .append(Component.text(payload.getServerFrom(), MC.CC.WHITE.getTextColor()))
                                .build()));
                    }
                });

        player.setCurrentServer(payload.getServerTo());

        Player.getCachedPlayerList().put(uuid, player);
    }

    @PayloadHandler
    public void onProxyDisconnect(ProxyPlayerDisconnectPayload payload) {
        Player player = Player.getPlayerByUuid(payload.getUuid());
        Player.getCachedPlayerList().remove(payload.getUuid());

        if(player != null) {
            Party party = Party.getPartyByMember(payload.getUuid());

            if(party != null) {
                PartyPlayerOfflineTimer timer = new PartyPlayerOfflineTimer(payload.getUuid());
                timer.start();
            }
        }

        Atom.getAtom().getProfilesManager()
                .getProfile(payload.getUuid())
                .thenAccept(profile -> {
                    if (profile == null) { return; }
                    if (profile.api().getAllPermissions().contains("flame.staff")) {
                        PigeonUtil.broadcast(new MessagePlayerPayload("flame.staff", Component.text()
                                .append(profile.api().getChatFormat())
                                .append(Component.text(" disconnected from ", MC.CC.GRAY.getTextColor()))
                                .append(Component.text(payload.getServer(), MC.CC.WHITE.getTextColor()))
                                .build()));
                    }
                });

    }

}
