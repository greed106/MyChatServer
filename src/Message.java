import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public abstract class Message implements Serializable {
    protected String uidSender;
    protected String uidReceiver;
    protected String message;
    protected String type;

    public String getUidSender() {
        return uidSender;
    }

    public String getUidReceiver() {
        return uidReceiver;
    }

    public String getMessage() {
        return message;
    }

    public String getType() {
        return type;
    }

    public Message(String uidSender, String uidReceiver, String message, String type) {
        this.uidSender = uidSender;
        this.uidReceiver = uidReceiver;
        this.message = message;
        this.type = type;
    }
    @Override
    public String toString(){
        return uidSender+"\n\t"+message;
    }
}
class SendChatMessage extends Message{
    protected LocalDateTime currentTime;

    public SendChatMessage(String uidSender, String uidReceiver, String message, String type, LocalDateTime currentTime) {
        super(uidSender, uidReceiver, message, type);
        this.currentTime = currentTime;
    }

    public String getCurrentTime() {
        return currentTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    @Override
    public String toString(){
        return uidSender+" "+getCurrentTime()+"\n\t"+message;
    }
}
class ReadChatMessage extends Message{
    protected LocalDateTime currentTime;

    public ReadChatMessage(String uidSender, String uidReceiver, String message, String type, LocalDateTime currentTime) {
        super(uidSender, uidReceiver, message, type);
        this.currentTime = currentTime;
    }
    public ReadChatMessage(SendChatMessage mes) {
        super(mes.uidSender, mes.uidReceiver, mes.message, "ReadChatMessage");
        this.currentTime = mes.currentTime;
    }

    @Override
    public String toString(){
        return uidSender+" "+currentTime+"\n\t"+message;
    }
}
class ExitMessage extends Message{
    public ExitMessage(String uidSender, String uidReceiver, String message, String type) {
        super(uidSender, uidReceiver, message, type);
    }
}
class ErrorMessage extends Message{
    public ErrorMessage(String uidSender, String uidReceiver, String message, String type) {
        super(uidSender, uidReceiver, message, type);
    }
}
class isErrorMessage extends Message{
    protected boolean isError;
    public isErrorMessage(String uidSender, String uidReceiver, String message, String type, boolean isError) {
        super(uidSender, uidReceiver, message, type);
        this.isError = isError;
    }

    public boolean isError() {
        return isError;
    }
}
class CheckUIDMessage extends Message{
    public CheckUIDMessage(String uidSender, String uidReceiver, String message, String type) {
        super(uidSender, uidReceiver, message, type);
    }
}
class CreatClientMessage extends Message{
    public CreatClientMessage(String uidSender, String uidReceiver, String message, String type) {
        super(uidSender, uidReceiver, message, type);
    }

}