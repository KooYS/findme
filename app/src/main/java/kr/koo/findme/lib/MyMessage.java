package kr.koo.findme.lib;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MyMessage {

    FirebaseDatabase database;
    DatabaseReference myRef;

    public MyMessage(String path){
        database= FirebaseDatabase.getInstance();
        myRef =  database.getReference(path);

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String value = dataSnapshot.getValue(String.class);
                Log.d("message", "Value is: " + value);
            }
            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("message", "Failed to read value.", error.toException());
            }
        });

    }
    public void setMessage(String text){
        myRef.setValue(text);
    }






}
