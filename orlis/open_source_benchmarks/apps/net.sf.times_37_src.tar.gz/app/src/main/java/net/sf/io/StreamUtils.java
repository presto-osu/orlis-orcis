package net.sf.io;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class StreamUtils {

    private static final int BUFFER_SIZE = 1024;

    private StreamUtils() {
    }

    /**
     * Read all the bytes from the input stream.
     *
     * @param in
     *         the input.
     * @return the array of bytes.
     * @throws IOException
     *         if an I/O error occurs.
     */
    public static ByteArrayOutputStream readFully(InputStream in) throws IOException {
        in = new BufferedInputStream(in);
        return readFully(in, Math.max(in.available(), 32));
    }

    /**
     * Read all the bytes from the input stream.
     *
     * @param in
     *         the input.
     * @param size
     *         the initial buffer size.
     * @return the array of bytes.
     * @throws IOException
     *         if an I/O error occurs.
     */
    public static ByteArrayOutputStream readFully(InputStream in, int size) throws IOException {
        size = Math.max(size, 32);
        ByteArrayOutputStream out = new ByteArrayOutputStream(size);
        final byte[] buf = new byte[BUFFER_SIZE];
        int count = in.read(buf);
        while (count >= 0) {
            out.write(buf, 0, count);
            count = in.read(buf);
        }
        out.close();
        return out;
    }
}
