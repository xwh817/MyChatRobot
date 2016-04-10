package com.xwh.speech;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;
import com.xwh.robot.ChatApplication;

/**
 * Created by Administrator on 2016/3/15.
 */
public class Speeker {

    private static final String TAG = "Speeker";

    /**
     * 发音人，请参考讯飞语音提供的
     * 发音人："小燕", "小宇", "凯瑟琳", "亨利", "玛丽", "小研", "小琪", "小峰", "小梅", "小莉", "小蓉", "小芸", "小坤", "小强 ", "小莹","小新", "楠楠", "老孙"
     * 对应参数："xiaoyan", "xiaoyu", "catherine", "henry", "vimary", "vixy", "xiaoqi", "vixf", "xiaomei", "xiaolin", "xiaorong", "xiaoqian", "xiaokun", "xiaoqiang", "vixying", "xiaoxin", "nannan", "vils"
     */
    public static final String VOICER_PERSON = "xiaoyan";
    
    public String[] persons = {"xiaoyan", "xiaoyu", 
    		"xiaoxin", "nannan", 
    		"vixy", "xiaoqi", 
    		"vixf", "xiaomei", "xiaolin", "xiaorong", 
    		"xiaoqian", "xiaokun", "xiaoqiang", "vixying", "vils"};
    public String[] personNames = {"小燕", "小宇",  
    		"小新", "楠楠",
    		"小研", "小琪", "小峰", 
    		"小梅", "小莉", "小蓉", 
    		"小芸", "小坤", "小强 ", "小莹", "老孙"};
    public int personIndex;
    
    private static Speeker instance;
    // 语音合成对象
    private SpeechSynthesizer mTts;

    public static synchronized Speeker getInstance(){
        if(instance == null){
            instance = new Speeker();
            instance.init();
        }

        return instance;
    }

    private void init(){
        Context context = ChatApplication.instance;
        // 初始化合成对象
        mTts = SpeechSynthesizer.createSynthesizer(context, mTtsInitListener);

        // 清空参数
        mTts.setParameter(SpeechConstant.PARAMS, null);
        //2.合成参数设置，详见《科大讯飞MSC API手册(Android)》SpeechSynthesizer  类
        mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
        mTts.setParameter(SpeechConstant.VOICE_NAME, persons[personIndex]);//设置发音人
        mTts.setParameter(SpeechConstant.SPEED, "50");//设置语速
        mTts.setParameter(SpeechConstant.PITCH, "50");	// 设置音调
        //mTts.setParameter(SpeechConstant.VOLUME, "80");//设置音量，范围 0~100    // 不设置，使用系统设置的值
        //设置播放器音频流类型
        //mTts.setParameter(SpeechConstant.STREAM_TYPE, "3");
        // 设置播放合成音频打断音乐播放，默认为true
        mTts.setParameter(SpeechConstant.KEY_REQUEST_FOCUS, "true");

        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        // 注：AUDIO_FORMAT参数语记需要更新版本才能生效
        // 如果不需要保存合成音频，注释该代码
        //mTts.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
        //mTts.setParameter(SpeechConstant.TTS_AUDIO_PATH, Environment.getExternalStorageDirectory()+"/msc/tts.wav");

        //3.开始合成
        //mTts.startSpeaking("科大讯飞，让世界聆听我们的声音", mSynListener);
        //合成监听器，回调都发生在主线程（UI 线程）中
    }

    /**
     * 初始化监听。
     */
    private InitListener mTtsInitListener = new InitListener() {
        @Override
        public void onInit(int code) {
            Log.d(TAG, "InitListener init() code = " + code);
            if (code != ErrorCode.SUCCESS) {
                Log.e(TAG, "初始化失败,错误码："+code);
            } else {
                // 初始化成功，之后可以调用startSpeaking方法
                // 注：有的开发者在onCreate方法中创建完合成对象之后马上就调用startSpeaking进行合成，
                // 正确的做法是将onCreate中的startSpeaking调用移至这里
            }
        }
    };
    
    public void setPerson(String person){
    	mTts.setParameter(SpeechConstant.VOICE_NAME, person);//设置发音人
    }
    
    public void nextPerson(){
    	personIndex++;
    	if(personIndex>=persons.length){
    		personIndex = 0;
    	}
    	setPerson(persons[personIndex]);
    }
    
    public String getCurrentPerson(){
    	return personNames[personIndex];
    }

    public void startSpeeking(String text){
        startSpeeking(text, null);
    }

    public void startSpeeking(String text, SynthesizerListener synthesizerListener){
        if(TextUtils.isEmpty(text)){
            return;
        }

        if(mTts == null){
            init();
        }

        int code = mTts.startSpeaking(text, synthesizerListener);
        if (code != ErrorCode.SUCCESS) {
            Log.e(TAG, "语音合成失败,错误码：" + code);
        }
    }

    public void stopSpeeking(){
        if(mTts!=null){
            mTts.stopSpeaking();
        }
    }

    public void pauseSpeeking(){
        if(mTts!=null){
            mTts.pauseSpeaking();
        }
    }

    public void finish(){
        if(mTts!=null){
            mTts.destroy();
        }
    }

}
