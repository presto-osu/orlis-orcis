package com.saladdressing.veterondo.generators;


import retrofit.RestAdapter;

public class RestAdapterGenerator {

    public RestAdapterGenerator() {

    }

    public static RestAdapter generateWithLog() {
        return new RestAdapter.Builder().setEndpoint("http://api.openweathermap.org/").setLogLevel(RestAdapter.LogLevel.FULL).build();
    }

    public static RestAdapter generate() {
        return new RestAdapter.Builder().setEndpoint("http://api.openweathermap.org/").build();
    }

}
