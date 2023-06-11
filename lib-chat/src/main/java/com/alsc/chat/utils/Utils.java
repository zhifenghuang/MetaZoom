package com.alsc.chat.utils;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;

import androidx.core.content.ContextCompat;

import com.alsc.chat.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    private final static String IMAGE_EXTENSION = ".jpg";
    public final static String VIDEO_EXTENSION = ".mp4";

    public static ContentValues mContentValues = null;

    public final static String IMAGE_CONTENT_URI = "content://media/external/images/media";
    public final static String VIDEO_CONTENT_URI = "content://media/external/video/media";


    public static String createVideoPath(Context context) {
        String fileName = UUID.randomUUID().toString() + VIDEO_EXTENSION;
        String dirPath = Environment.getExternalStorageDirectory() + "/Android/data/" + context.getPackageName() + "/download";
        File file = new File(dirPath);
        if (!file.exists() || !file.isDirectory())
            file.mkdirs();
        return dirPath + "/" + fileName;
    }

    /**
     * Checks if the result contains a {@link PackageManager#PERMISSION_GRANTED} result for a
     * permission from a runtime permissions request.
     */
    public static boolean isPermissionGranted(String[] grantPermissions, int[] grantResults,
                                              String permission) {
        for (int i = 0; i < grantPermissions.length; i++) {
            if (permission.equals(grantPermissions[i])) {
                return grantResults[i] == PackageManager.PERMISSION_GRANTED;
            }
        }
        return false;
    }


    /**
     * @param context
     * @param permission
     * @return
     */
    public static boolean isGrantPermission(Context context, String permission) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
        } else {
            return true;
        }
    }

    public static String createImagePath(Context context) {
        String fileName = UUID.randomUUID().toString() + IMAGE_EXTENSION;
        String dirPath = Environment.getExternalStorageDirectory() + "/Android/data/" + context.getPackageName() + "/download";
        File file = new File(dirPath);
        if (!file.exists() || !file.isDirectory())
            file.mkdirs();
        String filePath = dirPath + "/" + fileName;
        return filePath;
    }

    public static String createAppPath() {
        String fileName = UUID.randomUUID().toString() + IMAGE_EXTENSION;
        String dirPath = Environment.getExternalStorageDirectory() + "/ALSC";
        File file = new File(dirPath);
        if (!file.exists() || !file.isDirectory())
            file.mkdirs();
        String filePath = dirPath + "/" + fileName;
        return filePath;
    }

    public static String getSaveFilePath(Context context, String fileName) {
        String dirPath = Environment.getExternalStorageDirectory() + "/Android/data/" + context.getPackageName() + "/download";
        File file = new File(dirPath);
        if (!file.exists() || !file.isDirectory())
            file.mkdirs();
        String filePath = dirPath + "/" + fileName;
        return filePath;
    }

    /**
     * 保存JPG图片
     *
     * @param bmp
     */
    public static String saveJpegByFileName(Bitmap bmp, String fileName, Context context) {
        String folder = getSaveFilePath(context, fileName);
        FileOutputStream fout = null;
        BufferedOutputStream bos = null;
        try {
            fout = new FileOutputStream(folder);
            bos = new BufferedOutputStream(fout);
            bmp.compress(Bitmap.CompressFormat.JPEG, 70, bos);

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            try {
                if (fout != null) {
                    fout.close();
                }
                if (bos != null) {
                    bos.flush();
                    bos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return folder;
    }

    /**
     * 保存JPG图片
     *
     * @param bmp
     */
    public static String saveJpeg(Bitmap bmp, Context context) {
        String folder = createImagePath(context);
        FileOutputStream fout = null;
        BufferedOutputStream bos = null;
        try {
            fout = new FileOutputStream(folder);
            bos = new BufferedOutputStream(fout);
            bmp.compress(Bitmap.CompressFormat.JPEG, 70, bos);

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            try {
                if (fout != null) {
                    fout.close();
                }
                if (bos != null) {
                    bos.flush();
                    bos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return folder;
    }

    /**
     * @param bmp
     */
    public static String saveJpegToAlbum(Bitmap bmp, Context context) {
        String folder = makeAlbumPath(0);
        FileOutputStream fout = null;
        BufferedOutputStream bos = null;
        try {
            fout = new FileOutputStream(folder);
            bos = new BufferedOutputStream(fout);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            registerPath(context, folder, 0);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            try {
                if (fout != null) {
                    fout.close();
                }
                if (bos != null) {
                    bos.flush();
                    bos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return folder;
    }

    private static String makeAlbumPath(int type) {
        String title = UUID.randomUUID().toString();
        String fileName = UUID.randomUUID().toString() + (type == 0 ? IMAGE_EXTENSION : VIDEO_EXTENSION);
        String albumPath = Environment.getExternalStorageDirectory() + "/" + Environment.DIRECTORY_DCIM + "/Camera";
        File file = new File(albumPath);
        if (!file.exists() || !file.isDirectory())
            file.mkdirs();
        String path = albumPath + "/" + fileName;
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, title);
        values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        values.put(MediaStore.Images.Media.MIME_TYPE, type == 0 ? "image/jpeg" : "video/mp4");
        values.put(MediaStore.Images.Media.DATA, path);
        mContentValues = values;
        return path;
    }

    public static String copyMediaToAlbum(Context context, int type, String oldPath) {
        String title = UUID.randomUUID().toString();
        String fileName = title + (type == 0 ? IMAGE_EXTENSION : VIDEO_EXTENSION);
        String albumPath = Environment.getExternalStorageDirectory() + "/" + Environment.DIRECTORY_DCIM + "/Camera";
        File file = new File(albumPath);
        if (!file.exists() || !file.isDirectory())
            file.mkdirs();
        String path = albumPath + "/" + fileName;
        copyFile(oldPath, path);
        ContentValues values = new ContentValues();
        if (type == 0) {
            values.put(MediaStore.Images.Media.TITLE, title);
            values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
            values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            values.put(MediaStore.Images.Media.DATA, path);
        } else {
            values.put(MediaStore.Video.Media.TITLE, title);
            values.put(MediaStore.Video.Media.DISPLAY_NAME, fileName);
            values.put(MediaStore.Video.Media.DATE_TAKEN, System.currentTimeMillis());
            values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");
            values.put(MediaStore.Video.Media.DATA, path);
        }
        mContentValues = values;
        registerPath(context, path, type);
        return path;
    }

    /**
     * 将图片在系统内注册
     */
    private static void registerPath(Context context, String path, int type) {
        if (mContentValues != null) {
            Uri table = null;
            if (type == 0) {
                table = Uri.parse(IMAGE_CONTENT_URI);
                mContentValues.put(MediaStore.Images.Media.SIZE, new File(path).length());
            } else {
                table = Uri.parse(VIDEO_CONTENT_URI);
                mContentValues.put(MediaStore.Video.Media.SIZE, new File(path).length());
            }

            try {
                context.getContentResolver().insert(table, mContentValues);
            } catch (Throwable e) {
                e.printStackTrace();
            } finally {
            }
            mContentValues = null;
        }
    }


    /**
     * 复制文件
     *
     * @param oldPath
     * @param newPath
     */
    public static boolean copyFile(String oldPath, String newPath) {
        boolean isSuccessful = false;
        InputStream inStream = null;
        FileOutputStream fs = null;
        try {
            int byteread = 0;
            File oldfile = new File(oldPath);
            if (oldfile.exists()) {
                inStream = new FileInputStream(oldPath); //读入原文件
                fs = new FileOutputStream(newPath);
                byte[] buffer = new byte[1024];
                while ((byteread = inStream.read(buffer)) != -1) {
                    fs.write(buffer, 0, byteread);
                }
                isSuccessful = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            isSuccessful = false;
        } finally {
            try {
                if (inStream != null) {
                    inStream.close();
                    inStream = null;
                }
                if (fs != null) {
                    fs.close();
                    fs = null;
                }
            } catch (IOException e) {
                e.printStackTrace();
                isSuccessful = false;
            }
        }
        return isSuccessful;
    }


    /**
     * dp转px
     *
     * @param context
     * @param dipValue
     * @return
     */
    public static int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    /**
     * px转dp
     *
     * @param context
     * @param pxValue
     * @return
     */
    public static int px2dp(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    public static int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }


    @SuppressLint("SimpleDateFormat")
    public static String getTimeStrOnlyHour(long time) {
        SimpleDateFormat mSdf = new SimpleDateFormat("HH:mm");
        Date dt = new Date(time);
        return mSdf.format(dt);
    }

    public static String getDateString(int time, String keyString) {
        return String.format(keyString, time);
    }


    public static String getNewText(int number) {
        return number < 10 ? ("0" + number) : String.valueOf(number);
    }

    /**
     * @param defaultId
     * @param path
     * @param iv
     * @Param File file
     */
    public static void loadImage(Context context, int defaultId, File file, String path, ImageView iv) {
        if (file != null && file.exists()) {
            loadImage(context, defaultId, Uri.fromFile(file), iv);
        } else {
            loadImage(context, defaultId, path, iv);
        }
    }

    /**
     * @param defaultId
     * @param path
     * @param target
     * @Param File file
     */
    public static void loadImage(Context context, int defaultId, File file, String path, SimpleTarget target) {
        if (file != null && file.exists()) {
            loadImage(context, defaultId, Uri.fromFile(file), target);
        } else {
            loadImage(context, defaultId, path, target);
        }
    }


    /**
     * @param defaultId
     * @param path
     * @param iv
     * @Param File file
     */
    public static void loadImage(Context context, int defaultId, File file, String path, ImageView iv, String fileName) {
        if (file != null && file.exists()) {
            Glide.with(context.getApplicationContext())
                    .load(Uri.fromFile(file))
                    .apply(new RequestOptions()
                                    .placeholder(defaultId)
                                    .error(defaultId)
                                    .centerCrop()//中心切圖, 會填滿
                                    .fitCenter()//中心fit, 以原本圖片的長寬為主
                                    .diskCacheStrategy(DiskCacheStrategy.DATA)
                            //                           .dontAnimate()
                    )
                    .into(iv);
        } else {
            Glide.with(context.getApplicationContext())
                    .load(path)
                    .apply(new RequestOptions()
                                    .placeholder(defaultId)
                                    .error(defaultId)
                                    .centerCrop()//中心切圖, 會填滿
                                    .fitCenter()//中心fit, 以原本圖片的長寬為主
                                    .diskCacheStrategy(DiskCacheStrategy.DATA)
                            //                           .dontAnimate()
                    )
                    .into(iv);
        }
    }

    /**
     * @param defaultId
     * @param path
     * @param iv
     */
    public static void loadImage(Context context, int defaultId, String path, ImageView iv) {
        iv.setImageResource(defaultId);
//        Glide.with(context.getApplicationContext())
//                .load(path)
//                .apply(new RequestOptions()
//                                .placeholder(defaultId)
//                                .error(defaultId)
//                                .centerCrop()//中心切圖, 會填滿
//                                .fitCenter()//中心fit, 以原本圖片的長寬為主
//                                .diskCacheStrategy(DiskCacheStrategy.DATA)
//                        //                      .dontAnimate()

//                )
//                .into(iv);

    }

    /**
     * @param defaultId
     * @param path
     * @param target
     */
    public static void loadImage(Context context, int defaultId, String path, SimpleTarget target) {
        Glide.with(context.getApplicationContext())
                .load(path)
                .apply(new RequestOptions()
                                .placeholder(defaultId)
                                .error(defaultId)
                                .centerCrop()//中心切圖, 會填滿
                                .fitCenter()//中心fit, 以原本圖片的長寬為主
                                .diskCacheStrategy(DiskCacheStrategy.DATA)
                        //                      .dontAnimate()
                )
                .into(target);

    }

    /**
     * 显示图片
     *
     * @param context   上下文
     * @param defaultId
     * @param url       图片路径
     * @param imageView 图片view
     */
    public static void displayAvatar(Context context, int defaultId, String url, ImageView imageView) {
        Glide.with(context.getApplicationContext())
                .load(url)
                .placeholder(defaultId)
                .error(defaultId)
                .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                .into(imageView);
    }

    /**
     * @param defaultId
     * @param path
     * @param iv
     */
    public static void loadImage(Context context, int defaultId, int path, ImageView iv) {
        Glide.with(context.getApplicationContext())
                .load(path)
                .apply(new RequestOptions()
                                .placeholder(defaultId)
                                .error(defaultId)
                                .centerCrop()//中心切圖, 會填滿
                                .fitCenter()//中心fit, 以原本圖片的長寬為主
                                .diskCacheStrategy(DiskCacheStrategy.DATA)
                        //                       .dontAnimate()
                )
                .into(iv);

    }

    /**
     * @param defaultId
     * @param uri
     * @param iv
     */
    public static void loadImage(Context context, int defaultId, Uri uri, ImageView iv) {
        Glide.with(context.getApplicationContext())
                .load(uri)
                .apply(new RequestOptions()
                                .placeholder(defaultId)
                                .error(defaultId)
                                .centerCrop()//中心切圖, 會填滿
                                .fitCenter()//中心fit, 以原本圖片的長寬為主
                                .diskCacheStrategy(DiskCacheStrategy.DATA)
                        //                .dontAnimate()
                )
                .into(iv);
    }

    /**
     * @param defaultId
     * @param uri
     * @param target
     */
    public static void loadImage(Context context, int defaultId, Uri uri, SimpleTarget target) {
        Glide.with(context.getApplicationContext())
                .load(uri)
                .apply(new RequestOptions()
                                .placeholder(defaultId)
                                .error(defaultId)
                                .centerCrop()//中心切圖, 會填滿
                                .fitCenter()//中心fit, 以原本圖片的長寬為主
                                .diskCacheStrategy(DiskCacheStrategy.DATA)
                        //                .dontAnimate()
                )
                .into(target);
    }

    /**
     * 从Assets中读取图片
     *
     * @param fileName
     * @return
     */
    public static Bitmap getImageFromAssetsFile(Resources resources, String fileName) {
        Bitmap image = null;
        InputStream is = null;
        AssetManager am = resources.getAssets();
        try {
            is = am.open(fileName);
            image = BitmapFactory.decodeStream(is);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return image;
    }


    public static Bitmap rotateBmp(Bitmap bmp, float rotateDegree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(rotateDegree);
        Bitmap newBmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, false);
        bmp.recycle();
        bmp = null;
        return newBmp;
    }


    public static String getNewTime(int time) {
        int total = time / 1000;
        int second = total % 60;
        int minute = 0;
        if (total > 60) {
            minute = (total / 60) % 60;
        }
        int hour = 0;
        if (total > 3600) {
            hour = total / 3600;
        }
        return getNewText(hour) + ":" + getNewText(minute) + ":" + getNewText(second);
    }

    public static String getFileSize(long fileSize) {
        if (fileSize < 1024 * 1024) {
            return getNewNumber(fileSize * 1.0f / 1024) + "K";
        }
        return getNewNumber(fileSize * 1.0f / 1024 / 1024) + "M";
    }

    /**
     * 判断两个时间是不是同一天时间
     *
     * @param time1
     * @param time2
     * @return
     */
    public static boolean isSameDayTime(long time1, long time2) {
        if (Math.abs(time1 - time2) > 24 * 3600 * 1000) {
            return false;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time1);
        int day1 = calendar.get(Calendar.DAY_OF_MONTH);
        calendar.setTimeInMillis(time2);
        int day2 = calendar.get(Calendar.DAY_OF_MONTH);
        return day1 == day2;
    }

    public static String getRestTime(long time, long currentTime, Context context) {
        if (currentTime > time) {
            return "";
        }
        long detalTime = time - currentTime;
        int totalMinute = (int) (detalTime / 1000 / 60);
        int hour = 0;
        int day = 0;
        int minute = 0;
        String text = "";
        if (totalMinute > 60) {
            int totalHour = totalMinute / 60;
            minute = totalMinute % 60;
            if (totalHour > 24) {
                day = totalHour / 24;
                hour = totalHour % 24;
                text += context.getString(R.string.chat_day, String.valueOf(day))
                        + context.getString(R.string.chat_hour, String.valueOf(hour))
                        + context.getString(R.string.chat_minute, String.valueOf(minute));
            } else {
                hour = totalHour;
                text += context.getString(R.string.chat_hour, String.valueOf(hour))
                        + context.getString(R.string.chat_minute, String.valueOf(minute));
            }
        } else {
            minute = totalMinute;
            text += context.getString(R.string.chat_minute, String.valueOf(minute));
        }
        return context.getString(R.string.chat_rest_time_delete, text);
    }


    public static long dateStrToLong(String DateTime) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date time = sdf.parse(DateTime);
            return time.getTime();
        } catch (Exception e) {

        }
        return 0;
    }

    public static String longToDate(long time) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date(time);
        return sdf.format(date);
    }

    public static String longToDate2(long time) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date date = new Date(time);
        return sdf.format(date);
    }

    public static String longToDate3(long time) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(time);
        return sdf.format(date);
    }

    public static long dateStrToLong2(String DateTime) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            Date time = sdf.parse(DateTime);
            return time.getTime();
        } catch (Exception e) {

        }
        return 0;
    }

    public static String longToChatTime(long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        calendar.setTimeInMillis(time);
        int year1 = calendar.get(Calendar.YEAR);
        int month1 = calendar.get(Calendar.MONTH) + 1;
        int day1 = calendar.get(Calendar.DAY_OF_MONTH);
        int hour1 = calendar.get(Calendar.HOUR_OF_DAY);
        int minute1 = calendar.get(Calendar.MINUTE);
        if (year != year1) {
            return year1 + "-" + getNewText(month1) + "-" + getNewText(day1) + " " + getNewText(hour1) + ":" + getNewText(minute1);
        } else if (month != month1 || day != day1) {
            return getNewText(month1) + "-" + getNewText(day1) + " " + getNewText(hour1) + ":" + getNewText(minute1);
        }
        return getNewText(hour1) + ":" + getNewText(minute1);
    }

    public static boolean isShowTime(long lastTime, long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(lastTime);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        calendar.setTimeInMillis(time);
        int year1 = calendar.get(Calendar.YEAR);
        int month1 = calendar.get(Calendar.MONTH) + 1;
        int day1 = calendar.get(Calendar.DAY_OF_MONTH);
        int hour1 = calendar.get(Calendar.HOUR_OF_DAY);
        int minute1 = calendar.get(Calendar.MINUTE);
        if (year != year1 || month != month1 || day != day1 || hour != hour1) {
            return true;
        }
        if (minute / 10 != minute1 / 10) {
            return true;
        }
        return false;
    }

    /**
     * 判断是否为手机号
     *
     * @param mobile
     * @return
     */
    public static boolean isMobileNumber(String mobile) {
        String regExp = "^(1)\\d{10}$";
        Pattern p = Pattern.compile(regExp);
        Matcher m = p.matcher(mobile);
        return m.matches();
    }


    public static String getNewNumber(float num) {
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        return decimalFormat.format(num);
    }

    /**
     * MD5加密
     */
    public static String encryptMD5(String securityStr) {
        byte[] data = securityStr.getBytes();
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
            md5.update(data);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        byte[] resultBytes = md5.digest();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < resultBytes.length; i++) {
            if (Integer.toHexString(0xFF & resultBytes[i]).length() == 1) {
                builder.append("0").append(
                        Integer.toHexString(0xFF & resultBytes[i]));
            } else {
                builder.append(Integer.toHexString(0xFF & resultBytes[i]));
            }
        }
        return builder.toString();
    }

    public static boolean isContainUrl(String text) {
        Matcher m = Pattern.compile("(((https|http)?://)?([a-z0-9]+[.])|(www.))"
                + "\\w+[.|\\/]([a-z0-9]{0,})?[[.]([a-z0-9]{0,})]+((/[\\S&&[^,;\u4E00-\u9FA5]]+)+)?([.][a-z0-9]{0,}+|/?)").matcher(text);
        return m.find();
    }

    public static String getLanguageStr(int language) {
        if (language == 0) {
            return "zh-cn";
        }
//        else if (language == 2) {
//            return "ja";
//        } else if (language == 3) {
//            return "ko";
//        } else if (language == 4) {
//            return "vie";
//        }
        return "en-us";
    }
}


