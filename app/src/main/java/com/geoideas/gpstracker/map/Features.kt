package com.geoideas.gpstracker.map

import com.google.android.gms.maps.model.*
import com.google.android.gms.maps.model.Polygon

data class Point(var marker: Marker? = null, var options: MarkerOptions)

data class LineString(var line: Polyline? = null, val options: PolylineOptions, var midPoint: Marker? = null)

data class Polygon(var line: Polygon? = null, val options: PolygonOptions, var midPoint: Marker? = null)

data class GradientTrack(val markers: List<MarkerOptions>, val tracks: List<PolylineOptions>, val tracksInfo: List<MarkerOptions>)