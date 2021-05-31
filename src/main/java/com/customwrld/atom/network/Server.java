package com.customwrld.atom.network;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class Server {

    public static List<Server> serverList = new ArrayList<>();

    private final String name;
    private final Server.Type type;
    private final Long uptime;
    private final Double tps;
    private final Long lastUpdated;
    private final List<String> players;

    public Server(String name, Server.Type type, Long uptime, Double tps, List<String> players) {
        this.name = name;
        this.type = type;
        this.uptime = uptime;
        this.tps = tps;
        this.players = players;
        this.lastUpdated = System.currentTimeMillis();
    }

    public static Server getServerByName(String name) {
        return serverList.stream().filter(server -> server.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public void updateServer() {
        serverList.removeIf(server -> server.getName().equals(this.name));
        serverList.add(this);
    }

    @Getter
    @AllArgsConstructor
    public enum Type {
        LOBBY("Lobbies", "lobby"),
        SURVIVAL("Survival", "survival"),
        SKYBLOCK("SkyBlock", "skyblock"),
        PRACTICE("Practice", "practice"),
        SKYWARS("SkyWars", "sw"),
        WOOLWARS("WoolWars", "ww"),
        BEDWARS("BedWars", "bw");

        private final String display;
        private final String prefix;

    }

}
