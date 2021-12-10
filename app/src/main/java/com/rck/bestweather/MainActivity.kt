/**
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
import kotlin.math.*


// Use BuildConfig.EXAMPLE_API_KEY to access api keys

class MainActivity : AppCompatActivity() {
    private val MAX_POINTS = 60
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
        pointCoordinates(10, 42.2, -72.5)
    }

    /**
     * This function calculates a set of roughly equidistant geographic coordinates
     * within a given radius of the current location
     * @param radius the radius in miles
     * @param currentLatitude the current geographic latitude
     * @param currentLongitude the current geographic longitude
     * @return a Map of longitude and latitude values
     */
    private fun pointCoordinates(radius: Int, currentLatitude: Double, currentLongitude: Double): Map<Double, Double> {
        // arbitrary function to determine number of points per increase in radius
        val num = (1.23.pow(radius) + 5).roundToInt()
        val numberOfPoints = if (num > MAX_POINTS) MAX_POINTS else num

        /**
         * Uses the sunflower seed method to distribute roughly equidistant points
         * within a circle
         * @param numberOfPoints should not exceed MAX_POINTS
         * @return polar coordinates (theta, radius) of these roughly equidistant points
         */
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

        /**
         * Converts polar coordinates to cartesian coordinates
         * @param coords Map of polar coordinates
         * @return Map of cartesian coordinates
         */
        fun convertToCartesian(coords: Map<Double, Double>): Map<Double, Double> {
            val cartCoords = mutableMapOf<Double, Double>()
            for ((t, r) in coords) {
                cartCoords[r * cos(t)] = r * sin(t)
            }
            return cartCoords
        }

        /**
         * Converts generated cartesian coordinates (which are all in the range of -1 to 1)
         * into actual geographic coordinates as determined by the current location (passed by
         * parent function)
         * Note: longitude = x, latitude = y
         * @param coords a Map of cartesian coordinates all in the range of -1 to 1
         * @return a Map of geographic coordinates
         */
        fun convertToGeoCoords(coords: Map<Double, Double>): Map<Double, Double> {
            val geoCoords = mutableMapOf<Double, Double>()
            // radius is in miles, not degrees of lat/long
            // use current location to define number of miles per degree long/lat
            // one degree latitude = 69 miles
            // 1 degree of Longitude = cosine (latitude in radians) * 69.172 [distance in miles of longitude degree at equator]
            // second add current coords to coords

            val oneDegreeLatitudeInMiles = 69.0
            val oneDegreeLongitudeInMiles = cos(currentLatitude * (Math.PI / 180)) * 69.172

            val xRadius = radius / oneDegreeLongitudeInMiles
            val yRadius = radius / oneDegreeLatitudeInMiles

            for((x, y) in coords) {
                geoCoords[x * xRadius + currentLongitude] = y * yRadius + currentLatitude
            }
            return geoCoords
        }

        val polarCoords = generatePolarCoordinates(numberOfPoints)
        val coords = convertToCartesian(polarCoords)
        val geoCoords = convertToGeoCoords(coords)
        for (long in geoCoords.keys) {
            Log.d("GeoCoords: longitude, latitude", "${long}, ${geoCoords[long]}")
        }

        return geoCoords

    }

//    code to call google geocoding api
//    https://maps.googleapis.com/maps/api/geocode/json?latlng=40.714224,-73.961452&key=YOUR_API_KEY
//    code to call openweather api with sublocality
//    https://api.openweathermap.org/data/2.5/weather?q=Brooklyn&appid=YOUR_API_KEY
}