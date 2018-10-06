package com.spartons.distancetrackingapp.helper

import android.content.res.Resources
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.GeoApiContext
import com.spartons.distancetrackingapp.R
import java.text.DecimalFormat

class GoogleMapHelper constructor(private val resources: Resources) {

    companion object {
        private const val ZOOM_LEVEL = 18
        private const val TILT_LEVEL = 25
    }

    /**
     * @param latLng in which position to Zoom the camera.
     * @return the [CameraUpdate] with Zoom and Tilt level added with the given position.
     */

    private fun buildCameraUpdate(latLng: LatLng): CameraUpdate {
        val cameraPosition = CameraPosition.Builder()
                .target(latLng)
                .tilt(TILT_LEVEL.toFloat())
                .zoom(ZOOM_LEVEL.toFloat())
                .build()
        return CameraUpdateFactory.newCameraPosition(cameraPosition)
    }

    /**
     * @param position where to draw the [com.google.android.gms.maps.model.Marker]
     * @return the [MarkerOptions] with given properties added to it.
     */

    fun getCurrentMarkerOptions(position: LatLng): MarkerOptions {
        val options = getMarkerOptions(position)
        options.flat(true)
        return options
    }

    private fun getMarkerOptions(position: LatLng): MarkerOptions {
        return MarkerOptions()
                .icon(BitmapDescriptorFactory.defaultMarker())
                .position(position)
    }

    fun animateCamera(latLng: LatLng?, googleMap: GoogleMap) {
        if (latLng == null) return
        val cameraUpdate = buildCameraUpdate(latLng)
        googleMap.animateCamera(cameraUpdate, 10, null)
    }


    /**
     * @return the direction map api key.
     */

    private fun distanceApi(): String {
        return resources.getString(R.string.google_distance_api)
    }

    /**
     * The function returns the ${[GeoApiContext]} with distance api key.
     *
     * @return the ${[GeoApiContext]} with distance api.
     */

    fun geoContextDistanceApi(): GeoApiContext {
        return GeoApiContext.Builder()
                .apiKey(distanceApi())
                .build()
    }


    fun getDistanceInKm(totalDistance: Double): String {
        if (totalDistance == 0.0 || totalDistance < -1)
            return "0 Km"
        else if (totalDistance > 0 && totalDistance < 1000)
            return totalDistance.toInt().toString() + " meters"
        val df = DecimalFormat("#.##")
        return df.format(totalDistance / 1000) + " Km"
    }

    /**
     * This function sets the default google map settings.
     *
     * @param {[GoogleMap]} to set default settings.
     */

    fun defaultMapSettings(googleMap: GoogleMap) {
        googleMap.uiSettings.isZoomControlsEnabled = false
        googleMap.uiSettings.isMapToolbarEnabled = false
        googleMap.uiSettings.isRotateGesturesEnabled = true
        googleMap.uiSettings.isMapToolbarEnabled = false
        googleMap.uiSettings.isTiltGesturesEnabled = true
        googleMap.uiSettings.isCompassEnabled = false
        googleMap.isBuildingsEnabled = true
    }
}