package org.techtown.boda;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class RTDatabase {
    private static RTDatabase instance = null;
    private static FirebaseDatabase database;
    private static DatabaseReference dbRef;
    private static Map<String,Object> wordMap;


    // 새 유저 추가 시
    private RTDatabase() {
    }


    //해당 사용자의 idToken으로 접근
    //각 사용자마다 collection 존재
    //해당 collection에 단어-뜻 쌍 여러개 존재
    //생성자대신 getInstance로 객체 받기
    public static RTDatabase getInstance(String idToken) {
        if(instance == null) {
            instance = new RTDatabase();
            database = FirebaseDatabase.getInstance("https://boda-4e223-default-rtdb.firebaseio.com");
            dbRef = database.getReference().child("/BODA/UserAccount/"+idToken);
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

                }
            });
        }
        return instance;
    }
    //db에 key-value / 단어-뜻 형태로 각 UserAccount에 collection에 저장
    public void addWords(HashMap wordMap){
        dbRef.child("collection").updateChildren(wordMap);
    }

    // 각 유저의 콜렉션에서 모든 단어-뜻 추출
    public Map<String,Object> getWords(){
        return wordMap;
    }
}
