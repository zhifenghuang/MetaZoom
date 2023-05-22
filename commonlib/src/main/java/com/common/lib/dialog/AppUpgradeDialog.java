package com.common.lib.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.content.FileProvider;

import com.blankj.utilcode.util.AppUtils;
import com.common.lib.R;
import com.common.lib.activity.BaseActivity;
import com.common.lib.bean.VersionBean;
import com.common.lib.manager.ConfigurationManager;
import com.common.lib.network.OkHttpManager;
import com.common.lib.utils.BaseUtils;
import com.common.lib.utils.LogUtil;

import java.io.File;

public class AppUpgradeDialog extends Dialog implements View.OnClickListener {
    private Context mContext;
    private VersionBean mVersionBean;

    public AppUpgradeDialog(Context context, VersionBean bean) {
        this(context, R.style.LoadingDialog, bean);
    }

    public AppUpgradeDialog(Context context, int themeResId, VersionBean bean) {
        super(context, themeResId);
        setContentView(R.layout.common_updrade_dialog);
        mContext = context;
        mVersionBean = bean;
        Window view = getWindow();
        WindowManager.LayoutParams lp = view.getAttributes();
        lp.width = BaseUtils.StaticParams.dp2px(context, 293); // 设置宽度充满屏幕
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        view.setGravity(Gravity.CENTER);
        findViewById(R.id.tvUpgrade).setOnClickListener(this);
        findViewById(R.id.tvRetry).setOnClickListener(this);

        String content = "";
        if (bean.getContent() != null && bean.getContent().contains("&")) {
            String[] strs = bean.getContent().split("&");
            int length = strs.length;
            for (int i = 0; i < length; ++i) {
                content += strs[i];
                if (i < length - 1) {
                    content += "\n";
                }
            }
        } else {
            content = bean.getContent();
        }
        ((TextView) findViewById(R.id.tvContent)).setText(content);
        ((TextView) findViewById(R.id.tvVersion)).setText("V" + bean.getVersionName());
        setCancelable(false);
        setCanceledOnTouchOutside(false);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.tvUpgrade || id == R.id.tvRetry) {
            findViewById(R.id.ll).setVisibility(View.GONE);
            findViewById(R.id.llRetry).setVisibility(View.GONE);
            findViewById(R.id.rl).setVisibility(View.VISIBLE);
            if (!TextUtils.isEmpty(mVersionBean.getLink())) {
                downloadApk(mVersionBean.getLink());
            }
        }
    }


    private void downloadApk(String url) {
        final File file = new File(createApkPath());
        if (file.exists()) {
            file.delete();
        }
        OkHttpManager.Companion.getInstance().downloadAsyncWithProgress(url, file, new OkHttpManager.HttpCallBack() {
            @Override
            public void successful(final File f) {
                Log.e("aaaaaaaaa", "f:" + f);
                View flProgress = findViewById(R.id.flProgress);
                flProgress.post(new Runnable() {
                    @Override
                    public void run() {
                        if (f.exists()) {
                            installApk(f);
                        }
                    }
                });
            }

            @Override
            public void progress(final int progress) {
                Log.e("aaaaaaaaa", "progress:" + progress);
                View flProgress = findViewById(R.id.flProgress);
                flProgress.post(new Runnable() {
                    @Override
                    public void run() {
                        updateProgressUI(progress);
                    }
                });
            }

            @Override
            public void failed(Exception e) {
                Log.e("aaaaaaaaa", "failed:" + e);
            }
        });


    }

    private void updateProgressUI(int progress) {
        if (progress > 100) {
            progress = 100;
        }
        View flProgress = findViewById(R.id.flProgress);
        ((TextView) findViewById(R.id.tvProgress)).setText(progress + "%");
        if (flProgress.getWidth() != 0 || progress != 0) {
            int dx = (int) (flProgress.getWidth() * 0.01 * progress + 0.5);
            View progressView = findViewById(R.id.progress_view);
            FrameLayout.LayoutParams lp1 = (FrameLayout.LayoutParams) progressView.getLayoutParams();
            lp1.width = dx;
            progressView.setLayoutParams(lp1);

            View llThumb = findViewById(R.id.llThumb);
            RelativeLayout.LayoutParams lp2 = (RelativeLayout.LayoutParams) llThumb.getLayoutParams();
            lp2.leftMargin = BaseUtils.StaticParams.dp2px(mContext, 13) + dx;
            llThumb.setLayoutParams(lp2);
        }
    }

    private String createApkPath() {
        String fileName = mVersionBean.getVersionName() + ".apk";
        String filePath = BaseUtils.StaticParams.getSaveFilePath(
                ConfigurationManager.Companion.getInstance().getContext(), fileName);
//        File file = new File(dirPath);
//        if (!file.exists() || !file.isDirectory())
//            file.mkdirs();
//        String filePath = dirPath + "/" + fileName;
        return filePath;
    }


    private void installApk(File apkFile) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Uri apkUri = FileProvider.getUriForFile(mContext, AppUtils.getAppPackageName() + ".fileprovider", apkFile);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
            } else {
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                Uri uri = Uri.fromFile(apkFile);
                intent.setDataAndType(uri, "application/vnd.android.package-archive");
            }
            mContext.startActivity(intent);
            ((BaseActivity) mContext).finishAllActivity();
        } catch (Exception e) {
            LogUtil.LogE("e:" + e.toString());
        }
    }

}

