package com.customwrld.atom;

import com.customwrld.atom.listeners.*;
import com.customwrld.atom.network.*;

import com.customwrld.atom.util.Logger;
import com.customwrld.atom.util.timer.TimerManager;
import com.customwrld.commonlib.CommonLib;
import com.customwrld.pigeon.Pigeon;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import lombok.Getter;

import java.util.Arrays;

@Getter
public class Atom {

    // TODO: Friend Requests
    // TODO: Cooldowns

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
        loadPigeon();

        new CommonLib(this.mongoClient, this.mongoDatabase, this.pigeon);

        Logger.log(Logger.Level.INFO, "Done (" + (System.currentTimeMillis() - start) + "ms)! Atom has successfully been initialized!");
    }

    private void loadMongo() {
        this.mongoClient = new MongoClient("127.0.0.1", 27017);

        this.mongoDatabase = mongoClient.getDatabase("CUSTOMWRLD");
    }

    private void loadPigeon() {
        this.pigeon = new Pigeon();

        this.pigeon.initialize("127.0.0.1", 5672, "customwrld", "atom");

        Arrays.asList(
                new ConversationListener(),
                new FriendListener(),
                new PartyListener(),
                new ProxyListener(),
                new RankListener()
        ).forEach(listener -> this.pigeon.getListenersRegistry().registerListener(listener));

        this.pigeon.setupDefaultUpdater();

        this.pigeon.acceptDelivery();
    }

}
