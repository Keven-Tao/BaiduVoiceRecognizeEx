package com.baidu.speech.recognizerdemo;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.baidu.speech.asr.SpeechConstant;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements IStatus{

    private TextView textView;
    private Button startBtn;

    /**
     * 识别控制器，使用MyRecognizer控制识别的流程
     */
    protected MyRecognizer myRecognizer;
    /**
     * 控制UI按钮的状态
     */
    protected int status;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            handleMsg(msg);
        }

    };

    protected void handleMsg(Message msg) {
        if (textView != null && msg.obj != null) {
            textView.append(msg.obj.toString() + "\n");
        }
//        switch (msg.what) { // 处理MessageStatusRecogListener中的状态回调
//            case STATUS_FINISHED:
//            case STATUS_NONE:
//            case STATUS_READY:
//            case STATUS_SPEAKING:
//            case STATUS_RECOGNITION:
//                status = msg.what;
//                updateBtnTextByStatus();
//                break;
//
//        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = (TextView) findViewById(R.id.text);
        startBtn = (Button) findViewById(R.id.start_btn);

        startBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
//                switch (status) {
//                    case STATUS_NONE: // 初始状态
//                        start();
//                        status = STATUS_WAITING_READY;
//                        updateBtnTextByStatus();
//                        textView.setText("");
//                        break;
//                    case STATUS_WAITING_READY: // 调用本类的start方法后，即输入START事件后，等待引擎准备完毕。
//                    case STATUS_READY: // 引擎准备完毕。
//                    case STATUS_SPEAKING:
//                    case STATUS_FINISHED:// 长语音情况
//                    case STATUS_RECOGNITION:
//                        stop();
//                        status = STATUS_STOPPED; // 引擎识别中
//                        updateBtnTextByStatus();
//                        break;
//                    case STATUS_STOPPED: // 引擎识别中
//                        cancel();
//                        status = STATUS_NONE; // 识别结束，回到初始状态
//                        updateBtnTextByStatus();
//                        break;
//                }
//
//            }
//        });
            }});
        initRecog();
    }

    private void updateBtnTextByStatus() {
        switch (status) {
            case STATUS_NONE:
                startBtn.setText("开始录音");
                startBtn.setEnabled(true);
                break;
            case STATUS_WAITING_READY:
            case STATUS_READY:
            case STATUS_SPEAKING:
            case STATUS_RECOGNITION:
                startBtn.setText("停止录音");
                startBtn.setEnabled(true);
                break;

            case STATUS_STOPPED:
                startBtn.setText("取消整个识别过程");
                startBtn.setEnabled(true);
                break;
        }
    }

    /**
     * 在onCreate中调用。初始化识别控制类MyRecognizer
     */
    protected void initRecog() {
        StatusRecogListener listener = new MessageStatusRecogListener(handler);
        myRecognizer = new MyRecognizer(this, listener);
//        status = STATUS_NONE;
        start();
    }

    /**
     * 开始录音，点击“开始”按钮后调用。
     */
    protected void start() {
        stringParams.addAll(Arrays.asList(
                SpeechConstant.VAD,
                SpeechConstant.IN_FILE
        ));
        intParams.addAll(Arrays.asList(
                SpeechConstant.VAD_ENDPOINT_TIMEOUT
        ));
        boolParams.addAll(Arrays.asList(
                SpeechConstant.ACCEPT_AUDIO_DATA,
                SpeechConstant.ACCEPT_AUDIO_VOLUME
        ));
//        initSamplePath(context);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        // 上面的获取是为了得到下面的Map， 可以忽略
        Map<String, Object> params = fetch(sp);
        // 这里打印出params， 填写至您自己的app中，
        myRecognizer.start(params);
    }

    /**
     * 开始录音后，手动停止录音。SDK会识别在此过程中的录音。点击“停止”按钮后调用。
     */
    private void stop() {
        myRecognizer.stop();
    }

    /**
     * 开始录音后，取消这次录音。SDK会取消本次识别，回到原始状态。点击“取消”按钮后调用。
     */
    private void cancel() {
        myRecognizer.cancel();
    }

    public Map<String, Object> fetch(SharedPreferences sp) {
        Map<String, Object> map = new HashMap<String, Object>();

        parseParamArr(sp, map);

        map.put(SpeechConstant.VAD_ENDPOINT_TIMEOUT, 0);

        return map;
    }

    /**
     * 字符串格式的参数
     */
    protected ArrayList<String> stringParams = new ArrayList<String>();

    /**
     * int格式的参数
     */
    protected  ArrayList<String> intParams = new ArrayList<String>();

    /**
     * bool格式的参数
     */
    protected  ArrayList<String> boolParams = new ArrayList<String>();
    /**
     * 根据 stringParams intParams boolParams中定义的参数名称，提取SharedPreferences相关字段
     * @param sp
     * @param map
     */
    private void parseParamArr(SharedPreferences sp, Map<String, Object> map) {
        for (String name : stringParams) {
            if (sp.contains(name)) {
                String tmp = sp.getString(name, "").replaceAll(",.*", "").trim();
                if (null != tmp && !"".equals(tmp)) {
                    map.put(name, tmp);
                }
            }
        }
        for (String name : intParams) {
            if (sp.contains(name)) {
                String tmp = sp.getString(name, "").replaceAll(",.*", "").trim();
                if (null != tmp && !"".equals(tmp)) {
                    map.put(name, Integer.parseInt(tmp));
                }
            }
        }
        for (String name : boolParams) {
            if (sp.contains(name)) {
                map.put(name, sp.getBoolean(name, false));
            }
        }
    }

    @Override
    protected void onDestroy() {
        stop();
        cancel();
        myRecognizer.release();
        super.onDestroy();
    }
}
