package com.example.speechperson;


import android.content.Intent;
import android.net.Uri;
import android.os.Message;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    ServerConnection sc;
    Button bt1, bt2;
    TextView tv1, tv2;
    JSONArray ja;
    private TextToSpeech tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bt1 = (Button) findViewById(R.id.button1);
        bt2 = (Button) findViewById(R.id.button2);
        tv1 = (TextView) findViewById(R.id.textView1);
        tv2 = (TextView) findViewById(R.id.textView2);
        sc = new ServerConnection();
        ja = new JSONArray();
        bt1.setOnClickListener(listen);
        Thread t1 = new Thread(r1);
        t1.start();
    }


    private View.OnClickListener listen = new View.OnClickListener() {


        public void onClick(View v) {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "zh-TW");
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Listening...");
            startActivityForResult(intent, 1);
        }
    };


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        //用來儲存最後的辨識結果
        String firstMatch;
        if (requestCode == 1 && resultCode == RESULT_OK)
        {
            //取出多個辨識結果並儲存在String的ArrayList中
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            firstMatch = (String) result.get(0);
        }
        else
        {
            firstMatch = "無法辨識";
        }

        tv1.setText(firstMatch);

        try {
            for (int i = 0; i < ja.length(); i++) {
                if (firstMatch.equals(ja.getJSONObject(i).get("A").toString()) || firstMatch.equals(ja.getJSONObject(i).get("B").toString())) {
                    tv2.setText(ja.getJSONObject(i).get("C").toString());
                    tts = new TextToSpeech(MainActivity.this, ttsInitListener);
                }
            }
        } catch (JSONException e) {
        }

    }

    private Runnable r1 = new Runnable() {
        public void run() {
            ja = sc.query("imf70", "imf70", "A,B,C", "id>0");
        }

    };


    private TextToSpeech.OnInitListener ttsInitListener = new TextToSpeech.OnInitListener() {
        public void onInit(int status) {
            if (status == TextToSpeech.SUCCESS) {
                int result = tts.setLanguage(Locale.US);
    // 如果該語言資料不見了或沒有支援則無法使用
                if (result == TextToSpeech.LANG_MISSING_DATA
                        || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("TTS", "This Language is not supported");
                } else {
    // 語調(1為正常語調；0.5比正常語調低一倍；2比正常語調高一倍)
                    tts.setPitch((float) 0.5);
    // 速度(1為正常速度；0.5比正常速度慢一倍；2比正常速度快一倍)
                    tts.setSpeechRate((float) 0.5);
    // 設定要說的內容文字
                   tts.speak(tv2.getText().toString(), TextToSpeech.QUEUE_FLUSH, null);
                }
            } else {
                Toast.makeText(MainActivity.this, "Initialization Failed!",
                        Toast.LENGTH_LONG).show();
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
// Don't forget to shutdown tts!
        if (tts != null)
        {
            tts.stop();
            tts.shutdown();
        }
    }
}

