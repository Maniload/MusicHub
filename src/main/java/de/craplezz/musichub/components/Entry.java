package de.craplezz.musichub.components;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Entry {

    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool();

    private final String url;

    private String title;
    private Future<?> titleFuture;

    public Entry(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void fetchData(Runnable onFinishListener) {
        if (titleFuture != null) {
            if (titleFuture.isDone()) {
                onFinishListener.run();
            } else {
                EXECUTOR.execute(() -> {

                    try {
                        titleFuture.get();
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }

                    onFinishListener.run();

                });
            }
        } else {
            titleFuture = EXECUTOR.submit(() -> {

                try {
                    Process process = new ProcessBuilder()
                            .command(("youtube-dl -e " + url).split(" "))
                            .redirectInput(ProcessBuilder.Redirect.PIPE)
                            .start();

                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

                    byte[] buffer = new byte[8096];
                    int bytesRead;
                    while ((bytesRead = process.getInputStream().read(buffer)) >= 0) {
                        outputStream.write(buffer, 0, bytesRead);
                    }

                    title = outputStream.toString();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                onFinishListener.run();

            });
        }
    }

    @Override
    public String toString() {
        return titleFuture != null && titleFuture.isDone() ? title : url;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Entry && ((Entry) o).getUrl().equals(url);
    }

}
