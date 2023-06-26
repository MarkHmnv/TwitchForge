package com.twitchforge.exception;

public class BadResponseException extends RuntimeException{
    public BadResponseException(){
        super("Something went wrong\uD83D\uDE2B \nMake sure your url is valid!");
    }
}
