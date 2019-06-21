
### 前言
我之前写过一篇萤石云的集成文章，很多人问我有没有demo， 今天我再次总结一下， 并加个些功能。
- 集成步骤
- 视频预览播放
- 视频放大缩小
- 视频的质量切换
- 截图
- 视频加载细节

之前的文章大家可以看下面的链接：
https://mp.weixin.qq.com/s/V4F2_bkY8QFN167KY9gSDg

先看效果图：
![](https://upload-images.jianshu.io/upload_images/2787891-8f1b34325b4ec365.gif?imageMogr2/auto-orient/strip)

### 集成步骤

###### 1.安装SDK
```
dependencies {
    compile 'com.hikvision.ezviz:ezviz-sdk:4.8.6.2'
 }
```
###### 2.配置工程
(1).权限配置
在 AndroidMainfest.xml 文件中添加：
```
//网络权限
<uses-permission android:name="android.permission.INTERNET"/>  
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
<uses-permission android:name="android.permission.RECORD_AUDIO"/>
<uses-permission android:name="android.permission.READ_PHONE_STATE"/>

//存储权限
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>

//wifi 状态权限
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE"/>
<uses-permission android:name="android.permission.CHANGE_WIFI_STATE"/>
<uses-permission android:name="android.permission.CHANGE_WIFI_MULTICAST_STATE"/>
//热点配网扫描wifi需要使用
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
```

(2).配置build.gradle
```
  defaultConfig {
       ...
        ndk {
            abiFilters "armeabi-v7a"//只支持v7a
        }
    }
     sourceSets {
        main {
            jniLibs.srcDirs = ['libs']
        }
    }
```

(3).配置 AndroidManifest.xml
添加如下activity定义，用于sdk中间页显示，包含登录、开通云存储等。
```
 <activity
        android:name="com.videogo.main.EzvizWebViewActivity"
        android:screenOrientation="portrait"
        android:configChanges="orientation|keyboardHidden">
    </activity>
```

具体可以参考官方集成网站
https://open.ys7.com/doc/zh/book/4.x/android-sdk.html


### 视频预览播放
播放与之前版本不同，**加入设备的验证码**
```
 mEZPlayer.setPlayVerifyCode("验证码");
```
(1).SDK初始化（在application中初始化）
```
/** * sdk日志开关，正式发布需要去掉 */
EZOpenSDK.showSDKLog(true);
/** * 设置是否支持P2P取流,详见api */
EZOpenSDK.enableP2P(false);

/** * APP_KEY请替换成自己申请的 */
EZOpenSDK.initLib(this, APP_KEY);
```
(2).播放主要代码
设置token
```
 EZOpenSDK.getInstance().setAccessToken("你的token");
```
**实现代码:**
布局主要控件是SurfaceView，SurfaceView的用法大家应该都知道吧，主要实现了implements SurfaceHolder.Callback接口，声明SurfaceView 对象，并实方法，代码如下：
```
private SurfaceView mRealPlaySv = null;
private SurfaceHolder mRealPlaySh = null;
```
```
mRealPlaySh = mRealPlaySv.getHolder();
mRealPlaySh.addCallback(VideoActivity.this);
```
```
Override

public void surfaceCreated(SurfaceHolder holder) {

    if (mEZPlayer != null) {

        mEZPlayer.setSurfaceHold(holder);
    } else {

    }
    mRealPlaySh = holder;
}

@Override
public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

}

@Override
public void surfaceDestroyed(SurfaceHolder holder) {
    if (mEZPlayer != null) {
        mEZPlayer.setSurfaceHold(null);
    }
    mRealPlaySh = null;

}
```

```
<SurfaceView
    android:id="@+id/realplay_sv"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:layout_alignParentLeft="true"
    android:layout_alignParentStart="true"
    android:layout_alignParentTop="true"
    android:background="@android:color/transparent" />
```
实现Handler.Callback，来监听播放结果回调：
```
    @Override
    public boolean handleMessage(Message msg) {

        switch (msg.what){

        }
        return false;
    }
```
配置：
```
      //用来存储萤石云的播放质量、序列号
        mCameraInfo=new EZCameraInfo();
            // 2-高清，1-标清，0-流畅
            mCameraInfo.setVideoLevel(2);
            //序列号
            mCameraInfo.setDeviceSerial("C86398971");
            mCameraInfo.setCameraNo(1);
```
开始播放：
```
    private void startRealPlay() {
  
        mEZPlayer = YourApplication.getOpenSDK().createPlayer(mCameraInfo.getDeviceSerial(), mCameraInfo.getCameraNo());
 
        mEZPlayer.setPlayVerifyCode(mVerificationCode);
        mEZPlayer.setHandler(mHandler);
        mEZPlayer.setSurfaceHold(mRealPlaySh);
        mEZPlayer.startRealPlay();
        tvPlay.setEnabled(true);

    }
```

### **视频放大，手势放大功能**
在播放成功的回调里加上  setRealPlaySvLayout();方法，方法的代码如下:
```
private void setRealPlaySvLayout() throws InnerException, PlaySDKException {
    DisplayMetrics dm = new DisplayMetrics();
    getWindowManager().getDefaultDisplay().getMetrics(dm);
    int whdth = dm.widthPixels;
    int height = dm.heightPixels;
    mRealPlayTouchListener.setSacaleRect(Constant.MAX_SCALE, 0, 0, whdth, height);
    setPlayScaleUI(1, null, null);
}
```

```
private void setPlayScaleUI(float scale, CustomRect oRect, CustomRect curRect) {
    if (scale == 1) {
         
        try {
            if (mEZPlayer != null) {
                mEZPlayer.setDisplayRegion(false, null, null);
            }
        } catch (BaseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    } else {
       
        if (mPlayScale == scale) {
            try {
                if (mEZPlayer != null) {
                    mEZPlayer.setDisplayRegion(true, oRect, curRect);
                }
            } catch (BaseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return;
        }
        try {
            if (mEZPlayer != null) {
                mEZPlayer.setDisplayRegion(true, oRect, curRect);
            }
        } catch (BaseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    mPlayScale = scale;
}
```

### 视频质量切换

效果图：
![](https://upload-images.jianshu.io/upload_images/2787891-20607069bace631e.gif?imageMogr2/auto-orient/strip)
 
核心方法
```
 // 2-高清，1-标清，0-流畅
 mCameraInfo.setVideoLevel
```

切换画质
```
    /**
     * 码流配置 清晰度 2-高清，1-标清，0-流畅
     *
     * @see
     * @since V2.0
     */
    private void setQualityMode(final EZConstants.EZVideoLevel mode) {


        if (mEZPlayer != null) {

            mWaitDialog.setWaitText("正在设置画面质量…");
            mWaitDialog.show();

            Thread thr = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        // need to modify by yudan at 08-11
                        APP.getOpenSDK().setVideoLevel(mCameraInfo.getDeviceSerial(), mCameraInfo.getCameraNo(), mode.getVideoLevel());
                        mCurrentQulityMode = mode;
                        Message msg = Message.obtain();
                        msg.what = MSG_SET_VEDIOMODE_SUCCESS;
                        mHandler.sendMessage(msg);
                    } catch (BaseException e) {
                        mCurrentQulityMode = EZConstants.EZVideoLevel.VIDEO_LEVEL_FLUNET;
                        e.printStackTrace();
                        Message msg = Message.obtain();
                        msg.what = MSG_SET_VEDIOMODE_FAIL;
                        mHandler.sendMessage(msg);
                    }

                }
            }) {
            };
            thr.start();
        }
    }
```
切换画质结果：
```
     //切换画质成功
            case EZConstants.EZRealPlayConstants.MSG_SET_VEDIOMODE_SUCCESS:
                handleSetVedioModeSuccess();
                break;
            //切换画质失败
            case EZConstants.EZRealPlayConstants.MSG_SET_VEDIOMODE_FAIL:
                handleSetVedioModeFail(msg.arg1);
                break;

```
### 声音与截图
1.声音
核心方法：
```
mLocalInfo.setSoundOpen(false);
```

2.截图
核心方法：
```
mEZPlayer.capturePicture()
```

我的微信：lengquele5311
![](http://upload-images.jianshu.io/upload_images/2787891-d230087beed618f6?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)


大家可以关注我的微信公众号：「秦子帅」一个有质量、有态度的公众号！

![公众号](https://upload-images.jianshu.io/upload_images/2787891-27c5da75f456332a.jpg?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)







