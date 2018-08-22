package de.craplezz.musichub.net;

import java.io.*;

public class PacketInputStream {

    private final DataInputStream inputStream;

    public PacketInputStream(InputStream inputStream) {
        this.inputStream = new DataInputStream(inputStream);
    }

    public Packet read() throws IOException {
        byte id = inputStream.readByte();
        int length = inputStream.readInt();
        byte[] data = new byte[length];
        if (inputStream.read(data) != length) {
            throw new EOFException();
        }

        return PacketRegistry.getInstance().create(id, new DataInputStream(new ByteArrayInputStream(data)));
    }

}
