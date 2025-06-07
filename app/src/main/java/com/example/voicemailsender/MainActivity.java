package com.example.voicemailsender;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.widget.Button;
import android.widget.Toast;
import android.widget.TextView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;
import androidx.appcompat.app.ActionBarDrawerToggle;


import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {


    private Button btnSpeak, btnSendEmail;
    private TextView tvResult;


    private int step = 0;
    private String email = "", subject = "", message = "";
    private TextToSpeech tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //slide Bar
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawerLayout = findViewById(R.id.drawerLayout);
        NavigationView navigationView = findViewById(R.id.navigationView);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new HomeFragment())
                .commit();


        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                drawerLayout.closeDrawer(GravityCompat.START);
                // Handle the Home option

            } else if (id == R.id.nav_team) {
                Intent intent = new Intent(this, AboutTeamActivity.class);
                startActivity(intent);
                // Handle the About Team option
            } else if (id == R.id.nav_app) {
                Intent intent = new Intent(this, AboutAppActivity.class);
                startActivity(intent);
                // Handle the About App option

            }

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        //slide Bar over



        btnSpeak = findViewById(R.id.btnSpeak);
        btnSendEmail = findViewById(R.id.btnSendEmail);
        tvResult = findViewById(R.id.tvResult);


        // Initialize TextToSpeech
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(Locale.US);
            }
        });

        btnSpeak.setOnClickListener(view -> {
            step = 0;
            promptAndSpeak();  // Speak and then listen
        });

        btnSendEmail.setOnClickListener(view -> {
            if (!email.isEmpty() && !subject.isEmpty() && !message.isEmpty()) {
                sendEmail();
            } else {
                Toast.makeText(this, "Please complete voice input first.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void promptAndSpeak() {
        String prompt;
        switch (step) {
            case 0:
                prompt = "To whom do you want to send email?";
                break;
            case 1:
                prompt = "What is the subject of the email?";
                break;
            case 2:
                prompt = "What message do you want to send?";
                break;
            default:
                prompt = "";
        }

        tts.speak(prompt, TextToSpeech.QUEUE_FLUSH, null, null);

        new Handler().postDelayed(this::startVoiceInput, 2000); // Delay before listening
    }

    private void startVoiceInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                step == 0 ? "Speak Email ID" :
                        step == 1 ? "Speak Subject" :
                                "Speak Message");

        try {
            startActivityForResult(intent, 100);
        } catch (Exception e) {
            Toast.makeText(this, "Speech recognition not supported.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (result != null && !result.isEmpty()) {
                switch (step) {
                    case 0:
                        String spokenText = result.get(0);
//                        tvResult.setText(spokenText); // Display what the user said
                        email = result.get(0)
                                .toLowerCase()
                                .replaceAll("\\s+", "") // removes all spaces (single or multiple)
                                .replace("at", "@")
                                .replace("dot", ".");

                        Toast.makeText(this, "Email: " + email, Toast.LENGTH_SHORT).show();
                        step++;
                        promptAndSpeak();
                        break;
                    case 1:
                        String spokenSubject = result.get(0);
//                        tvResult.setText(spokenSubject); // Display spoken subject

                        subject = result.get(0);

                        Toast.makeText(this, "Subject: " + subject, Toast.LENGTH_SHORT).show();
                        step++;
                        promptAndSpeak();
                        break;
                    case 2:
                        String spokenMessage = result.get(0);
//                        tvResult.setText(spokenMessage); // Display spoken message
                        message = result.get(0);
                        Toast.makeText(this, "Message: " + message, Toast.LENGTH_SHORT).show();
                        break;
                }
                tvResult.setText(String.format("Email: %s\n\nSubject: %s\n\nMessage: \n%s", email, subject, message));
            }
        }
    }

    private void sendEmail() {
        if (email != null && message != null) {
            Intent emailIntent = new Intent(Intent.ACTION_SEND);
            emailIntent.setData(Uri.parse("mailto:"));
            emailIntent.setType("text/plain");
            emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{email});
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
            emailIntent.putExtra(Intent.EXTRA_TEXT, message);
            emailIntent.setPackage("com.google.android.gm");
//            emailIntent.setPackage("com.microsoft.office.outlook");

            try {
                startActivity(Intent.createChooser(emailIntent, "Send email..."));
            } catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(MainActivity.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Please enter both email ID and message.", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
}


// Version 2.0