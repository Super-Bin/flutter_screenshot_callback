package com.flutter.screenshot_callback;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import androidx.core.content.ContextCompat;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * 媒体查询语句有问题：
 * 出错了 java.lang.IllegalArgumentException: Invalid token limit
 *
 * 在android 11中，添加了一个约束以不允许在排序值中使用LIMIT。
 * 您需要将查询与包参数一起使用。例如
 * https://stackoverflow.com/questions/10390577/limiting-number-of-rows-in-a-contentresolver-query-function/62891878#62891878
 *
 */
public class ScreenShotListenManager {
    private static final String TAG = "ScreenShotListenManager";

    /**
     * 读取媒体数据库时需要读取的列
     */
    private static final String[] MEDIA_PROJECTIONS = {
            MediaStore.Images.ImageColumns.DATA,
            MediaStore.Images.ImageColumns.DATE_TAKEN,
    };

    /**
     * 截屏依据中的路径判断关键字
     */
    private static final String[] KEYWORDS = {
            "screenshot", "screen_shot", "screen-shot", "screen shot",
            "screencapture", "screen_capture", "screen-capture", "screen capture",
            "screencap", "screen_cap", "screen-cap", "screen cap"
    };

    private static Point sScreenRealSize;

    /**
     * 已回调过的路径
     */
    private final static List<String> sHasCallbackPaths = new ArrayList<String>();

    private Context mContext;

    private OnScreenShotListener mListener;

    private long mStartListenTime;

    /**
     * 外部存储器内容观察者
     */
    private MediaContentObserver mExternalObserver;

    /**
     * 运行在 UI 线程的 Handler, 用于运行监听器回调
     */
    private final Handler mUiHandler = new Handler(Looper.getMainLooper());

    public ScreenShotListenManager(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("The context must not be null.");
        }
        mContext = context;
        Log.d(TAG, "ScreenShotListenManager 初始化");

        // 获取屏幕真实的分辨率
        if (sScreenRealSize == null) {
            sScreenRealSize = getRealScreenSize();
            if (sScreenRealSize != null) {
                Log.d(TAG, "Screen Real Size: " + sScreenRealSize.x + " * " + sScreenRealSize.y);
            } else {
                Log.w(TAG, "Get screen real size failed.");
            }
        }
    }

    /**
     * 启动监听
     */
    public void startListen() {
        assertInMainThread();

//        sHasCallbackPaths.clear();

        if (mExternalObserver == null) {
            Log.i(TAG, "启动监听startListen");
            // 记录开始监听的时间戳
            mStartListenTime = System.currentTimeMillis();
            // 创建内容观察者
            mExternalObserver = new MediaContentObserver(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, mUiHandler);

            mContext.getContentResolver().registerContentObserver(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    true,
                    mExternalObserver
            );
        }

    }

    /**
     * 停止监听
     * 这个应该要放在onStop之后，才停止监听，放在onResume不行
     * 一些第三方的截图功能，会执行到OnResume方法，
     */
    public void stopListen() {
        assertInMainThread();
        if (mExternalObserver != null) {
            try {
                mContext.getContentResolver().unregisterContentObserver(mExternalObserver);
            } catch (Exception e) {
                e.printStackTrace();
            }
            mExternalObserver = null;
        }

        // 清空数据
        mStartListenTime = 0;
//        sHasCallbackPaths.clear();

        //切记！！！:必须设置为空 可能mListener 会隐式持有Activity导致释放不掉
//        mListener = null;
    }

    /**
     * 处理媒体数据库的内容改变
     *
     * video手机没有权限会直接报错，所以要先判断是否有存储权限。
     * Permission Denial: reading com.android.providers.media.MediaProvider
     * uri content://media/external/images/media from pid=3202, uid=10326
     * requires android.permission.READ_EXTERNAL_STORAGE, or grantUriPermission()
     */
    private void handleMediaContentChange(Uri contentUri) {
        Log.i(TAG, "contentUri = " + contentUri);
        Cursor cursor = null;
        try {
            if (!isReadExternalStoragePermissionGranted()) {
                Log.i(TAG, "没有权限直接返回");
                mListener.onScreenCapturedWithDeniedPermission();
                return;
            }

            // 数据改变时查询数据库中最后加入的一条数据
            cursor = mContext.getContentResolver().query(
                    contentUri,
                    MEDIA_PROJECTIONS,
                    null,
                    null,
//                    MediaStore.Images.ImageColumns.DATE_MODIFIED + " desc limit 1"
                        MediaStore.Images.ImageColumns.DATE_MODIFIED + " desc"
            );

//            还要适配一下，大于api26的方案
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                Bundle bundle = new Bundle();
//                bundle.putInt(ContentResolver.QUERY_ARG_LIMIT, 1);
//                // 排序列
//                ArrayList<String> list = new ArrayList<String>();
////                list.add(MediaStore.Files.FileColumns.DATE_MODIFIED);
//                list.add(MediaStore.Files.FileColumns.DATE_TAKEN);
//                bundle.putStringArrayList(ContentResolver.QUERY_ARG_SORT_COLUMNS, list);
//                // 排序方向，降序
//                bundle.putInt(ContentResolver.QUERY_ARG_SORT_DIRECTION, ContentResolver.QUERY_SORT_DIRECTION_DESCENDING);
//
//                cursor = mContext.getContentResolver().query(
//                        contentUri,
//                        MEDIA_PROJECTIONS,
//                        bundle,
//                        null
//                );
//            } else {
//                cursor = mContext.getContentResolver().query(
//                        contentUri,
//                        MEDIA_PROJECTIONS,
//                        null,
//                        null,
//                    MediaStore.Images.ImageColumns.DATE_MODIFIED + " desc limit 1"
////                        MediaStore.Images.ImageColumns.DATE_MODIFIED + " desc"
//                );
//            }

            if (cursor == null) {
                Log.e(TAG, "Deviant logic.");
                return;
            }
            if (!cursor.moveToFirst()) {
                Log.d(TAG, "Cursor no data.");
                mListener.onScreenCapturedWithDeniedPermission();
                return;
            }
            String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            int dateTakenIndex = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATE_TAKEN);
            long dateTaken = cursor.getLong(dateTakenIndex);

            if (isReadExternalStoragePermissionGranted()) {
                // 处理获取到的第一行数据
                if (checkScreenShot(path, dateTaken) && !checkCallback(path)) {
                    Log.i(TAG, "截图回调 path = " + path);
                    mListener.onShot(path);
                }
            } else {
                mListener.onScreenCapturedWithDeniedPermission();
            }


        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "出错了 " + e);

        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
    }


    private boolean checkScreenShot(String path, long dateTaken) {
        long currentTimeMillis = System.currentTimeMillis();
        Log.i(TAG, "checkScreenShot path = " + path + " dateTaken " + dateTaken);
        Log.i(TAG, "mStartListenTime = " + mStartListenTime + " System.currentTimeMillis() = " + currentTimeMillis);
        /*
         * 判断依据一: 时间判断
         */
        // 如果加入数据库的时间在开始监听之前, 或者与当前时间相差大于10秒, 则认为当前没有截屏
        if (dateTaken < mStartListenTime || (currentTimeMillis - dateTaken) > 10 * 1000) {
            return false;
        }

        /*
         * 判断依据二: 路径判断
         */
        if (TextUtils.isEmpty(path)) {
            return false;
        }
        path = path.toLowerCase();
        // 判断图片路径是否含有指定的关键字之一, 如果有, 则认为当前截屏了
        for (String keyWork : KEYWORDS) {
            if (path.contains(keyWork)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 判断是否已回调过, 某些手机ROM截屏一次会发出多次内容改变的通知; <br/>
     * 删除一个图片也会发通知, 同时防止删除图片时误将上一张符合截屏规则的图片当做是当前截屏.
     */
    private boolean checkCallback(String imagePath) {
        if (sHasCallbackPaths.contains(imagePath)) {
            Log.d(TAG, "ScreenShot: imgPath has done"
                    + "; imagePath = " + imagePath);
            return true;
        }
        // 大概缓存15~20条记录便可
        if (sHasCallbackPaths.size() >= 20) {
            for (int i = 0; i < 5; i++) {
                sHasCallbackPaths.remove(0);
            }
        }
        sHasCallbackPaths.add(imagePath);
        return false;
    }

    /**
     * 获取屏幕分辨率
     */
    private Point getRealScreenSize() {
        Point screenSize = null;
        try {
            screenSize = new Point();
            WindowManager windowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
            Display defaultDisplay = windowManager.getDefaultDisplay();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                defaultDisplay.getRealSize(screenSize);
            } else {
                try {
                    Method mGetRawW = Display.class.getMethod("getRawWidth");
                    Method mGetRawH = Display.class.getMethod("getRawHeight");
                    screenSize.set(
                            (Integer) mGetRawW.invoke(defaultDisplay),
                            (Integer) mGetRawH.invoke(defaultDisplay)
                    );
                } catch (Exception e) {
                    screenSize.set(defaultDisplay.getWidth(), defaultDisplay.getHeight());
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return screenSize;
    }

    private int dp2px(Context ctx, float dp) {
        float scale = ctx.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    /**
     * 设置截屏监听器
     */
    public void setListener(OnScreenShotListener listener) {
        mListener = listener;
    }

    public interface OnScreenShotListener {
        void onShot(String imagePath);

        void onScreenCapturedWithDeniedPermission();
    }

    private static void assertInMainThread() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            StackTraceElement[] elements = Thread.currentThread().getStackTrace();
            String methodMsg = null;
            if (elements != null && elements.length >= 4) {
                methodMsg = elements[3].toString();
            }
            throw new IllegalStateException("Call the method must be in main thread: " + methodMsg);
        }
    }

    /**
     * 判断权限
     */
    private boolean isReadExternalStoragePermissionGranted() {
        int result;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            result = ContextCompat.checkSelfPermission(mContext, Manifest.permission.READ_MEDIA_IMAGES);
        } else {
            result = ContextCompat.checkSelfPermission(mContext, Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        return result == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * 媒体内容观察者(观察媒体数据库的改变)
     */
    private class MediaContentObserver extends ContentObserver {

        private Uri mContentUri;

        public MediaContentObserver(Uri contentUri, Handler handler) {
            super(handler);
            mContentUri = contentUri;
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            handleMediaContentChange(mContentUri);

        }

    }


}
