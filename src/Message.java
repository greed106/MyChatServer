import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public abstract class Message implements Serializable {
    protected String nameSender;
    protected String nameReceiver;
    protected String message;
    protected String type;

    public String getNameSender() {
        return nameSender;
    }

    public String getNameReceiver() {
        return nameReceiver;
    }

    public String getMessage() {
        return message;
    }

    public String getType() {
        return type;
    }

    public Message(String nameSender, String nameReceiver, String message, String type) {
        this.nameSender = nameSender;
        this.nameReceiver = nameReceiver;
        this.message = message;
        this.type = type;
    }
    @Override
    public String toString(){
        return nameSender +"\n\t"+message;
    }
}
class SendChatMessage extends Message{
    protected LocalDateTime currentTime;

    public SendChatMessage(String nameSender, String nameReceiver, String message, String type, LocalDateTime currentTime) {
        super(nameSender, nameReceiver, message, type);
        this.currentTime = currentTime;
    }

    public String getCurrentTime() {
        return currentTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    @Override
    public String toString(){
        return nameSender +" "+getCurrentTime()+"\n\t"+message;
    }
}
class ReadChatMessage extends Message{
    protected LocalDateTime currentTime;

    public ReadChatMessage(String nameSender, String nameReceiver, String message, String type, LocalDateTime currentTime) {
        super(nameSender, nameReceiver, message, type);
        this.currentTime = currentTime;
    }
    public ReadChatMessage(SendChatMessage mes) {
        super(mes.nameSender, mes.nameReceiver, mes.message, "ReadChatMessage");
        this.currentTime = mes.currentTime;
    }

    public String getCurrentTime() {
        return currentTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    @Override
    public String toString(){
        return nameSender +" "+getCurrentTime()+"\n\t"+message;
    }
}
class ExitMessage extends Message{
    public ExitMessage(String nameSender, String nameReceiver, String message, String type) {
        super(nameSender, nameReceiver, message, type);
    }
}
class ErrorMessage extends Message{
    public ErrorMessage(String nameSender, String nameReceiver, String message, String type) {
        super(nameSender, nameReceiver, message, type);
    }
}
class isErrorMessage extends Message{
    protected boolean isError;
    public isErrorMessage(String nameSender, String nameReceiver, String message, String type, boolean isError) {
        super(nameSender, nameReceiver, message, type);
        this.isError = isError;
    }
    @Override
    public String toString(){
        return nameSender +": "+message;
    }
    public boolean isError() {
        return isError;
    }
}
class CheckNameMessage extends Message{
    public CheckNameMessage(String nameSender, String nameReceiver, String message, String type) {
        super(nameSender, nameReceiver, message, type);
    }
}
class CheckUserMessage extends Message{
    public CheckUserMessage(String nameSender, String nameReceiver, String message, String type) {
        super(nameSender, nameReceiver, message, type);
    }
}
class CreatClientMessage extends Message{
    public CreatClientMessage(String nameSender, String nameReceiver, String message, String type) {
        super(nameSender, nameReceiver, message, type);
    }
}
class HistoryChatMessage extends Message{
    protected String currentTime;

    public HistoryChatMessage(String nameSender, String nameReceiver, String message, String type, String currentTime) {
        super(nameSender, nameReceiver, message, type);
        this.currentTime = currentTime;
    }
}
class CreatUserMessage extends Message{
    public CreatUserMessage(String nameSender, String nameReceiver, String message, String type) {
        super(nameSender, nameReceiver, message, type);
    }
}
class GetUserMessage extends Message{
    public GetUserMessage(String nameSender, String nameReceiver, String message, String type) {
        super(nameSender, nameReceiver, message, type);
    }
}
class ReturnUserMessage extends Message{
    User user;
    public ReturnUserMessage(String nameSender, String nameReceiver, String message, String type, User user) {
        super(nameSender, nameReceiver, message, type);
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
class SignupMessage extends Message{

    public SignupMessage(String nameSender, String nameReceiver, String message, String type) {
        super(nameSender, nameReceiver, message, type);
    }
}
class LoginMessage extends Message{

    public LoginMessage(String nameSender, String nameReceiver, String message, String type) {
        super(nameSender, nameReceiver, message, type);
    }
}