package moby.mobyv02.parse;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

/**
 * Created by quezadjo on 9/9/2015.
 */
@ParseClassName("Upvote")
public class Upvote extends ParseObject {

    public void setUser(ParseUser user){
        put("user", user);
    }

    public ParseUser getUser(){
        return getParseUser("user");
    }

    public void setPost(Post post){
        put("post", post);
    }

    public Post getPost(){
        return (Post) getParseObject("post");
    }

    public static ParseQuery<Upvote> getQuery(){
        return ParseQuery.getQuery(Upvote.class);
    }

}
