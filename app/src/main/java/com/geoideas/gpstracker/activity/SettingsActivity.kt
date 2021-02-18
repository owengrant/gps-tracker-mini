package com.geoideas.gpstracker.activity

import android.app.AlertDialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.geoideas.gpstracker.R
import com.geoideas.gpstracker.repository.Repository
import kotlin.concurrent.thread


class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        supportFragmentManager
            .beginTransaction()
            .replace(
                R.id.settings,
                SettingsFragment()
            )
            .commit()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
            val cxt = this.requireContext()
            val button = findPreference<Preference>("data_delete_all")
            button?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                AlertDialog.Builder(cxt).run {
                    setTitle("Delete Location Data")
                    setMessage("All location tracking data will be deleted. This action cannot be undone.")
                    create()
                    setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss()}
                    setPositiveButton("Delete") { _, _ ->
                        thread {
                            Repository(cxt).db.pointDao().deleteAll()
                        }
                    }
                }.show()
                true
            }
        }
    }
}