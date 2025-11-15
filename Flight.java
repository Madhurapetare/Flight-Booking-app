package com.example.flightapp.model;

public class Flight {
    public int id;
    public String airline;
    public String flight_number;
    public String src;
    public String dest;
    public String depart_datetime; // ISO string from server e.g. "2025-12-20T07:00:00"
    public String arrive_datetime;
    public double price;
    public int seats_available;
}
