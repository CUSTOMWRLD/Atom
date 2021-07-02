package com.customwrld.atom.pigeon.payloads;

import com.customwrld.atom.network.Gamemode;
import com.customwrld.atom.network.Server;
import com.customwrld.pigeon.annotations.Payload;
import com.customwrld.pigeon.annotations.Transmit;
import com.customwrld.pigeon.payloads.bases.BasePayload;
import lombok.Getter;

import java.util.List;

@Getter @Payload
public class ServerUpdatePayload extends BasePayload {

    @Transmit String name;
    @Transmit Gamemode gamemode;
    @Transmit Long uptime;
    @Transmit Double tps;
    @Transmit List<String> players;
    @Transmit Integer maxPlayers;

    public ServerUpdatePayload() {
        payloadId = "server-serverUpdate";
    }

    public ServerUpdatePayload(String name, Gamemode gamemode, Long uptime, Double tps, List<String> players, Integer maxPlayers) {
        this();
        this.name = name;
        this.gamemode = gamemode;
        this.uptime = uptime;
        this.tps = tps;
        this.players = players;
        this.maxPlayers = maxPlayers;
    }

    @Override
    public void receive() {
    }

}
