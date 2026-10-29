package com.example.ibclearningapp

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {

    private val forest = Color.rgb(15, 48, 36)
    private val gold = Color.rgb(218, 180, 105)
    private val cream = Color.rgb(250, 247, 239)
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = forest
        window.navigationBarColor = forest

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(28, 32, 28, 32)
            setBackgroundColor(forest)
        }

        val ornament = TextView(this).apply {
            text = "✦"
            textSize = 38f
            setTextColor(gold)
            gravity = Gravity.CENTER
        }

        val brand = TextView(this).apply {
            text = "I B C"
            textSize = 52f
            setTextColor(cream)
            gravity = Gravity.CENTER
            typeface = Typeface.create("serif", Typeface.BOLD)
            letterSpacing = 0.12f
        }

        val divider = View(this).apply {
            setBackgroundColor(gold)
        }

        val title = TextView(this).apply {
            text = "Learning App"
            textSize = 29f
            setTextColor(gold)
            gravity = Gravity.CENTER
            typeface = Typeface.create("serif", Typeface.NORMAL)
        }

        val byline = TextView(this).apply {
            text = "BY ANMOL RAI"
            textSize = 13f
            setTextColor(cream)
            gravity = Gravity.CENTER
            letterSpacing = 0.22f
        }

        fun space(height: Int) {
            root.addView(View(this), LinearLayout.LayoutParams(1, height))
        }

        root.addView(ornament)
        space(12)
        root.addView(brand)
        space(14)
        root.addView(divider, LinearLayout.LayoutParams(110, 2))
        space(16)
        root.addView(title)
        space(12)
        root.addView(byline)

        setContentView(root)

        handler.postDelayed({
            showHomeScreen()
        }, 3000)
    }

    private fun showHomeScreen() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(24, 24, 24, 24)
            setBackgroundColor(cream)
        }

        val title = TextView(this).apply {
            text = "Welcome to IBC"
            textSize = 28f
            setTextColor(forest)
            gravity = Gravity.CENTER
            typeface = Typeface.create("serif", Typeface.BOLD)
        }

        val subtitle = TextView(this).apply {
            text = "Your learning journey starts here."
            textSize = 16f
            setTextColor(forest)
            gravity = Gravity.CENTER
        }

        root.addView(title)
        root.addView(View(this), LinearLayout.LayoutParams(1, 14))
        root.addView(subtitle)
        setContentView(root)
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }
}
