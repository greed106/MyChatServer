

public interface Command {

    void execute(ClientConnection client, Message message);
}


