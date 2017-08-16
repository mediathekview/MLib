package de.mediathekview.mlib.messages;

public enum LibMessages  implements Message
{
    FILMLIST_WRITE_ERROR("filmlistWritieError",MessageTypes.FATAL_ERROR);

    private String messageKey;
    private MessageTypes messageType;

    LibMessages(String aMessageKey, MessageTypes aMessageType)
    {
        messageKey = aMessageKey;
        messageType = aMessageType;
    }

    @Override
    public String getMessageKey()
    {
        return messageKey;
    }

    @Override
    public MessageTypes getMessageType()
    {
        return messageType;
    }
}