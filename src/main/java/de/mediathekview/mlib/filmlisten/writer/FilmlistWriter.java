package de.mediathekview.mlib.filmlisten.writer;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ConcurrentSkipListSet;

import de.mediathekview.mlib.daten.Filmlist;
import de.mediathekview.mlib.messages.Message;
import de.mediathekview.mlib.messages.listener.MessageListener;

public abstract class FilmlistWriter {
   private Collection<MessageListener> messageListeners;
   
   public FilmlistWriter()
   {
       super();
       messageListeners = new ConcurrentSkipListSet<>();
   }
   
   public FilmlistWriter(MessageListener... aListeners)
   {
       this();
       messageListeners.addAll(Arrays.asList(aListeners));
   }
   
   public boolean addMessageListener(MessageListener aMessageListener)
   {
       return messageListeners.add(aMessageListener);
   }
   
   public boolean removeMessageListener(MessageListener aMessageListener)
   {
       return messageListeners.remove(aMessageListener);
   }
   
   public boolean addAllMessageListener(Collection<MessageListener> aMessageListeners)
   {
       return messageListeners.addAll(aMessageListeners);
   }
   
   public boolean removeAllMessageListener(Collection<MessageListener> aMessageListeners)
   {
       return messageListeners.removeAll(aMessageListeners);
   }
   
   protected void publishMessage(Message aMessage, Object... aParams)
   {
       messageListeners.parallelStream().forEach(l -> l.consumeMessage(aMessage,aParams));
   }
   
   public abstract boolean write(Filmlist aFilmlist, Path aSavePath);
}