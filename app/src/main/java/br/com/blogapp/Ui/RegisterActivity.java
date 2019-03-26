package br.com.blogapp.Ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import br.com.blogapp.R;

public class RegisterActivity extends AppCompatActivity {

    private ImageView imageView;
    static int PReqCode = 1;
    static int REQUESTCODE = 1;
    private EditText userName, userEmail, userPassword, userPassword2;
    private Uri pickedUri;
    private ProgressBar progressBar;
    private Button button;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        getFields();
        auth = FirebaseAuth.getInstance();

        progressBar.setVisibility(View.INVISIBLE);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= 22) {
                    checkAddRequestForPermission();
                } else {
                    openGallery();
                }
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                button.setVisibility(View.INVISIBLE);
                progressBar.setVisibility(View.VISIBLE);
                validatedFields();
            }
        });

    }

    private void validatedFields() {
        final String email = userEmail.getText().toString();
        final String name = userName.getText().toString();
        final String password = userPassword.getText().toString();
        final String password2 = userPassword2.getText().toString();

        if (email.isEmpty() || name.isEmpty() || password.isEmpty() || password2.isEmpty()){
            showMessage("Please is required all fields.");
            button.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.INVISIBLE);
        } else {
            createUserAccount(email, name, password);
        }
    }

    private void createUserAccount(String email, final String name, String password) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isComplete()){
                            showMessage("Account created");
                            updateuserInfo(name, pickedUri, auth.getCurrentUser());
                        }else{
                            showMessage("Account creation failed: " + task.getException().getMessage());
                            button.setVisibility(View.VISIBLE);
                            progressBar.setVisibility(View.INVISIBLE);
                        }
                    }
                });
    }

    private void showMessage(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }

    private void getFields() {
        userName = findViewById(R.id.regName);
        userEmail = findViewById(R.id.regMail);
        userPassword = findViewById(R.id.regPassword);
        userPassword2 = findViewById(R.id.regConfirmPassword);
        progressBar = findViewById(R.id.regProcess);
        imageView = findViewById(R.id.imgProfile);
        button = findViewById(R.id.regRegister);
    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, REQUESTCODE);
    }

    private void checkAddRequestForPermission() {

        if (ContextCompat.checkSelfPermission(RegisterActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(RegisterActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Toast.makeText(RegisterActivity.this, "Please Accept for required permission", Toast.LENGTH_SHORT).show();
            } else {
                ActivityCompat.requestPermissions(RegisterActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PReqCode);
            }
        } else {
            openGallery();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUESTCODE && data != null) {
            pickedUri = data.getData();
            imageView.setImageURI(pickedUri);
        }
    }

    private void updateuserInfo(final String name, final Uri pickedUri, final FirebaseUser user) {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("users_photos");
        final StorageReference imagePath = storageReference.child(pickedUri.getLastPathSegment());
        imagePath.putFile(pickedUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                imagePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest
                                .Builder()
                                .setDisplayName(name)
                                .setPhotoUri(uri)
                                .build();

                        user.updateProfile(profileChangeRequest).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    showMessage("Register Complete");
                                    updateUi();
                                }else{
                                    showMessage("Register account failed");
                                    button.setVisibility(View.VISIBLE);
                                    progressBar.setVisibility(View.INVISIBLE);
                                }
                            }
                        });
                    }
                });
            }
        });
    }

    private void updateUi() {
        Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
        startActivity(intent);
        finish();
    }
}
