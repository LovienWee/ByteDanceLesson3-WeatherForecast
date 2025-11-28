package com.example.weatherforecast.model;
import java.util.List;

public class WeatherResponse {
    public String status;
    public String count;
    public String info;
    public String infocode;
    public List<Forecast> forecasts;
}
