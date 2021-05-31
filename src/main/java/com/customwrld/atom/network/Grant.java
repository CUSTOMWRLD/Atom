package com.customwrld.atom.network;

import lombok.Getter;
import lombok.Setter;

import java.util.Comparator;
import java.util.Map;
import java.util.UUID;

@Getter
public class Grant {

    public static Comparator<Grant> COMPARATOR = (rank1, rank2) -> rank2.getRank().getWeight() - rank1.getRank().getWeight();

    private final UUID uuid;
    private final Rank rank;
    private final UUID addedBy;
    private final long addedAt;
    private final String addedReason;
    private final long duration;
    @Setter
    private UUID removedBy;
    @Setter
    private long removedAt;
    @Setter
    private String removedReason;
    @Setter
    private boolean removed;

    public Grant(UUID uuid, Rank rank, UUID addedBy, long addedAt, String addedReason, long duration) {
        this.uuid = uuid;
        this.rank = rank;
        this.addedBy = addedBy;
        this.addedAt = addedAt;
        this.addedReason = addedReason;
        this.duration = duration;
    }

    public boolean isPermanent() {
        return duration == Integer.MAX_VALUE;
    }

    public boolean hasExpired() {
        return (!isPermanent()) && (System.currentTimeMillis() >= addedAt + duration);
    }

    public static Grant fromSimple(Map<String, String> simple) {
        Grant grant = new Grant(UUID.fromString(simple.get("uuid")),
                (!simple.get("rank").equals("null") ? Rank.getRankByName(simple.get("rank")) : null),
                (!simple.get("addedBy").equals("null") ? UUID.fromString(simple.get("addedBy")) : null),
                Long.parseLong(simple.get("addedAt")),
                simple.get("addedReason"),
                Long.parseLong(simple.get("duration"))
        );

        String isRemoved = simple.get("removed");
        if (isRemoved != null && isRemoved.equals("true")) {
            grant.setRemoved(true);
            grant.setRemovedAt(Long.parseLong(simple.get("removedAt")));
            grant.setRemovedBy(!simple.get("removedBy").equals("null") ? UUID.fromString(simple.get("removedBy")) : null);
            grant.setRemovedReason(simple.get("removedReason"));
        }

        return grant;
    }

}