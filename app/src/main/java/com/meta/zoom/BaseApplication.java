package com.meta.zoom;

import android.os.StrictMode;
import android.text.TextUtils;

import androidx.multidex.MultiDexApplication;

import com.alsc.chat.manager.ChatManager;
import com.common.lib.manager.ConfigurationManager;
import com.common.lib.activity.db.DatabaseOperate;
import com.common.lib.manager.DataManager;
import com.meta.zoom.wallet.AppFilePath;

public class BaseApplication extends MultiDexApplication {


//    static {//static 代码段可以防止内存泄露
//        //设置全局的Header构建器
//        SmartRefreshLayout.setDefaultRefreshHeaderCreater(new DefaultRefreshHeaderCreater() {
//            @Override
//            public RefreshHeader createRefreshHeader(Context context, RefreshLayout layout) {
//                layout.setPrimaryColorsId(R.color.color_bg_theme, R.color.color_16_16_16);//全局设置主题颜色
//                return new ClassicsHeader(context).setSpinnerStyle(SpinnerStyle.Translate);//指定为经典Header，默认是 贝塞尔雷达Header
//            }
//        });
//
//        //设置全局的Footer构建器
//        SmartRefreshLayout.setDefaultRefreshFooterCreater(new DefaultRefreshFooterCreater() {
//            @Override
//            public RefreshFooter createRefreshFooter(Context context, RefreshLayout layout) {
//                //指定为经典Footer，默认是 BallPulseFooter
//                return new ClassicsFooter(context).setSpinnerStyle(SpinnerStyle.Translate);
//            }
//        });
//    }

    @Override
    public void onCreate() {
        super.onCreate();
        System.loadLibrary("TrustWalletCore");
        AppFilePath.init(this);
        ConfigurationManager.Companion.getInstance().setContext(this);
        DatabaseOperate.setContext(this);
        DatabaseOperate.getInstance();
        String token = DataManager.getInstance().getToken();
        ChatManager.getInstance().setContext(this);
        ChatManager.getInstance().initWebSocket(token);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        builder.detectFileUriExposure();
//
//        CrashReport.initCrashReport(getApplicationContext(), "7d629e6f81", false);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }
}
