package cn.wildfirechat.pojos;

public class InputGetToken {
    private String userId;
    private String clientId;
    private String section;

    public InputGetToken(String userId, String clientId, String section) {
        this.userId = userId;
        this.clientId = clientId;
        this.section = section;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
}
