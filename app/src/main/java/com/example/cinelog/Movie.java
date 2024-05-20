package com.example.cinelog;

import java.io.Serializable;

public class Movie implements Serializable {
    public int id;
    public String title;
    public int year;
    public String genre;
    public float rate;
    public Long date;
    public int rewatch;
    public String review;
    public byte[] cover;
}
