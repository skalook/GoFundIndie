package com.IndieAn.GoFundIndie.Domain.Entity;

import javax.persistence.*;
import java.util.Date;

@Entity
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private int rating;

    @ManyToOne(targetEntity = User.class)
    @JoinColumn(name = "user_id", nullable = false)
    private User userId;

    @ManyToOne(targetEntity = Board.class)
    @JoinColumn(name = "board_id", nullable = false)
    private Board boardId;

    @Column(columnDefinition = "integer default 0")
    private int donation;

    @Column(length = 2000)
    private String body;

    @Column(name = "created_at", columnDefinition = "datetime default now()")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    public Comment() {}

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public User getUserId() {
        return userId;
    }

    public void setUserId(User userId) {
        this.userId = userId;
    }

    public Board getBoardId() {
        return boardId;
    }

    public void setBoardId(Board boardId) {
        this.boardId = boardId;
    }

    public int getDonation() {
        return donation;
    }

    public void setDonation(int donation) {
        this.donation = donation;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}