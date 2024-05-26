package org.techtown.boda;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.util.List;

public class ImgWordDataAdapter extends RecyclerView.Adapter<ImgWordDataAdapter.ViewHolder> {

    private List<WordData> wordDataList;
    private Context context;
    private OnItemClickListener listener;
    private FirebaseAuth mFirebaseAuth;
    private DatabaseReference mDatabaseRef;
    private DatabaseReference databaseReference;

    public ImgWordDataAdapter(Context context, List<WordData> wordDataList, OnItemClickListener listener) {
        this.context = context;
        this.wordDataList = wordDataList;
        this.listener = listener;
        mFirebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mFirebaseAuth.getCurrentUser();
        String userId = "";
        if (user != null) {
            userId = user.getUid();
        }
        mDatabaseRef = FirebaseDatabase.getInstance().getReference().child("BODA").child("UserAccount").child(userId).child("collection");

    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_picture_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WordData wordData = wordDataList.get(position);
        holder.wordTextView.setText(wordData.getWord());
        holder.dateTimeTextView.setText(wordData.getDateTime()); // dateTime으로 설정

        holder.imageView.setImageResource(R.drawable.img); // 기본 이미지 설정
        String word = wordData.getWord();
        mDatabaseRef.child(word).child("path").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String imagePath = snapshot.getValue(String.class);
                    if (imagePath != null) {
                        Uri imageUri = Uri.parse(imagePath);
                        File files = new File(imagePath);
                        Bitmap myBitmap = BitmapFactory.decodeFile(files.getAbsolutePath());
                        holder.imageView.setImageBitmap(myBitmap);
                    }
                } else {
                    // 기본 이미지 유지 또는 다른 처리
                    holder.imageView.setImageResource(R.drawable.img);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ImgWordDataAdapter", "Firebase error: " + error.getMessage());

            }

        });

        // 아이템 클릭 이벤트 처리
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int clickedPosition = holder.getAdapterPosition();
                if (listener != null && clickedPosition != RecyclerView.NO_POSITION) {
                    listener.onItemClick(clickedPosition);
                }
                Intent intent = new Intent(context, DetailActivity.class);
                intent.putExtra("wordData", wordData);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return wordDataList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView wordTextView;
        TextView dateTimeTextView;
        ImageView imageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            wordTextView = itemView.findViewById(R.id.item_spelling);
            dateTimeTextView = itemView.findViewById(R.id.item_date);
            imageView = itemView.findViewById(R.id.picture);
        }
    }

    // 데이터 설정 메서드 추가
    public void setData(List<WordData> newData) {
        this.wordDataList = newData;
        notifyDataSetChanged();
    }
}