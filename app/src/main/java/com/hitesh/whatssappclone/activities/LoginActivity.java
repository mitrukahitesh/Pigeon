package com.hitesh.whatssappclone.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.hitesh.whatssappclone.R;

import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {

    private EditText number, code;
    private Button button;
    private TextView enterCode;
    private ProgressBar progressBar;
    private String verID;
    private String phoneNum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        number = findViewById(R.id.number);
        code = findViewById(R.id.code);
        progressBar = findViewById(R.id.progress);
        progressBar.setVisibility(View.GONE);
        enterCode = findViewById(R.id.enterCode);
        enterCode.setVisibility(View.GONE);
        code.setVisibility(View.GONE);
        button = findViewById(R.id.button);
        code.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(code.getText().toString().trim().length() == 6) {
                    progressBar.setVisibility(View.VISIBLE);
                    hideKeyboard();
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verID, code.getText().toString());
                    MainActivity.mAuth.signInWithCredential(credential).addOnCompleteListener(authResultOnCompleteListener);
                }
            }
        });
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startLoginProcess();
            }
        });
    }

    private void startLoginProcess() {
        if(number.getText() == null)
            return;
        if(number.getText().toString().equals(""))
            return;
        phoneNum = number.getText().toString().trim();
        hideKeyboard();
        progressBar.setVisibility(View.VISIBLE);
        PhoneAuthOptions authOptions = PhoneAuthOptions.newBuilder(MainActivity.mAuth)
                .setPhoneNumber(phoneNum)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setCallbacks(onVerificationStateChangedCallbacks)
                .setActivity(this)
                .build();
        PhoneAuthProvider.verifyPhoneNumber(authOptions);
    }

    private void hideKeyboard() {
        InputMethodManager manager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        manager.hideSoftInputFromWindow(code.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    private final PhoneAuthProvider.OnVerificationStateChangedCallbacks onVerificationStateChangedCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
            code.setVisibility(View.VISIBLE);
            enterCode.setVisibility(View.VISIBLE);
            button.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
            code.setText(phoneAuthCredential.getSmsCode());
            MainActivity.mAuth.signInWithCredential(phoneAuthCredential).addOnCompleteListener(authResultOnCompleteListener);
        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(LoginActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            verID = s;
            progressBar.setVisibility(View.GONE);
            code.setVisibility(View.VISIBLE);
            enterCode.setVisibility(View.VISIBLE);
            button.setVisibility(View.GONE);
            code.requestFocus();
        }
    };

    private final OnCompleteListener<AuthResult> authResultOnCompleteListener = new OnCompleteListener<AuthResult>() {
        @Override
        public void onComplete(@NonNull Task<AuthResult> task) {
            if(task.isSuccessful()) {
                progressBar.setVisibility(View.GONE);
                setDpAndName();
                LoginActivity.this.finish();
            }
            else {
                Toast.makeText(LoginActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            }
        }
    };

    private void setDpAndName() {
        Intent intent = new Intent(this, DpAndName.class);
        intent.putExtra(MainActivity.PHONE, phoneNum);
        startActivity(intent);
    }
}