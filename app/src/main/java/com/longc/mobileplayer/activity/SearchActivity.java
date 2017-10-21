package com.longc.mobileplayer.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;
import com.longc.mobileplayer.R;
import com.longc.mobileplayer.adapter.SearchAdapter;
import com.longc.mobileplayer.domain.SearchBean;
import com.longc.mobileplayer.utils.Constants;
import com.longc.mobileplayer.utils.JsonParser;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by longc on 2016/12/25.
 */
public class SearchActivity extends Activity {

    private EditText etInput;
    private ImageView ivVoice;
    private TextView tvSearch;
    private ListView listview;
    private ProgressBar progressBar;
    private TextView tvNodata;

    // 用HashMap存储听写结果
    private HashMap<String, String> mIatResults = new LinkedHashMap<String, String>();
    private List<SearchBean.ItemData> items;
    private String url;
    private SearchAdapter adapter;

    /**
     * Find the Views in the layout<br />
     * <br />
     * Auto-created on 2016-12-25 20:04:35 by Android Layout Finder
     * (http://www.buzzingandroid.com/tools/android-layout-finder)
     */
    private void findViews() {
        etInput = (EditText) findViewById(R.id.et_input);
        ivVoice = (ImageView) findViewById(R.id.iv_voice);
        tvSearch = (TextView) findViewById(R.id.tv_search);
        listview = (ListView) findViewById(R.id.listview);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        tvNodata = (TextView) findViewById(R.id.tv_nodata);
        MyOnClickListener myOnClickListener = new MyOnClickListener();
        //设置点击监听
        ivVoice.setOnClickListener(myOnClickListener);
        tvSearch.setOnClickListener(myOnClickListener);
    }

     class MyOnClickListener implements View.OnClickListener {
         @Override
         public void onClick(View v) {
             switch (v.getId()) {
                 case R.id.iv_voice:
//                    Toast.makeText(SearchActivity.this, "语音", Toast.LENGTH_SHORT).show();
                     showDialog();
                     break;
                 case R.id.tv_search:
//                    Toast.makeText(SearchActivity.this, "搜索", Toast.LENGTH_SHORT).show();
//                    speechText();
                     searchText();
                     break;

             }
         }
     }
        private void searchText() {
            String text = etInput.getText().toString().trim();
            if (!TextUtils.isEmpty(text)) {
                if (items != null && items.size() > 0) {
                    items.clear();
                }try {
                    text = URLEncoder.encode(text, "UTF-8");
                    url = Constants.SEARCH_URL + text;
                    getDataFromNet();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }

    private void getDataFromNet() {
        progressBar.setVisibility(View.VISIBLE);
        RequestParams params = new RequestParams(url);
        x.http().get(params, new Callback.CommonCallback<String>() {

            @Override
            public void onSuccess(String result) {
                processData(result);
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(CancelledException cex) {

            }

            @Override
            public void onFinished() {
                progressBar.setVisibility(View.GONE);
            }
        });
    }
    private void processData(String result) {
        SearchBean searchBean = parsedJson(result);
        items =  searchBean.getItems();
        showData();
    }

    private void showData() {
        if(items !=null && items.size() > 0){
            //设置适配器
            adapter = new SearchAdapter(this,items);
            listview.setAdapter(adapter);
            tvNodata.setVisibility(View.GONE);
        }else{
            tvNodata.setVisibility(View.VISIBLE);
            adapter.notifyDataSetChanged();
        }
        progressBar.setVisibility(View.GONE);
    }

    private SearchBean parsedJson(String result) {
        return new Gson().fromJson(result,SearchBean.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        findViews();
        /**
         * 点击search的Item跳转到系统浏览器加载新闻信息
         */
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String detailUrl = items.get(position).getDetailUrl();
//                Toast.makeText(SearchActivity.this,detailUrl,Toast.LENGTH_SHORT).show();
                Intent intent = new Intent();

                intent.setAction("android.intent.action.VIEW");

                intent.addCategory("android.intent.category.DEFAULT");

                intent.setData(Uri.parse(detailUrl));

                startActivity(intent);

            }
        });
    }

    private void showDialog() {
        //1.创建RecognizerDialog对象
        RecognizerDialog mDialog = new RecognizerDialog(this, new MyInitListener());
        //2.设置accent、 language等参数
        mDialog.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
        mDialog.setParameter(SpeechConstant.ACCENT, "mandarin");
        //若要将UI控件用于语义理解，必须添加以下参数设置，设置之后onResult回调返回将是语义理解
        //结果
        // mDialog.setParameter("asr_sch", "1");
        // mDialog.setParameter("nlp_version", "2.0");
        //3.设置回调接口
        mDialog.setListener(new MyRecognizerDialogListener());
        //4.显示dialog，接收语音输入
        mDialog.show();
    }

    class MyRecognizerDialogListener implements RecognizerDialogListener {

        /**
         * @param recognizerResult
         * @param b                是否说话结束
         */
        @Override
        public void onResult(RecognizerResult recognizerResult, boolean b) {
            String result = recognizerResult.getResultString();
            Log.e("MainActivity", "result ==" + result);
            String text = JsonParser.parseIatResult(result);
            //解析好的
            Log.e("MainActivity", "text ==" + text);

            String sn = null;
            // 读取json结果中的sn字段
            try {
                JSONObject resultJson = new JSONObject(recognizerResult.getResultString());
                sn = resultJson.optString("sn");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            mIatResults.put(sn, text);

            StringBuffer resultBuffer = new StringBuffer();//拼成一句
            for (String key : mIatResults.keySet()) {
                resultBuffer.append(mIatResults.get(key));
            }

            etInput.setText(resultBuffer.toString());
            etInput.setSelection(etInput.length());

        }

        /**
         * 出错了
         *
         * @param speechError
         */
        @Override
        public void onError(SpeechError speechError) {
            Log.e("MainActivity", "onError ==" + speechError.getMessage());

        }
    }

    class MyInitListener implements InitListener {

        @Override
        public void onInit(int i) {
            if (i != ErrorCode.SUCCESS) {
                Toast.makeText(SearchActivity.this, "初始化失败", Toast.LENGTH_SHORT).show();
            }
        }
    }

        /*
                private void speechText() {
                    //1.创建 SpeechSynthesizer 对象, 第二个参数： 本地合成时传 InitListener
                    SpeechSynthesizer mTts = SpeechSynthesizer.createSynthesizer(SearchActivity.this, null);
                    //2.合成参数设置，详见《 MSC Reference Manual》 SpeechSynthesizer 类
                    //设置发音人（更多在线发音人，用户可参见 附录13.2
                    mTts.setParameter(SpeechConstant.VOICE_NAME, "vixr"); //设置发音人
                    mTts.setParameter(SpeechConstant.SPEED, "50");//设置语速
                    mTts.setParameter(SpeechConstant.VOLUME, "80");//设置音量，范围 0~100
                    mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD); //设置云端
                    //设置合成音频保存位置（可自定义保存位置），保存在“./sdcard/iflytek.pcm”
                    //保存在 SD 卡需要在 AndroidManifest.xml 添加写 SD 卡权限
                    //仅支持保存为 pcm 和 wav 格式， 如果不需要保存合成音频，注释该行代码
                    mTts.setParameter(SpeechConstant.TTS_AUDIO_PATH, "./sdcard/iflytek.pcm");
                    //3.开始合成
                    mTts.startSpeaking(etSearch.getText().toString(), mSynListener);

                }

                //合成监听器
                private SynthesizerListener mSynListener = new SynthesizerListener() {
                    //会话结束回调接口，没有错误时， error为null
                    public void onCompleted(SpeechError error) {
                        Toast.makeText(SearchActivity.this, "结束了", Toast.LENGTH_SHORT).show();
                    }

                    //缓冲进度回调
                    //percent为缓冲进度0~100， beginPos为缓冲音频在文本中开始位置， endPos表示缓冲音频在
                    //文本中结束位置， info为附加信息。
                    public void onBufferProgress(int percent, int beginPos, int endPos, String info) {
                    }

                    //开始播放
                    public void onSpeakBegin() {
                        Toast.makeText(SearchActivity.this, "开始了", Toast.LENGTH_SHORT).show();
                    }

                    //暂停播放
                    public void onSpeakPaused() {
                    }

                    //播放进度回调
                    //percent为播放进度0~100,beginPos为播放音频在文本中开始位置， endPos表示播放音频在文
                    //本中结束位置.
                    public void onSpeakProgress(int percent, int beginPos, int endPos) {
                    }

                    //恢复播放回调接口
                    public void onSpeakResumed() {
                    }

                    //会话事件回调接口
                    public void onEvent(int arg0, int arg1, int arg2, Bundle arg3) {
                    }
                };

        */
}
