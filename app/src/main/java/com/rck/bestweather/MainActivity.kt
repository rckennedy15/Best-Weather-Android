package com.rck.bestweather

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

// Use BuildConfig.EXAMPLE_API_KEY to access api keys

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

    }

//    code to call google geocoding api
//    https://maps.googleapis.com/maps/api/geocode/json?latlng=40.714224,-73.961452&key=YOUR_API_KEY
//    code to call openweather api with sublocality
//    https://api.openweathermap.org/data/2.5/weather?q=Brooklyn&appid=YOUR_API_KEY
}