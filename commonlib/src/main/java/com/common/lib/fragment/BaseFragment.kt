package com.common.lib.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Html
import android.text.Html.ImageGetter
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.common.lib.activity.BaseActivity
import com.common.lib.mvp.IPresenter
import com.common.lib.network.OkHttpManager
import com.common.lib.network.OkHttpManager.HttpCallBack
import com.common.lib.utils.BaseUtils
import com.common.lib.utils.LogUtil
import com.common.lib.utils.MD5Util
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.File
import java.io.FileInputStream
import java.util.concurrent.TimeUnit

@SuppressLint("UseRequireInsteadOfGet")
abstract class BaseFragment<P : IPresenter> : BaseDialogFragment(), View.OnClickListener {
    companion object {
        const val TAG = "BaseFragment"
    }


    protected abstract fun onCreatePresenter(): P

    protected abstract fun getLayoutId(): Int

    /**
     * fragment的View创建好后调用
     */
    protected abstract fun initView(view: View, savedInstanceState: Bundle?)

    protected var presenter: P? = null
    protected val compositeDisposable = CompositeDisposable()
    protected val mFragments: ArrayList<BaseFragment<*>> = ArrayList()
    protected var mCurrentFragment: BaseFragment<*>? = null
    protected var mCurrentFragmentPosition = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
//        val language = DataManager.getInstance().getLanguage()
//        BaseUtils.changeAppLanguage(context!!, language)
        return inflater.inflate(getLayoutId(), null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        LogUtil.LogE(javaClass)
        bindPresenter()
        initView(view, savedInstanceState)
        EventBus.getDefault().register(this)
//        if (isNeedSetTopStyle()) {
//            setTopStatusBarStyle(view);
//        }
    }

    open fun onSelectFragment() {
        mCurrentFragment?.onActivityResume()
    }

    /**
     * Fragment 的 onResume 在某些裝置上是不會被調用的
     * **/
    open fun onActivityResume() {
        mCurrentFragment?.onActivityResume()
    }

    open fun onRefresh() {

    }

    override fun onDestroyView() {
        super.onDestroyView()
        EventBus.getDefault().unregister(this)
        unbindPresenter()
        compositeDisposable.clear()
    }

    override fun onLogout() {
        presenter?.logout()
    }

    private fun bindPresenter() {
        unbindPresenter()
        presenter = onCreatePresenter()
        presenter?.onBind()
    }

    private fun unbindPresenter() {
        presenter?.onUnbind()
        presenter = null
    }

    protected fun setViewsOnClickListener(vararg views: View) {
        for (view in views) {
            view.setOnClickListener(this)
        }
    }

    protected fun setViewsOnClickListener(vararg viewIds: Int) {
        for (viewId in viewIds) {
            view!!.findViewById<View>(viewId).setOnClickListener(this)
        }
    }

    protected fun setTextColor(tv: TextView, clorId: Int) {
        tv.setTextColor(ContextCompat.getColor(activity!!, clorId))
    }

    protected fun setTextColor(id: Int, clorId: Int) {
        val tv = view!!.findViewById<TextView>(id)
        tv.setTextColor(ContextCompat.getColor(activity!!, clorId))
    }

    protected fun setText(tv: TextView, serverKey: String) {
        tv.text = serverKey
        if (tv is EditText) {
            (tv as EditText).setSelection(tv.text.length)
        }
    }


    protected fun setText(id: Int, strId: Int) {
        view!!.findViewById<TextView>(id).setText(getString(strId))
    }


    protected fun setText(id: Int, str: String?) {
        val tv = view!!.findViewById<TextView>(id)
        tv.text = str ?: ""
        if (tv is EditText) {
            (tv as EditText).setSelection(tv.text.length)
        }
    }

    protected fun setEditTextHint(id: Int, strId: Int) {
        view!!.findViewById<EditText>(id).hint = getString(strId)
    }

    protected fun setEditTextHint(id: Int, str: String?) {
        if (str == null) {
            view!!.findViewById<EditText>(id).hint = ""
        } else {
            view!!.findViewById<EditText>(id).hint = str
        }
    }

    protected fun getTextById(id: Int): String {
        return view!!.findViewById<TextView>(id).text.toString().trim()
    }

    protected fun getTextById(tv: TextView): String {
        return tv.text.toString().trim()
    }

    protected fun setViewVisible(vararg views: View) {
        for (view in views) {
            view.visibility = View.VISIBLE
        }
    }

    protected fun setViewGone(vararg views: View) {
        for (view in views) {
            view.visibility = View.GONE
        }
    }

    protected fun setViewInvisible(vararg views: View) {
        for (view in views) {
            view.visibility = View.INVISIBLE
        }
    }

    protected fun setViewInvisible(vararg ids: Int) {
        for (id in ids) {
            view!!.findViewById<View>(id).visibility = View.INVISIBLE
        }
    }

    protected fun setHtml(id: Int, str: String) {
        LogUtil.LogE(str)
        view!!.findViewById<TextView>(id).setText(
            Html.fromHtml(
                str,
                ImageGetter { source ->
                    LogUtil.LogE(source)
                    if (view == null) {
                        return@ImageGetter null
                    }
                    val fileName = MD5Util.getMd5(source)
                    val file = File(BaseUtils.getSaveFilePath(activity, fileName!!))
                    if (file.exists()) {
                        LogUtil.LogE(file.absoluteFile)
                        try {
                            val `is` = FileInputStream(file);
                            val d =
                                Drawable.createFromStream(`is`, "src")
                            d!!.setBounds(
                                0, 0, d.intrinsicWidth,
                                d.intrinsicHeight
                            )
                            `is`.close()
                            return@ImageGetter d
                        } catch (e: Exception) {
                            return@ImageGetter null
                        }
                    }
                    LogUtil.LogE(source)
                    OkHttpManager.getInstance().downloadAsync(source, file, object : HttpCallBack {
                        override fun successful(file: File) {
                            if (view != null) {
                                activity!!.runOnUiThread {
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

    protected fun setViewGone(vararg ids: Int) {
        for (id in ids) {
            view!!.findViewById<View>(id)?.visibility = View.GONE
        }
    }


    protected fun setViewVisible(vararg ids: Int) {
        for (id in ids) {
            view!!.findViewById<View>(id)?.visibility = View.VISIBLE
        }
    }

    protected fun setViewInVisible(vararg ids: Int) {
        for (id in ids) {
            view!!.findViewById<View>(id)?.visibility = View.INVISIBLE
        }
    }

    protected fun setImage(id: Int, resId: Int) {
        view!!.findViewById<ImageView>(id).setImageResource(resId)
    }

    protected fun setBackground(id: Int, resId: Int) {
        view!!.findViewById<View>(id).setBackgroundResource(resId)
    }

    protected fun openActivity(cls: Class<*>) {
        openActivity(cls, null)
    }

    protected fun openActivity(cls: Class<*>, bundle: Bundle?) {
        (activity as BaseActivity<*>).openActivity(cls, bundle)
    }

    protected open fun setTopStatusBarStyle(id: Int) {
        setTopStatusBarStyle(view!!.findViewById(id))
    }


    protected open fun setTopStatusBarStyle(topView: View) {
        topView.setPadding(
            topView.paddingLeft,
            BaseUtils.getStatusBarHeight(resources) + topView.paddingTop,
            topView.paddingRight,
            topView.paddingBottom
        )
    }

    open fun getDisplayMetrics(): DisplayMetrics? {
        val displayMetrics = DisplayMetrics()
        val windowManager = context?.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    open fun onReceive(map: HashMap<String, Any>) {
    }

    open fun showKeyboard(view: View?) {
        val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        imm?.showSoftInput(view, 0)
    }

    open fun hideKeyboard() {
        if (view != null) {
            val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
            imm?.hideSoftInputFromWindow(view?.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
        }
    }


    fun showToast(text: String) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
    }

    fun showToast(resId: Int) {
        Toast.makeText(context, resId, Toast.LENGTH_SHORT).show()
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
                    Log.e(TAG, "lockView fail: " + it.toString())
                })
        )
    }

    fun finish() {
        activity?.finish()
    }

    open fun getContainerViewId(): Int {
        return 0
    }

    fun switchFragment(to: BaseFragment<*>) {
        var hasSelectedFragment = false
        if (mCurrentFragment !== to) {
            val ft = childFragmentManager.beginTransaction()
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

    protected fun goLogin(cls: Class<*>): Boolean {
//        if (DataManager.getInstance().getMyInfo() == null) {
//            openActivity(cls)
//            return true
//        }
        return false
    }


    open fun setTextViewLinearGradient(vararg textViewIds: Int) {
        for (id in textViewIds) {
            val textView = view!!.findViewById<TextView>(id)
            val linearGradient: LinearGradient = LinearGradient(
                0f, 0f,
                textView.paint.textSize * textView.text.length, 0f,
                Color.parseColor("#304062"),
                Color.parseColor("#6E8BB2"), Shader.TileMode.CLAMP
            )
            textView.paint.shader = linearGradient
            textView.invalidate()
        }
    }

    open fun setTextViewLinearGradient2(vararg textViewIds: Int) {
        for (id in textViewIds) {
            val textView = view!!.findViewById<TextView>(id)
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

//    open fun isNeedSetTopStyle(): Boolean {
//        return false
//    }

}
