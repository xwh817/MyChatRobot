package com.xwh.robot;

import android.app.Application;
import android.util.Log;

import com.iflytek.cloud.SpeechUtility;
import com.xwh.robot.constant.Constant;

public class ChatApplication extends Application{
	private static final String TAG = "ChatAppliance";

	public static ChatApplication instance;

	@Override
	public void onCreate() {
		super.onCreate();
		Log.i(TAG, "KnobApplication onCreate");
		instance = this;
		
		initSpeech();
	}
	

	/**
	 * 初始化语音识别
	 */
	private void initSpeech(){
		try{
			SpeechUtility.createUtility(this, "appid=" + Constant.SPEECH_APP_ID);



		}catch (Exception e){
			e.printStackTrace();
		}
	}

}
