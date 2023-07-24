import java.util.List;

/**
 * UserClient类是用户客户端的表示，扩展了User类。
 */
public class UserClient extends User {

    /**
     * 用户是否在线的状态标志。
     */
    private boolean isOnline;

    /**
     * 用户的好友列表。
     */
    private List<FriendUser> friends;

    /**
     * 创建一个新的UserClient实例。
     */
    public UserClient() {
        super();
        isOnline = false;
    }

    /**
     * 检查用户是否在线。
     *
     * @return 如果用户在线则返回true，否则返回false。
     */
    public boolean isOnline() {
        return isOnline;
    }

    /**
     * 设置用户在线状态。
     *
     * @param online 表示用户是否在线的布尔值。
     */
    public void setOnline(boolean online) {
        isOnline = online;
    }

    /**
     * 设置UserClient对象的属性，以匹配给定用户对象。
     *
     * @param user 要复制属性的用户对象。
     */
    public void setUserClient(User user) {
        this.uid = user.getUid();
        this.Sex = user.getSex();
        this.Email = user.getEmail();
        this.Password = user.getPassword();
        this.PhoneNumber = user.getPhoneNumber();
        this.Username = user.getUsername();
        this.Age = user.getAge();
    }

    /**
     * 设置用户的好友列表。
     *
     * @param friends 包含好友用户对象的列表。
     */
    public void setFriends(List<FriendUser> friends) {
        this.friends = friends;
    }

    /**
     * 获取指定好友的聊天历史消息的总页数。
     *
     * @param friendName 好友的用户名。
     * @param pageSize   每页包含的聊天历史消息数量。
     * @return 聊天历史消息的总页数。
     */
    public int getTotalHistoryPages(String friendName, int pageSize) {
        FriendUser friend = getFriend(friendName);
        int Pages = 0;
        if (friend != null) {
            Pages = friend.getTotalHistoryPages(pageSize);
        }
        return Pages;
    }

    /**
     * 获取指定好友指定页数的聊天历史消息。
     *
     * @param currentPage 要获取的页数。
     * @param pageSize    每页包含的聊天历史消息数量。
     * @param friendName  好友的用户名。
     * @return 包含聊天历史消息的列表。
     */
    public List<HistoryChatMessage> getChatHistory(int currentPage, int pageSize, String friendName) {
        sortChatHistory(friendName);
        return getFriend(friendName).getChatHistory(currentPage, pageSize);
    }

    /**
     * 对指定好友的聊天历史消息进行排序。
     *
     * @param friendName 好友的用户名。
     */
    public void sortChatHistory(String friendName) {
        getFriend(friendName).sortChatHistory();
    }

    /**
     * 向指定好友的聊天历史消息中添加一条新的消息。
     *
     * @param hMes       要添加的历史聊天消息。
     * @param friendName 好友的用户名。
     */
    public void addChatHistory(HistoryChatMessage hMes, String friendName) {
        FriendUser friend = getFriend(friendName);
        friend.addChatHistory(hMes);
        friend.sortChatHistory();
    }

    /**
     * 获取用户的好友列表。
     *
     * @return 包含好友用户对象的列表。
     */
    public List<FriendUser> getFriends() {
        return friends;
    }

    /**
     * 根据用户名获取好友对象。
     *
     * @param friendName 好友的用户名。
     * @return 匹配的好友用户对象，如果找不到则返回null。
     */
    public FriendUser getFriend(String friendName) {
        FriendUser friend = null;
        for (FriendUser f : friends) {
            if (f.getUsername().equals(friendName)) {
                friend = f;
                break;
            }
        }
        return friend;
    }
}
