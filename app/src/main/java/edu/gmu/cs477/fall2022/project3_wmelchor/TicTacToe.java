package edu.gmu.cs477.fall2022.project3_wmelchor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.database.*;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TicTacToe extends AppCompatActivity {

    private FirebaseAnalytics mFirebaseAnalytics;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference myRef = database.getReference();
    private Button[][]buttons;
    private TicTacToeLogic tttGame;
    private TextView status;
    private EditText request;
    String email;
    String requestEmail;
    String sessionId;
    boolean multiplayer = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_tic);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        tttGame = new TicTacToeLogic();
        Bundle b = getIntent().getExtras();
        email = b.getString("Email");
        requestEmail = b.getString("Request Email");
        boolean request = b.getBoolean("Request");
        boolean accept = b.getBoolean("Accept");
        IncomingCalls();
        buildGuiByCode();
        if(request) {
            multiplayer = true;
            playOnline(splitString(email) + splitString(requestEmail));
            //Toast.makeText(getApplicationContext(), "Buttons enabled by default", Toast.LENGTH_SHORT).show();
        }
        if(accept) {
            multiplayer = true;
            playOnline(splitString(requestEmail) + splitString(email));
            enableButtons(true);
            //Toast.makeText(getApplicationContext(),"Buttons disabled",Toast.LENGTH_SHORT).show();
        }
    }


    public void IncomingCalls()
    {
        myRef.child("Users").child(splitString(email)).child("Request")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        try {
                            HashMap<String, ?> td = (HashMap<String, ?>) snapshot.getValue();
                            if (td != null) {
                                String value;
                                for(String key : td.keySet()) {
                                    value = (String) td.get(key);
                                    //EditText etEmail = findViewById(R.id.etEmail);
                                    //etEmail.setText(value);
                                    myRef.child("Users").child(splitString(email)).child("Request").setValue(true);
                                    break;
                                }
                            }
                        } catch (Exception e) {

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    public void playOnline(String sessionId) {
        this.sessionId = sessionId;
        myRef.child("playOnline").removeValue();
        myRef.child("playOnline").child(sessionId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        /* For some reason, when clicking the button[0][0] in the game, the value sent
                        *  to the database is returned as a list, so here is the solution to that weird
                        *  issue. */
                        if(snapshot.getValue() instanceof List) {
                            ArrayList<String> list = (ArrayList<String>) snapshot.getValue();
                            myRef.child("playOnline").child(sessionId).removeValue();
                            enableButtons(list.get(1) != email);
                            autoPlayOnline(Integer.parseInt("1"));
                            return;
                        }
                        HashMap<String, Object> td = (HashMap<String, Object>) snapshot.getValue();
                        if (td != null) {
                            String value;
                            for (String key : td.keySet()) {
                                myRef.child("playOnline").child(sessionId).removeValue();
                                value = (String) td.get(key);
                                if (value != email) {
                                    //Toast.makeText(getApplicationContext(),"Buttons now enabled" ,Toast.LENGTH_SHORT).show();
                                    enableButtons(true);
                                } else {
                                    //Toast.makeText(getApplicationContext(),"Buttons now disabled" ,Toast.LENGTH_SHORT).show();
                                    enableButtons(false);
                                }
                                //Toast.makeText(getApplicationContext(),"Square " + key + " was selected",Toast.LENGTH_SHORT).show();
                                autoPlayOnline(Integer.parseInt(key));
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    public void autoPlayOnline(int square) {
        int row;
        int column;
        if(square == 1) {
            row = 0;
            column = 0;
        }
        else if(square == 2) {
            row = 0;
            column = 1;
        }
        else if(square == 3) {
            row = 0;
            column = 2;
        }
        else if(square == 4) {
            row = 1;
            column = 0;
        }
        else if(square == 5) {
            row = 1;
            column = 1;
        }
        else if(square == 6) {
            row = 1;
            column = 2;
        }
        else if(square == 7) {
            row = 2;
            column = 0;
        }
        else if(square == 8) {
            row = 2;
            column = 1;
        }
        else if(square == 9) {
            row = 2;
            column = 2;
        } else {
            row = -1;
            column = -1;
        }
        //Toast.makeText(getApplicationContext(),"Attempting to edit row " + (row+1) + " column " + (column+1),Toast.LENGTH_SHORT).show();
        if(row != -1) {
            update(row, column);
        }
    }

    public void buildGuiByCode() {
        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);
        int w = size.x / TicTacToeLogic.SIDE;
        GridLayout gridLayout = new GridLayout(this);
        gridLayout.setColumnCount(TicTacToeLogic.SIDE);
        gridLayout.setRowCount(TicTacToeLogic.SIDE + 1);  // space for a message
        buttons = new Button[TicTacToeLogic.SIDE][TicTacToeLogic.SIDE];
        ButtonHandler bh = new ButtonHandler();
        for (int i=0;i<TicTacToeLogic.SIDE;i++)
            for (int j=0;j<TicTacToeLogic.SIDE;j++) {
                buttons[i][j] = new Button(this);
                buttons[i][j].setTextSize((float) ((int)w*.2));
                buttons[i][j].setOnClickListener(bh);
                gridLayout.addView(buttons[i][j], w, w);
            }
        status = new TextView(this);
        GridLayout.Spec rowSpec = GridLayout.spec(TicTacToeLogic.SIDE,1);
        GridLayout.Spec columnSpec = GridLayout.spec(0,TicTacToeLogic.SIDE);
        GridLayout.LayoutParams lpStatus =
                new GridLayout.LayoutParams(rowSpec,columnSpec);
        status.setLayoutParams(lpStatus);
        status.setWidth(TicTacToeLogic.SIDE*w);
        status.setHeight(w-1);
        status.setGravity(Gravity.CENTER);
        status.setBackgroundColor(Color.GREEN);
        status.setTextSize((int)(w*.15));
        status.setText(tttGame.result());
        gridLayout.addView(status);

        setContentView(gridLayout);

    }

    public void update(int row, int col) {
        int play = tttGame.play(row,col);
        if (play == 1) buttons[row][col].setText("X");
        else if (play == 2) buttons[row][col].setText("O");
        if (tttGame.isGameOver() ) {
            enableButtons(false);
            status.setBackgroundColor(Color.RED);
            status.setText(tttGame.result());
            showNewGameDialog();
        }
    }
    public void enableButtons(boolean enabled) {
        for (int row=0;row<TicTacToeLogic.SIDE;row++)
            for (int col=0;col<TicTacToeLogic.SIDE;col++)
                buttons[row][col].setEnabled(enabled);
    }
    public void resetButtons() {
        for (int row=0;row<TicTacToeLogic.SIDE;row++)
            for (int col=0;col<TicTacToeLogic.SIDE;col++)
                buttons[row][col].setText("");
    }
    private class ButtonHandler implements View.OnClickListener {
        public void onClick(View v) {
            for (int row = 0;row<TicTacToeLogic.SIDE;row++) {
                for (int column=0;column<TicTacToeLogic.SIDE;column++) {
                    if (v == buttons[row][column]) {
                        int square = 0;
                        if (row == 0 && column == 0) {
                            square = 1;
                        }
                        if (row == 0 && column == 1) {
                            square = 2;
                        }
                        if (row == 0 && column == 2) {
                            square = 3;
                        }
                        if (row == 1 && column == 0) {
                            square = 4;
                        }
                        if (row == 1 && column == 1) {
                            square = 5;
                        }
                        if (row == 1 && column == 2) {
                            square = 6;
                        }
                        if (row == 2 && column == 0) {
                            square = 7;
                        }
                        if (row == 2 && column == 1) {
                            square = 8;
                        }
                        if (row == 2 && column == 2) {
                            square = 9;
                        }
                        if (!multiplayer) {
                            update(row, column);
                        } else {
                            myRef.child("playOnline").child(sessionId).child(String.valueOf(square)).setValue(email);
                            update(row, column);
                            //Toast.makeText(getApplicationContext(), "Sending " + square + " to " + email, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        }
    }

    public void showNewGameDialog() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("This is fun");
        alert.setMessage("Play again?");
        PlayDialog playAgain = new PlayDialog();
        alert.setPositiveButton("Yes", playAgain);
        alert.setNegativeButton("No",playAgain);
        alert.show();

    }
    private class PlayDialog implements DialogInterface.OnClickListener {
        public void onClick(DialogInterface dialog, int id) {
            if (id == -1) {  // yes
                tttGame.resetGame();
                enableButtons(true);
                resetButtons();
                status.setBackgroundColor(Color.GREEN);
                status.setText(tttGame.result());
            } else if (id == -2)  // no
                TicTacToe.this.finish();
        }
    }
    public String splitString(String str) {
        String [] split = str.split("@");
        return split[0];
    }
}