package com.example.map

import androidx.lifecycle.ViewModel
import com.amap.api.location.AMapLocation
import com.amap.api.maps.model.LatLng

class MainViewModel: ViewModel() {

    var destination: LatLng? = null
    var currentLocation: AMapLocation? = null
    var lastNaviInfo: NaviResultInfo? = null
}

data class NaviResultInfo(
    val path: List<LatLng>,
    val duration: Long,
    val distance: Float
)
