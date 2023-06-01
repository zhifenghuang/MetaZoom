package com.common.lib.activity

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Html
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.common.lib.constant.Constants
import com.common.lib.constant.EventBusEvent
import com.common.lib.fragment.BaseFragment
import com.common.lib.mvp.IPresenter
import com.common.lib.network.OkHttpManager
import com.common.lib.utils.BaseUtils
import com.common.lib.utils.LogUtil
import com.common.lib.utils.MD5Util
import com.gyf.immersionbar.ImmersionBar
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.File
import java.io.FileInputStream
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


abstract class BaseActivity<P : IPresenter> : BaseDialogActivity(), View.OnClickListener {
    companion object {
        const val TAG = "BaseActivity"
    }

    protected var autoRequestPermission = true
    protected var presenter: P? = null

    protected abstract fun getLayoutId(): Int

    protected abstract fun onCreated(savedInstanceState: Bundle?)

    protected abstract fun onCreatePresenter(): P

    protected lateinit var context: Context

    protected val compositeDisposable = CompositeDisposable()
    protected val mFragments: ArrayList<BaseFragment<*>> = ArrayList()
    protected var mCurrentFragment: BaseFragment<*>? = null
    protected var mCurrentFragmentPosition = 0

    protected var isFinish = false

    /**
     * onCreate 後 的第一次 onResume 不調用onActivityResume
     * **/
    protected var mCreated = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        val language = DataManager.getInstance().getLanguage()
//        BaseUtils.changeAppLanguage(this, language)
        LogUtil.LogE(javaClass)
        mCreated = true
        context = this
        isFinish = false
        setContentView(getLayoutId())
        EventBus.getDefault().register(this)
        createAndBindPresenter()
        initImmersionBar()
        onCreated(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        if (!mCreated) {
            mCurrentFragment?.onActivityResume()
        } else {
            mCreated = false
        }
    }

    override fun onStart() {
        super.onStart()
        if (this.autoRequestPermission) {
            requestExternalStoragePermission()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
        unbindPresenter()
        isFinish = true
        compositeDisposable.clear()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    open fun onReceive(map: HashMap<String, Any>) {
        if (map.containsKey(EventBusEvent.FINISH_ACTIVITIES)) {
            finish()
        } else if (map.containsKey(EventBusEvent.FINISH_OTHER_ACTIVITIES)) {
            if (!javaClass.toString()
                    .contains("MainActivity")
            ) {
                finish()
            }
        }
    }

    open fun finishAllActivity() {
        val map = HashMap<String, Any>();
        map[EventBusEvent.FINISH_ACTIVITIES] = ""
        EventBus.getDefault().post(map)
    }

    open fun finishAllOtherActivity() {
        val map = HashMap<String, Any>();
        map[EventBusEvent.FINISH_OTHER_ACTIVITIES] = ""
        EventBus.getDefault().post(map)
    }

    override fun onLogout() {
        presenter?.logout()
        finishAllActivity()
//        try {
//            val intent = Intent()
//            val com = ComponentName(
//                packageName,
//                "com.blokbase.pos.activity.LoginActivity"
//            )
//            intent.component = com
//            startActivity(intent)
//        } catch (e: Exception) {
//
//        }
    }

    private fun createAndBindPresenter() {
        unbindPresenter()
        presenter = onCreatePresenter()
        presenter?.onBind()
    }

    private fun unbindPresenter() {
        presenter?.onUnbind()
        presenter = null
    }

    open fun showKeyboard(view: View?) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, 0)
    }

    open fun hideKeyboard(view: View?) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if (view == null) {
            currentFocus?.let {
                imm.hideSoftInputFromWindow(it.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
            }
        } else {
            imm.hideSoftInputFromWindow(view.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
        }
    }

    protected open fun setTopStatusBarStyle(topView: View) {
        topView.setPadding(
            topView.paddingLeft, BaseUtils.getStatusBarHeight(resources) + topView.paddingTop,
            topView.paddingRight, topView.paddingBottom
        )
    }

    protected open fun setTopStatusBarStyle(id: Int) {
        setTopStatusBarStyle(findViewById(id))
    }

    protected fun setViewsOnClickListener(vararg views: View) {
        for (view in views) {
            view.setOnClickListener(this)
        }
    }

    protected fun setViewsOnClickListener(vararg ids: Int) {
        for (id in ids) {
            findViewById<View>(id)?.setOnClickListener(this)
        }
    }

    protected fun setTextColor(tv: TextView, colorResId: Int) {
        tv.setTextColor(ContextCompat.getColor(this, colorResId))
    }

    protected fun setTextColor(id: Int, colorResId: Int) {
        findViewById<TextView>(id).setTextColor(ContextCompat.getColor(this, colorResId))
    }

    protected fun setText(id: Int, strId: Int) {
        findViewById<TextView>(id).text = getString(strId)
    }

    protected fun setText(tv: TextView, str: String?) {
        if (str == null) {
            tv.text = ""
        } else {
            tv.text = str
        }
    }

    protected fun setText(id: Int, str: String?) {
        val tv = findViewById<TextView>(id)
        if (str == null) {
            tv.text = ""
        } else {
            tv.text = str
        }
        if (tv is EditText) {
            (tv as EditText).setSelection(tv.text.length)
        }
    }

    protected fun setEditTextHint(id: Int, strId: Int) {
        findViewById<EditText>(id).hint = getString(strId)
    }

    protected fun setEditTextHint(id: Int, str: String?) {
        if (str == null) {
            findViewById<EditText>(id).hint = ""
        } else {
            findViewById<EditText>(id).hint = str
        }
    }

    protected fun setImage(id: Int, resId: Int) {
        findViewById<ImageView>(id).setImageResource(resId)
    }

    protected fun setImage(id: Int, bmp: Bitmap) {
        findViewById<ImageView>(id).setImageBitmap(bmp)
    }

    protected fun setBackground(id: Int, bmp: Bitmap) {
        findViewById<View>(id).background = BitmapDrawable(resources, bmp)
    }

    protected fun setBackgroundColor(id: Int, color: Int) {
        findViewById<View>(id).setBackgroundColor(ContextCompat.getColor(this, color))
    }

    protected fun setHtml(id: Int, str: String) {
        findViewById<TextView>(id).setText(
            Html.fromHtml(
                str,
                Html.ImageGetter { source ->
                    if (isFinish) {
                        return@ImageGetter null
                    }
                    var url = source
                    if (!source.startsWith("http")) {
                        url = "http:" + source
                    }
                    val fileName = MD5Util.getMd5(url)
                    val file = File(BaseUtils.getSaveFilePath(context, fileName!!))
                    if (file.exists()) {
                        try {
                            val `is` = FileInputStream(file);
                            val d =
                                Drawable.createFromStream(`is`, "src")
                            var width = getDisplayMetrics()!!.widthPixels
                            var height = d!!.intrinsicHeight * 1.0 * width / d.intrinsicHeight
                            d.setBounds(
                                0, 0, width,
                                height.toInt()
                            )
                            `is`.close()
                            return@ImageGetter d
                        } catch (e: Exception) {
                            return@ImageGetter null
                        }
                    }
                    OkHttpManager.getInstance().downloadAsync(url, file, object :
                        OkHttpManager.HttpCallBack {
                        override fun successful(f: File) {
                            if (!isFinish) {
                                runOnUiThread {
                                    setHtml(id, str)
                                }
                            }
                        }

                        override fun progress(progress: Int) {

                        }

                        override fun failed(e: java.lang.Exception?) {

                        }
                    })
                    return@ImageGetter null
                }, null
            )
        )
    }

    protected fun getTextById(id: Int): String {
        return findViewById<TextView>(id).text.toString().trim()
    }


    protected fun setViewVisible(vararg views: View) {
        for (view in views) {
            view.visibility = View.VISIBLE
        }
    }

    protected fun setViewVisible(vararg ids: Int) {
        for (id in ids) {
            findViewById<View>(id)?.visibility = View.VISIBLE
        }
    }

    protected fun setViewGone(vararg views: View) {
        for (view in views) {
            view.visibility = View.GONE
        }
    }

    protected fun setViewGone(vararg ids: Int) {
        for (id in ids) {
            findViewById<View>(id)?.visibility = View.GONE
        }
    }

    protected fun setViewInvisible(vararg views: View) {
        for (view in views) {
            view.visibility = View.INVISIBLE
        }
    }

    protected fun setViewInvisible(vararg ids: Int) {
        for (id in ids) {
            findViewById<View>(id)?.visibility = View.INVISIBLE
        }
    }

    fun openActivity(cls: Class<*>) {
        openActivity(cls, null)
    }

    fun openActivity(pagerClass: Class<*>, bundle: Bundle?) {
        if (Activity::class.java.isAssignableFrom(pagerClass)) {
            val intent = Intent(this, pagerClass)
            if (bundle != null) {
                intent.putExtras(bundle)
            }
            startActivity(intent)
        } else {
            val name: String = pagerClass.name
            val intent = Intent(this, EmptyActivity::class.java)
            if (bundle != null) {
                intent.putExtras(bundle)
            }
            intent.putExtra(EmptyActivity.KEY_FRAGMENT_NAME, name)
            startActivity(intent)
        }
    }

    /**
     * 点后退按钮触发
     *
     * @param view
     */
    open fun onBackClick(view: View) {
        finish()
    }

    /**
     * 初始化状态栏
     */
    protected open fun initImmersionBar() {
        ImmersionBar.with(this)
            .statusBarDarkFont(true) //状态栏字体是深色，不写默认为亮色
            .init()
    }

    open fun getDisplayMetrics(): DisplayMetrics? {
        val displayMetrics = DisplayMetrics()
        val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics
    }

    private fun openPhotoPreviewActivity(path: String) {
        val bundle = Bundle()
        bundle.putString(Constants.BUNDLE_EXTRA, path)
        openActivity(PhotoPreviewActivity::class.java, bundle)
    }

    fun showToast(text: String) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
    }

    fun showToast(resId: Int) {
        Toast.makeText(this, resId, Toast.LENGTH_SHORT).show()
    }

    fun lockView(view: View?) {
        view?.isClickable = false
        compositeDisposable.add(
            Single.timer(100, TimeUnit.MILLISECONDS)
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    view?.isClickable = true
                }, {
                    Log.e(BaseFragment.TAG, "lockView fail: " + it.toString())
                })
        )
    }

    open fun getContainerViewId(): Int {
        return 0
    }

    fun switchFragment(to: BaseFragment<*>) {
        var hasSelectedFragment = false
        if (mCurrentFragment !== to) {
            val ft = supportFragmentManager.beginTransaction()
            if (!to.isAdded) {
                if (mCurrentFragment != null) {
                    ft.hide(mCurrentFragment!!)
                }
                ft.add(getContainerViewId(), to).commitAllowingStateLoss()
            } else {
                if (mCurrentFragment != null) {
                    ft.hide(mCurrentFragment!!)
                }
                ft.show(to).commitAllowingStateLoss()
                hasSelectedFragment = true
            }
        }
        mCurrentFragment = to
        for ((i, f) in mFragments.withIndex()) {
            if (mCurrentFragment == f) {
                mCurrentFragmentPosition = i
                break
            }
        }

        if (hasSelectedFragment) {
            mCurrentFragment?.onSelectFragment()
        }
    }


    open fun setTextViewLinearGradient(vararg textViewIds: Int) {
        for (id in textViewIds) {
            val textView = findViewById<TextView>(id)
            val linearGradient = LinearGradient(
                0f, 0f,
                textView.paint.textSize * textView.text.length, 0f,
                Color.parseColor("#F89512"),
                Color.parseColor("#ED282A"), Shader.TileMode.CLAMP
            )
            textView.paint.shader = linearGradient
            textView.invalidate()
        }
    }

    open fun setTextViewLinearGradient2(vararg textViewIds: Int) {
        for (id in textViewIds) {
            val textView = findViewById<TextView>(id)
            val linearGradient: LinearGradient = LinearGradient(
                0f, 0f,
                textView.paint.textSize * textView.text.length, 0f,
                Color.parseColor("#D29C54"),
                Color.parseColor("#E8C687"), Shader.TileMode.CLAMP
            )
            textView.paint.shader = linearGradient
            textView.invalidate()
        }
    }

    open fun setTextViewLinearGradientNull(vararg textViewIds: Int) {
        for (id in textViewIds) {
            val textView = findViewById<TextView>(id)
            textView.paint.shader = null
            textView.invalidate()
        }
    }
}


