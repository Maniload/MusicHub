package de.craplezz.musichub.media;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;

import java.io.*;
import java.util.Map;
import java.util.concurrent.*;

public class MediaPlayer {

    private final ExecutorService executor = Executors.newCachedThreadPool();

    private final Listener listener;

    private Player player;
    private BlockingQueue<String> urlQueue = new LinkedBlockingQueue<>();
    private Map<String, Future<ByteArrayInputStream>> preLoadedData = new ConcurrentHashMap<>();

    public MediaPlayer(Listener listener) {
        this.listener = listener;
    }

    public void append(String url) {
        preLoadedData.put(url, executor.submit(() -> fetchData(url)));

        urlQueue.add(url);
    }

    public void remove(String url) {
        preLoadedData.remove(url);

        urlQueue.remove(url);
    }

    public void skip() {
        if (player != null) {
            player.close();
        }
    }

    public void play() throws InterruptedException {
        while (true) {
            String url = urlQueue.take();
            listener.onPlayerStart(url);
            try {
                System.out.println("Starting to play " + url);
                long time = System.currentTimeMillis();
                player = new Player(preLoadedData.remove(url).get());
                System.out.println("Started playing after " + (System.currentTimeMillis() - time) + " ms");
                player.play();
                player.close();
            } catch (JavaLayerException | ExecutionException e) {
                e.printStackTrace();
            }
            listener.onPlayerFinish(url);
        }
    }

    private ByteArrayInputStream fetchData(String url) throws IOException {
        Process downloadProcess = new ProcessBuilder()
                .command(("youtube-dl -f bestaudio -o - " + url).split(" "))
                .redirectInput(ProcessBuilder.Redirect.PIPE)
                .start();

        Process encodeProcess = new ProcessBuilder()
                .command("ffmpeg -i pipe: -f mp3 pipe:1".split(" "))
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .redirectInput(ProcessBuilder.Redirect.PIPE)
                .start();

        executor.execute(() -> {

            try {
                copy(downloadProcess.getInputStream(), encodeProcess.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }

        });

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        copy(encodeProcess.getInputStream(), outputStream);

        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
        outputStream.close();

        return inputStream;
    }

    private void copy(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[8096];
        int bytesRead;
        while ((bytesRead = in.read(buffer)) >= 0) {
            out.write(buffer, 0, bytesRead);
            out.flush();
        }
        out.close();
    }

    public interface Listener {

        void onPlayerStart(String url);

        void onPlayerFinish(String url);

    }

}
