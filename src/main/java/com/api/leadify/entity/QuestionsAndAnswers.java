package com.api.leadify.entity;

public class QuestionsAndAnswers {
    private Integer id;
    private String answer;
    private Integer position;
    private String question;
    private Integer booked_id;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }


    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public Integer getBooked_id() {
        return booked_id;
    }

    public void setBooked_id(Integer booked_id) {
        this.booked_id = booked_id;
    }
}
