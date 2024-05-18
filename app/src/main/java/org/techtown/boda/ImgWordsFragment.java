package org.techtown.boda;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ImgWordsFragment extends Fragment {

    private RecyclerView recyclerView;
    private ImgWordDataAdapter adapter;
    private List<WordData> wordDataList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_imgwords, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2)); // 2행 2열 그리드 레이아웃 사용

        // 어댑터 초기화
        adapter = new ImgWordDataAdapter(getContext(), wordDataList, new ImgWordDataAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                WordData clickedWord = wordDataList.get(position);
                Intent intent = new Intent(getActivity(), DetailActivity.class);
                intent.putExtra("wordData", clickedWord);
                startActivity(intent);
            }
        });
        recyclerView.setAdapter(adapter);

        return view;
    }

    // 데이터 업데이트 메서드 수정
    public void updateData(List<WordData> newData) {
        wordDataList.clear();
        wordDataList.addAll(newData);
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    // 검색 필터링 메서드 수정
    public void filter(String text) {
        List<WordData> filteredList = new ArrayList<>();
        for (WordData wordData : wordDataList) {
            if (wordData.getWord().toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(wordData);
            }
        }
        if (adapter != null) {
            adapter.setData(filteredList);
            adapter.notifyDataSetChanged();
        }
    }
}
