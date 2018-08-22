package de.craplezz.musichub.net.packet;

import de.craplezz.musichub.net.Packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class SkipPacket extends Packet {

    public SkipPacket() {}

    public SkipPacket(DataInputStream inputStream) {}

    @Override
    public void write(DataOutputStream outputStream) throws IOException {}

    @Override
    public byte getId() {
        return 7;
    }

}
