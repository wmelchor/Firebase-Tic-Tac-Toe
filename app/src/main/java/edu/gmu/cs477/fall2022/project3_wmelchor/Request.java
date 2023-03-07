package edu.gmu.cs477.fall2022.project3_wmelchor;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Request extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference myRef = database.getReference();

    String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request);

        Bundle b = getIntent().getExtras();
        email = b.getString("Email");
    }

    public void request(View view) {
        EditText req = (EditText) findViewById(R.id.request);
        String requestEmail = req.getText().toString();
        Intent intent = new Intent(this, TicTacToe.class);
        intent.putExtra("Email", email);
        intent.putExtra("Request Email", requestEmail);
        intent.putExtra("Request", true);
        intent.putExtra("Accept", false);
        myRef.child("Users").child(splitString(requestEmail)).child("Request").push().setValue(email);
        someActivityResultLauncher.launch(intent);
    }

    public void accept(View view) {
        EditText req = (EditText) findViewById(R.id.request);
        String requestEmail = req.getText().toString();
        Intent intent = new Intent(this, TicTacToe.class);
        intent.putExtra("Email", email);
        intent.putExtra("Request Email", requestEmail);
        intent.putExtra("Request", false);
        intent.putExtra("Accept", true);
        myRef.child("Users").child(splitString(requestEmail)).child("Request").push().setValue(email);
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