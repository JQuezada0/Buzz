package moby.mobyv02.parse;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

/**
 * Created by quezadjo on 9/10/2015.
 */
@ParseClassName("Follow")
public class Follow extends ParseObject {

    public void setTo(ParseUser user){
        put("toUser", user);
    }

    public ParseUser getTo(){
        return getParseUser("toUser");
    }

    public void setFrom(ParseUser user){
        put("fromUser", user);
    }

    public ParseUser getFrom(){
        return getParseUser("fromUser");
    }

    public static ParseQuery<Follow> getQuery() {
        return ParseQuery.getQuery(Follow.class);
    }

}
