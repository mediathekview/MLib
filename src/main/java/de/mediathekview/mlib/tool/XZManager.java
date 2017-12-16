package de.mediathekview.mlib.tool;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import org.tukaani.xz.LZMA2Options;
import org.tukaani.xz.XZInputStream;
import org.tukaani.xz.XZOutputStream;

/**
 * A util class to work with XZ compressed files.
 */
public class XZManager {
  private static final String XZ_FILE_ENDING = ".xz";
  private static XZManager instance;

  private XZManager() {
    super();
  }

  public static XZManager getInstance() {
    if (instance == null) {
      instance = new XZManager();
    }
    return instance;
  }

  /**
   * Compresses a file and uses the old name and appends ".xz".
   *
   * @param aSourceFile The file to compress.
   * @return true if all worked fine
   */
  public void compress(final Path aSourceFile) throws IOException {
    compress(aSourceFile, aSourceFile);
  }

  /**
   * Compresses a file.
   *
   * @param aSourceFile The file to compress.
   * @param aTargetPath The path for the compressed file.
   * @return true if all worked fine
   */
  public void compress(final Path aSourceFile, final Path aTargetPath) throws IOException {
    final Path targetPath =
        aTargetPath.getFileName().toString().endsWith(XZ_FILE_ENDING) ? aTargetPath
            : aTargetPath.resolveSibling(aTargetPath.getFileName().toString() + XZ_FILE_ENDING);

    try (InputStream input = new BufferedInputStream(Files.newInputStream(aSourceFile));
        final OutputStream output =
            new XZOutputStream(Files.newOutputStream(targetPath), new LZMA2Options())) {
      fastChannelCopy(Channels.newChannel(input), Channels.newChannel(output));
    }
  }

  /**
   * Uncompresses a {@link InputStream}.
   *
   * @param aInputStream The {@link InputStream} to uncompress.
   * @return A uncompressed {@link InputStream}
   */
  public InputStream decompress(final InputStream aInputStream) throws IOException {
    return new XZInputStream(aInputStream);
  }

  /**
   * Decompresses a file and uses the old name and removes ".xz".
   *
   * @param aSourceFile
   * @return
   */
  public void decompress(final Path aSourceFile) throws IOException {
    decompress(aSourceFile, aSourceFile
        .resolveSibling(aSourceFile.getFileName().toString().replace(XZ_FILE_ENDING, "")));
  }

  /**
   * Decompresses a file.
   *
   * @param aSourceFile The file to decompress.
   * @param aTargetPath The path for the decompressed file.
   * @return true if all worked fine
   */
  public void decompress(final Path aSourceFile, final Path aTargetPath) throws IOException {
    try (final InputStream input = new XZInputStream(Files.newInputStream(aSourceFile));
        OutputStream output = new BufferedOutputStream(Files.newOutputStream(aTargetPath))) {
      fastChannelCopy(Channels.newChannel(input), Channels.newChannel(output));
    }
  }

  private void fastChannelCopy(final ReadableByteChannel src, final WritableByteChannel dest)
      throws IOException {
    final ByteBuffer buffer = ByteBuffer.allocateDirect(64 * 1024);
    while (src.read(buffer) != -1) {
      buffer.flip();
      dest.write(buffer);
      buffer.compact();
    }

    buffer.flip();

    while (buffer.hasRemaining()) {
      dest.write(buffer);
    }
  }
}
