package de.craplezz.musichub.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public final class IOUtils {

    private IOUtils() {}

    public static void copy(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[8096];
        int bytesRead;
        while ((bytesRead = in.read(buffer)) >= 0) {
            out.write(buffer, 0, bytesRead);
            out.flush();
        }
        out.close();
    }

}
