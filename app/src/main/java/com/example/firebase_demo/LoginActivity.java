package com.example.firebase_demo;

import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
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

import static com.example.firebase_demo.R.id.passwd;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private EditText edUserid;
    private EditText edPasswd;
    FirebaseAuth auth;
    FirebaseAuth.AuthStateListener authStateListener= new FirebaseAuth.AuthStateListener() {
        @Override
        public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

            //Like Parse we used before.
            FirebaseUser firebaseUser= firebaseAuth.getCurrentUser();
            Log.d(TAG, "onAuthStateChanged:"+firebaseAuth.getCurrentUser().getUid());
            if(firebaseUser!=null){
                Toast.makeText(LoginActivity.this, "Login Success!!!", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(LoginActivity.this, "Logged out.", Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        findViews();
        auth= FirebaseAuth.getInstance();


    }

    @Override
    protected void onStart() {
        super.onStart();
        auth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        auth.removeAuthStateListener(authStateListener);
    }

    public void login(View v){
        //一行接著一行執行,即同步((synchronized)
        final String email=edUserid.getText().toString();
        final String password=edPasswd.getText().toString();

        //login process is asynchronized. so need a listener to get the result.
        auth.signInWithEmailAndPassword(email,password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        //Because "SignIn"  also fires onAuthStateChanged ,so here we do nothig about login result.

                        //Complete doesn't mean success.
                        if(!task.isSuccessful()){
                            new AlertDialog.Builder(LoginActivity.this)
                                    .setMessage("Failed!")
                                    .setPositiveButton("OK",null)
                                    .setNeutralButton("Sign-up", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            createUser(email,password);
                                        }
                                    })
                                    .show();
                            Log.d(TAG, "Task is Failed.");
                        }else{
                            Log.d(TAG, "Task is Succeeded");
                        }
                    }
                });


    }

    private void createUser(String email, String passwd) {

        auth.createUserWithEmailAndPassword(email,passwd).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                  new AlertDialog.Builder(LoginActivity.this)
                          .setMessage("Sign-up "+ (task.isSuccessful()? "Success!" : "Failed!"))
                          .setPositiveButton("OK",null)
                          .show();

            }
        });
    }

    private void findViews() {
        edUserid = (EditText) findViewById(R.id.userid);
        edPasswd = (EditText) findViewById(passwd);
    }
}
