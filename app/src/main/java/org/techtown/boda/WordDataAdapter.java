package org.techtown.boda;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class WordDataAdapter extends RecyclerView.Adapter<WordDataAdapter.ViewHolder>{
    private List<WordData> wordDataList;
    private OnItemClickListener listener;

    public WordDataAdapter(List<WordData> wordDataList, OnItemClickListener listener) {
        this.listener = listener;
        this.wordDataList = wordDataList;
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WordData wordData = wordDataList.get(position);
        holder.wordTextView.setText(wordData.getWord());
        holder.meaningTextView.setText(wordData.getMeaning());

        // 아이템 클릭 이벤트 처리
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int clickedPosition = holder.getAdapterPosition(); // 클릭된 아이템의 위치를 가져옴
                if (listener != null && clickedPosition != RecyclerView.NO_POSITION) { // 클릭된 위치가 유효한지 확인
                    listener.onItemClick(clickedPosition);
                }
                Intent intent = new Intent(v.getContext(), DetailActivity.class);
                intent.putExtra("wordData", wordData);
                v.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return wordDataList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView wordTextView;
        TextView meaningTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            wordTextView = itemView.findViewById(R.id.item_spelling);
            meaningTextView = itemView.findViewById(R.id.item_meaning);
        }
    }

    // WordData 목록을 반환하는 메서드 추가
    public List<WordData> getWordDataList() {
        return wordDataList;
    }
}
