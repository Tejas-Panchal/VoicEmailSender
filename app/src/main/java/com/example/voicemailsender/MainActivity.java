package com.example.voicemailsender;

import static java.util.Collections.replaceAll;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int REQ_CODE_EMAIL = 100;
    private static final int REQ_CODE_SUBJECT = 101;
    private static final int REQ_CODE_MESSAGE = 102;

    private TextView tvEmail, tvSubject, tvMessage;
    private Button btnEmail, btnMessage, btnSubject, btnSendEmail;

    private String emailID, subjectBody, messageBody;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvEmail = findViewById(R.id.tvEmail);
        tvSubject = findViewById(R.id.tvSubject);
        tvMessage = findViewById(R.id.tvMessage);
        btnEmail = findViewById(R.id.btnEmail);
        btnSubject = findViewById(R.id.btnSubject);
        btnMessage = findViewById(R.id.btnMessage);
        btnSendEmail = findViewById(R.id.btnSendEmail);

        btnEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                promptSpeechInput(REQ_CODE_EMAIL);
            }
        });

        btnSubject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                promptSpeechInput(REQ_CODE_SUBJECT);
            }
        });

        btnMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                promptSpeechInput(REQ_CODE_MESSAGE);
            }
        });

        btnSendEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendEmail();
            }
        });
    }

    private void promptSpeechInput(int requestCode) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, requestCode == REQ_CODE_EMAIL ? "Speak the email ID" : "Speak the message");

        try {
            startActivityForResult(intent, requestCode);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(), "Sorry! Your device doesn’t support speech input", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && null != data) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (requestCode == REQ_CODE_EMAIL) {
                emailID = result.get(0);
                emailID = emailID.toLowerCase().replaceAll("\\s+", "");
                tvEmail.setText(emailID);
            } else if (requestCode == REQ_CODE_SUBJECT) {
                subjectBody = result.get(0);
                tvSubject.setText(subjectBody);
            } else if (requestCode == REQ_CODE_MESSAGE) {
                messageBody = result.get(0);
                tvMessage.setText(messageBody);
            }
        }
    }

    private void sendEmail() {
        if (emailID != null && messageBody != null) {
            Intent emailIntent = new Intent(Intent.ACTION_SEND);
            emailIntent.setData(Uri.parse("mailto:"));
//            emailIntent.setType("text/plain");
            emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{emailID});
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, subjectBody);
            emailIntent.putExtra(Intent.EXTRA_TEXT, messageBody);

            try {
                startActivity(Intent.createChooser(emailIntent, "Send email..."));
            } catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(MainActivity.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Please enter both email ID and message.", Toast.LENGTH_SHORT).show();
        }
    }
}
// Version 1.0.0.1