package com.hrkne.cookbook;

public class Reviewers {
    String Date, Review, ReviewPic, Score, Time, Username;

    public Reviewers(){}

    public Reviewers(String date, String review, String reviewPic, String score, String time, String username) {
        Date = date;
        Review = review;
        ReviewPic = reviewPic;
        Score = score;
        Time = time;
        Username = username;
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String date) {
        Date = date;
    }

    public String getReview() {
        return Review;
    }

    public void setReview(String review) {
        Review = review;
    }

    public String getReviewPic() {
        return ReviewPic;
    }

    public void setReviewPic(String reviewPic) {
        ReviewPic = reviewPic;
    }

    public String getScore() {
        return Score;
    }

    public void setScore(String score) {
        Score = score;
    }

    public String getTime() {
        return Time;
    }

    public void setTime(String time) {
        Time = time;
    }

    public String getUsername() {
        return Username;
    }

    public void setUsername(String username) {
        Username = username;
    }
}
