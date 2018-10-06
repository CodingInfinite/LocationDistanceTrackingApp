package com.spartons.distancetrackingapp.activity.main.viewModel

import android.annotation.SuppressLint
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.ViewModel
import android.location.Location
import android.os.Looper
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.maps.DistanceMatrixApi
import com.google.maps.PendingResult
import com.google.maps.model.DistanceMatrix
import com.google.maps.model.TravelMode
import com.spartons.distancetrackingapp.helper.GoogleMapHelper
import com.spartons.distancetrackingapp.util.AppRxSchedulers
import com.spartons.distancetrackingapp.util.NonNullMediatorLiveData
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import java.util.concurrent.TimeUnit

class MainActivityViewModel constructor(private val googleMapHelper: GoogleMapHelper, private val appRxScheduler: AppRxSchedulers, private val locationProviderClient: FusedLocationProviderClient, private val locationRequest: LocationRequest) : ViewModel() {

    companion object {
        private val TAG = MainActivityViewModel::class.java.simpleName
    }

    private var locationCallback: LocationCallback? = null
    lateinit var currentLocation: Location

    private lateinit var locationTrackingCoordinates: Location

    private var locationFirstTimeFlag = true
    private var totalDistance = 0L

    private val compositeDisposable = CompositeDisposable()
    private val locationLiveData = NonNullMediatorLiveData<Location>()
    private val distanceTracker = NonNullMediatorLiveData<String>()

    init {
        createLocationCallback()
    }

    fun currentLocation(): LiveData<Location> = locationLiveData

    fun distanceTracker(): LiveData<String> = distanceTracker

    private fun createLocationCallback() {
        locationCallback = object : LocationCallback() {

            override fun onLocationResult(locationResult: LocationResult?) {
                super.onLocationResult(locationResult)
                locationResult?.let {
                    val location = it.lastLocation
                    if (locationFirstTimeFlag) {
                        currentLocation = location
                        locationLiveData.value = currentLocation
                    }
                    val accuracy = currentLocation.accuracy
                    if (!currentLocation.hasAccuracy() || accuracy > 35f) return
                    currentLocation = location
                    locationLiveData.value = currentLocation
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun requestLocationUpdates() {
        locationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
    }

    fun startLocationTracking() {
        locationTrackingCoordinates = currentLocation
        compositeDisposable.add(Observable.interval(10, TimeUnit.SECONDS)
                .subscribeOn(appRxScheduler.threadPoolSchedulers())
                .subscribe({ _ -> makeDistanceCalculationCall() }
                        , { _ -> startLocationTracking() }))
    }

    private fun makeDistanceCalculationCall() {
        val tempLocation = currentLocation
        val origin = arrayOf(locationTrackingCoordinates.latitude.toString() + "," + locationTrackingCoordinates.longitude)
        val destination = arrayOf(tempLocation.latitude.toString() + "," + tempLocation.longitude.toString())
        DistanceMatrixApi.getDistanceMatrix(googleMapHelper.geoContextDistanceApi(), origin, destination)
                .mode(TravelMode.WALKING)
                .setCallback(object : PendingResult.Callback<DistanceMatrix> {
                    override fun onResult(result: DistanceMatrix) {
                        locationTrackingCoordinates = tempLocation
                        val temp = result.rows[0].elements[0].distance.inMeters
                        totalDistance += temp
                        distanceTracker.postValue(getDistance())
                    }

                    override fun onFailure(e: Throwable) {
                        e.printStackTrace()
                    }
                })
    }

    private fun getDistance(): String {
        Log.e(TAG, "Total Distance -> $totalDistance")
        return googleMapHelper.getDistanceInKm(totalDistance.toDouble())
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
        locationProviderClient.removeLocationUpdates(locationCallback)
        locationCallback = null
    }
}