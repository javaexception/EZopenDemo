package com.qzs.ezopendemo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatTextView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.qzs.ezopendemo.widget.WaitDialog;
import com.tbruyelle.rxpermissions2.Permission;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.videogo.constant.Constant;
import com.videogo.exception.BaseException;
import com.videogo.exception.ErrorCode;
import com.videogo.exception.InnerException;
import com.videogo.exception.PlaySDKException;
import com.videogo.openapi.EZConstants;
import com.videogo.openapi.EZOpenSDK;
import com.videogo.openapi.EZPlayer;
import com.videogo.openapi.bean.EZCameraInfo;
import com.videogo.openapi.bean.EZDeviceInfo;
import com.videogo.realplay.RealPlayStatus;
import com.videogo.util.LocalInfo;
import com.videogo.util.Utils;
import com.videogo.widget.CheckTextButton;
import com.videogo.widget.CustomRect;
import com.videogo.widget.CustomTouchListener;

public class MainActivity extends AppCompatActivity  implements Handler.Callback, SurfaceHolder.Callback, View.OnClickListener {

    /***
     * 视频播放
     */
    private Handler mHandler = null;
    private String  deviceserail;
    private SurfaceView mRealPlaySv = null;
    private SurfaceHolder mRealPlaySh = null;
    //视频的播放状态  -初始化  播放成功  播放失败等等
    private int mStatus = RealPlayStatus.STATUS_INIT;
    private EZPlayer mEZPlayer = null;

    /***
     * 切花视频质量成功的回调
     */
    public static final int MSG_SET_VEDIOMODE_SUCCESS = 105;
    /**
     * 切花视频质量失败的回调
     */
    public static final int MSG_SET_VEDIOMODE_FAIL = 106;

    /***
     * 视频放大缩小功能
     */
    private EZConstants.EZVideoLevel mCurrentQulityMode = EZConstants.EZVideoLevel.VIDEO_LEVEL_HD;
    private EZDeviceInfo mDeviceInfo = null;
    private EZCameraInfo mCameraInfo = null;
    private String mVerifyCode;
    private CustomTouchListener mRealPlayTouchListener = null;
    // 播放比例
    private float mPlayScale = 1;

    /**
     * 加载旋转动画
     */
    private Animation operatingAnim;
    /**
     * 屏幕当前方向
     */
    private int mOrientation = Configuration.ORIENTATION_PORTRAIT;


    //其他
    private FrameLayout flVideo;
    private LinearLayout layoutBg;
    private ImageView ivLoad;
    private AppCompatTextView tvTxtShow;
    private ImageView realplayPlayIv;
    private LinearLayout llLeftBack;
    private LinearLayout realplayControlRl;
    private AppCompatTextView tvPlay;
    private ImageButton tvVoice;
    private Button realplayQualityBtn;
    private AppCompatTextView tvScreenshot;
    private CheckBox checkVideo;
    private CheckTextButton fullscreen_button;
    /***
     * 切换 画质dialog
     */
    private WaitDialog mWaitDialog = null;

    private PopupWindow mQualityPopupWindow = null;

    private LocalInfo mLocalInfo = null;

    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initData();
    }

    @SuppressLint("ResourceType")
    private void initData() {
        //iv旋转
        operatingAnim = AnimationUtils.loadAnimation(this, R.animator.tip);
        LinearInterpolator lin = new LinearInterpolator();
        operatingAnim.setInterpolator(lin);

        // 获取配置信息操作对象
        mLocalInfo = LocalInfo.getInstance();
        if (mLocalInfo.isSoundOpen()) {
            tvVoice.setBackgroundResource(R.drawable.ic_viedeo_sound1);
        } else {
            tvVoice.setBackgroundResource(R.drawable.ic_viedeo_sound2);
        }

        //用来存储萤石云的播放质量、序列号
        mCameraInfo=new EZCameraInfo();
            // 2-高清，1-标清，0-流畅
            mCameraInfo.setVideoLevel(2);
            //序列号
            mCameraInfo.setDeviceSerial("你的序列号");
            mCameraInfo.setCameraNo(1);
            if (mCameraInfo != null) {
                mCurrentQulityMode = (mCameraInfo.getVideoLevel());
            }
            mHandler = null;
            mHandler = new Handler(MainActivity.this);
    }

    /***
     * EZ设置
     */
    private void initVideoSetting() {

       //视频放大缩小
        mRealPlayTouchListener = new CustomTouchListener() {
            @Override
            public boolean canZoom(float v) {
                if (mStatus == RealPlayStatus.STATUS_PLAY) {
                    return true;
                } else {
                    return false;
                }
            }
            @Override
            public boolean canDrag(int direction) {
                if (mStatus != RealPlayStatus.STATUS_PLAY) {
                    return false;
                }
                if (mEZPlayer != null) {
                    // 出界判断
                    if (DRAG_LEFT == direction || DRAG_RIGHT == direction) {
//                        // 左移/右移出界判断
//                        if (mDeviceInfo.isSupportPTZ()) {
//                            return true;
//                        }
                    } else if (DRAG_UP == direction || DRAG_DOWN == direction) {
//                        // 上移/下移出界判断
//                        if (mDeviceInfo.isSupportPTZ()) {
//                            return true;
//                        }
                    }
                }
                return false;
            }

            @Override
            public void onSingleClick() {
            }

            @Override
            public void onDoubleClick(MotionEvent motionEvent) {

            }

            @Override
            public void onZoom(float scale) {
                if (mEZPlayer != null) {
                    //startZoom(scale);
                }
            }

            @Override
            public void onZoomChange(float scale, CustomRect oRect, CustomRect curRect) {

                if (mStatus == RealPlayStatus.STATUS_PLAY) {

                    //ToastUtils.showToast(ShiPinActivity.this,"5");
                    if (scale > 1.0f && scale < 1.1f) {
                        scale = 1.1f;
                    }
                    setPlayScaleUI(scale, oRect, curRect);
                }
            }

            @Override
            public void onDrag(int direction, float distance, float rate) {
                if (mEZPlayer != null) {
                    //Utils.showLog(RealPlayActivity.this, "onDrag rate:" + rate);

                    startDrag(direction, distance, rate);
                }
            }

            public void startDrag(int direction, float distance, float rate) {


            }

            @Override
            public void onEnd(int i) {

            }
        };
        mRealPlaySv.setOnTouchListener(mRealPlayTouchListener);
    }

    /***
     * 实例化
     */
    private void initView() {

        flVideo = findViewById(R.id.fl_video);
        mRealPlaySv = findViewById(R.id.realplay_sv);
        layoutBg = findViewById(R.id.layout_bg);
        ivLoad = findViewById(R.id.iv_load);
        tvTxtShow = findViewById(R.id.tv_txt_show);
        realplayPlayIv = findViewById(R.id.realplay_play_iv);

        realplayControlRl = findViewById(R.id.realplay_control_rl);
        tvPlay = findViewById(R.id.tv_play);
        tvVoice = findViewById(R.id.tv_voice);
        realplayQualityBtn = findViewById(R.id.realplay_quality_btn);
        tvScreenshot = findViewById(R.id.tv_screenshot);
        checkVideo = findViewById(R.id.check_video);
        fullscreen_button = findViewById(R.id.fullscreen_button);

        realplayQualityBtn.setOnClickListener(this);
        tvPlay.setOnClickListener(this);
        realplayPlayIv.setOnClickListener(this);
        tvVoice.setOnClickListener(this);
        mWaitDialog = new WaitDialog(this, android.R.style.Theme_Translucent_NoTitleBar);
        mWaitDialog.setCancelable(false);
        tvScreenshot.setOnClickListener(this);
        fullscreen_button.setOnClickListener(this);

    }
    /*
 视频的放大缩小功能
  */
    private void setRealPlaySvLayout() throws InnerException, PlaySDKException {
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int whdth = dm.widthPixels;
        int height = dm.heightPixels;
        mRealPlayTouchListener.setSacaleRect(Constant.MAX_SCALE, 0, 0, whdth, height);
        setPlayScaleUI(1, null, null);
    }


    private void setPlayScaleUI(float scale, CustomRect oRect, CustomRect curRect) {
        if (scale == 1) {

            // ToastUtils.showToast(ShiPinActivity.this,"5");
            try {
                if (mEZPlayer != null) {
                    mEZPlayer.setDisplayRegion(false, null, null);
                }
            } catch (BaseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {

            // ToastUtils.showToast(ShiPinActivity.this,"4");
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


    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what){

            case EZConstants.EZRealPlayConstants.MSG_REALPLAY_PLAY_SUCCESS:
              //播放成功
                tvPlay.setBackgroundResource(R.drawable.ic_play_play_selector2);
                layoutBg.setVisibility(View.GONE);
                realplayPlayIv.setVisibility(View.GONE);
                try {
                    mStatus = RealPlayStatus.STATUS_PLAY;
                    setRealPlaySvLayout();
                } catch (InnerException e) {
                    e.printStackTrace();
                } catch (PlaySDKException e) {
                    e.printStackTrace();
                }
                break;
            case EZConstants.EZRealPlayConstants.MSG_REALPLAY_PLAY_FAIL:

                mStatus = RealPlayStatus.STATUS_STOP;
               //播放失败
                tvPlay.setBackgroundResource(R.drawable.ic_play_play_selector1);
                layoutBg.setVisibility(View.VISIBLE);
                tvTxtShow.setVisibility(View.GONE);
                ivLoad.clearAnimation();
                ivLoad.setVisibility(View.GONE);
                realplayPlayIv.setVisibility(View.VISIBLE);
                break;
            case ErrorCode.ERROR_INNER_VERIFYCODE_ERROR:
                //没有验证码
                tvPlay.setBackgroundResource(R.drawable.ic_play_play_selector1);
                break;
            //切换画质成功
            case EZConstants.EZRealPlayConstants.MSG_SET_VEDIOMODE_SUCCESS:
                handleSetVedioModeSuccess();
                break;
            //切换画质失败
            case EZConstants.EZRealPlayConstants.MSG_SET_VEDIOMODE_FAIL:
                handleSetVedioModeFail(msg.arg1);
                break;

        }
        return false;
    }

    /***
     * 切换画质成功
     */
    private void handleSetVedioModeSuccess() {
        closeQualityPopupWindow();
        setRealPlayStartUI();
        setVideoLevel();
        try {
            mWaitDialog.setWaitText(null);
            mWaitDialog.dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (mStatus == RealPlayStatus.STATUS_PLAY) {
            // 停止播放
            stopRealPlay();
            //下面语句防止stopRealPlay线程还没释放surface, startRealPlay线程已经开始使用surface
            //因此需要等待500ms
            SystemClock.sleep(500);
            // 开始播放
            startRealPlay();
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (mEZPlayer != null) {
            mEZPlayer.setSurfaceHold(holder);
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



    private void closeQualityPopupWindow() {
        if (mQualityPopupWindow != null) {
            dismissPopWindow(mQualityPopupWindow);
            mQualityPopupWindow = null;
        }
    }
    private void dismissPopWindow(PopupWindow popupWindow) {
        if (popupWindow != null && !isFinishing()) {
            try {
                popupWindow.dismiss();
            } catch (Exception e) {
                // TODO: handle exception
            }
        }
    }
    /***
     * 高清流畅弹框
     * @param anchor
     */
    private void openQualityPopupWindow(View anchor) {
        if (mEZPlayer == null) {
            Toast.makeText(getApplicationContext(),"只有在播放状态下才能切换画面质量",Toast.LENGTH_SHORT).show();
            return;
        }
        closeQualityPopupWindow();
        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewGroup layoutView = (ViewGroup) layoutInflater.inflate(R.layout.realplay_quality_items, null, true);

        Button qualityHdBtn = (Button) layoutView.findViewById(R.id.quality_hd_btn);
        qualityHdBtn.setOnClickListener(mOnPopWndClickListener);
        Button qualityBalancedBtn = (Button) layoutView.findViewById(R.id.quality_balanced_btn);
        qualityBalancedBtn.setOnClickListener(mOnPopWndClickListener);
        Button qualityFlunetBtn = (Button) layoutView.findViewById(R.id.quality_flunet_btn);
        qualityFlunetBtn.setOnClickListener(mOnPopWndClickListener);
       // LogUtils.d("弹出的画质----  "+mCameraInfo.getVideoLevel());
        // 视频质量，2-高清，1-标清，0-流畅
        if (mCameraInfo.getVideoLevel() == EZConstants.EZVideoLevel.VIDEO_LEVEL_FLUNET) {
            qualityFlunetBtn.setEnabled(false);
        } else if (mCameraInfo.getVideoLevel() == EZConstants.EZVideoLevel.VIDEO_LEVEL_BALANCED) {
            qualityBalancedBtn.setEnabled(false);
        } else if (mCameraInfo.getVideoLevel() == EZConstants.EZVideoLevel.VIDEO_LEVEL_HD) {
            qualityHdBtn.setEnabled(false);
        }

        int height = 180;

        qualityFlunetBtn.setVisibility(View.VISIBLE);
        qualityBalancedBtn.setVisibility(View.VISIBLE);
        qualityHdBtn.setVisibility(View.VISIBLE);

        height = Utils.dip2px(this, height);
        mQualityPopupWindow = new PopupWindow(layoutView, RelativeLayout.LayoutParams.WRAP_CONTENT, height, true);
        mQualityPopupWindow.setBackgroundDrawable(new BitmapDrawable());
        mQualityPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {

            @Override
            public void onDismiss() {
                //   LogUtil.infoLog(TAG, "KEYCODE_BACK DOWN");
                mQualityPopupWindow = null;
                closeQualityPopupWindow();
            }
        });
        try {
            mQualityPopupWindow.showAsDropDown(anchor, -Utils.dip2px(this, 5),
                    -(height + anchor.getHeight() + Utils.dip2px(this, 8)));
        } catch (Exception e) {
            e.printStackTrace();
            closeQualityPopupWindow();
        }
    }
    private View.OnClickListener mOnPopWndClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                //高清
                case R.id.quality_hd_btn:
                    setQualityMode(EZConstants.EZVideoLevel.VIDEO_LEVEL_HD);
                    break;
                //均衡
                case R.id.quality_balanced_btn:
                    setQualityMode(EZConstants.EZVideoLevel.VIDEO_LEVEL_BALANCED);
                    break;
                //流畅
                case R.id.quality_flunet_btn:
                    setQualityMode(EZConstants.EZVideoLevel.VIDEO_LEVEL_FLUNET);
                    break;

                default:
                    break;
            }
        }
    };


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

    /***
     * 切换画质失败
     * @param errorCode
     */
    private void handleSetVedioModeFail(int errorCode) {

        closeQualityPopupWindow();
        setVideoLevel();
        try {
            mWaitDialog.setWaitText(null);
            mWaitDialog.dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }
       // CommonUtil.showToast("视频质量切换失败");

    }

    /***
     * 设置播放质量等级
     */
    private void setVideoLevel() {
        if (mCameraInfo == null || mEZPlayer == null ) {
            return;
        }

//        if (mDeviceInfo.getStatus() == 1) {
//            realplayQualityBtn.setEnabled(true);
//        } else {
//            realplayQualityBtn.setEnabled(false);
//        }

        /************** 本地数据保存 需要更新之前获取到的设备列表信息，开发者自己设置 *********************/
        mCameraInfo.setVideoLevel(mCurrentQulityMode.getVideoLevel());

        // 视频质量，2-高清，1-标清，0-流畅
        if (mCurrentQulityMode.getVideoLevel() == EZConstants.EZVideoLevel.VIDEO_LEVEL_FLUNET.getVideoLevel()) {
            realplayQualityBtn.setText(R.string.quality_flunet);
        } else if (mCurrentQulityMode.getVideoLevel() == EZConstants.EZVideoLevel.VIDEO_LEVEL_BALANCED.getVideoLevel()) {
            realplayQualityBtn.setText(R.string.quality_balanced);
        } else if (mCurrentQulityMode.getVideoLevel() == EZConstants.EZVideoLevel.VIDEO_LEVEL_HD.getVideoLevel()) {
            realplayQualityBtn.setText(R.string.quality_hd);
        }

    }

    /***
     * 开始时的UI
     */
    private void setRealPlayStartUI() {

        layoutBg.setVisibility(View.VISIBLE);
        realplayPlayIv.setVisibility(View.GONE);
        ivLoad.setVisibility(View.VISIBLE);
        tvTxtShow.setVisibility(View.VISIBLE);
        ivLoad.startAnimation(operatingAnim);
    }

    /**
     * 停止播放
     *
     * @see
     * @since V1.0
     */
    private void stopRealPlay() {

        mStatus = RealPlayStatus.STATUS_STOP;
        // stopUpdateTimer();
        if (mEZPlayer != null) {
            //stopRealPlayRecord();
            mEZPlayer.stopRealPlay();
        }
        tvPlay.setEnabled(true);
    }



    private void startRealPlay() {
        //  if (mCameraInfo != null) {
        //  if (mEZPlayer == null) {
        mEZPlayer = APP.getOpenSDK().createPlayer(mCameraInfo.getDeviceSerial(), mCameraInfo.getCameraNo());
        //     Log.e("startrealplay","startrealplay");
//              mEZPlayer = mEZOpenSDK.createPlayerWithUrl(EZRealPlayActivity.this, "ysproto://vtm.ys7.com:8554/live?dev=473224256&chn=1&stream=1&cln=1&isp=0&biz=3");
//              mEZPlayer = EzvizApplication.getOpenSDK().createPlayerWithDeviceSerial(EZRealPlayActivity.this, mCameraInfo.getDeviceSerial(), mCameraInfo.getChannelNo(), 1);
        //   }
        //   mEZPlayer.setPlayVerifyCode(DataManager.getInstance().getDeviceSerialVerifyCode("647095818"));
        mEZPlayer.setPlayVerifyCode("你的验证码");
        mEZPlayer.setHandler(mHandler);
        mEZPlayer.setSurfaceHold(mRealPlaySh);
        mEZPlayer.startRealPlay();
        tvPlay.setEnabled(true);

    }

    /***
     * 点击事件
     * @param v
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            //横竖屏
            case R.id.fullscreen_button:
                if (mOrientation==Configuration.ORIENTATION_PORTRAIT){
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);   //切换横屏
                    return;
                }
                if (mOrientation==Configuration.ORIENTATION_LANDSCAPE){
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);   //竖屏模式
                    return;
                }
                break;

            //播放与暂停
            case R.id.tv_play:
                handlePlayAndSuspend();
                break;
            case R.id.realplay_play_iv:
                handlePlayAndSuspend();
                break;
            //声音
            case R.id.tv_voice:
                onSoundBtnClick();
                break;

            //高清-流畅选择
            case R.id.realplay_quality_btn:
                openQualityPopupWindow(realplayQualityBtn);
                break;

            //截图
            case R.id.tv_screenshot:
            CommonUtil.showToast("暂未开通");
                break;
        }
    }




    /***
     * 播放与暂停
     */
    private void handlePlayAndSuspend() {
            tvPlay.setEnabled(false);
            if (mStatus != RealPlayStatus.STATUS_STOP) {

                stopRealPlay();
                setRealPlayStopUI();
            }
            else {
                if (mEZPlayer!=null){
                    startRealPlay();
                    setRealPlayStartUI();
                }else {
                    Log.e("qzs","播放异常");
                }


            }
    }

    /***
     * 停止时的UI
     */
    private void setRealPlayStopUI() {
        ivLoad.clearAnimation();
        ivLoad.setVisibility(View.GONE);
        tvPlay.setBackgroundResource(R.drawable.ic_play_play_selector1);
        layoutBg.setVisibility(View.VISIBLE);
        realplayPlayIv.setVisibility(View.VISIBLE);
        tvTxtShow.setVisibility(View.GONE);

    }
    /***
     * 声音设置
     */
    private void onSoundBtnClick() {
        if (mLocalInfo.isSoundOpen()) {
            tvVoice.setBackgroundResource(R.drawable.ic_viedeo_sound2);
            mLocalInfo.setSoundOpen(false);


        } else {
            tvVoice.setBackgroundResource(R.drawable.ic_viedeo_sound1);
          //  LogUtils.d("当前没声音，准备去打开");
            mLocalInfo.setSoundOpen(true);

        }

        setRealPlaySound();
    }
    private void setRealPlaySound() {
        if (mEZPlayer != null) {
            if (mLocalInfo.isSoundOpen()) {
                mEZPlayer.openSound();
            } else {
                mEZPlayer.closeSound();
            }

        }else {
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            return;
        }
   
            mHandler = null;
            mHandler = new Handler(MainActivity.this);
            handleVideo();
            initVideoSetting();

        }

    /***
     * 去播放
     */
    private void handleVideo() {
        //token
        EZOpenSDK.getInstance().setAccessToken("你的token");
        setRealPlayStartUI();
        mRealPlaySh = mRealPlaySv.getHolder();
        //    mRealPlaySh.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mRealPlaySh.addCallback(MainActivity.this);
        startRealPlay();
    }
    @Override
    protected void onStop() {
        super.onStop();
        if (mEZPlayer != null) {
            mEZPlayer.stopRealPlay();
        }
        if(mOrientation== Configuration.ORIENTATION_LANDSCAPE){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);   //竖屏模式
        }

    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mEZPlayer != null) {
            mEZPlayer.release();
            ivLoad.clearAnimation();
            mEZPlayer.stopRealPlay();
        }
        mHandler = null;
    }

    @Override
    public void onBackPressed() {
        if(mOrientation== Configuration.ORIENTATION_LANDSCAPE){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);   //竖屏模式
        }else {
            finish();
        }
    }
}
