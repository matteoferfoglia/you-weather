YouWeather
==========

Repository for the Android course's project (A.Y. 2020-2021).

This project implements an Android application for consulting and reporting weather information
about a location.

The application is entirely distributed and no servers are present.

For online services, the application uses third parties services (e.g., *Google Firebase*).

**Important** for the build: you have to replace the file *google-services.json* with your own file
taken from Google Firebase Console, otherwise you will have troubles with your build.

## Main features

### Firebase authentication

Users who want to use the application must authenticate. The authentication process is demanded to
*Firebase Authentication*.

### Online storage

The application uses *Firebase Realtime Database* for online storage.

*Firebase* automatically handles the local storage, too, e.g., in case of temporary network
interruptions.

### Weather information

The provider for weather information is *OpenWeather*.

You must require an API key to use OpenWeather's services and put it into the file
*app/src/main/res/raw/openweathermap_apikey.txt* (in the raw resources).

### Maps

*OpenStreetMap* is used as geographic database, for showing locations on a map.

## Local emulators

### Firebase local emulators

*Firebase local emulators* are used for developing purposes. The following emulators are used:

- *Database Emulator*
- *Authentication Emulator*

Data created when using the emulators are not persistent and will not be propagated to the (real)
online services. If you need those data, you have to export them.

The command `firebase init` is used to initialize them for the project and associate them to your
online (real) Firebase project. The configuration will be saved in the file *firebase.json*.

The command `firebase emulators:start` is used to start the emulators on your computer.

#### Firebase Database Emulator

This emulator is for the *Firebase Realtime Database*.

The file *database.rules.json* defines the *Firebase Realtime Database Security Rules* for your
local emulator: those rules allow you to define how your data should be structured and when your
data can be read from and written to.

#### Firebase Authentication Emulator

This emulator is for the authentication service.