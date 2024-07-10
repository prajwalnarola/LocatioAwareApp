package com.example.locationawareapp

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {

    companion object {
        private const val UPDATE_INTERVAL = 5000L // 5 seconds
        private const val LOCATION_PERMISSION = 100
    }

    private lateinit var locationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private var currentLocation: Location? = null

    private lateinit var buttonGetLocation: Button
    private lateinit var buttonStopLocation: Button
    private lateinit var textViewLatitude: TextView
    private lateinit var textViewLongitude: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        buttonGetLocation = findViewById(R.id.buttonGetLocation)
        buttonStopLocation = findViewById(R.id.buttonStopLocation)
        textViewLatitude = findViewById(R.id.textViewLatitude)
        textViewLongitude = findViewById(R.id.textViewLongitude)

        locationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = UPDATE_INTERVAL
        }
        locationCallback = object : LocationCallback() {
            override fun onLocationAvailability(locationAvailability: LocationAvailability) {
                super.onLocationAvailability(locationAvailability)
                if (locationAvailability.isLocationAvailable) {
                    Log.i("MainActivity", "Location is available")
                } else {
                    Log.i("MainActivity", "Location is unavailable")
                }
            }

            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                Log.i("MainActivity", "Location result is available")
                locationResult.locations.firstOrNull()?.let { location ->
                    currentLocation = location
                    textViewLatitude.text = "${location.latitude}"
                    textViewLongitude.text = "${location.longitude}"
                }
            }
        }

        buttonGetLocation.setOnClickListener { startGettingLocation() }
        buttonStopLocation.setOnClickListener { stopLocationRequests() }
    }

    private fun startGettingLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        ) {
            locationProviderClient.requestLocationUpdates(locationRequest, locationCallback, mainLooper)
            locationProviderClient.lastLocation.addOnSuccessListener { location ->
                currentLocation = location
                location?.let {
                    textViewLatitude.text = "${it.latitude}"
                    textViewLongitude.text = "${it.longitude}"
                }
            }.addOnFailureListener { e ->
                Log.i("MainActivity", "Exception while getting the location: ${e.message}")
            }
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                Toast.makeText(this, "Permission needed", Toast.LENGTH_LONG).show()
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                    LOCATION_PERMISSION
                )
            }
        }
    }

    private fun stopLocationRequests() {
        locationProviderClient.removeLocationUpdates(locationCallback)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                startGettingLocation()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopLocationRequests()
    }
}




//import android.Manifest
//import android.content.pm.PackageManager
//import android.os.Bundle
//import android.util.Log
//import android.widget.TextView
//import androidx.appcompat.app.AppCompatActivity
//import androidx.core.app.ActivityCompat
//import androidx.core.content.ContextCompat
//import com.google.android.gms.location.FusedLocationProviderClient
//import com.google.android.gms.location.LocationServices
//import com.google.android.gms.tasks.Task
//
//class MainActivity : AppCompatActivity() {
//
//    private lateinit var fusedLocationClient: FusedLocationProviderClient
//    private lateinit var latitudeTextView: TextView
//    private lateinit var longitudeTextView: TextView
//
//    private val LOCATION_PERMISSION_REQUEST_CODE = 1
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
//
//        // Initialize the FusedLocationProviderClient
//        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
//
//        // Initialize UI elements
//        latitudeTextView = findViewById(R.id.latitudeTextView)
//        longitudeTextView = findViewById(R.id.longitudeTextView)
//
//        // Check for location permissions
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
//            != PackageManager.PERMISSION_GRANTED) {
//            // Request location permission
//            ActivityCompat.requestPermissions(this,
//                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
//                LOCATION_PERMISSION_REQUEST_CODE)
//        } else {
//            // Permission already granted, fetch the location
//            getLastLocation()
//        }
//    }
//
//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<out String>,
//        grantResults: IntArray
//    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
//            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
//                // Permission granted, fetch the location
//                getLastLocation()
//            } else {
//                // Permission denied, show a message
//                latitudeTextView.text = "Permission denied"
//                longitudeTextView.text = "Permission denied"
//            }
//        }
//    }
//
//    private fun getLastLocation() {
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
//            == PackageManager.PERMISSION_GRANTED) {
//            fusedLocationClient.lastLocation
//                .addOnSuccessListener { location ->
//                    if (location != null) {
//                        // Use the location object
//                        val latitude = location.latitude
//                        val longitude = location.longitude
//
//                        // Log the latitude and longitude
//                        Log.d("Location", "Latitude: $latitude, Longitude: $longitude")
//
//                        // Update UI with location data
//                        latitudeTextView.text = "Latitude: $latitude"
//                        longitudeTextView.text = "Longitude: $longitude"
//                    } else {
//                        // Handle case where location is null
//                        latitudeTextView.text = "Location not available"
//                        longitudeTextView.text = "Location not available"
//                    }
//                }
//                .addOnFailureListener {
//                    // Handle failure to get location
//                    latitudeTextView.text = "Failed to get location"
//                    longitudeTextView.text = "Failed to get location"
//                }
//        }
//    }
//}
