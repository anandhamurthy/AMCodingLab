package com.amcodinglab.Model;

public class Tips {


    String tip_id, user_id, title, code, timestamp, explanation, image;

    public Tips() {
    }

    public Tips(String tip_id, String user_id, String title, String code, String timestamp, String explanation, String image) {
        this.tip_id = tip_id;
        this.user_id = user_id;
        this.title = title;
        this.code = code;
        this.timestamp = timestamp;
        this.explanation = explanation;
        this.image = image;
    }

    public String getTip_id() {
        return tip_id;
    }

    public void setTip_id(String tip_id) {
        this.tip_id = tip_id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
