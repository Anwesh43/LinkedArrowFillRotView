package com.anwesh.uiprojects.arrowfillrotview

/**
 * Created by anweshmishra on 25/09/18.
 */

import android.app.Activity
import android.view.View
import android.view.MotionEvent
import android.graphics.Paint
import android.graphics.Canvas
import android.graphics.Path
import android.graphics.Color
import android.content.Context
import android.util.Log

val nodes : Int = 5

fun Canvas.drawAFRNode(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    val gap : Float = h / (nodes + 1)
    val wSize : Float = gap / 3
    val hSize : Float = gap / 2
    paint.strokeWidth = Math.min(w, h) / 80
    paint.strokeCap = Paint.Cap.ROUND
    paint.style = Paint.Style.FILL_AND_STROKE
    paint.color = Color.parseColor("#7B1FA2")
    save()
    translate(w/2, gap * i * gap)
    for (j in 0..1) {
        val sf : Float = 1f - 2 * j
        val sc : Float = Math.min(0.5f, Math.max(0f, scale - j * 0.5f)) * 2
        val sc1 : Float = Math.min(0.5f, sc) * 2
        val sc2 : Float = Math.min(0.5f, Math.max(0f, sc - 0.5f)) * 2
        save()
        translate((w/2 - hSize - wSize) * sc2 * sf, gap/2)
        rotate(90f * sf * sc1)
        drawRect(-wSize/2, -hSize, wSize/2, 0f, paint)
        val path : Path = Path()
        path.moveTo(-wSize/2, -hSize)
        path.lineTo(0f, -hSize - wSize)
        path.lineTo(wSize/2, -hSize)
        drawPath(path, paint)
        restore()
    }
    restore()
}

class ArrowFillRotView(ctx : Context) : View(ctx) {

    private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val renderer : Renderer = Renderer(this)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas, paint)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var prevScale : Float = 0f, var dir : Float = 0f){
        fun update(cb : (Float) -> Unit) {
            scale += 0.025f * dir
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1f - 2 * prevScale
                cb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(50)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class AFRNode(var i : Int, val state : State = State()) {

        private var next : AFRNode? = null
        private var prev : AFRNode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < nodes - 1) {
                next = AFRNode(i + 1)
                next?.prev = this
            }
        }

        override fun toString(): String {
            return "${i}, next : ${next?.i}, prev : ${prev?.i}"
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawAFRNode(i, state.scale, paint)
            next?.draw(canvas, paint)
        }

        fun update(cb : (Int, Float) -> Unit) {
            state.update {
                cb(i, it)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            Log.d("curr", this.toString())
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : AFRNode {
            var curr : AFRNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class LinkedAFR(var i : Int) {
        private val root : AFRNode = AFRNode(0)
        private var curr : AFRNode = root
        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            root.draw(canvas, paint)
        }

        fun update(cb : (Int, Float) ->  Unit) {
            curr.update {i, scl ->
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                Log.d("curr", curr.toString())
                cb(i, scl)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }

    data class Renderer(var view : View, var animated : Boolean = false) {

        private val afr : LinkedAFR = LinkedAFR(0)
        private val animator : Animator = Animator(view)

        fun render(canvas : Canvas, paint : Paint) {
            canvas.drawColor(Color.parseColor("#BDBDBD"))
            afr.draw(canvas, paint)
            animator.animate {
                afr.update {i, scl ->
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            afr.startUpdating {
                animator.start()
            }
        }
    }

    companion object {
        fun create(activity : Activity) : ArrowFillRotView {
            val view : ArrowFillRotView = ArrowFillRotView(activity)
            activity.setContentView(view)
            return view
        }
    }
}