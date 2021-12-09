# Best Weather
This is an app which algorithmically finds the "best" weather within a certain distance or other set of paramaters.

## How to edit and run source code
* You will need to get both a regular Google Maps API key, as well as an openweathermaps API key with "Startup Plan" or higher. 
  * Note: The "Free Plan" for openweathermap limits API calls to 60 per minute, which will most likely be exceeded by this app depending on user settings.
  However, student/education plans are available at no cost which grants access to the "Developer Plan" with 3000 calls per minute.

Create a new file called "keys.properties" in the root directory.

In this file write:
```
maps.api.key="<EXAMPLE_GOOGLE_MAPS_API_KEY>"
weather.api.key="<EXAMPLE_OPENWEATHERMAPS_API_KEY>"
```
replacing <EXAMPLE_API_KEY> with your actual api keys
