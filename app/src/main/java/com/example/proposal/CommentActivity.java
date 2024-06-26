package com.example.proposal;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;

public class CommentActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    EditText etText;
    ImageView ivMic,ivCopy;
    Spinner spLangs;
    String lcode = "en-US";

    // Languages included
    String[] languages = {"English","Tamil","Hindi","Spanish","French",
            "Arabic","Chinese","Japanese","German"};

    // Language codes
    String[] lCodes = {"en-US","ta-IN","hi-IN","es-CL","fr-FR",
            "ar-SA","zh-TW","jp-JP","de-DE"};

    private Instant startActivityTime;

    @Override
    protected void onPause() {
        super.onPause();
        if(MainActivity.recordToday != null) {
            Instant now = Instant.now();
            Duration appTimeElapsed = Duration.between(startActivityTime, now);
            MainActivity.recordToday.setApptime(MainActivity.recordToday.getApptime() + appTimeElapsed.getSeconds());
            MainActivity.recordToday.setPhototime(MainActivity.recordToday.getPhototime() + appTimeElapsed.getSeconds());
            RecordUtil.modifyRecord(MainActivity.recordToday);
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        startActivityTime = Instant.now();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        // initialize views
        etText = findViewById(R.id.etSpeech);
        ivMic = findViewById(R.id.ivSpeak);
        ivCopy = findViewById(R.id.ivCopy);
        spLangs = findViewById(R.id.spLang);

        // set onSelectedItemListener for the spinner
        spLangs.setOnItemSelectedListener(this);

        // setting array adapter for spinner
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item,languages);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spLangs.setAdapter(adapter);

        // on click listener for mic icon
        ivMic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // creating intent using RecognizerIntent to convert speech to text
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,lcode);
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT,"Speak now!");
                // starting intent for result
                activityResultLauncher.launch(intent);
            }
        });

        // on click listener to copy the speech
        ivCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // code to copy to clipboard
                ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                clipboardManager.setPrimaryClip(ClipData.newPlainText("label",etText.getText().toString().trim()));
                Toast.makeText(CommentActivity.this, "Copied!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // activity result launcher to start intent
    ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    // if result is not empty
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData()!=null) {
                        // get data and append it to editText
                        ArrayList<String> d=result.getData().getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                        etText.setText(etText.getText()+" "+d.get(0));
                        MainActivity.recordToday.setCommentnumber(MainActivity.recordToday.getCommentnumber() +1);
                    }
                }
            });

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        // setting lcode corresponding
        // to the language selected
        lcode = lCodes[i];
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        // automatically generated method
        // for implementing onItemSelectedListener
    }
}
