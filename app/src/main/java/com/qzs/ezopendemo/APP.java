package com.qzs.ezopendemo;

import android.app.Application;
import android.content.Context;

import com.videogo.openapi.EZOpenSDK;

/**
 * @author qinzishuai
 * 描述：
 * 创建日期：2019/5/18
 *
 */
public class APP  extends Application {
    public static Context mContext;
    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        initEZOpenSDK();

    }
    /***
     * 初始化萤石云
     */
    private void initEZOpenSDK() {

        /** * sdk日志开关，正式发布需要去掉 */
        EZOpenSDK.showSDKLog(true);
        /** * 设置是否支持P2P取流,详见api */
        EZOpenSDK.enableP2P(false);

        /** * APP_KEY请替换成自己申请的 */
        EZOpenSDK.initLib(this, "你的appkey");


    }

    /**获取context对象
     *
     * @return
     */
    public static synchronized Context getContext() {
        return mContext;
    }

    public static EZOpenSDK getOpenSDK() {
        return EZOpenSDK.getInstance();
    }

}
