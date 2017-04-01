

/**
 * Created by kmursi on 3/11/17.
 */
public class Message {
    String id;
    String ip;
    String command;
    String data;
    public Message(String id, String ip, String command, String data)
    {
        this.id=id;
        this.command=command;
        this.ip=ip;
        this.data=data;
    }
}
