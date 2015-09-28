package moby.mobyv02.parse;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

/**
 * Created by quezadjo on 9/9/2015.
 */
@ParseClassName("Comment")
public class Comment extends ParseObject {

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

    public void setText(String text){
        put("content", text);
    }

    public String getText(){
        return getString("content");
    }

    public static ParseQuery<Comment> getQuery() {
        return ParseQuery.getQuery(Comment.class);
    }

}
