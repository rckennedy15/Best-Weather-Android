/*
   © Copyright Ryan Kennedy

    This file is part of Best Weather.

    Best Weather is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Best Weather is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Best Weather.  If not, see <https://www.gnu.org/licenses/>
*/

package com.rck.bestweather

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import android.graphics.PorterDuff
import android.util.Log
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt


// Use BuildConfig.EXAMPLE_API_KEY to access api keys

class MainActivity : AppCompatActivity() {
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val button = findViewById<ImageView>(R.id.getWeather)
        // Just adds a 'click' effect when you press the button
        button.setOnTouchListener { v, event ->
            when (event?.action) {
                MotionEvent.ACTION_DOWN -> {
                    val view = v as ImageView
                    //overlay is black with transparency of 0x77 (119)
                    view.drawable.setColorFilter(0x77000000, PorterDuff.Mode.SRC_ATOP)
                    view.invalidate()
                }
                MotionEvent.ACTION_UP -> {
                    val view = v as ImageView
                    //clear the overlay
                    view.drawable.clearColorFilter()
                    view.invalidate()
                }
            }

            v?.onTouchEvent(event) ?: true
        }

    }

    fun start(view: View) {
        Log.d("start", "onClick works")
    }
    
    // radius in miles
    fun pointCoordinates(radius: Int) {
        val numberOfPoints = (1.23.pow(radius) + 5).roundToInt()
        fun generatePolarCoordinates(numberOfPoints: Int): Map<Double, Double> {
            val coords: MutableMap<Double, Double> = mutableMapOf()
            val phi = (1 + sqrt(5.0)) / 2
            for (k in 1..numberOfPoints) {
                val r = sqrt(k - 0.5) / sqrt(numberOfPoints - 0.5)
                val theta = 2 * Math.PI * k / phi.pow(2)
                coords[theta] = r
            }
            return coords
        }
    }

//    code to call google geocoding api
//    https://maps.googleapis.com/maps/api/geocode/json?latlng=40.714224,-73.961452&key=YOUR_API_KEY
//    code to call openweather api with sublocality
//    https://api.openweathermap.org/data/2.5/weather?q=Brooklyn&appid=YOUR_API_KEY
}