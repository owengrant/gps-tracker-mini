package com.geoideas.gpstrackermini.activity

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.geoideas.gpstrackermini.R
import java.util.*

class TrackFilterActivity : AppCompatActivity() {

    private var fromDate = "0000-00-00"
    private var fromTime = "00:00:00"
    private var toDate = "0000-00-00"
    private var toTime = "00:00:00"


    private lateinit var fromDateBtn: EditText
    private lateinit var fromTimeBtn: EditText
    private lateinit var toDateBtn: EditText
    private lateinit var toTimeBtn: EditText
    private lateinit var accuracyField: EditText
    private lateinit var gradientBox: Switch
    private lateinit var maxField: EditText
    private lateinit var speedLabel: TextView
    private lateinit var filterLevel: SeekBar
    private lateinit var speedArea: View

    private lateinit var dialog: AlertDialog

    init {
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH)
        val day = cal.get(Calendar.DAY_OF_MONTH)
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val min = cal.get(Calendar.MINUTE)
        fromDate = createDate(year, month, day)
        fromTime = createTime(hour, min)
        toDate = createDate(year, month, day)
        toTime = createTime(hour, min)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_track_filter)
        speedLabel = findViewById(R.id.text_speed)
        fromDateBtn = findViewById<EditText>(R.id.input_from_date).apply { setText(fromDate) }
        fromTimeBtn = findViewById<EditText>(R.id.input_from_time).apply { setText(fromTime) }
        toDateBtn = findViewById<EditText>(R.id.input_to_date).apply { setText(toDate) }
        toTimeBtn = findViewById<EditText>(R.id.input_to_time).apply { setText(toTime) }
        accuracyField = findViewById(R.id.input_accuracy)
        gradientBox = findViewById(R.id.box_gradient)
        maxField = findViewById(R.id.input_max_speed)
        filterLevel = findViewById(R.id.seekBar_filter_level)
        speedArea = findViewById(R.id.layout_speed_track)
        gradientBox.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                speedArea.visibility = View.VISIBLE
            } else {
                speedArea.visibility = View.GONE
                maxField.setText("")
            }
        }
        dialog = ceateSmoothDialog()
    }

    private fun ceateSmoothDialog() : AlertDialog {
        return AlertDialog.Builder(this).run {
            setTitle("Map Performance Booster")
            setMessage("""
                Apply advanced smoothing algorithm to speed tracks.
                Increasing the level will improve map performance.
                If no points are shown on the map, try setting level to zero.
            """.trimIndent())
            setNegativeButton("Okay") { dialog, _ -> dialog.dismiss()}
            create()
        }
    }

    private fun putZero(value: Int) = if (value < 10) "0$value" else value.toString()


    private fun createDate(year: Int, month: Int, day: Int) = "$year-${putZero(month+1)}-${putZero(day)}"

    private fun createTime(hour: Int, minute: Int) = "${putZero(hour)}:${putZero(minute)}:00"

    fun fromDate(view: View) {
        val picker = DatePicker().apply{ onSelected = { year, month, day ->
                fromDate = createDate(year, month, day)
                fromDateBtn.setText(fromDate)
            }
        }
        picker.show(supportFragmentManager, "fromDatePicker")
    }

    fun fromTime(view: View) {
        val picker = TimePicker().apply{ onSelected =  { hour, min ->
                fromTime = createTime(hour, min)
                fromTimeBtn.setText(fromTime)
            }
        }
        picker.show(supportFragmentManager, "fromTimePicker")
    }

    fun toDate(view: View) {
        val picker = DatePicker().apply{ onSelected = { year, month, day ->
                toDate = createDate(year, month, day)
                toDateBtn.setText(toDate)
            }
        }
        picker.show(supportFragmentManager, "toDatePicker")
    }

    fun toTime(view: View) {
        val picker = TimePicker().apply { onSelected =  { hour, min ->
                toTime = createTime(hour, min)
                toTimeBtn.setText(toTime)
            }
        }
        picker.show(supportFragmentManager, "toTimePicker")
    }


    fun create(view: View) {
        val max = maxField.text.toString()
        val maxVal = if(max.isEmpty()) 0 else max.toInt()
        if(maxVal == 0 && gradientBox.isChecked) {
            Toast.makeText(this, "Speed Limit required", Toast.LENGTH_LONG).show()
            return
        }
        val result = Intent().apply {
            putExtra("max", maxVal)
            putExtra("from", "$fromDate $fromTime")
            putExtra("to", "$toDate $toTime")
            val acc = accuracyField.text.toString()
            val accVal = if(acc.isEmpty()) 0.0 else acc.toDouble()
            putExtra("accuracy", accVal)
            putExtra("gradient", gradientBox.isChecked)
            putExtra("filter", filterLevel.progress)
        }
        setResult(RESULT_OK, result)
        finish()
    }

    fun dismissSpeedInfo(view: View) {
        dialog.dismiss()
    }

    fun showSmoothingInfo(view: View) {
        dialog.show()
    }
}
