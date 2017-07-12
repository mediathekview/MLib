package de.mediathekview.mlib.messages;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * A util class to read messages from system.
 */
public final class MessageUtil
{
    private static final Logger LOG = LogManager.getLogger(MessageUtil.class);
    private static final String DEFAULT_BUNDLE_NAME = "MediathekView_Messages";
    private static final String MESSAGE_NOT_IN_BUNDLE_ERROR_TEXT_TEMPLATE = "Tried to load the message with the key \"%s\" from the resource bundle \"%s\" but the bundle doesn't contains a message with this key.";
    private static MessageUtil instance;

    public static MessageUtil getInstance()
    {
        if (instance == null)
        {
            instance = new MessageUtil();
        }
        return instance;
    }

    private MessageUtil()
    {
        super();
    }

    public String loadMessageText(Message aMessage, String aBundleName, Locale aLocale)
    {
        ResourceBundle resourceBundle = ResourceBundle.getBundle(aBundleName, aLocale);
        try
        {
            return resourceBundle.getString(aMessage.getMessageKey());
        } catch (MissingResourceException missingResourceException)
        {
            LOG.fatal(
                    String.format(MESSAGE_NOT_IN_BUNDLE_ERROR_TEXT_TEMPLATE, aMessage.getMessageKey(), aBundleName),
                    missingResourceException);
            throw missingResourceException;
        }
    }

    public String loadMessageText(Message aMessage, String aBundleName)
    {
        return loadMessageText(aMessage,aBundleName,Locale.getDefault());
    }

    public String loadMessageText(Message aMessage)
    {
        return loadMessageText(aMessage,DEFAULT_BUNDLE_NAME);
    }
}
