package com.example.disertatie;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class PaymentActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);
        String BTCAddress = getIntent().getStringExtra("address");
        TextView textAddress = findViewById(R.id.textViewAddress);
        textAddress.setText(BTCAddress);
        Button clipboardCopyButton = findViewById(R.id.buttonClipboard);
        Button goBack = findViewById(R.id.buttonPayment);
        goBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PaymentActivity.this,MainActivity.class);
                startActivity(intent);
            }
        });
        clipboardCopyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("BTCAddress", textAddress.getText());
                clipboard.setPrimaryClip(clip);
                Toast toast = Toast.makeText(getApplicationContext(),"Copied!",Toast.LENGTH_SHORT);
                toast.show();
            }
        });
    }
}