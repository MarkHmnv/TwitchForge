package com.twitchforge.exception;

public class InvalidUrlException extends RuntimeException{
    public InvalidUrlException(){
        super("Invalid url, it should be like https://www.twitch.tv/videos/{id}");
    }
}
