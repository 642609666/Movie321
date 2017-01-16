package com.example.administrator.movie321.activity;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.administrator.movie321.R;
import com.example.administrator.movie321.bean.SearchAdapter;
import com.example.administrator.movie321.bean.SearchBean;
import com.example.administrator.movie321.utils.Constants;
import com.example.administrator.movie321.utils.JsonParser;
import com.google.gson.Gson;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;

public class SearchActivity extends AppCompatActivity implements View.OnClickListener {
    private ImageView ivVoice;
    private TextView etSearch;
    private ListView listview;
    private EditText ed_sou;
    private List<SearchBean> data;
    private HashMap<String, String> mIatResults = new HashMap<>();

    /**
     * Find the Views in the layout<br />
     * <br />
     * Auto-created on 2017-01-15 16:12:34 by Android Layout Finder
     * (http://www.buzzingandroid.com/tools/android-layout-finder)
     */
    private void findViews() {
        setContentView(R.layout.activity_search);
        SpeechUtility.createUtility(this, SpeechConstant.APPID + "=5838f0d9");
        ivVoice = (ImageView) findViewById(R.id.iv_voice);
        etSearch = (TextView) findViewById(R.id.et_search);
        listview = (ListView) findViewById(R.id.listview);
        ed_sou = (EditText) findViewById(R.id.ed_sou);
        ivVoice.setOnClickListener(this);
        etSearch.setOnClickListener(this);
//        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Intent intent = new Intent(SearchActivity.this, SystemVideoPlayerActivity.class);
//
//                Bundle bundle = new Bundle();
//                //列表数据
//                bundle.putSerializable("videolist", (Serializable) data);
//                intent.putExtras(bundle);
//                //传递点击的位置
//                intent.putExtra("position", position);
//                startActivity(intent);
//            }
//        });
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        findViews();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_voice:
                showDialogVoice();
                break;
            case R.id.et_search:
                gotoSearch();
                break;
        }
    }

    private void gotoSearch() {
        String word = etSearch.getText().toString().trim();
        try {
            word = URLEncoder.encode(word, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        if (!TextUtils.isEmpty(word)) {

            String url = Constants.NET_SEARCH_URL + word;
            getDataFromNet(url);


        } else {
            //请输入关键字
            Toast.makeText(this, "请输入关键字", Toast.LENGTH_SHORT).show();
        }
    }

    private void getDataFromNet(String url) {

        RequestParams params = new RequestParams(url);
        x.http().get(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                processData(result);

            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {

            }

            @Override
            public void onCancelled(CancelledException cex) {

            }

            @Override
            public void onFinished() {

            }
        });
    }

    private void processData(String result) {
        SearchBean searchBean = new Gson().fromJson(result, SearchBean.class);
        List<SearchBean.ItemsBean> items = searchBean.getItems();
        if (items != null && items.size() > 0) {
            SearchAdapter searchAdapter = new SearchAdapter(SearchActivity.this, items);
            listview.setAdapter(searchAdapter);

        }


    }

    private void showDialogVoice() {
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

        @Override
        public void onResult(RecognizerResult recognizerResult, boolean b) {
            String result = recognizerResult.getResultString();
            System.out.println(result);
            String text = JsonParser.parseIatResult(recognizerResult.getResultString());

            String sn = null;
            // 读取json结果中的sn字段
            try {
                JSONObject resultJson = new JSONObject(recognizerResult.getResultString());
                sn = resultJson.optString("sn");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            mIatResults.put(sn, text);

            StringBuffer resultBuffer = new StringBuffer();
            for (String key : mIatResults.keySet()) {
                resultBuffer.append(mIatResults.get(key));
            }
            String reulst = resultBuffer.toString();
            reulst = reulst.replace("。", "");
            ed_sou.setText(reulst);
            ed_sou.setSelection(etSearch.length());

        }

        @Override
        public void onError(SpeechError speechError) {

            Toast.makeText(SearchActivity.this, "出错了哦", Toast.LENGTH_SHORT).show();
        }
    }

    class MyInitListener implements InitListener {

        @Override
        public void onInit(int i) {


        }
    }
}
