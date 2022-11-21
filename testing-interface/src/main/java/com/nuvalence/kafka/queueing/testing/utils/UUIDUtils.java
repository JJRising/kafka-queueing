package com.nuvalence.kafka.queueing.testing.utils;

import com.google.protobuf.ByteString;

import java.nio.ByteBuffer;
import java.util.UUID;

public class UUIDUtils {

    public static UUID uuidFromBytes(ByteString bytes) {
        ByteBuffer bb = ByteBuffer.wrap(bytes.toByteArray());
        long l1 = bb.getLong();
        long l2 = bb.getLong();
        return new UUID(l1, l2);
    }

    public static ByteString bytesFromUUID(UUID uuid) {
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return ByteString.copyFrom(bb.array());
    }
}
