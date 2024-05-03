package org.techtown.boda;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class WordDataAdapter extends RecyclerView.Adapter<WordDataAdapter.ViewHolder>{
    private List<WordData> wordDataList;

    public WordDataAdapter(List<WordData> wordDataList) {
        this.wordDataList = wordDataList;
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
}