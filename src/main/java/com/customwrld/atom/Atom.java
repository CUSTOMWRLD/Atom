package com.customwrld.atom;

import com.customwrld.atom.network.*;
import com.customwrld.atom.pigeon.Listeners;

import com.customwrld.atom.pigeon.listeners.PartyListener;
import com.customwrld.atom.util.Logger;
import com.customwrld.atom.util.timer.TimerManager;
import com.customwrld.commonlib.CommonLib;
import com.customwrld.pigeon.Pigeon;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import lombok.Getter;

@Getter
public class Atom {

    // TODO: ADD FLAME PROFILES
    // TODO: Cached Guilds
    // TODO: Friend Requests
    // TODO: Partys
    // TODO: Cooldowns
    // TODO: Timers

    @Getter
    private static Atom atom;

    private final Gson gson;

    private final TimerManager manager;

    private MongoClient mongoClient;
    private MongoDatabase mongoDatabase;

    private Pigeon pigeon;

    public Atom() {
        long start = System.currentTimeMillis();

        Logger.log(Logger.Level.INFO, "Starting the initialization process of Atom.");

        atom = this;

        this.gson = new GsonBuilder().create();

        this.manager = new TimerManager();

        new Thread(() -> {
            while (true) {
                try {
                    Server.serverList.removeIf(server -> (System.currentTimeMillis() - server.getLastUpdated()) >= (1000 * 30));
                    Thread.sleep(5000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

        loadMongo();
        loadCollections();
        loadPigeon();

        new CommonLib(this.mongoClient, this.mongoDatabase, this.pigeon);

        Gamemode.loadAll();

        Logger.log(Logger.Level.INFO, "Done (" + (System.currentTimeMillis() - start) + "ms)! Atom has successfully been initialized!");
    }

    private void loadMongo() {
        this.mongoClient = new MongoClient("127.0.0.1", 27017);

        this.mongoDatabase = mongoClient.getDatabase("CUSTOMWRLD");
    }

    private void loadPigeon() {
        this.pigeon = new Pigeon();

        this.pigeon.initialize("127.0.0.1", 5672, "customwrld", "atom");

        this.pigeon.getConvertersRegistry()
                .registerConvertersInPackage("com.customwrld.atom.pigeon.converters");

        this.pigeon.getPayloadsRegistry()
                .registerPayloadsInPackage("com.customwrld.atom.pigeon.payloads");

        this.pigeon.getListenersRegistry().registerListener(new Listeners());
        this.pigeon.getListenersRegistry().registerListener(new PartyListener());

        this.pigeon.setupDefaultUpdater();

        this.pigeon.acceptDelivery();
    }

    private void loadCollections() {
        Gamemode.collection = this.mongoDatabase.getCollection("gamemodes");
    }

}
