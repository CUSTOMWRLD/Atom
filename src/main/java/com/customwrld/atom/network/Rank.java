package com.customwrld.atom.network;

import com.customwrld.atom.util.ChatColor;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import lombok.Getter;
import org.bson.Document;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
public class Rank {

    public static MongoCollection<Document> collection;

    public static Comparator<Rank> COMPARATOR = (rank1, rank2) -> rank2.getWeight() - rank1.getWeight();

    public static final Map<UUID, Rank> ranks = new HashMap<>();

    private final UUID uuid;
    private final String name;
    private ChatColor color;
    private String prefix;
    private int weight;

    public Rank(String name) {
        this.uuid = UUID.randomUUID();
        this.name = name;
        this.color = ChatColor.WHITE;
        this.weight = 0;

        ranks.put(uuid, this);
    }

    public Rank(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;

        ranks.put(uuid, this);
    }

    public static Rank getRankByUuid(UUID uuid) {
        return ranks.get(uuid);
    }

    public static Rank getRankByName(String name) {
        for (Rank rank : ranks.values()) {
            if (rank.getName().equalsIgnoreCase(name)) {
                return rank;
            }
        }

        return null;
    }

    public static void init() {
        try (MongoCursor<Document> cursor = collection.find().iterator()) {
            while (cursor.hasNext()) {
                Document document = cursor.next();

                Rank rank = new Rank(UUID.fromString(document.getString("uuid")), document.getString("name"));
                rank.load(document);

                ranks.put(rank.getUuid(), rank);
            }
        }
    }

    private void load(Document document) {
        if (document == null) {
            return;
        }

        color = ChatColor.valueOf(document.getString("color"));
        prefix = document.getString("prefix");
        weight = document.getInteger("weight");
    }

}
