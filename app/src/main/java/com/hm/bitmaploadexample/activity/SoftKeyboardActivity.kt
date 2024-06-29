package com.hm.bitmaploadexample.activity

import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.hm.bitmaploadexample.R
import com.hm.bitmaploadexample.databinding.ActivitySoftKeyboardBinding


/**
 * Created by p_dmweidu on 2023/6/1
 * Desc: 监听软键盘的弹起和关闭
 */
class SoftKeyboardActivity : AppCompatActivity(), ViewTreeObserver.OnGlobalLayoutListener {


    companion object {

        private val TAG = "SoftKeyboardActivity"

        fun launch(context: Context) {
            val intent = Intent(context, SoftKeyboardActivity::class.java)
            context.startActivity(intent)
        }
    }

    private var activityRootView: View? = null

    //设定一个认为是软键盘弹起的阈值
    private var softKeyboardHeight: Int = 0
    private var heightPixels: Int = 0

    private var im: InputMethodManager? = null

    private lateinit var binding: ActivitySoftKeyboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySoftKeyboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val dm = resources.displayMetrics
        //设定一个认为是软键盘弹起的阈值
        softKeyboardHeight = (100 * dm.density).toInt()

        //屏幕高度
        heightPixels = dm.heightPixels

        Log.i(TAG, "onCreate: softKeyboardHeight= $softKeyboardHeight,heightPixels= $heightPixels")

        im = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        activityRootView = window.decorView.findViewById(android.R.id.content)
        activityRootView?.viewTreeObserver?.addOnGlobalLayoutListener(this)

        binding.tvOpenKeyBoard.setOnClickListener {
            showKeyBoard(binding.editText)
            //showKeyBoard()
        }

        binding.tvCloseKeyBoard.setOnClickListener {
            hideKeyBoard()
        }
    }

    override fun onGlobalLayout() {
        activityRootView?.let {
            if (isKeyboardShown(it)) {
                Log.e(TAG, "软键盘弹起")
            } else {
                Log.e(TAG, "软键盘关闭")
            }
        }
    }

    private fun isKeyboardShown(rootView: View): Boolean {
        //得到屏幕可见区域的大小
        val r = Rect()
        rootView.getWindowVisibleDisplayFrame(r)
        val heightDiff = heightPixels - r.bottom
        return heightDiff > softKeyboardHeight
    }

    private fun showKeyBoard(editText: EditText) {
        //第二个参数传0就行，不必纠结为什么，官方文档也没有说清楚
        im?.showSoftInput(editText, InputMethodManager.SHOW_FORCED)
    }

    private fun isKeyboardShown2(rootView: View): Boolean {
        val dm = rootView.resources.displayMetrics
        //设定一个认为是软键盘弹起的阈值
        val softKeyboardHeight = (100 * dm.density).toInt()
        //得到屏幕可见区域的大小
        val r = Rect()
        rootView.getWindowVisibleDisplayFrame(r)
        //rootView 的bottom和当前屏幕可见区域bottom的差值
        val heightDiff = rootView.bottom - r.bottom
        return heightDiff > softKeyboardHeight
    }


    private fun showKeyBoard() {
        im?.toggleSoftInput(0, 0)
    }

    private fun hideKeyBoard() {
        im?.hideSoftInputFromWindow(
            binding.tvOpenKeyBoard.windowToken,
            InputMethodManager.HIDE_NOT_ALWAYS
        )

    }

    override fun onDestroy() {
        super.onDestroy()
        activityRootView?.viewTreeObserver?.removeOnGlobalLayoutListener(this)
    }

}
