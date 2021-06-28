package com.customwrld.atom.network;

import com.customwrld.atom.util.RecordValue;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bson.Document;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@AllArgsConstructor
public class Gamemode {

    public static MongoCollection<Document> collection;

    @Getter private static final List<Gamemode> gamemodes = Collections.synchronizedList(new ArrayList<>());

    @Getter private final String name;

    @Getter @Setter private int totalPlayers;
    @Getter @Setter private RecordValue totalPlayersRecord;

    @Getter private final List<Server> servers = Collections.synchronizedList(new ArrayList<>());

    public Gamemode(Document document) {
        this.name = document.getString("_id");
        this.totalPlayersRecord = new RecordValue((Document) document.get("totalPlayersRecord"));
    }

    public static Gamemode getByName(String name) {
        return gamemodes.stream().filter(gamemode -> gamemode.getName().equals(name)).findFirst().orElse(null);
    }

    public static void loadAll() {
        try (MongoCursor<Document> cursor = collection.find().iterator()) {
            while (cursor.hasNext()) {
                var document = cursor.next();
                load(new Gamemode(document));
            }
        }
    }

    public static boolean load(Gamemode gamemode) {
        if(getByName(gamemode.getName()) != null) { return false; }
        gamemodes.add(gamemode);
        return true;
    }

    public static boolean save(Gamemode gamemode) {
        if(!load(gamemode)) { return false; }
        collection.insertOne(new Document("_id", gamemode.getName())
                .append("totalPlayersRecord", gamemode.getTotalPlayersRecord().toDocument()));
        return true;
    }
}
