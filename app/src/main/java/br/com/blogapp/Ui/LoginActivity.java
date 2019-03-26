package br.com.blogapp.Ui;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import br.com.blogapp.R;

public class LoginActivity extends AppCompatActivity {

    private EditText emailField, passwordField;
    private TextView createAccount;
    private Button button;
    private ProgressBar progressBar;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initializeFields();
        auth = FirebaseAuth.getInstance();

        createAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerUi();
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                button.setVisibility(View.INVISIBLE);
                progressBar.setVisibility(View.VISIBLE);
                validateFields();
            }
        });
    }

    private void validateFields() {
        final String email = emailField.getText().toString();
        final String password = passwordField.getText().toString();
        if (email.isEmpty() || password.isEmpty()) {
            showMessage("All Fields required");
            button.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.INVISIBLE);
        } else {
            doLogin(email, password);
        }
    }

    private void doLogin(String email, String password) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    updateUi();
                } else {
                    showMessage("E-mail or Password invalid");
                    button.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    private void updateUi() {
        Intent intent = new Intent(getApplicationContext(), Home.class);
        startActivity(intent);
        finish();
    }

    private void registerUi() {
        Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
        startActivity(intent);
    }

    private void showMessage(final String message) {
        Snackbar.make(findViewById(R.id.login), message, Snackbar.LENGTH_LONG).show();
    }

    private void initializeFields() {
        emailField = findViewById(R.id.loginEmail);
        passwordField = findViewById(R.id.loginPassword);
        button = findViewById(R.id.loginBtn);
        progressBar = findViewById(R.id.progressLogin);
        createAccount = findViewById(R.id.newAccount);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(auth.getCurrentUser() != null){
            updateUi();
        }
    }
}
