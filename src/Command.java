import java.util.HashMap;

public interface Command {

    void execute(Connection client,Message message);
}


