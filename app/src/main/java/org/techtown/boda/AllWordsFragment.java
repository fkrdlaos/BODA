package org.techtown.boda;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class AllWordsFragment extends Fragment {

    private RecyclerView recyclerView;
    private WordDataAdapter adapter;
    private List<WordData> wordDataList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_allwords, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        // 간격 추가
        int verticalSpacing = getResources().getDimensionPixelSize(R.dimen.vertical_item_margin);
        int horizontalSpacing = getResources().getDimensionPixelSize(R.dimen.horizontal_item_margin);
        recyclerView.addItemDecoration(new SpaceItemDecoration(verticalSpacing, horizontalSpacing));

        // 어댑터 초기화
        adapter = new WordDataAdapter(wordDataList, position -> {
            WordData clickedWord = wordDataList.get(position);
            Intent intent = new Intent(getActivity(), DetailActivity.class);
            intent.putExtra("wordData", clickedWord);
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        return view;
    }

    // 데이터 업데이트 메서드
    public void updateData(List<WordData> newData) {
        wordDataList.clear();
        wordDataList.addAll(newData);
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    // 검색 필터링 메서드
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

    // 아이템 간격을 추가하는 클래스
    public class SpaceItemDecoration extends RecyclerView.ItemDecoration {
        private final int verticalSpaceHeight;
        private final int horizontalSpaceWidth;

        public SpaceItemDecoration(int verticalSpaceHeight, int horizontalSpaceWidth) {
            this.verticalSpaceHeight = verticalSpaceHeight;
            this.horizontalSpaceWidth = horizontalSpaceWidth;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            outRect.bottom = verticalSpaceHeight; // 아래쪽 간격
            outRect.left = horizontalSpaceWidth;  // 왼쪽 간격
            outRect.right = horizontalSpaceWidth; // 오른쪽 간격
        }
    }
}