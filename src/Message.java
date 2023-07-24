import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

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
        return nameSender +" "+getCurrentTime()+"\n   "+message+"\n";
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
    protected int messageId;
    protected boolean haveRead;

    public HistoryChatMessage(String nameSender, String nameReceiver, String message, String type, String currentTime) {
        super(nameSender, nameReceiver, message, type);
        this.currentTime = currentTime;
    }
    public HistoryChatMessage(SendChatMessage sMes){
        super(sMes.getNameSender(), sMes.getNameReceiver(),
                sMes.getMessage(),"HistoryChatMessage");
        currentTime = sMes.getCurrentTime();
    }
    @Override
    public String toString(){
        return nameSender +" "+currentTime+"\n   "+message+"\n";
    }

    public String getCurrentTime() {
        return currentTime;
    }

    public int getMessageId() {
        return messageId;
    }

    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }

    public boolean haveRead() {
        return haveRead;
    }

    public void setHaveRead(boolean haveRead) {
        this.haveRead = haveRead;
    }
}
class CreatUserMessage extends Message{
    private User user;
    public CreatUserMessage(String nameSender, String nameReceiver, String message, String type, User user) {
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
class GetFriendsMessage extends Message{
    public GetFriendsMessage(String nameSender, String nameReceiver, String message, String type) {
        super(nameSender, nameReceiver, message, type);
    }
}
class ReturnFriendsMessage extends Message{
    List<FriendUser> friends;
    public ReturnFriendsMessage(String nameSender, String nameReceiver, String message, String type,List<FriendUser> friends) {
        super(nameSender, nameReceiver, message, type);
        this.friends = friends;
    }

    public List<FriendUser> getFriends() {
        return friends;
    }
}
class LoginMessage extends Message{

    public LoginMessage(String nameSender, String nameReceiver, String message, String type) {
        super(nameSender, nameReceiver, message, type);
    }
}
class StatusUpdateMessage extends Message{
    boolean isOnline;

    public StatusUpdateMessage(String nameSender, String nameReceiver, String message, String type, boolean isOnline) {
        super(nameSender, nameReceiver, message, type);
        this.isOnline = isOnline;
    }

    public boolean isOnline() {
        return isOnline;
    }
}
class UnreadUpdateMessage extends Message{
    List<Integer> updateMessageId;

    public UnreadUpdateMessage(String nameSender, String nameReceiver,
                               String message, String type, List<Integer> updateMessageId) {
        super(nameSender, nameReceiver, message, type);
        this.updateMessageId = updateMessageId;
    }

    public List<Integer> getUpdateMessageId() {
        return updateMessageId;
    }
}
class ReturnRequestsMessage extends Message{
    private List<FriendRequest> friendRequests;

    public ReturnRequestsMessage(String nameSender, String nameReceiver, String message,
                                 String type, List<FriendRequest> friendRequests) {
        super(nameSender, nameReceiver, message, type);
        this.friendRequests = friendRequests;
    }
    public List<FriendRequest> getFriendRequests() {
        return friendRequests;
    }
}
class GetRequestsMessage extends Message{

    public GetRequestsMessage(String nameSender, String nameReceiver, String message, String type) {
        super(nameSender, nameReceiver, message, type);
    }
}
class UpdateRequestMessage extends Message{
    private FriendRequest request;
    public UpdateRequestMessage(String nameSender, String nameReceiver, String message, String type, FriendRequest request) {
        super(nameSender, nameReceiver, message, type);
        this.request = request;
    }
    public FriendRequest getRequest() {
        return request;
    }
}
class GetSearchFriendMessage extends Message{
    public GetSearchFriendMessage(String nameSender, String nameReceiver, String message, String type) {
        super(nameSender, nameReceiver, message, type);
    }
}
class AddRequestMessage extends Message{
    public AddRequestMessage(String nameSender, String nameReceiver, String message, String type) {
        super(nameSender, nameReceiver, message, type);
    }
}