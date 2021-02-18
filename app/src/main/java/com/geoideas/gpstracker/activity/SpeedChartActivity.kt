package com.geoideas.gpstracker.activity

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.geoideas.gpstracker.R
import com.geoideas.gpstracker.activity.util.ActivityUtils
import com.geoideas.gpstracker.util.PermissionsUtil
import com.geoideas.gpstracker.util.AppConstant.GREEN_RED_COLOURS
import kotlinx.android.synthetic.main.activity_main.view.*
import lecho.lib.hellocharts.model.*
import lecho.lib.hellocharts.view.LineChartView
import lecho.lib.hellocharts.view.PieChartView
import java.io.File
import java.io.FileOutputStream
import java.util.*

class SpeedChartActivity : AppCompatActivity() {

    private val TAG = SpeedChartActivity::class.java.simpleName

    private val util = ActivityUtils()
    private lateinit var view: View
    private lateinit var pieChart: PieChartView
    private lateinit var lineChart: LineChartView
    private lateinit var label: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_speed_chart)
        view = findViewById(R.id.layout_speed_chart)
        pieChart = findViewById(R.id.chart_pie_speed)
        lineChart = findViewById(R.id.chart_line_speed)
        label = findViewById(R.id.label_chart_speed)
        val data = this.intent
        if(data == null) finish()
        label.text = data.extras.getString("label") ?: ""
        createPieChart()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_speed, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when(item?.itemId ?: -1){
            android.R.id.home -> {
                onBackPressed()
                true
            }
            R.id.menu_speed_screenshot -> {
                createScreenShot()
                true
            }
            R.id.menu_speed_pie -> {
                lineChart.visibility = View.GONE
                pieChart.visibility = View.VISIBLE
                createPieChart()
                true
            }
            R.id.menu_speed_line -> {
                pieChart.visibility = View.GONE
                lineChart.visibility = View.VISIBLE
                createLineChart()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun noData() {
        util.showSnackBar(view, "No data available")
    }

    private fun createLineChart() {
        val data = this.intent
        if(data == null) finish()
        val speeds = data.extras.getFloatArray("speeds").run {
            map { it.toInt() }.toIntArray()
        }
        if(speeds.isEmpty() || speeds.size < 2) {
            noData()
            return
        }
        val maxSpeed = speeds?.max() ?: 0
        val max = data.extras.getInt("max")
        val tenthF = (max*10)/100f
        val tenth = if(max > 0 && tenthF < 1) 1 else tenthF.toInt()
        var lower = 0
        var upper = tenth
        val yAxisValues = mutableListOf<PointValue>(PointValue(0f, 0f))
        val xAxisValues = mutableListOf<AxisValue>(AxisValue(0f).setLabel("0"))
        var i = 1
        while(upper < max) {
            upper += upperOverflow(upper, tenth, max)
            val count = rangeCount(lower, upper, speeds)
            if(count > 0 && upper <= maxSpeed) {
                val percent = (count.toFloat() / speeds.size) * 100
                yAxisValues.add(PointValue(i.toFloat(), percent))
                xAxisValues.add(AxisValue(i.toFloat()).setLabel("$upper"))
            }
            lower = upper
            upper += tenth
            ++i
        }
        val outliners = rangeCount(lower, Int.MAX_VALUE, speeds)
        if(outliners > 0) {
            val percent = (outliners.toFloat() / speeds.size) * 100
            yAxisValues.add(PointValue(i.toFloat(), percent))
            xAxisValues.add(AxisValue(i.toFloat()).setLabel(">${lower}"))
        }

        val line = Line(yAxisValues).apply { color = Color.parseColor("#FF00F0") }
        val lineList = listOf(line)
        val axis = Axis().apply{
            values = xAxisValues
            textColor = Color.parseColor("#03A9F4")
            name = "Kilometers Per Hour"
        }
        lineChart.lineChartData = LineChartData().apply {
            lines = lineList
            axisXBottom = axis
            axisYLeft = Axis().apply {
                textColor = Color.parseColor("#03A9F4")
                name = "Speed Distribution As Percentage"
            }
        }
    }

    private fun createPieChart() {
        val data = this.intent
        if(data == null) finish()
        val speeds = data.extras.getFloatArray("speeds").run {
            map { it.toInt() }.toIntArray()
        }
        if(speeds.isEmpty()) {
            noData()
            return
        }
        val max = data.extras.getInt("max")
        val tenth = (max*10)/100
        var lower = 0
        var upper = tenth
        val slices = mutableListOf<SliceValue>()
        val per = GREEN_RED_COLOURS.size/max.toFloat()
        while(upper < max) {
            upper += upperOverflow(upper, tenth, max)
            val slice = rangeCount(lower, upper, speeds)
            if(slice > 0) {
                val value = upper * per
                val colour = if (value > GREEN_RED_COLOURS.size - 1) GREEN_RED_COLOURS.size - 1 else value.toInt()
                val sliceValue = createSlice(
                    (slice.toFloat() / speeds.size) * 100,
                    GREEN_RED_COLOURS[colour],
                    "$lower-$upper"
                )
                slices.add(sliceValue)
            }
            upper++
            lower = upper
            upper += tenth
        }
        val outVal = lower-1
        val outliners = rangeCount(outVal, Int.MAX_VALUE, speeds)
        if(outliners > 0) {
            val outlinerSlice = createSlice(
                (outliners.toFloat() / speeds.size) * 100,
                GREEN_RED_COLOURS[GREEN_RED_COLOURS.size - 1],
                "> $outVal"
            )
            slices.add(outlinerSlice)
        }
        pieChart.pieChartData = PieChartData().apply {
            this.values = slices
            setHasLabels(true)
            setHasCenterCircle(true)
            setCenterText1FontSize(20)
            setCenterText1("KMPH")
        }

        //pieChart.title.text = "Speed Distribution (KMPH)"
    }

    private fun upperOverflow(upper: Int, tenth: Int, max: Int) = if((upper + tenth) >= max) (max - (upper)) else 0

    private fun createSlice(count: Float, colour: String, label: String) = SliceValue().apply {
        value = count
        color = Color.parseColor(colour)
        setLabel(label)
    }

    private fun rangeCount(lower: Int, upper: Int, speeds: IntArray): Int {
        val count = speeds.filter { it in lower..upper }.count()
        return if(count == 0) -1 else count
    }

    private fun createScreenShot() {
        if (PermissionsUtil.resolveFilePermission(this)) {
            try {
                val root =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                if (!root.exists()) root.mkdir()
                val imageFile = File(root, "where_${Random().nextInt(10000)}.jpg").absoluteFile
                if (imageFile.exists()) imageFile.delete()
                val out = FileOutputStream(imageFile)
                var view = if (pieChart.visibility == View.GONE) lineChart else pieChart
                val image = util.viewToImage(view);
                image.compress(Bitmap.CompressFormat.JPEG, 90, out)
                out.flush()
                out.close()
                val path = FileProvider.getUriForFile(
                    this,
                    "com.geoideas.gpstracker.fileprovider",
                    imageFile
                )
                val f = File(imageFile.absolutePath)
                val uri = Uri.fromFile(f)
                Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).also {
                    it.data = uri
                    sendBroadcast(it)
                }
                var viewIntent = Intent(Intent.ACTION_VIEW).apply {
                    setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    setDataAndType(path, "image/*")
                }
                startActivity(viewIntent)
            } catch (e: Exception) {
                e.printStackTrace()
                util.showSnackBar(view, "Image save error")
            }
        }
    }

}
