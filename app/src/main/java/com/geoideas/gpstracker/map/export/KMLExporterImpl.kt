package com.geoideas.gpstracker.map.export

import android.os.Environment
import com.geoideas.gpstracker.util.AppConstant
import com.google.android.gms.maps.model.LatLng
import java.io.File
import java.util.*

class KMLExporterImpl: Exportable {

    private lateinit var data: String

    private val DOCUMENT_TEMPLATE = """<?xml version="1.0" encoding="UTF-8"?>
                        <kml xmlns="http://www.opengis.net/kml/2.2">
                            <Document>
                                {{points}}
                                {{placemarks}}
                            </Document>
                        </kml>
                        """

    private val MARKER_TEMPLATE = """
          <Placemark>
            <name>{{name}}</name>
            <description>{{description}}</description>
            <Style> 
               <IconStyle>
                <color>{{color}}</color>
                <scale>1</scale>
                <Icon>
                  <href>https://www.gstatic.com/mapspro/images/stock/503-wht-blank_maps.png</href>
                </Icon>
                <hotSpot x="32" xunits="pixels" y="64" yunits="insetPixels"/>
              </IconStyle>
              <LabelStyle>
                <scale>1</scale>
              </LabelStyle>
             </Style> 
            <Point>
              <coordinates>{{coordinates}}</coordinates>
            </Point>
          </Placemark>
    """

    private val PLACEMARK_TEMPLATE =
        """<Placemark> 
            <name>{{name}}</name>
            <description>{{description}}</description>
             <Style> 
              <LineStyle>  
               <color>{{color}}</color>
               <width>5</width>
              </LineStyle> 
             </Style> 
             <LineString>
              <coordinates>
                {{coordinates}}
              </coordinates>
             </LineString>
         </Placemark>"""

    override fun exportTrack(track: List<LatLng>, start: String, end: String): String {
        val placemark = createPlacemark(track, "","","00FF0000")

        val sp = track.first()
        val startPM = MARKER_TEMPLATE
            .replace("{{name}}", "Start")
            .replace("{{color}}", "5014F06E")
            .replace("{{description}}", "time - $start")
            .replace("{{coordinates}}", "${sp.longitude},${sp.latitude}")
        val lp = track.last()
        val lastPM = MARKER_TEMPLATE
            .replace("{{name}}", "End")
            .replace("{{color}}", "501400FF")
            .replace("{{description}}", "time - $end")
            .replace("{{coordinates}}", "${lp.longitude},${lp.latitude}")

        val points = "$startPM \n $lastPM"

        data = createDocument(placemark, points)
        return data

    }

    override fun save(handler: (File, String) -> Unit) {
        val docFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        val root = File(docFolder.parent+"/"+ AppConstant.APP_NAME)
        if(!root.exists()) root.mkdir()
        val filename = "${AppConstant.APP_NAME} Track ${Random().nextInt(10000)}.kml"
        val file = File(root, filename)
        if(file.exists()) file.delete()
        file.writeText(data)
        handler(file, "${AppConstant.APP_NAME}>"+filename)
    }

    override fun exportTrackGradient(
        track: List<List<LatLng>>,
        speeds: List<Float>,
        durations: List<Long>,
        colours: List<String>,
        startTimes: List<String>,
        endTimes: List<String>
    ): String {
        val placemarks = track.mapIndexed { index, list ->
            val hexColour = colours[index]
            val colour = hexToKmlColour(hexColour)
            val start = startTimes[index]
            val end = endTimes[index]
            val name = "$start - $end"
            val desc = """
                start = $start
                end = $end
                speed = ${speeds[index]} km/h
                color = $colour
            """.trimIndent()
            createPlacemark(list, name, desc, colour)
        }.reduce { s1, s2 -> "$s1 \n $s2" }
        val sp = track.first().first()
        val startPM = MARKER_TEMPLATE
            .replace("{{name}}", "Start")
            .replace("{{color}}", "5014F06E")
            .replace("{{description}}", "time - ${startTimes.first()}")
            .replace("{{coordinates}}", "${sp.longitude},${sp.latitude}")
        val lp = track.last().last()
        val lastPM = MARKER_TEMPLATE
            .replace("{{name}}", "End")
            .replace("{{color}}", "501400FF")
            .replace("{{description}}", "time - ${endTimes.first()}")
            .replace("{{coordinates}}", "${lp.longitude},${lp.latitude}")

        val points = "$startPM \n $lastPM"
        data = createDocument(placemarks, points)
        return data
    }

    private fun polyLineOptionsCoordinates(points: List<LatLng>) =
        points
            .map { "${it.longitude},${it.latitude}" }
            .reduce { s1, s2 -> "$s1 \n $s2" }

    private fun createPlacemark(track: List<LatLng>, name: String, desc: String, color: String): String {
        val coors = polyLineOptionsCoordinates(track)
        return PLACEMARK_TEMPLATE.replace("{{name}}", name).replace("{{description}}", desc)
            .replace("{{coordinates}}", coors).replace("{{color}}", color)

    }

    private fun createPlacemarkPoint(loc: LatLng, name: String, desc: String, color: String): String {
        val coors = "${loc.longitude},${loc.latitude}"
        return MARKER_TEMPLATE.replace("{{name}}", name).replace("{{description}}", desc)
            .replace("{{coordinates}}", coors).replace("{{color}}", color)

    }

    private fun createDocument(placemarks: String, points: String) =
        DOCUMENT_TEMPLATE.replace("{{points}}", points).replace("{{placemarks}}", placemarks)

    private fun hexToKmlColour(colour: String) =
        "ff${colour[5]}${colour[6]}${colour[3]}${colour[4]}${colour[1]}${colour[2]}".toLowerCase()
}