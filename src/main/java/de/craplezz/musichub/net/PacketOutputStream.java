package de.craplezz.musichub.net;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class PacketOutputStream {

    private final DataOutputStream outputStream;

    public PacketOutputStream(OutputStream outputStream) {
        this.outputStream = new DataOutputStream(outputStream);
    }

    public void write(Packet packet) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        DataOutputStream outputStream = new DataOutputStream(buffer);
        packet.write(outputStream);

        this.outputStream.write(packet.getId());
        this.outputStream.writeInt(buffer.size());
        buffer.writeTo(this.outputStream);
        outputStream.flush();
    }

}
