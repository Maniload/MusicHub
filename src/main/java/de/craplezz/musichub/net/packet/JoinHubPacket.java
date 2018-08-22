package de.craplezz.musichub.net.packet;

import de.craplezz.musichub.net.Packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;

public class JoinHubPacket extends Packet {

    private final UUID hostUserUniqueId;

    public JoinHubPacket(UUID hostUserUniqueId) {
        this.hostUserUniqueId = hostUserUniqueId;
    }

    public JoinHubPacket(DataInputStream inputStream) throws IOException {
        hostUserUniqueId = new UUID(inputStream.readLong(), inputStream.readLong());
    }

    public UUID getHostUserUniqueId() {
        return hostUserUniqueId;
    }

    @Override
    public void write(DataOutputStream outputStream) throws IOException {
        outputStream.writeLong(hostUserUniqueId.getLeastSignificantBits());
        outputStream.writeLong(hostUserUniqueId.getMostSignificantBits());
    }

    @Override
    public byte getId() {
        return 1;
    }

}
