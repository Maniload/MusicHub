package de.craplezz.musichub.media;

import de.craplezz.musichub.util.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MediaResolver {

    private final ExecutorService executor = Executors.newCachedThreadPool();

    public Future<ByteArrayInputStream> resolve(String url) {
        return executor.submit(() -> {

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
                    IOUtils.copy(downloadProcess.getInputStream(), encodeProcess.getOutputStream());
                } catch (IOException e) {
                    e.printStackTrace();
                }

            });

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            IOUtils.copy(encodeProcess.getInputStream(), outputStream);

            ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
            outputStream.close();

            return inputStream;

        });
    }

}
