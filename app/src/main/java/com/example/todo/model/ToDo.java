package com.example.todo.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class ToDo {

    @PrimaryKey (autoGenerate = true)
    private int toDoId;

    private int toDoOwnerId;

    private String content;

    private boolean isDone;

    public ToDo(int toDoOwnerId, String content, boolean isDone){
        this.toDoOwnerId = toDoOwnerId;
        this.content = content;
        this.isDone = isDone;
    }

    // accessor

    public String getContent() {
        return content;
    }

    public boolean isDone() {
        return isDone;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setDone(boolean done) {
        isDone = done;
    }

    public int getToDoOwnerId() {
        return toDoOwnerId;
    }

    public void setToDoOwnerId(int toDoOwnerId) {
        this.toDoOwnerId = toDoOwnerId;
    }

    public int getToDoId() {
        return toDoId;
    }

    public void setToDoId(int toDoId) {
        this.toDoId = toDoId;
    }

    @Override
    public String toString() {
        return "ToDo{" +
                "toDoId=" + toDoId +
                ", toDoOwnerId=" + toDoOwnerId +
                ", content='" + content + '\'' +
                ", isDone=" + isDone +
                '}';
    }
}
