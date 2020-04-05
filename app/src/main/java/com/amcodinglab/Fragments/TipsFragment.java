package com.amcodinglab.Fragments;

import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;

import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.amcodinglab.Adapter.TipsAdapter;
import com.amcodinglab.Adapter.TipsAdapter.SearchAdapterListener;
import com.amcodinglab.Model.Tips;
import com.amcodinglab.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class TipsFragment extends Fragment implements SearchAdapterListener {

    private RecyclerView Tips_List;
    private TipsAdapter tipsAdapter;
    private List<Tips> tipsList;
    private SearchView Search_View;
    private DatabaseReference mTipsDatabase;

    public TipsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tips, container, false);
        Tips_List = view.findViewById(R.id.tips_list);
        Search_View = view.findViewById(R.id.search);
        Tips_List.setHasFixedSize(true);
        tipsList = new ArrayList<>();

        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        mLayoutManager.setReverseLayout(true);
        mLayoutManager.setStackFromEnd(true);
        Tips_List.setLayoutManager(mLayoutManager);
        tipsAdapter = new TipsAdapter(getContext(), tipsList, this);
        Tips_List.setAdapter(tipsAdapter);

        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        Search_View.setSearchableInfo(searchManager
                .getSearchableInfo(getActivity().getComponentName()));
        Search_View.setMaxWidth(Integer.MAX_VALUE);

        mTipsDatabase = FirebaseDatabase.getInstance().getReference().child("Tips");
        mTipsDatabase.keepSynced(true);

        Search_View.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                tipsAdapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                tipsAdapter.getFilter().filter(query);
                return false;
            }
        });

        readTips();

        return view;
    }

    private void readTips() {

        mTipsDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                tipsList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Tips tips = snapshot.getValue(Tips.class);
                    tipsList.add(tips);
                }
                tipsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onSearchSelected(Tips tips) {

    }
}
