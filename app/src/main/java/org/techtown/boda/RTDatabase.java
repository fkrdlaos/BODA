package org.techtown.boda;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class RTDatabase {
    private static Map<String, RTDatabase> instances = new HashMap<>();
    private FirebaseDatabase database;
    private static DatabaseReference dbRef;
    private Map<String,Object> wordMap;

    private RTDatabase(String userId) {
        database = FirebaseDatabase.getInstance("https://boda-4e223-default-rtdb.firebaseio.com");
        dbRef = database.getReference().child("/BODA/UserAccount/"+userId);
        wordMap = new HashMap<>();

        dbRef.child("collection").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    Map<String, Object> map = (Map<String, Object>)snapshot.getValue();
                    map.forEach((key,value)->{
                                wordMap.put(key, value);
                                System.out.println(key);
                            }
                    );
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle cancellation
            }
        });
    }

    public static RTDatabase getInstance(String userId) {
        if (!instances.containsKey(userId)) {
            instances.put(userId, new RTDatabase(userId));
        }
        return instances.get(userId);
    }

    public static DatabaseReference getUserDBRef(String userId) {
        RTDatabase instance = instances.get(userId);
        if (instance != null) {
            return instance.dbRef;
        }
        return null;
    }

    public void addWords(HashMap wordMap){
        dbRef.child("collection").updateChildren(wordMap);
    }

    public Map<String,Object> getWords(){
        return wordMap;
    }

    public static void addCatchImgPath(String word, String path){
        if(dbRef != null){
            dbRef.child("collection/"+word+"/path").setValue(path);

        }else{
            Log.i("NULLLLLLLL", "REference NULLLLLLL");

        }
    }
}