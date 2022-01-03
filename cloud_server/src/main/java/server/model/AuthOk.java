package server.model;

public class AuthOk implements AbstractMessage{
    @Override
    public MessageType getMessageType() {
        return MessageType.AUTH_OK;
    }
}
