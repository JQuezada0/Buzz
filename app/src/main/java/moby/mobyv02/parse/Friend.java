package moby.mobyv02.parse;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import moby.mobyv02.ParseOperation;

/**
 * Created by Johnil on 10/10/2015.
 */
@ParseClassName("Friend")
public class Friend extends ParseObject {

    public void setFrom(ParseUser user){
        put("from", user);
    }

    public ParseUser getFrom(){
        return getParseUser("from");
    }

    public void setTo(ParseUser user){
        put("to", user);
    }

    public ParseUser getTo(){
        return getParseUser("to");
    }

    public void setAccepted(boolean value){
        put("accepted", value);
    }

    public boolean getAccepted(){
        return getBoolean("accepted");
    }

    public void setRejected(boolean value){
        put("rejected", value);
    }

    public boolean getRejected(){
        return getBoolean("rejected");
    }

    public void setCancelled(boolean value){
        put("cancelled", value);
    }

    public boolean getCancelled(){
        return getBoolean("cancelled");
    }

    public static ParseQuery<Friend> getQuery(){
        return ParseQuery.getQuery(Friend.class);
    }

}
