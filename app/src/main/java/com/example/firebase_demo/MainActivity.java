package com.example.firebase_demo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private boolean logon=false;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ListView list = (ListView) findViewById(R.id.list);
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,android.R.id.text1);
        list.setAdapter(adapter);

        auth = FirebaseAuth.getInstance();

        //Start using FirebaseDB
        FirebaseDatabase db=FirebaseDatabase.getInstance();
        //getReference ,we call it "contacts" ,"contacts" is the string we determined in firebase website.
        DatabaseReference contacts = db.getReference("contacts");

//
//         {
//            "contacts" :
//             [ null,
//                {
//                 "name" : "Luke",
//                 "phone" : "0922918375"
//                },
//                {
//                 "name":"王小明",
//                 "phone":"0912345678"
//                }
//             ]
//         }


        //We need be noticed when data is changed, so adding a listener!
        contacts.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                //why my data is a "snapshot"? cause the data may be changed when reading.

                Log.d(TAG,"onChildAdded");
                String name= (String) dataSnapshot.child("name").getValue();
                adapter.add(name);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG,"onChildChanged");


            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.d(TAG,"onChildRemoved");

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG,"onChildMoved");

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG,"onCancelled");

            }
        });
        if(!logon) {
            startActivity(new Intent(this,LoginActivity.class));
        }

    }


    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user= auth.getCurrentUser();
        if(user!=null){
            Log.d(TAG,"user:" +user.getUid());
            Log.d(TAG,"email:"+user.getUid());

            //Add a user data if logon.
            FirebaseDatabase db = FirebaseDatabase.getInstance();
            //Get the root of your data.
            DatabaseReference dbr= db.getReference();
            //"child" can let you insert the data ,will create it if not exists.
            dbr.child(user.getUid()).child("nickname").setValue("Jack");
        }else{

        }
    }
}
