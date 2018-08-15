package com.saladdressing.veterondo.retrofitinterfaces;


import com.saladdressing.veterondo.pojos.OpenCurrentWeather;
import com.saladdressing.veterondo.utils.Constants;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Query;

public interface GetCurrentWeatherInterface {

    @GET("/data/2.5/weather?APPID="+Constants.OWM_API_KEY)
    void connect(@Query("lat") double lat, @Query("lon") double lon,
                 Callback<OpenCurrentWeather> result);

}
