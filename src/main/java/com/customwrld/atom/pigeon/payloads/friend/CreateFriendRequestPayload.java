package com.customwrld.atom.pigeon.payloads.friend;

import com.customwrld.pigeon.annotations.Payload;
import com.customwrld.pigeon.annotations.Transmit;
import com.customwrld.pigeon.payloads.bases.BasePayload;
import lombok.Getter;

import java.util.UUID;

@Getter
@Payload
public class CreateFriendRequestPayload extends BasePayload {

    public CreateFriendRequestPayload() {
        this.payloadId = "minecraft-createFriendRequest";
    }

    @Transmit
    UUID initiator;
    @Transmit
    UUID target;

    public CreateFriendRequestPayload(UUID initiator, UUID target) {
        this();
        this.initiator = initiator;
        this.target = target;
    }

    @Override
    public void receive() {
    }

}
