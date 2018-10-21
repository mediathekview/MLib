package de.mediathekview.mlib.tool;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * User: Christian F. Date: 15.06.16 Time: 14:03
 */
@Deprecated
public class ProgressMonitorInputStream extends FilterInputStream {
  private InputStreamProgressMonitor monitor = null;


  /**
   * The number of bytes that can be read from the InputStream.
   */
  private long size = 0;

  /**
   * The number of bytes that have been read from the InputStream.
   */
  private long bytesRead = 0;

  /**
   * Creates a <code>FilterInputStream</code> by assigning the argument <code>in</code> to the field
   * <code>this.in</code> so as to remember it for later use.
   *
   * @param in the underlying input stream, or <code>null</code> if this instance is to be created
   *        without an underlying stream.
   * @param maxSize  Maximum size to adjust the Progressbar.
   * @param mon The InputStreamProgressMonitor to Monitor the InputStream
   * @throws IOException Throws an IOException if the is Zero
   */
  public ProgressMonitorInputStream(final InputStream in, final long maxSize,
      final InputStreamProgressMonitor mon) throws IOException {
    super(in);
    monitor = mon;
    size = maxSize;
    if (size == 0) {
      throw new IOException("Size must be greater than zero!");
    }
  }

  @Override
  public int read() throws IOException {
    final int read = super.read();
    if (read != -1) {
      bytesRead++;
      if (monitor != null) {
        monitor.progress(bytesRead, size);
      }
    }
    return read;
  }

  @Override
  public int read(final byte[] b) throws IOException {
    final int read = super.read(b);
    if (read != -1) {
      bytesRead += read;
      if (monitor != null) {
        monitor.progress(bytesRead, size);
      }
    }
    return read;
  }

  @Override
  public int read(final byte[] b, final int off, final int len) throws IOException {
    final int read = super.read(b, off, len);
    if (read != -1) {
      bytesRead += read;
      if (monitor != null) {
        monitor.progress(bytesRead, size);
      }
    }
    return read;
  }
}
