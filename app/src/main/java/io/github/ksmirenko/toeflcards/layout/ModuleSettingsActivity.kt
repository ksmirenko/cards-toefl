package io.github.ksmirenko.toeflcards.layout

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceFragment

import io.github.ksmirenko.toeflcards.R

class ModuleSettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_module_settings)

        fragmentManager.beginTransaction()
            .replace(android.R.id.content, ModuleSettingsFragment())
            .commit();
    }

    class ModuleSettingsFragment : PreferenceFragment() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.module_settings)
        }
    }
}
