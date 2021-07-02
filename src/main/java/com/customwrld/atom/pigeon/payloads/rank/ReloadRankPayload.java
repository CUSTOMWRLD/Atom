package com.customwrld.atom.pigeon.payloads.rank;

import com.customwrld.commonlib.modules.rank.Rank;
import com.customwrld.pigeon.annotations.Payload;
import com.customwrld.pigeon.annotations.Transmit;
import com.customwrld.pigeon.payloads.bases.BasePayload;
import lombok.Getter;

import java.util.UUID;

@Getter @Payload
public class ReloadRankPayload extends BasePayload {

    @Transmit UUID rank;

    public ReloadRankPayload() {
        payloadId = "flame-reloadRank";
    }

    public ReloadRankPayload(Rank rank) {
        this();
        this.rank = rank.getUuid();
    }

    @Override
    public void receive() {
    }

}
