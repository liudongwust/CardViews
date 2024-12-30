package com.wustfly.example

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.wustfly.example.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = DataBindingUtil.inflate<ActivityMainBinding>(layoutInflater, R.layout.activity_main, findViewById(android.R.id.content), false)
        setContentView(binding.root)
    }
}