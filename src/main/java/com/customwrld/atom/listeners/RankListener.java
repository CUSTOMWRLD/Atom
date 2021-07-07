package com.customwrld.atom.listeners;

import com.customwrld.commonlib.modules.pigeon.payloads.rank.RankReloadPayload;
import com.customwrld.commonlib.modules.pigeon.payloads.rank.RankRemovePayload;
import com.customwrld.commonlib.modules.rank.Rank;
import com.customwrld.pigeon.annotations.PayloadHandler;
import com.customwrld.pigeon.annotations.PayloadListener;
import com.customwrld.pigeon.listeners.Listener;

import java.util.UUID;

@PayloadListener
public class RankListener implements Listener {

    @PayloadHandler
    public void onReloadRank(RankReloadPayload payload) {
        Rank rank = Rank.getRank(payload.getRank(), false);

        Rank.getRanks().remove(payload.getRank());

        if (rank != null) {
            Rank.getRanks().put(rank.getUuid(), rank);
        }
    }

    @PayloadHandler
    public void onRemoveRank(RankRemovePayload payload) {
        UUID rank = payload.getRank();

        Rank.getRanks().remove(rank);
    }

}
