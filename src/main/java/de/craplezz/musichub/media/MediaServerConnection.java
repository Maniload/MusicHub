package de.craplezz.musichub.media;

import de.craplezz.musichub.components.Hub;
import de.craplezz.musichub.components.User;
import de.craplezz.musichub.net.packet.UpdateEntryListPacket;
import de.craplezz.musichub.util.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

public class MediaServerConnection {

    private static final MediaResolver RESOLVER = new MediaResolver();

    private final Hub hub;

    private BlockingQueue<String> urlQueue = new LinkedBlockingQueue<>();
    private Map<String, Future<ByteArrayInputStream>> preLoadedData = new ConcurrentHashMap<>();

    public MediaServerConnection(Hub hub) {
        this.hub = hub;
    }

    public void handle(DataInputStream inputStream, DataOutputStream outputStream) {
        while (true) {
            try {
                String url = urlQueue.take();

                System.out.println("Starting to play " + url);
                long time = System.currentTimeMillis();
                ByteArrayInputStream data = preLoadedData.remove(url).get();
                System.out.println("Started playing after " + (System.currentTimeMillis() - time) + " ms");

                for (User user : hub.getUsers()) {
                    user.getConnection().sendPacket(new UpdateEntryListPacket(new UpdateEntryListPacket.Change(
                            UpdateEntryListPacket.Change.Mode.NOW_PLAYING,
                            url
                    )));
                }

                byte[] buffer = new byte[8096];
                int bytesRead;
                while ((bytesRead = data.read(buffer)) >= 0) {
                    outputStream.write(buffer, 0, bytesRead);
                    outputStream.flush();
                }

                inputStream.readByte();
            } catch (InterruptedException | ExecutionException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void append(String url) {
        preLoadedData.put(url, RESOLVER.resolve(url));

        urlQueue.add(url);
    }

    public void remove(String url) {
        preLoadedData.remove(url);

        urlQueue.remove(url);
    }

}
