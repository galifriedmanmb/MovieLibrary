package com.gali.apps.movielibrary;

/**
 * Created by 1 on 2/13/2017.
 */

public class Movie {
    String subject;
    String body;
    String imageUrl;
    String imdbID;

    public Movie(String subject, String imdbID, String imageUrl) {
        this.subject = subject;
        this.imdbID = imdbID;
        this.imageUrl = imageUrl;
    }

    @Override
    public String toString() {
        return subject;
    }
/*
    public Movie(String subject, String imdbID, String imageUrl) {
        this.subject = subject;
        this.imdbID = imdbID;
        this.imageUrl = imageUrl;
    }
*/
}
