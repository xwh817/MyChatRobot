package com.xwh.speech;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.xwh.robot.ChatApplication;

/**
 * 命令词识别
 */
public class WordsRecognizer implements IRecognizer {

    private static final String TAG = "WordsRecognizer";
    private Context context;
    private static WordsRecognizer instance;
    private SpeechRecognizer mAsr;


    public synchronized static WordsRecognizer getInstance(RecognizerListener mRecognizerListener){
        if(instance==null){
            instance = new WordsRecognizer();
            instance.context = ChatApplication.instance;
            instance.init();
        }

        instance.mRecognizerListener = mRecognizerListener;

        return instance;
    }
    /**
     * 切换语言后，需重新初始化一下
     */
    public static void resetInstance(){
        instance = null;
    }

    private WordsRecognizer(){ }


    public void init(){

        //  在线命令词识别，不启用终端级语法
        // 1.创建SpeechRecognizer对象
        mAsr = SpeechRecognizer.createRecognizer(context, mInitListener);
        // 2.设置参数
       setParam();

    }


    /**
     * 初始化监听器。
     */
    private InitListener mInitListener = new InitListener() {
        @Override
        public void onInit(int code) {
            if (code != ErrorCode.SUCCESS) {
                Log.e(TAG, "初始化失败，错误码：" + code);
            }
        }
    };


    @Override
    public void startSpeech() {

        if(mAsr.isListening()) {
            mAsr.stopListening();
        }
        // 3.开始识别
        int ret = mAsr.startListening(mRecognizerListener);
        if (ret != ErrorCode.SUCCESS) {
            Log.e(TAG, "识别失败,错误码: " + ret);
        }
    }

    @Override
    public void stopListening(){
        if(mAsr!=null){
            mAsr.stopListening();
        }
    }

	@Override
	public boolean isListening(){
		if(mAsr!=null){
			return mAsr.isListening();
		}else{
			return false;
		}
	}
	
    //  识别监听器
    private RecognizerListener mRecognizerListener = new RecognizerListener() {
        //  音量变化
        public void onVolumeChanged(int volume, byte[] data) {}
        //  返回结果
        public void onResult(final RecognizerResult result, boolean isLast) {}
        //  开始说话
        public void onEndOfSpeech() {}
        //  结束说话
        public void onBeginOfSpeech() {}
        //  错误回调
        public void onError(SpeechError error) {}
        //  事件回调
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {}
    };



    /**
     * 参数设置
     *
     * @return
     */
    public void setParam() {
        // 清空参数
        mAsr.setParameter(SpeechConstant.PARAMS, null);

        // 设置听写引擎
        mAsr.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
        mAsr.setParameter(SpeechConstant.SUBJECT, "asr");
        // 设置返回结果格式
        //mAsr.setParameter(SpeechConstant.RESULT_TYPE, "json");


        //String lag = "mandarin";

        // 设置语言
        mAsr.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
        // 设置语言区域
        mAsr.setParameter(SpeechConstant.ACCENT, "mandarin");
    

        // 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
        mAsr.setParameter(SpeechConstant.VAD_BOS, "5000");

        // 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
        mAsr.setParameter(SpeechConstant.VAD_EOS, "1000");

        // 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
        mAsr.setParameter(SpeechConstant.ASR_PTT, "0");

        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        // 注：AUDIO_FORMAT参数语记需要更新版本才能生效
        //mAsr.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
        //mAsr.setParameter(SpeechConstant.ASR_AUDIO_PATH, Environment.getExternalStorageDirectory() + "/msc/iat.wav");

        // 设置听写结果是否结果动态修正，为“1”则在听写过程中动态递增地返回结果，否则只在听写结束之后返回最终结果
        // 注：该参数暂时只对在线听写有效
        mAsr.setParameter(SpeechConstant.ASR_DWA, "0");
    }

}
