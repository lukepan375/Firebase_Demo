package com.example.firebase_demo;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthCredential;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import static com.example.firebase_demo.R.id.passwd;

public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener{
    private static final String TAG = "LoginActivity";
    private static final int RC_SIGN_IN = 1;
    private EditText edUserid;
    private EditText edPasswd;

    //Google
    GoogleSignInOptions googleSignInOptions;
    GoogleApiClient googleApiClient;


    FirebaseAuth auth;
    FirebaseAuth.AuthStateListener authStateListener= new FirebaseAuth.AuthStateListener() {
        @Override
        public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
            Log.d(TAG, "onAuthStateChanged");
            //Like Parse we used before.
            FirebaseUser firebaseUser= firebaseAuth.getCurrentUser();
            if(firebaseUser!=null){
                Log.d(TAG, "onAuthStateChanged:"+firebaseAuth.getCurrentUser().getUid());

                Toast.makeText(LoginActivity.this, "Now in Login state.", Toast.LENGTH_SHORT).show();

                //Add a user data if logon.
                FirebaseDatabase db = FirebaseDatabase.getInstance();
                //Get the root of your data,and use this dbr to manipulate your Database..
                DatabaseReference dbr= db.getReference();
                //"child" can let you insert the data ,will create it if the key is not exists.
//                dbr.child("members").child(firebaseUser.getUid()).child("latest_use_time").setValue(new Date().getTime());

                //save data
                getSharedPreferences(getString(R.string.pref_name),MODE_PRIVATE)
                        .edit()
                        .putString(getString(R.string.pref_uid), firebaseUser.getUid())
                        .putString(getString(R.string.pref_email),firebaseUser.getEmail())
                        .apply();//write into sharedpreference asap.



                LoginActivity.this.finish();

            }else{
                Toast.makeText(LoginActivity.this, "Now in Logout state.", Toast.LENGTH_SHORT).show();

            }
        }
    };
    private CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
        setContentView(R.layout.activity_login);



        //fb
        LoginButton fbLogin= (LoginButton) findViewById(R.id.fbLogin);
        fbLogin.setReadPermissions("email","public_profile");

        callbackManager = CallbackManager.Factory.create();
        fbLogin.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG,"fb login success.");
                handleFacebookToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(TAG,"fb login onCancel.");

            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG,"fb login Error:"+error);

            }
        });



        //google,see googleLogin()

        findViews();
        auth= FirebaseAuth.getInstance();


    }

    private void handleFacebookToken(AccessToken accessToken) {
        //Firebase can handle the token we got from facebook!!!!
        //we use firebase's "Credential" to interact(such as login) with firebase api .
        //Firebasd offer "fb/google/twitter Authprovider" to create Credential(create by accesstoken).
        AuthCredential credential=FacebookAuthProvider.getCredential(accessToken.getToken());

        auth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Log.d(TAG,"fb login success.");
                }else{
                    Log.d(TAG,"fb login failed.");
                }
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        //fb
        callbackManager.onActivityResult(requestCode,resultCode,data);

        //google
        if(requestCode == RC_SIGN_IN){
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if(result.isSuccess()){
                // Google Sign In was successful, authenticate with Firebase

                GoogleSignInAccount googleSignInAccount=result.getSignInAccount();
                AuthCredential authCredential = GoogleAuthProvider.getCredential(googleSignInAccount.getIdToken(),null);

                auth.signInWithCredential(authCredential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Log.d(TAG,"google login success.");
                        }else{
                            Log.d(TAG,"google login failed.");
                        }
                    }
                });
            }


        }


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

    public void googleLogin(View v){
        //requestIdToken("Your project id in google,you can find it at firebase console")
        googleSignInOptions =new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken("728769707941-t9gbbj7gd9joodgcvjb85gq7i4oa68ml.apps.googleusercontent.com")
                .build();
        googleApiClient =new GoogleApiClient.Builder(this)
                .enableAutoManage(this,this)
                .addApi(Auth.GOOGLE_SIGN_IN_API,googleSignInOptions)
                .build();
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);


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

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG,"onConnectFailed");
    }
}
