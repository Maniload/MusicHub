package de.craplezz.musichub.media;

import de.craplezz.musichub.MusicHubServer;
import de.craplezz.musichub.components.Hub;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.UUID;

public class MediaServer {

    private final MusicHubServer hubServer;

    private ServerSocket server;

    public MediaServer(MusicHubServer hubServer) {
        this.hubServer = hubServer;
    }

    public void start() {
        try {
            server = new ServerSocket(8081, 0);

            while (true) {
                Socket socket = server.accept();
                new Thread(() -> {

                    try {
                        DataInputStream inputStream = new DataInputStream(socket.getInputStream());
                        DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());

                        UUID hubUniqueId = new UUID(inputStream.readLong(), inputStream.readLong());
                        Hub hub = hubServer.getHub(hubUniqueId);

                        MediaServerConnection connection = new MediaServerConnection(hub);

                        hub.setMediaServerConnection(connection);

                        connection.handle(inputStream, outputStream);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
