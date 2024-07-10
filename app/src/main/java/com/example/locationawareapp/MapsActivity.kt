package com.example.locationawareapp

import UserViewModel
import UserViewModelFactory
import android.Manifest
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.locationawareapp.databinding.ActivityMapsBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.Locale

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private val UPDATE_INTERVAL: Long = 5000 // 5 seconds
    private lateinit var locationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private var currentLocation: Location? = null
    private val LOCATION_PERMISSION = 100
    private lateinit var binding: ActivityMapsBinding
    private lateinit var viewModel: UserViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val userRepository = UserRepository()
        val factory = UserViewModelFactory(userRepository)
        viewModel = ViewModelProvider(this, factory).get(UserViewModel::class.java)

        locationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = UPDATE_INTERVAL
        }

        binding.buttonGetAddress.setOnClickListener {
            getCurrentAddress()
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationAvailability(locationAvailability: LocationAvailability) {
                super.onLocationAvailability(locationAvailability)
                if (locationAvailability.isLocationAvailable) {
                    Log.i("MapsActivity", "Location is available")
                } else {
                    Log.i("MapsActivity", "Location is unavailable")
                }
            }

            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                Log.i("MapsActivity", "Location result is available")
                currentLocation = locationResult.lastLocation
                updateMapLocation()
            }
        }

        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        observeViewModel()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        startGettingLocation()
    }

    private fun startGettingLocation() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            locationProviderClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                mainLooper
            )
            locationProviderClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    currentLocation = location
                    updateMapLocation()
                } else {
                    Toast.makeText(this, "Unable to get last known location", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener { e ->
                Log.i("MapsActivity", "Exception while getting the location: ${e.message}")
            }
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                LOCATION_PERMISSION
            )
        }
    }

    private fun updateMapLocation() {
        currentLocation?.let {
            val currentPlace = LatLng(it.latitude, it.longitude)
            mMap.clear() // Clear existing markers
            mMap.addMarker(MarkerOptions().position(currentPlace).title("Marker"))
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentPlace, 15.0f))
        }
    }

    private fun getCurrentAddress() {
        currentLocation?.let { location ->

            val geocoder = Geocoder(this, Locale.getDefault())
            val addresses: List<Address>?
            val lat = location.latitude.toString()
            val long = location.longitude.toString()
            val locationType = "user"

            try {
                addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                if (addresses == null || addresses.isEmpty()) {
                    Log.i("AddressFetcherService", getString(R.string.error_address_unavailable))
                    return
                } else {
                    val address = addresses[0].getAddressLine(0)
                    Log.i("AddressState", addresses[0].postalCode)
                    Log.i("AddressState", addresses[0].locality)
                    Log.i("AddressState", addresses[0].adminArea)
                    Log.i("AddressCountry", addresses[0].countryName)
                    binding.textViewAddress.text = "Address: $address"

                    val location = address.toRequestBody("text/plain".toMediaTypeOrNull())
                    val latitude = lat.toRequestBody("text/plain".toMediaTypeOrNull())
                    val longitude = long.toRequestBody("text/plain".toMediaTypeOrNull())
                    val locationType = locationType.toRequestBody("text/plain".toMediaTypeOrNull())

                    viewModel.addLocationData(location, latitude, longitude, locationType)
                }
            } catch (e: IOException) {
                Log.i("AddressFetcherService", getString(R.string.error_exception_while_getting_address))
            }
        }
    }

    private fun stopLocationRequests() {
        locationProviderClient.removeLocationUpdates(locationCallback)
    }

    private fun observeViewModel() {
        viewModel.responseSuccess.observe(this, androidx.lifecycle.Observer { response ->
            Log.d("MainActivity", "Response: $response")
        })

        viewModel.error.observe(this) { error ->
            Log.e("MainActivity", "Error: ${error.message}", error)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startGettingLocation()
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopLocationRequests()
    }
}
