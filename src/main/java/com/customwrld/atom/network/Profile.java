package com.customwrld.atom.network;

import com.customwrld.atom.util.CollectionsUtil;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import lombok.Getter;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
public class Profile {

    public static MongoCollection<Document> collection;

    private final UUID uuid;
    private String name;
    private Grant activeGrant;
    private final List<Grant> grants;
    private final List<UUID> friends;

    private boolean loaded;

    public Profile(UUID uuid) {
        this.uuid = uuid;
        this.grants = new ArrayList<>();
        this.friends = new ArrayList<>();
        load();
    }

    public Rank getActiveRank() {
        return this.activeGrant.getRank();
    }

    public void checkGrants() {
        List<Grant> removableGrants = new ArrayList<>();
        List<Grant> activeGrants = new ArrayList<>();

        for (Grant grant : this.grants) {
            if (grant.getRank() == null || Rank.ranks.get(grant.getRank().getUuid()) == null) {
                removableGrants.add(grant);
            } else if (!grant.isRemoved() && !grant.hasExpired()) {
                activeGrants.add(grant);
            }
        }

        this.grants.removeAll(removableGrants);

        activeGrants.sort(Grant.COMPARATOR);

        if (activeGrants.isEmpty()) {
            throw new RuntimeException(this.getName() + " did not have an active grant.");
        } else {
            this.activeGrant = activeGrants.get(0);
        }
    }

    private void load() {
        Document document = collection.find(Filters.eq("uuid", this.uuid.toString())).first();

        if (document != null) {
            if (this.name == null) {
                this.name = document.getString("name");
            }

            for (Map<?, ?> obj : document.getList("grants", Map.class, new ArrayList<>())) {
                this.grants.add(Grant.fromSimple(
                        CollectionsUtil.genericMapToType(obj, String.class, String.class)));
            }

            for (String uuid : document.getList("friends", String.class)) {
                UUID friendUUID = UUID.fromString(uuid);
                this.friends.add(friendUUID);
            }

            this.checkGrants();
        }

        this.loaded = true;
    }

    public void save() {
        List<String> simpleFriends = new ArrayList<>();

        for (UUID uuid : friends) {
            simpleFriends.add(uuid.toString());
        }

        collection.updateOne(Filters.eq("uuid", this.uuid.toString()),
                Updates.combine(
                        Updates.set("friends", simpleFriends)
                ),

                new UpdateOptions().upsert(true)
        );
    }

}
