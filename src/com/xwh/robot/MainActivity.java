package com.xwh.robot;


import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ScrollView;
import android.widget.TextView;

import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechError;
import com.xwh.robot.util.NetworkUtil;
import com.xwh.robot.util.StringUtil;
import com.xwh.speech.IRecognizer;
import com.xwh.speech.JsonParser;
import com.xwh.speech.SpeechRecognizer;
import com.xwh.speech.Speeker;

public class MainActivity extends Activity {

	private static final String TAG = "SpeechActivity";
	
	private boolean isOnPause = false;

	private IRecognizer recognizer;
	
	private TextView textInfo;
	private TextView textMessage;
	private ScrollView scrollView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		textInfo = (TextView) this.findViewById(R.id.text_info);
		textMessage = (TextView) this.findViewById(R.id.text_message);
		scrollView = (ScrollView) this.findViewById(R.id.scrollView);
		
		this.findViewById(R.id.bt_chat).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startSpeech();
				
				Speeker.getInstance().stopSpeeking();
			}
		});
		
		textInfo.postDelayed(new Runnable(){
			@Override
			public void run() {
				String currentPerson = Speeker.getInstance().getCurrentPerson();
				String sayHello = getString(R.string.reply_start, currentPerson);
				appendMessage("\n"+currentPerson+"："+sayHello);
				Speeker.getInstance().startSpeeking(sayHello);
			}
		}, 1000);
	}

	@Override
	protected void onResume() {
		super.onResume();
		isOnPause = false;
	}

	@Override
	protected void onPause() {
		super.onPause();
		isOnPause = true;
		if(recognizer!=null){
			recognizer.stopListening();
		}
	}

	private void startSpeech(){
		
		Log.i(TAG, "开始识别");
		if (recognizer == null) {
			recognizer = SpeechRecognizer.getInstance(mRecognizerListener);
		}
		if(recognizer.isListening() || isOnPause){
			return;
		}

		textInfo.setText("开始录音...");
		recognizer.startSpeech();
	}


	/**
	 * 听写监听器。
	 */
	private RecognizerListener mRecognizerListener = new RecognizerListener() {

		@Override
		public void onBeginOfSpeech() {
			// 此回调表示：sdk内部录音机已经准备好了，用户可以开始语音输入
			Log.i(TAG, "开始录音");
			onPrepared();
		}

		@Override
		public void onError(SpeechError error) {
			// Tips：
			// 错误码：10118(您没有说话)，可能是录音机权限被禁，需要提示用户打开应用的录音权限。
			// 如果使用本地功能（语记）需要提示用户开启语记的录音权限。

			/*if (error.getErrorCode() == 10118) {
			}*/
			onFail(error.getPlainDescription(true));

			Log.e(TAG,"识别失败："+error.getErrorCode()+":"+error.getPlainDescription(true));

		}

		@Override
		public void onEndOfSpeech() {
			// 此回调表示：检测到了语音的尾端点，已经进入识别过程，不再接受语音输入
			//voiceCallback("",2);
			Log.i(TAG, "结束录音");
			textInfo.setText("");
		}

		@Override
		public void onResult(RecognizerResult results, boolean isLast) {

			String text = JsonParser.parseIatResult(results.getResultString()).trim();
			Log.d(TAG, "识别结果：" + text);

			if (text==null || "".equals(text) || "。".equals(text)) {
				return;
			}
			onSpeechSuccess(text);
			textInfo.setText("");
		}

		@Override
		public void onVolumeChanged(int volume, byte[] data) {
			//Log.d(TAG, "当前正在说话，音量大小：" + volume);
			//Log.d(TAG, "返回音频数据：" + data.length);
		}

		@Override
		public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
			//Log.d(TAG, "onEvent " + eventType);
		}
	};

	private void onPrepared(){
		//img_record.setVisibility(View.GONE);
	}
	

	private void onSpeechSuccess(final String result) {
		appendMessage("\n\n我："+result);
		
		if(result.contains("清空")){
			textMessage.setText("");
			Speeker.getInstance().startSpeeking(getString(R.string.reply_clear));
			return;
		}
		
		if(result.contains("换个人") || result.contains("换一个人")){
			Speeker.getInstance().nextPerson();

			String personName = Speeker.getInstance().getCurrentPerson();
			appendMessage("\n更换发音人："+personName);
			Speeker.getInstance().startSpeeking(getString(R.string.reply_change_person, personName));
			return;
		}
		
		if(result.contains("打开浏览器") || result.contains("百度")){
			Intent i = new Intent(Intent.ACTION_VIEW);
			i.setData(Uri.parse("http://www.baidu.com"));
			startActivity(i);
			return;
		}
		if(result.contains("打开微信")){
			startApp("com.tencent.mm");
			return;
		}
		if(result.contains("打开QQ")){
			//String url="mqqwpa://im/chat?chat_type=wpa&uin=1642084864";
			//startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
			startApp("com.tencent.mobileqq");
			return;
		}
		if(result.contains("打开微博")){
			startApp("com.sina.weibo");
			return;
		}
		
		new AsyncTask<Object, Object, String>() {
			@Override
			protected String doInBackground(Object... params) {
				String re = null;
				try {
					String apiUrl = "http://www.tuling123.com/openapi/api?key=79938c3a5664cb083cf47f779d482763&info="+StringUtil.urlEncode(result);
					JSONObject json = NetworkUtil.getJsonObject(apiUrl, null, 5000);
					
					re = json.optString("text");
					
				} catch (IOException e) {
					e.printStackTrace();
				} catch (JSONException e) {
					e.printStackTrace();
				}
				
				return re;
			}
			
			@Override
			protected void onPostExecute(String result) {
				
				recognizer.stopListening();	// 关掉录音，防止机器人自己录自己播报的语音。
				
				if(result!=null){
					appendMessage("\n"+Speeker.getInstance().getCurrentPerson()+"："+result);
					
					Speeker.getInstance().startSpeeking(result);
				}else{
					Speeker.getInstance().startSpeeking(getString(R.string.reply_unkown));
				}
			}
		}.execute();
		
	}


	private void onFail(String error){
		textInfo.setText(error);
	}

	private void appendMessage(String message){
		textMessage.append(message);
		scrollView.fullScroll(ScrollView.FOCUS_DOWN);  
	}
	
	private void startApp(String packageName){
		try{
			Intent intent = getPackageManager().getLaunchIntentForPackage(packageName);  
			startActivity(intent); 
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
