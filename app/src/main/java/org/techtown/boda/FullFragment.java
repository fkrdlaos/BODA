package org.techtown.boda;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FullFragment extends Fragment {
    private RecyclerView recyclerView;
    private WordDataAdapter adapter;
    private List<WordData> wordDataList;
    private DatabaseReference databaseRef;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d("FragmentLoad", "Loading FullFragment");
        View view = inflater.inflate(R.layout.fragment_full_words, container, false);
        recyclerView = view.findViewById(R.id.full_recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        wordDataList = new ArrayList<>();
        adapter = new WordDataAdapter(wordDataList, position -> {
            WordData selectedWord = wordDataList.get(position);
            Intent intent = new Intent(getContext(), DetailActivity.class);
            intent.putExtra("wordData", selectedWord);
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        loadWordData();
        return view;
    }

    private void loadWordData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            databaseRef = FirebaseDatabase.getInstance().getReference()
                    .child("BODA").child("UserAccount").child(userId).child("collection");
            databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    wordDataList.clear(); // Clear existing data
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String word = snapshot.child("word").getValue(String.class);
                        String meaning = snapshot.child("meaning").getValue(String.class);
                        String example = snapshot.child("example").getValue(String.class);
                        String dateTime = snapshot.child("dateTime").getValue(String.class);

                        if (word != null && meaning != null && example != null && dateTime != null) {
                            wordDataList.add(new WordData(word, meaning, example, dateTime));
                        }
                    }
                    adapter.notifyDataSetChanged(); // Notify adapter of data change
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(getContext(), "Failed to load data: " + databaseError.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
    }
}
