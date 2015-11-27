package monash.infotech.monashp2pdatasync.messaging;

import com.google.gson.Gson;

import monash.infotech.monashp2pdatasync.entities.Peer;

/**
 * Created by john on 11/27/2015.
 */
public class Message {
    private MessageType type;
    private Peer sender;
    private Peer reciver;
    private String msgBody;
    private int msgId;

    public static Message fromJson(String json)
    {
        Gson g=new Gson();
        return (Message)g.fromJson(json,Message.class);
    }

    public String toJson()
    {
        Gson g=new Gson();
        return g.toJson(this);
    }

    public Message(int msgId,MessageType type, Peer sender, Peer reciver, String msgBody) {
        this.msgId=msgId;
        this.type = type;
        this.sender = sender;
        this.reciver = reciver;
        this.msgBody = msgBody;
    }
    public Message(int mssgId)
    {
        this.msgId=mssgId;
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public Peer getSender() {
        return sender;
    }

    public void setSender(Peer sender) {
        this.sender = sender;
    }

    public Peer getReciver() {
        return reciver;
    }

    public void setReciver(Peer reciver) {
        this.reciver = reciver;
    }

    public String getMsgBody() {
        return msgBody;
    }

    public void setMsgBody(String msgBody) {
        this.msgBody = msgBody;
    }

    public int getMsgId() {
        return msgId;
    }

    public void setMsgId(int msgId) {
        this.msgId = msgId;
    }
}
