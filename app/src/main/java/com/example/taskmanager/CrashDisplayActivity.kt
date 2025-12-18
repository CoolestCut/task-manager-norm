package com.example.taskmanager

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class CrashDisplayActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crash_display)

        val errorText = intent.getStringExtra("error")
        findViewById<TextView>(R.id.tvErrorText).text = errorText
    }
}