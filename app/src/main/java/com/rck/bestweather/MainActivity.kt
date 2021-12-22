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
import androidx.appcompat.app.AppCompatActivity
import android.graphics.PorterDuff
import android.util.Log
import androidx.preference.PreferenceManager
import kotlinx.coroutines.*
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.*
import kotlin.properties.Delegates
import android.app.ProgressDialog
import android.content.pm.PackageManager
import android.widget.*
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import java.util.*

// future
// TODO max distance is hardcoded to 100 miles, change to dynamic
// TODO Save weather results and do not update them unless location has significantly changed
// TODO make weather api calls multithreaded
// TODO add button to manually refresh location

class MainActivity : AppCompatActivity() {
    private val MAX_POINTS = 60 // hardcoded to not exceed api limits
    private var radius = 10

    // user settings (shared prefs)
    private var prioritizeTemp by Delegates.notNull<Boolean>()
    private var preferHighTemp by Delegates.notNull<Boolean>()
    private var preferHighCloudCover by Delegates.notNull<Boolean>()

    // additional user settings (shared prefs)
    private var allowThunderstorms by Delegates.notNull<Boolean>()
    private var allowDrizzle by Delegates.notNull<Boolean>()
    private var allowRain by Delegates.notNull<Boolean>()
    private var allowSnow by Delegates.notNull<Boolean>()
    private var allowAtmospheric by Delegates.notNull<Boolean>()

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

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
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

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
        val radiusBar = findViewById<SeekBar>(R.id.seekBar)
        val t1 = findViewById<TextView>(R.id.t1)
        radiusBar.min = 10
        radiusBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onStopTrackingTouch(seekBar: SeekBar) {}

            override fun onStartTrackingTouch(seekBar: SeekBar) {}

            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                t1.text = "$progress Miles"
                radius = progress
            }
        })
        initalizePrefs()
    }

    /**
     * Uses the google play services fusedLocationProviderClient to fetch the current location.
     */
    fun fetchLocation(view: View) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED && ActivityCompat
                    .checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 101)
            return
        }
        // Note: this is deprecated but its the easiest way I found to show a blocking loading screen
        val nDialog: ProgressDialog
        nDialog = ProgressDialog(this)
        nDialog.setMessage("Loading...")
        nDialog.setTitle("Fetching Weather Data Within $radius Miles...")
        nDialog.isIndeterminate = false
        nDialog.setCancelable(false)
        nDialog.show()

        val task1 = fusedLocationProviderClient.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, null)
        task1.addOnSuccessListener {
            if (it != null) {
                Toast.makeText(this, "Latitude: ${it.latitude}, Longitude: ${it.longitude}", Toast.LENGTH_SHORT).show()
                start(it.latitude, it.longitude, nDialog)
            } else {
                Toast.makeText(this, "unable to get location", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Provides the basic starting point of this app. It is called after fetchLocation has found
     * current location information. Launches results activity on completion.
     * @param lat the current latitude
     * @param lon the current longitude
     * @param nDialog the ongoing ProgressDialog that is loading while fetching the location and
     * will continue to display until results are ready.
     */
    private fun start(lat: Double, lon: Double, nDialog: ProgressDialog) {
        // double check that prefs are up to date before running
        initalizePrefs()

        val coordinatesMap = pointCoordinates(radius, lat, lon)
        val context = this
        GlobalScope.launch {
            val bestWeatherObject: JSONObject? = findBestWeatherObject(coordinatesMap)
            if (bestWeatherObject != null) {
                Log.d("results", bestWeatherObject.toString())
                val name = bestWeatherObject.getString("name")
                val temp = bestWeatherObject.getJSONObject("main").getDouble("feels_like")
                val clouds = bestWeatherObject.getJSONObject("clouds").getDouble("all")
                val latitude = bestWeatherObject.getJSONObject("coord").getDouble("lat")
                val longitude = bestWeatherObject.getJSONObject("coord").getDouble("lon")
                nDialog.dismiss()
                runOnUiThread {
                    val intent = Intent(context, ResultsActivity::class.java)
                    intent.putExtra("name", name)
                    intent.putExtra("temp", temp)
                    intent.putExtra("clouds", clouds)
                    intent.putExtra("lat", latitude)
                    intent.putExtra("lon", longitude)
                    startActivity(intent)
                }
            } else {
                // no coordinates found with current settings
                runOnUiThread {
                    val intent = Intent(context, ErrorActivity::class.java)
                    startActivity(intent)
                }
            }
        }
    }

    /**
     * Uses map of coordinates to query openweathermap api, then uses settings from SharedPrefs to
     * algorithmically determine the best weather coordinate, which then returns the whole JSON
     * response linked to these coordinates.
     * @param coordinates a map of coordinates (longitude, latitude)
     * @return JSONObject of the
     */
    private suspend fun findBestWeatherObject(coordinates: Map<Double, Double>): JSONObject? = withContext(Dispatchers.IO) {
        suspend fun getWeathers(): List<JSONObject> {
            val weathers = mutableListOf<JSONObject>()
            coroutineScope {
                coordinates.map {
                    async(Dispatchers.IO) {
                        Log.d("threadLaunch", "started")
                        val JSONResponse =
                            getJSONResponse("https://api.openweathermap.org/data/2.5/weather?lat=${it.value}&lon=${it.key}&appid=${BuildConfig.WEATHER_API_KEY}")
                        if (JSONResponse != null) {
                            weathers.add(JSONResponse)
                        }
                        Log.d("threadLaunch", "ended")
                    }
                }.awaitAll()
                Log.d("threadLaunch", "finished")
            }

            for (weather in weathers) {
                Log.d("list", weather.toString())
            }
            return weathers
        }

        fun filterResults(result: List<JSONObject>): List<JSONObject> {
            val filteredResult = mutableListOf<JSONObject>()
            for (res in result) {
                val weather = res.getJSONArray("weather")[0] as JSONObject
                val id = weather.getInt("id")
                if (id in 200..299 && allowThunderstorms) {
                    filteredResult.add(res)
                } else if (id in 300..399 && allowDrizzle) {
                    filteredResult.add(res)
                } else if (id in 500..599 && allowRain) {
                    filteredResult.add(res)
                } else if (id in 600..699 && allowSnow) {
                    filteredResult.add(res)
                } else if (id in 700..799 && allowAtmospheric) {
                    filteredResult.add(res)
                } else if (id in 800..899) {
                    // clear or cloudy weather is never filtered
                    filteredResult.add(res)
                }
            }
            return filteredResult
        }

        fun pickBest(result: List<JSONObject>): JSONObject? {
            // if there's a tie, pick the best one based on the other criteria
            // TODO refactor this, lots of repeated logic
            if (prioritizeTemp) {
                if (preferHighTemp) {
                    // pick highest temp
                    var highestTemp = -1.0
                    var bestWeather: JSONObject? = null
                    for (res in result) {
                        var resultTemp = res.getJSONObject("main").getDouble("feels_like")
                        if (resultTemp > highestTemp) {
                            highestTemp = resultTemp
                            bestWeather = res
                        } else if (resultTemp == highestTemp) {
                            if (preferHighCloudCover) {
                                // pick tie breaker with higher cloud cover
                                if (bestWeather != null) {
                                    if (res.getJSONObject("clouds").getDouble("all") >
                                        bestWeather.getJSONObject("clouds").getDouble("all"))
                                            bestWeather = res
                                } else {
                                    bestWeather = res
                                }
                            } else {
                                // pick tie breaker with lower cloud cover
                                if (bestWeather != null) {
                                    if (res.getJSONObject("clouds").getDouble("all") <
                                        bestWeather.getJSONObject("clouds").getDouble("all"))
                                            bestWeather = res
                                } else {
                                    bestWeather = res
                                }
                            }
                        }
                    }
                    return bestWeather
                } else {
                    // pick lowest temp
                    var lowestTemp = 999.0
                    var bestWeather: JSONObject? = null
                    for (res in result) {
                        var resultTemp = res.getJSONObject("main").getDouble("feels_like")
                        if (resultTemp < lowestTemp) {
                            lowestTemp = resultTemp
                            bestWeather = res
                        } else if (resultTemp == lowestTemp) {
                            if (preferHighCloudCover) {
                                // pick tie breaker with higher cloud cover
                                if (bestWeather != null) {
                                    if (res.getJSONObject("clouds").getDouble("all") >
                                        bestWeather.getJSONObject("clouds").getDouble("all"))
                                            bestWeather = res
                                } else {
                                    bestWeather = res
                                }
                            } else {
                                // pick tie breaker with lower cloud cover
                                if (bestWeather != null) {
                                    if (res.getJSONObject("clouds").getDouble("all") <
                                        bestWeather.getJSONObject("clouds").getDouble("all"))
                                            bestWeather = res
                                } else {
                                    bestWeather = res
                                }
                            }
                        }
                    }
                    return bestWeather
                }
            } else {
                if (preferHighCloudCover) {
                    // pick highest cloud cover
                    var highestCloudCover = -1.0
                    var bestWeather: JSONObject? = null
                    for (res in result) {
                        var resultCloudCover = res.getJSONObject("clouds").getDouble("all")
                        if (resultCloudCover > highestCloudCover) {
                            highestCloudCover = resultCloudCover
                            bestWeather = res
                        } else if (resultCloudCover == highestCloudCover) {
                            if (preferHighTemp) {
                                // pick tie breaker with higher temp
                                if (bestWeather != null) {
                                    if (res.getJSONObject("main").getDouble("feels_like") >
                                        bestWeather.getJSONObject("main").getDouble("feels_like"))
                                            bestWeather = res
                                } else {
                                    bestWeather = res
                                }
                            } else {
                                // pick the tie breaker with lower temp
                                if (bestWeather != null) {
                                    if (res.getJSONObject("main").getDouble("feels_like") <
                                        bestWeather.getJSONObject("main").getDouble("feels_like"))
                                        bestWeather = res
                                } else {
                                    bestWeather = res
                                }
                            }
                        }
                    }
                    return bestWeather
                } else {
                    // pick lowest clouds
                    var lowestCloudCover = 999.0
                    var bestWeather: JSONObject? = null
                    for (res in result) {
                        var resultCloudCover = res.getJSONObject("clouds").getDouble("all")
                        if (resultCloudCover < lowestCloudCover) {
                            lowestCloudCover = resultCloudCover
                            bestWeather = res
                        } else if (resultCloudCover == lowestCloudCover) {
                            if (preferHighTemp) {
                                // pick tie breaker with higher temp
                                if (bestWeather != null) {
                                    if (res.getJSONObject("main").getDouble("feels_like") >
                                        bestWeather.getJSONObject("main").getDouble("feels_like"))
                                        bestWeather = res
                                } else {
                                    bestWeather = res
                                }
                            } else {
                                // pick the tie breaker with lower temp
                                if (bestWeather != null) {
                                    if (res.getJSONObject("main").getDouble("feels_like") <
                                        bestWeather.getJSONObject("main").getDouble("feels_like"))
                                        bestWeather = res
                                } else {
                                    bestWeather = res
                                }
                            }
                        }
                    }
                    return bestWeather
                }
            }
        }

        val best: JSONObject?
        var weathers: List<JSONObject> = getWeathers()
        weathers = filterResults(weathers)
        best = pickBest(weathers)
        best
    }

    /**
     * This function calculates a set of roughly equidistant geographic coordinates
     * within a given radius of the current location
     * @param radius the radius in miles
     * @param currentLatitude the current geographic latitude
     * @param currentLongitude the current geographic longitude
     * @return a Map of longitude and latitude values where (key = longitude, value = latitude)
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

    fun openSettings(view: View) {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }
//    code to call google geocoding api
//    https://maps.googleapis.com/maps/api/geocode/json?latlng=40.714224,-73.961452&key=YOUR_API_KEY
//    code to call openweather api with sublocality
//    https://api.openweathermap.org/data/2.5/weather?q=Brooklyn&appid=YOUR_API_KEY
}