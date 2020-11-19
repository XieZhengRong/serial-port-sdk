package com.ikudot.serialportlibrary.model;


public class Command {

    String command;
    String comment;

    public Command() {
    }

    public Command(String command, String comment) {
        this.command = command;
        this.comment = comment;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
