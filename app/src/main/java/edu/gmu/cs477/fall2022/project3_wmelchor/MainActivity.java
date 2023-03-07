package edu.gmu.cs477.fall2022.project3_wmelchor;


import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference myRef = database.getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
    }

    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        //LoadMain();
    }

    public void login(View view) {
        EditText etEmail = (EditText) findViewById(R.id.etEmail);
        String email = etEmail.getText().toString();
        EditText etPassword = findViewById(R.id.etPassword);
        String password = etPassword.getText().toString();
        loginToFirebase(email, password);
    }

    public void loginToFirebase(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                Toast.makeText(getApplicationContext(),"Successful Login",Toast.LENGTH_SHORT).show();
                LoadMain();
            } else {
                //Toast.makeText(MainActivity.this, "Authentication failed", Toast.LENGTH_SHORT).show();
                //updateUI(null);
            }
        });
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = mAuth.getCurrentUser();
                if(user != null) {
                    myRef.child("Users").child(splitString(user.getEmail())).child("Request").setValue(user.getUid());
                }
                Toast.makeText(getApplicationContext(),"Successfully created account for " + splitString(user.getEmail()),Toast.LENGTH_SHORT).show();
                LoadMain();
            } else {
                //Toast.makeText(MainActivity.this, "Account creation failed", Toast.LENGTH_SHORT).show();
                //updateUI(null);
            }
        });
    }

    public void LoadMain() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Intent intent = new Intent(this, Request.class);
            intent.putExtra("Email", currentUser.getEmail());
            intent.putExtra("Uid", currentUser.getUid());
            //startActivity(intent);
            someActivityResultLauncher.launch(intent);
        }
    }

    public void playSolo(View view) {
        Intent intent = new Intent(this, TicTacToe.class);
        intent.putExtra("Email", "");
        intent.putExtra("Request Email", "");
        intent.putExtra("Request", false);
        intent.putExtra("Accept", false);
        someActivityResultLauncher.launch(intent);
    }

    public String splitString(String str) {
        String [] split = str.split("@");
        return split[0];
    }
    ActivityResultLauncher<Intent>
            someActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result)
                {
                    if (result.getResultCode() ==
                            Activity.RESULT_OK) {
                        Intent data = result.getData();
                    }
                }
            });
}