package com.saladdressing.veterondo.pojos;


import java.util.List;

public class OpenCurrentWeather {


    private  String name;
    private Sys sys;
    private Wind wind;
    private List<Weather> weather;
    private MainWeatherInfo main;

    public List<Weather> getWeather() {
        return weather;
    }

    public void setWeather(List<Weather> weather) {
        this.weather = weather;
    }

    public MainWeatherInfo getMain() {
        return main;
    }

    public void setMain(MainWeatherInfo main) {
        this.main = main;
    }

    public Wind getWind() {
        return wind;
    }

    public void setWind(Wind wind) {
        this.wind = wind;
    }

    public Sys getSys() {
        return sys;
    }

    public void setSys(Sys sys) {
        this.sys = sys;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
