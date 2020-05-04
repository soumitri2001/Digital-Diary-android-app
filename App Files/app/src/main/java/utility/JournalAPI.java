package utility;

import android.app.Application;

public class JournalAPI extends Application {

    private String username,userID;
    private static JournalAPI instance;

    public static JournalAPI getInstance() {
        if(instance==null) {
            instance=new JournalAPI();
        }
        return instance;
    }

    public JournalAPI() {  /* default constructor needed for firebase */ }

    public String getUserID() {
        return userID;
    }

    public String getUsername() {
        return username;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
