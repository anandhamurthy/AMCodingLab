package com.amcodinglab.Model;

public class Answers {


    String answer_id, user_id, code, timestamp, explanation, image, language, question_id;

    public Answers() {
    }

    public Answers(String answer_id, String user_id, String code, String timestamp, String explanation, String image, String language, String question_id) {
        this.answer_id = answer_id;
        this.user_id = user_id;
        this.code = code;
        this.timestamp = timestamp;
        this.explanation = explanation;
        this.image = image;
        this.language = language;
        this.question_id = question_id;
    }

    public String getAnswer_id() {
        return answer_id;
    }

    public void setAnswer_id(String answer_id) {
        this.answer_id = answer_id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
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

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getQuestion_id() {
        return question_id;
    }

    public void setQuestion_id(String question_id) {
        this.question_id = question_id;
    }
}
