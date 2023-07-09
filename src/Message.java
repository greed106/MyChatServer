import java.io.Serializable;
import java.time.LocalDateTime;

public class Message implements Serializable {
    protected String uidSender;
    protected  String uidReceiver;
    protected String message;

    public Message(String uidSender, String uidReceiver, String message) {
        this.uidSender = uidSender;
        this.uidReceiver = uidReceiver;
        this.message = message;
    }

    public Message() { }

    public String getUidSender() {
        return uidSender;
    }

    public String getUidReceiver() {
        return uidReceiver;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString(){
        return uidSender+"\n\t"+message;
    }
}
class ChatMessage extends Message{
    protected LocalDateTime currentTime;
    public ChatMessage(String uidSender, String uidReceiver, String message, LocalDateTime currentTime) {
        super(uidSender, uidReceiver, message);
        this.currentTime = currentTime;
    }

    @Override
    public String toString(){
        return uidSender+" "+currentTime.toString()+"\n\t"+message;
    }
}
