package com.spartons.distancetrackingapp.activity.main.viewModel

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.spartons.distancetrackingapp.helper.GoogleMapHelper
import com.spartons.distancetrackingapp.util.AppRxSchedulers

@Suppress("UNCHECKED_CAST")
class MainActivityViewModelFactory constructor(private val googleMapHelper: GoogleMapHelper, private val appRxScheduler: AppRxSchedulers, private val locationProviderClient: FusedLocationProviderClient, private val locationRequest: LocationRequest) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>) = MainActivityViewModel(googleMapHelper, appRxScheduler, locationProviderClient, locationRequest) as T
}