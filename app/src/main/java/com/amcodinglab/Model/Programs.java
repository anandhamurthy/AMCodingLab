package com.amcodinglab.Model;

public class Programs {

    String program_id, user_id, statement, code, explanation, timestamp, image, description, language;
    boolean need_solution;

    public Programs() {
    }

    public Programs(String program_id, String user_id, String statement, String code, String explanation, String timestamp, String image, String description, String language, boolean need_solution) {
        this.program_id = program_id;
        this.user_id = user_id;
        this.statement = statement;
        this.code = code;
        this.explanation = explanation;
        this.timestamp = timestamp;
        this.image = image;
        this.description = description;
        this.language = language;
        this.need_solution = need_solution;
    }

    public String getProgram_id() {
        return program_id;
    }

    public void setProgram_id(String program_id) {
        this.program_id = program_id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getStatement() {
        return statement;
    }

    public void setStatement(String statement) {
        this.statement = statement;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public boolean isNeed_solution() {
        return need_solution;
    }

    public void setNeed_solution(boolean need_solution) {
        this.need_solution = need_solution;
    }
}
