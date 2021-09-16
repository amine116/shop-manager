package com.amine.bmanager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class ActivityLogIn extends AppCompatActivity implements View.OnClickListener {

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);
        initialize();
    }

    private void initialize(){
        auth = FirebaseAuth.getInstance();
        findViewById(R.id.login_btnLogin).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if(id == R.id.login_btnLogin){
            EditText edtEmail = findViewById(R.id.login_edtEmail),
                    edtPass = findViewById(R.id.login_edtPassword);
            String email = edtEmail.getText().toString().trim(),
                    password = edtPass.getText().toString().trim();

            if(email.isEmpty()){
                edtEmail.setError("Email Needed");
                edtEmail.requestFocus();
                return;
            }

            if(password.isEmpty()){
                edtPass.setError("Password Needed");
                edtPass.requestFocus();
                return;
            }

            if(password.length() < 6){
                edtPass.setError("Password length must greater than 6");
                edtPass.requestFocus();
                return;
            }

            findViewById(R.id.login_progress).setVisibility(View.VISIBLE);
            findViewById(R.id.login_emailLayout).setVisibility(View.GONE);
            findViewById(R.id.login_passwordLayout).setVisibility(View.GONE);
            findViewById(R.id.login_btnLogin).setEnabled(false);
            logIn(email, password);


        }
    }

    private void logIn(String email, String password){
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        Toast.makeText(ActivityLogIn.this, "Logged In successfully",
                                Toast.LENGTH_LONG).show();

                        Intent intent = new Intent(ActivityLogIn.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);

                        findViewById(R.id.login_progress).setVisibility(View.GONE);

                    }
                    else{
                        if(task.getException() != null){
                            Toast.makeText(ActivityLogIn.this, task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                        findViewById(R.id.login_progress).setVisibility(View.GONE);
                        findViewById(R.id.login_emailLayout).setVisibility(View.VISIBLE);
                        findViewById(R.id.login_passwordLayout).setVisibility(View.VISIBLE);
                        findViewById(R.id.login_btnLogin).setEnabled(true);
                    }
                });
    }
}