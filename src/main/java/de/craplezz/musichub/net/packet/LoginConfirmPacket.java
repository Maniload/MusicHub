package de.craplezz.musichub.net.packet;

import de.craplezz.musichub.net.Packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;

public class LoginConfirmPacket extends Packet {

    private UUID uniqueId;

    public LoginConfirmPacket(UUID uniqueId) {
        this.uniqueId = uniqueId;
    }

    public LoginConfirmPacket(DataInputStream inputStream) throws IOException {
        uniqueId = new UUID(inputStream.readLong(), inputStream.readLong());
    }

    public UUID getUniqueId() {
        return uniqueId;
    }

    @Override
    public void write(DataOutputStream outputStream) throws IOException {
        outputStream.writeLong(uniqueId.getLeastSignificantBits());
        outputStream.writeLong(uniqueId.getMostSignificantBits());
    }

    @Override
    public byte getId() {
        return 6;
    }

}
