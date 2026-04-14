package edu.gvsu.cis.kmp_wordy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            val db: AppDB = getDatabaseInstance(getDatabaseBuilder(applicationContext))
            App(db.getDao())
        }
    }
}