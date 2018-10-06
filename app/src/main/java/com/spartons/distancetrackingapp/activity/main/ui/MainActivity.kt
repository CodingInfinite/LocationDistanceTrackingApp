package com.spartons.distancetrackingapp.activity.main.ui

import android.Manifest
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.spartons.distancetrackingapp.R
import com.spartons.distancetrackingapp.activity.main.viewModel.MainActivityViewModel
import com.spartons.distancetrackingapp.activity.main.viewModel.MainActivityViewModelFactory
import com.spartons.distancetrackingapp.extensionFunction.nonNull
import com.spartons.distancetrackingapp.extensionFunction.observe
import com.spartons.distancetrackingapp.helper.GoogleMapHelper
import com.spartons.distancetrackingapp.helper.MarkerAnimationHelper
import com.spartons.distancetrackingapp.helper.UiHelper
import com.spartons.distancetrackingapp.listeners.IPositiveNegativeListener
import com.spartons.distancetrackingapp.util.AppRxSchedulers
import com.spartons.distancetrackingapp.util.LatLngInterpolator
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    companion object {
        private const val MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 3568
    }

    private val uiHelper = UiHelper()
    private lateinit var googleMapHelper: GoogleMapHelper
    private val appRxSchedulers = AppRxSchedulers()

    private lateinit var googleMap: GoogleMap
    private lateinit var viewModel: MainActivityViewModel

    private var firstTimeFlag = true
    private var marker: Marker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val locationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        googleMapHelper = GoogleMapHelper(resources)
        val viewModelFactory = MainActivityViewModelFactory(googleMapHelper, appRxSchedulers, locationProviderClient, uiHelper.getLocationRequest())
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(MainActivityViewModel::class.java)
        if (!uiHelper.isPlayServicesAvailable(this)) {
            Toast.makeText(this, "Play Services did not installed!", Toast.LENGTH_SHORT).show()
            finish()
        } else checkLocationPermission()
        val mapFragment: SupportMapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync {
            googleMapHelper.defaultMapSettings(it)
            googleMap = it
            startListenNewLocation()
        }
        currentLocationImageButton.setOnClickListener {
            googleMapHelper.animateCamera(marker?.position, googleMap)
        }
    }

    private fun startListenNewLocation() {
        viewModel.currentLocation()
                .nonNull()
                .observe(this) {
                    if (firstTimeFlag) {
                        firstTimeFlag = false
                        googleMapHelper.animateCamera(LatLng(it.latitude, it.longitude), googleMap)
                        startDistanceTracking()
                    }
                    showOrAnimateMarker(it)
                }
    }

    private fun startDistanceTracking() {
        viewModel.startLocationTracking()
        viewModel.distanceTracker()
                .nonNull()
                .observe(this) {
                    Log.e("Hello", it)
                    distanceCoveredTextView.text = it
                }
    }

    private fun showOrAnimateMarker(location: Location) {
        val latLng = LatLng(location.latitude, location.longitude)
        if (marker == null)
            marker = googleMap.addMarker(googleMapHelper.getCurrentMarkerOptions(latLng))
        else MarkerAnimationHelper.animateMarkerToGB(marker, latLng, LatLngInterpolator.Spherical())
    }

    private fun checkLocationPermission() {
        if (!uiHelper.isHaveLocationPermission(this)) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION)
            return
        }
        if (uiHelper.isLocationProviderEnabled(this))
            uiHelper.showPositiveDialogWithListener(this, resources.getString(R.string.need_location), resources.getString(R.string.location_content), object : IPositiveNegativeListener {
                override fun onPositive() {
                    startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }
            }, "Turn On", false)
        viewModel.requestLocationUpdates()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            val value = grantResults[0]
            if (value == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(this, "Location Permission denied", Toast.LENGTH_SHORT).show()
                finish()
            } else if (value == PackageManager.PERMISSION_GRANTED) viewModel.requestLocationUpdates()
        }
    }
}
