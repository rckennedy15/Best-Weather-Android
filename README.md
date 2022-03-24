# Best Weather
This is an app which algorithmically finds the "best" weather within a certain distance or other set of paramaters.

## How to edit and run source code
* You will need to get an openweathermaps API key with "Startup Plan" or higher. 
  * Note: The "Free Plan" for openweathermap limits API calls to 60 per minute, which will most likely be exceeded by this app depending on user settings.
  However, student/education plans are available at no cost which grants access to the "Developer Plan" with 3000 calls per minute.

Create a new file called "keys.properties" in the root directory.

In this file write:
```
weather.api.key="<EXAMPLE_OPENWEATHERMAPS_API_KEY>"
```
replacing <EXAMPLE_API_KEY> with your actual api keys.

Next, in Android Studio, select Build -> Clean Project and then Build -> Rebuild Project to allow api key to register

## Screenshots
Home Activity | Asking for location
:----:|:-----:
![home screen](https://user-images.githubusercontent.com/23503751/160009054-dbbbe339-a9c1-4c91-b626-52c4f711359b.png) | ![asking for location](https://user-images.githubusercontent.com/23503751/160009354-b774b389-568a-4e10-9d7b-efdd0f0e7de9.png)
Results Activity | test4
![results](https://user-images.githubusercontent.com/23503751/160010442-68d30fee-4aeb-4126-a8be-71feeaa1adcb.png) | ![settings screen](https://user-images.githubusercontent.com/23503751/160010474-ccedd0ad-dd75-4b54-81fb-9592729a395f.png)






