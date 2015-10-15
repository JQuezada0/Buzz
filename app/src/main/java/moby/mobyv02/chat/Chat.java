package moby.mobyv02.chat;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.HashMap;
import java.util.List;

/**
 * Created by Johnil on 10/14/2015.
 */
public class Chat {

    private HashMap<String, Object> chat;
    private List<HashMap<String, Object>> serializedMessages;
    private String id;
    private long timeStamp;
    private Gson g = new Gson();

    public Chat(){

    }

    public Chat(String string){

    }

    public String getId(){
        return id;
    }

    public long getTimeStamp(){
        return timeStamp;
    }

    private void readFromString(String chat){

        HashMap<String, String> serializedChat = g.fromJson(chat, new TypeToken<HashMap<String, String>>(){}.getType());
        String serializedMessages = serializedChat.get("messages");
        String serializedTimeStamp = serializedChat.get("timeStamp");
        this.serializedMessages = g.fromJson(serializedMessages, new TypeToken<HashMap<String, Object>>(){}.getType());
        this.timeStamp = Long.valueOf(serializedTimeStamp);
        this.id = serializedChat.get("id");
    }

}
