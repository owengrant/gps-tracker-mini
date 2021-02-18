package com.geoideas.gpstracker.map.export

import com.google.android.gms.maps.model.LatLng
import java.io.File

interface Exportable {

    fun exportTrack(track: List<LatLng>, start: String, end: String): String

    fun exportTrackGradient(
        track: List<List<LatLng>>,
        speeds: List<Float>,
        durations: List<Long>,
        colours: List<String>,
        startTimes: List<String>,
        endTimes: List<String>
    ): String

    fun save(handler: (File, String) -> Unit)

}