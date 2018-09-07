package de.craplezz.musichub.media;

import de.craplezz.musichub.components.Hub;
import de.craplezz.musichub.util.IOUtils;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.concurrent.*;

public class MediaPlayer {

    private final Hub hub;

    private Player player;

    public MediaPlayer(Hub hub) {
        this.hub = hub;
    }

    public void connect() {
        new Thread(() -> {

            try {
                Socket socket = new Socket("localhost", 8081);

                DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
                outputStream.writeLong(hub.getHostUserUniqueId().getLeastSignificantBits());
                outputStream.writeLong(hub.getHostUserUniqueId().getMostSignificantBits());

                while (true) {
                    try {
                        ByteArrayOutputStream bufferOut = new ByteArrayOutputStream();
                        IOUtils.copy(socket.getInputStream(), bufferOut);
                        player = new Player(new ByteArrayInputStream(bufferOut.toByteArray()));
                        player.play();
                    } catch (JavaLayerException e) {
                        e.printStackTrace();
                    } finally {
                        if (player != null) {
                            player.close();
                        }
                    }

                    outputStream.writeByte((byte) 1);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }).start();
    }

    public void skip() {
        if (player != null) {
            player.close();
        }
    }

}
