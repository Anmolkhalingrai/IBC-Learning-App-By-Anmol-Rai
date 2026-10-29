
package com.example.ibclearningapp

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {

    private val forest = Color.rgb(20, 65, 47)
    private val gold = Color.rgb(196, 157, 83)
    private val cream = Color.rgb(249, 246, 237)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = forest
        window.navigationBarColor = forest

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(28, 32, 28, 32)
            setBackgroundColor(cream)
        }

        val emblem = TextView(this).apply {
            text = "✦"
            textSize = 48f
            setTextColor(gold)
            gravity = Gravity.CENTER
        }

        val title = TextView(this).apply {
            text = "IBC LEARNING APP"
            textSize = 27f
            setTextColor(forest)
            gravity = Gravity.CENTER
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        val byline = TextView(this).apply {
            text = "by Anmol Rai"
            textSize = 17f
            setTextColor(gold)
            gravity = Gravity.CENTER
        }

        val subtitle = TextView(this).apply {
            text = "YOUR LEARNING JOURNEY STARTS HERE"
            textSize = 12f
            setTextColor(forest)
            gravity = Gravity.CENTER
        }

        root.addView(emblem)
        root.addView(title)
        root.addView(byline)

        val gap = android.view.View(this)
        root.addView(gap, LinearLayout.LayoutParams(1, 20))

        root.addView(subtitle)
        setContentView(root)
    }
}
