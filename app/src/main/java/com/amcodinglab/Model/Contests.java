package com.amcodinglab.Model;

public class Contests {

    String user_id, title, organization_name, description, image, timestamp, website, contest_id;

    public Contests() {
    }

    public Contests(String user_id, String title, String organization_name, String description, String image, String timestamp, String website, String contest_id) {
        this.user_id = user_id;
        this.title = title;
        this.organization_name = organization_name;
        this.description = description;
        this.image = image;
        this.timestamp = timestamp;
        this.website = website;
        this.contest_id = contest_id;
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

    public String getOrganization_name() {
        return organization_name;
    }

    public void setOrganization_name(String organization_name) {
        this.organization_name = organization_name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getContest_id() {
        return contest_id;
    }

    public void setContest_id(String contest_id) {
        this.contest_id = contest_id;
    }
}
