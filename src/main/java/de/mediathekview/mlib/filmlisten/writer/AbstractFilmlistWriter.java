package de.mediathekview.mlib.filmlisten.writer;

import de.mediathekview.mlib.daten.Filmlist;
import de.mediathekview.mlib.messages.LibMessages;
import de.mediathekview.mlib.messages.MessageCreator;
import de.mediathekview.mlib.messages.listener.MessageListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;

public abstract class AbstractFilmlistWriter extends MessageCreator {
  private static final Logger LOG = LogManager.getLogger(AbstractFilmlistWriter.class);

  public AbstractFilmlistWriter() {
    super();
  }

  public AbstractFilmlistWriter(final MessageListener... aListeners) {
    super(aListeners);
  }

  public abstract boolean write(Filmlist filmlist, OutputStream outputStream) throws IOException;

  public boolean write(Filmlist filmlist, Path savePath) {
    try {
      return write(filmlist, new FileOutputStream(savePath.toFile()));
    } catch (final IOException ioException) {
      LOG.debug("Something went wrong on writing the film list.", ioException);
      publishMessage(LibMessages.FILMLIST_WRITE_ERROR, savePath.toAbsolutePath().toString());
      return false;
    }
  }
}
