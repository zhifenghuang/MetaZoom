package com.common.lib.activity

import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import androidx.fragment.app.Fragment
import com.common.lib.R
import com.common.lib.fragment.BaseFragment
import com.common.lib.mvp.contract.EmptyContract
import com.common.lib.mvp.presenter.EmptyPresenter

class EmptyActivity : BaseActivity<EmptyPresenter>(), EmptyContract.View {

    companion object {
        const val KEY_FRAGMENT_NAME = "fragment_name"
    }

    private var mCanBackFinish = true


    override fun onCreatePresenter(): EmptyPresenter {
        return EmptyPresenter(this)
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_empty
    }

    override fun onCreated(savedInstanceState: Bundle?) {
        mCanBackFinish = true
        val intent = intent
        val fragmentName = intent.getStringExtra(KEY_FRAGMENT_NAME)!!
        val fragment = Fragment.instantiate(
            this,
            fragmentName
        ) as BaseFragment<*>
        val b = intent.extras
        fragment.arguments = b

        mFragments.add(fragment)
        switchFragment(fragment)
    }

    override fun onClick(v: View?) {
    }

    fun setCanBackFinish(canBackFinish: Boolean) {
        mCanBackFinish = canBackFinish
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (!mCanBackFinish) {
                return false
            }
            return false
        }
        return super.onKeyDown(keyCode, event)
    }

}