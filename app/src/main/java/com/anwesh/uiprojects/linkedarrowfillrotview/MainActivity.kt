package com.anwesh.uiprojects.linkedarrowfillrotview

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.anwesh.uiprojects.arrowfillrotview.ArrowFillRotView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ArrowFillRotView.create(this)
    }
}
