package moby.mobyv02.parse;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

/**
 * Created by quezadjo on 9/9/2015.
 */
@ParseClassName("Heart")
public class Heart extends ParseObject {

    public void setPost(Post post){
        put("post", post);
    }

    public Post getPost(){
        return (Post) getParseObject("post");
    }

    public void setUser(ParseUser user){
        put("user", user);
    }

    public ParseUser getUser(){
        return getParseUser("user");
    }

    public static ParseQuery<Heart> getQuery() {
        return ParseQuery.getQuery(Heart.class);
    }


}
