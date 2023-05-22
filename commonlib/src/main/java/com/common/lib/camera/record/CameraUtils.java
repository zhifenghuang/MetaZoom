package com.common.lib.camera.record;

import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import androidx.core.content.ContextCompat;

import com.common.lib.constant.Constants;
import com.common.lib.utils.UidUtil;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class CameraUtils {


    public static ContentValues mContentValues = null;

    public final static String IMAGE_CONTENT_URI = "content://media/external/images/media";
    public final static String VIDEO_CONTENT_URI = "content://media/external/video/media";


    public static String createVideoPath(Context context) {
        String fileName = UidUtil.INSTANCE.createUid() + Constants.MP4_EXTENSION;
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
        String fileName = UidUtil.INSTANCE.createUid() + Constants.JPG_EXTENSION;
        String dirPath = Environment.getExternalStorageDirectory() + "/Android/data/" + context.getPackageName() + "/download";
        File file = new File(dirPath);
        if (!file.exists() || !file.isDirectory())
            file.mkdirs();
        String filePath = dirPath + "/" + fileName;
        return filePath;
    }

    public static String createAppPath() {
        String fileName = UidUtil.INSTANCE.createUid() + Constants.JPG_EXTENSION;
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
        String title = UidUtil.INSTANCE.createUid();
        String fileName = UidUtil.INSTANCE.createUid() + (type == 0 ? Constants.JPG_EXTENSION : Constants.MP4_EXTENSION);
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
        String title = UidUtil.INSTANCE.createUid();
        String fileName = title + (type == 0 ? Constants.JPG_EXTENSION : Constants.MP4_EXTENSION);
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


    public static Bitmap rotateBmp(Bitmap bmp, float rotateDegree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(rotateDegree);
        Bitmap newBmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, false);
        bmp.recycle();
        bmp = null;
        return newBmp;
    }

    /**
     * @param bmp
     * @param name
     * @param context
     * @return
     */
    public static String saveThumb(Bitmap bmp, String name, Context context) {
        String filePath = "";
        if (bmp != null) {
            float scaleX = 100f / bmp.getWidth();
            float scaleY = 200f / bmp.getHeight();
            if (scaleX > 1.0f || scaleY > 1.0f) {
                saveJpegByFileName(bmp, name, context);
            } else {
                Matrix matrix = new Matrix();
                if (scaleX > scaleY) {
                    matrix.postScale(scaleX, scaleX);
                } else {
                    matrix.postScale(scaleY, scaleY);
                }
                Bitmap newBmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
                saveJpegByFileName(bmp, name, context);
                newBmp.recycle();
                newBmp = null;
            }
        }
        return filePath;
    }
}

