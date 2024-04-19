package generator;

public class LoginInfo {
    boolean isLogin;
    String usermail;
    String key;
    boolean needAuth;

    public boolean isLogin() {
        return isLogin;
    }

    public void setLogin(boolean login) {
        isLogin = login;
    }

    public String getUsermail() {
        return usermail;
    }

    public void setUsermail(String usermail) {
        this.usermail = usermail;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public boolean isNeedAuth() {
        return needAuth;
    }

    public void setNeedAuth(boolean needAuth) {
        this.needAuth = needAuth;
    }

    public LoginInfo() {
    }

    public LoginInfo(boolean isLogin, String usermail, String key, boolean needAuth) {
        this.setLogin(isLogin);
        this.setUsermail(usermail);
        this.setKey(key);
        this.setNeedAuth(needAuth);
    }
}
