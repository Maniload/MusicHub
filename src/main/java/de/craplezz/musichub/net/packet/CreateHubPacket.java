package de.craplezz.musichub.net.packet;

import de.craplezz.musichub.net.Packet;

import java.io.*;

public class CreateHubPacket extends Packet {

    private final String displayName;

    public CreateHubPacket(String displayName) {
        this.displayName = displayName;
    }

    public CreateHubPacket(DataInputStream inputStream) throws IOException {
        displayName = inputStream.readUTF();
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public void write(DataOutputStream outputStream) throws IOException {
        outputStream.writeUTF(displayName);
    }

    @Override
    public byte getId() {
        return 0;
    }

}
