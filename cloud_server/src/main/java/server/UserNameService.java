package server;

public class UserNameService {

    private int cnt;

    public UserNameService() {
        cnt = 0;
    }

    public void userConnect() {
        cnt++;
    }

    public void userDisconnect() {
        cnt--;
    }

    public String getUserName() {
        return "User#" + cnt;
    }

}