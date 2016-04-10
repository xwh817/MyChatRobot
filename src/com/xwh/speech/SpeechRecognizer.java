package com.xwh.speech;


import java.io.InputStream;

import android.content.Context;
import android.util.Log;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.LexiconListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.xwh.robot.ChatApplication;

public class SpeechRecognizer implements IRecognizer {
	private static final String TAG = "VoiceToWord";
	private Context context;
	// 语音听写对象
	private com.iflytek.cloud.SpeechRecognizer mIat;
	// 语音听写UI
	//private RecognizerDialog mIatDialog;
	// 用HashMap存储听写结果
	//private HashMap<String, String> mIatResults = new LinkedHashMap<String, String>();
	// 引擎类型
	private String mEngineType = SpeechConstant.TYPE_CLOUD;

	//private Toast mToast;
	//private SharedPreferences mSharedPreferences;

	private RecognizerListener mRecognizerListener;

	private static SpeechRecognizer instance;

	public synchronized static SpeechRecognizer getInstance(RecognizerListener mRecognizerListener){
		if(instance==null){
			instance = new SpeechRecognizer();
			instance.context = ChatApplication.instance;
			instance.init();
			//instance.uploadUserWords();
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

	private SpeechRecognizer(){
	}



	private void init() {
		// 初始化识别无UI识别对象
		// 使用SpeechRecognizer对象，可根据回调消息自定义界面；
		mIat = com.iflytek.cloud.SpeechRecognizer.createRecognizer(context, mInitListener);
		
		// 设置参数
		setParam();

		// 初始化听写Dialog，如果只使用有UI听写功能，无需创建SpeechRecognizer
		// 使用UI听写功能，请根据sdk文件目录下的notice.txt,放置布局文件和图片资源
		//mIatDialog = new RecognizerDialog(context, mInitListener);

		// 上传用户自定义词
		//uploadUserWords();
	}

	/**
	 * 初始化监听器。
	 */
	private InitListener mInitListener = new InitListener() {

		@Override
		public void onInit(int code) {
			//Log.d(TAG, "SpeechRecognizer init() code = " + code);
			if (code != ErrorCode.SUCCESS) {
				//showTip("初始化失败，错误码：" + code);
				Log.e(TAG, "初始化失败，错误码：" + code);
			}
		}
	};

	int ret = 0; // 函数调用返回值

	@Override
	public void startSpeech() {
		//mIatResults.clear();
		
		//boolean isShowDialog = mSharedPreferences.getBoolean("isShowDialog", false);

		boolean isShowDialog = false;

		if (isShowDialog) {
			// 显示听写对话框
			/*mIatDiaLog.setListener(mRecognizerDialogListener);
			mIatDiaLog.show();
			showTip("请开始说话…");*/
		} else {
			// 不显示听写对话框
			
			if(mIat.isListening()) {
				mIat.stopListening();
			}
			
			
			ret = mIat.startListening(mRecognizerListener);
			if (ret != ErrorCode.SUCCESS) {
				//showTip("听写失败,错误码：" + ret);
			} else {
				//showTip("请开始说话…");
			}
		}
	}

	@Override
	public void stopListening(){
		if(mIat!=null){
			mIat.stopListening();
		}
	}
	
	@Override
	public boolean isListening(){
		if(mIat!=null){
			return mIat.isListening();
		}else{
			return false;
		}
	}

	public void uploadUserWords() {
		String contents = readFile(context, "userwords.json", "utf-8");
		//mResultText.setText(contents);
		// 指定引擎类型
		mIat.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
		mIat.setParameter(SpeechConstant.TEXT_ENCODING, "utf-8");
		ret = mIat.updateLexicon("userword", contents, mLexiconListener);
		if (ret != ErrorCode.SUCCESS){
			//showTip("上传热词失败,错误码：" + ret);
			Log.e(TAG, "上传热词失败,错误码：" + ret);
		}else{
			Log.i(TAG, "上传热词成功");
		}

	}

	/**
	 * 读取asset目录下文件。
	 * @return content
	 */
	public static String readFile(Context mContext,String file,String code)
	{
		int len = 0;
		byte []buf = null;
		String result = "";
		try {
			InputStream in = mContext.getAssets().open(file);
			len  = in.available();
			buf = new byte[len];
			in.read(buf, 0, len);

			result = new String(buf,code);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 上传联系人/词表监听器。
	 */
	private LexiconListener mLexiconListener = new LexiconListener() {

		@Override
		public void onLexiconUpdated(String lexiconId, SpeechError error) {
			if (error != null) {
				//showTip(error.toString());
			} else {
				//showTip("上传用户词成功");
			}
		}
	};


	/**
	 * 参数设置
	 *
	 * @return
	 */
	public void setParam() {
		// 清空参数
		mIat.setParameter(SpeechConstant.PARAMS, null);

		// 设置听写引擎
		mIat.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);
		// 设置返回结果格式
		//mIat.setParameter(SpeechConstant.RESULT_TYPE, "json");


		//String lag = "mandarin";


		// 设置语言
		mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
		// 设置语言区域
		mIat.setParameter(SpeechConstant.ACCENT, "mandarin");


		// 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
		mIat.setParameter(SpeechConstant.VAD_BOS, "5000");

		// 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
		mIat.setParameter(SpeechConstant.VAD_EOS, "1000");

		// 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
		mIat.setParameter(SpeechConstant.ASR_PTT, "0");

		// 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
		// 注：AUDIO_FORMAT参数语记需要更新版本才能生效
		//mIat.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
		//mIat.setParameter(SpeechConstant.ASR_AUDIO_PATH, Environment.getExternalStorageDirectory() + "/msc/iat.wav");

		// 设置听写结果是否结果动态修正，为“1”则在听写过程中动态递增地返回结果，否则只在听写结束之后返回最终结果
		// 注：该参数暂时只对在线听写有效
		mIat.setParameter(SpeechConstant.ASR_DWA, "0");
		
	}

	/*private void showTip(final String str) {
		mToast.setText(str);
		mToast.show();
	}*/

}
