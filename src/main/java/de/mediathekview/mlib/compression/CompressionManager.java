package de.mediathekview.mlib.compression;

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
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.tukaani.xz.LZMA2Options;
import org.tukaani.xz.XZInputStream;
import org.tukaani.xz.XZOutputStream;

/**
 * A util class to work with compressed files.
 */
public class CompressionManager {
  private static CompressionManager instance;

  private CompressionManager() {
    super();
  }

  public static CompressionManager getInstance() {
    if (instance == null) {
      instance = new CompressionManager();
    }
    return instance;
  }

  public OutputStream compress(final CompressionType aCompressionType,
      final OutputStream aOutputStream) throws IOException {
    switch (aCompressionType) {
      case XZ:
        return new XZOutputStream(aOutputStream, new LZMA2Options());
      case GZIP:
        return new GZIPOutputStream(aOutputStream);
      default:
        throw new IllegalArgumentException(
            String.format("The type \"%s\" isn't supported.", aCompressionType.name()));
    }
  }

  /**
   * Compresses a file and uses the old name and appends the file appender based on the
   * {@link CompressionType}.
   *
   * @param aCompressionType The type in which should be compressed.
   * @param aSourceFile The file to compress.
   * @return true if all worked fine
   */
  public void compress(final CompressionType aCompressionType, final Path aSourceFile)
      throws IOException {
    compress(aCompressionType, aSourceFile, aSourceFile);
  }

  /**
   * Compresses a file.
   *
   * @param aCompressionType The type in which should be compressed.
   * @param aSourceFile The file to compress.
   * @param aTargetPath The path for the compressed file.
   * @return true if all worked fine
   */
  public void compress(final CompressionType aCompressionType, final Path aSourceFile,
      final Path aTargetPath) throws IOException {
    final Path targetPath =
        aTargetPath.getFileName().toString().endsWith(aCompressionType.getFileEnding())
            ? aTargetPath
            : aTargetPath.resolveSibling(
                aTargetPath.getFileName().toString() + aCompressionType.getFileEnding());

    try (InputStream input = new BufferedInputStream(Files.newInputStream(aSourceFile));
        final OutputStream output = compress(aCompressionType, Files.newOutputStream(targetPath))) {
      fastChannelCopy(Channels.newChannel(input), Channels.newChannel(output));
    }
  }

  /**
   * Uncompresses a {@link InputStream}.
   *
   * @param aCompressionType The type in which should be compressed.
   * @param aInputStream The {@link InputStream} to uncompress.
   * @return A uncompressed {@link InputStream}
   */
  public InputStream decompress(final CompressionType aCompressionType,
      final InputStream aInputStream) throws IOException {
    switch (aCompressionType) {
      case XZ:
        return new XZInputStream(aInputStream);
      case GZIP:
        return new GZIPInputStream(aInputStream);
      default:
        throw new IllegalArgumentException(
            String.format("The type \"%s\" isn't supported.", aCompressionType.name()));
    }
  }

  /**
   * Decompresses a file and uses the old name and removes the file appender based on the
   * {@link CompressionType}.
   *
   * @param aCompressionType The type in which should be compressed.
   * @param aSourceFile
   * @return
   */
  public void decompress(final CompressionType aCompressionType, final Path aSourceFile)
      throws IOException {
    decompress(aCompressionType, aSourceFile, aSourceFile.resolveSibling(
        aSourceFile.getFileName().toString().replace(aCompressionType.getFileEnding(), "")));
  }

  /**
   * Decompresses a file.
   *
   * @param aCompressionType The type in which should be compressed.
   * @param aSourceFile The file to decompress.
   * @param aTargetPath The path for the decompressed file.
   * @return true if all worked fine
   */
  public void decompress(final CompressionType aCompressionType, final Path aSourceFile,
      final Path aTargetPath) throws IOException {
    try (final InputStream input = decompress(aCompressionType, Files.newInputStream(aSourceFile));
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
