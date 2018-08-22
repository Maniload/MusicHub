package de.craplezz.musichub.net;

import de.craplezz.musichub.net.packet.CreateHubPacket;

import java.io.Closeable;
import java.io.IOException;
import java.net.Socket;
import java.util.function.Consumer;

public class Connection implements Closeable {

    private final Socket socket;

    private final PacketInputStream inputStream;
    private final PacketOutputStream outputStream;

    public Connection(Socket socket) throws IOException {
        this.socket = socket;

        inputStream = new PacketInputStream(socket.getInputStream());
        outputStream = new PacketOutputStream(socket.getOutputStream());
    }

    public void sendPacket(Packet packet) throws IOException {
        outputStream.write(packet);
    }

    public Packet waitForPacket() throws IOException {
        return inputStream.read();
    }

    public void startPacketListener(Consumer<Packet> listener) throws IOException {
        Packet packet;
        while ((packet = waitForPacket()) != null) {
            listener.accept(packet);
        }
    }

    public void close() throws IOException {
        socket.close();
    }

}