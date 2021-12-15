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
import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import android.graphics.PorterDuff
import android.util.Log
import android.widget.Toast
import androidx.preference.PreferenceManager
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.*
import kotlin.properties.Delegates


// Use BuildConfig.EXAMPLE_API_KEY to access api keys

class MainActivity : AppCompatActivity() {
    private val MAX_POINTS = 60
    // user settings
    private var prioritizeTemp by Delegates.notNull<Boolean>()
    private var preferHighTemp by Delegates.notNull<Boolean>()
    private var preferHighCloudCover by Delegates.notNull<Boolean>()

    // additional user settings
    private var allowThunderstorms by Delegates.notNull<Boolean>()
    private var allowDrizzle by Delegates.notNull<Boolean>()
    private var allowRain by Delegates.notNull<Boolean>()
    private var allowSnow by Delegates.notNull<Boolean>()
    private var allowAtmospheric by Delegates.notNull<Boolean>()

    private fun initalizePrefs() {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        val prioritizeTemp = sharedPref.getBoolean(SettingsActivity.KEY_PREF_PRIORITIZE_TEMP, false)
        val preferHighTemp = sharedPref.getBoolean(SettingsActivity.KEY_PREF_PREFER_HIGH_TEMP, true)
        val preferHighCloudCover = sharedPref.getBoolean(SettingsActivity.KEY_PREF_PREFER_HIGH_CLOUD_COVER, false)

        val allowThunderstorms = sharedPref.getBoolean(SettingsActivity.KEY_PREF_ALLOW_THUNDERSTORMS, false)
        val allowDrizzle = sharedPref.getBoolean(SettingsActivity.KEY_PREF_ALLOW_DRIZZLE, false)
        val allowRain = sharedPref.getBoolean(SettingsActivity.KEY_PREF_ALLOW_RAIN, false)
        val allowSnow = sharedPref.getBoolean(SettingsActivity.KEY_PREF_ALLOW_SNOW, false)
        val allowAtmospheric = sharedPref.getBoolean(SettingsActivity.KEY_PREF_ALLOW_ATMOSPHERIC, false)

        this.prioritizeTemp = prioritizeTemp
        this.preferHighTemp = preferHighTemp
        this.preferHighCloudCover = preferHighCloudCover

        this.allowThunderstorms = allowThunderstorms
        this.allowDrizzle = allowDrizzle
        this.allowRain = allowRain
        this.allowSnow = allowSnow
        this.allowAtmospheric = allowAtmospheric
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        PreferenceManager.setDefaultValues(this, R.xml.root_preferences, false)

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
        initalizePrefs()

    }

    fun start(view: View) {
        // double check that prefs are up to date before running
        initalizePrefs()

        val coordinatesMap = pointCoordinates(10, 42.2, -72.5)
        val bestWeatherCoordinates = findBestWeatherCoordinates(coordinatesMap)
        if (bestWeatherCoordinates != null) {
            // TODO
        } else {
            // TODO no coordinates found with current settings
        }
    }

    private fun findBestWeatherCoordinates(coordinates: Map<Double, Double>): Pair<Double, Double>? {
        // TODO
        // steps
        // find weather of every point
        // eliminate options based on add'l user settings
        // based on prioritizeTemp pick highest/lowest temp (based on preferHighTemp)
        // OR pick highest/lowest clouds (based on preferHighCloudCover)
        return null
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

    /**
     * FIXME does not let you use a restricted api key, will probable switch to nominatim/openstreetmap
     * Is supposed to call google's geocoding api to convert coordinates into an actual town name
     * @param latitude the latitude of the coordinates
     * @param longitude the longitude of the coordinates
     * @return the name of the town
     */
    private suspend fun geocodeCoordinates(latitude: Double, longitude: Double): String {
        val url = "https://maps.googleapis.com/maps/api/geocode/json?latlng=${latitude},${longitude}&result_type=locality&key=${BuildConfig.MAPS_API_KEY}"
        var res: JSONObject?
        var name: String? = null
        var JSONResponse: JSONObject?
        var isDone = false
        GlobalScope.launch {
            JSONResponse = getJSONResponse(url)
            Log.d("json", JSONResponse!!.toString())
            try {
                res = JSONResponse!!.getJSONArray("results")[0] as JSONObject
                res = res!!.getJSONArray("address_components")[0] as JSONObject
                synchronized(this) {
                    name = res!!.getString("long_name")
                }
            } catch (err: ArrayIndexOutOfBoundsException) {
                res = null
                synchronized(this) {
                    name = null
                }
            }
            Log.d("name", name!!)
            isDone = true
        }.join()
        while(true) {
            if (isDone)
                return name!!
            else
                delay(1000L)
                continue
        }

    }

    /**
     * A simple helper function that queries an https api and returns the JSON response.
     * NOTE: must be run in a separate thread, will hang if run on UI thread. (Use GlobalScope.launch
     * to run this function as a coroutine.
     * @param src the http/https url (api query)
     * @return the api response in JSON format, as a JSONObject
     */
    private fun getJSONResponse (src: String): JSONObject? {
        return try {
            val url = URL(src)
            val connection = url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            val content = connection.inputStream.bufferedReader().use(BufferedReader::readText)
            JSONObject(content)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    fun openSettings(view: android.view.View) {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }
//    code to call google geocoding api
//    https://maps.googleapis.com/maps/api/geocode/json?latlng=40.714224,-73.961452&key=YOUR_API_KEY
//    code to call openweather api with sublocality
//    https://api.openweathermap.org/data/2.5/weather?q=Brooklyn&appid=YOUR_API_KEY
}