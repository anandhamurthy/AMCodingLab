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

import com.amcodinglab.Adapter.ContestsAdapter;
import com.amcodinglab.Adapter.ProblemAdapter;
import com.amcodinglab.Model.Contests;
import com.amcodinglab.Model.Programs;
import com.amcodinglab.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ContestsFragment extends Fragment implements ContestsAdapter.SearchAdapterListener{


    private RecyclerView Contexts_List;
    private SearchView Search_View;
    private ContestsAdapter contextsAdapter;
    private List<Contests> contextsList;
    private DatabaseReference mContextsDatabase;

    public ContestsFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_contests, container, false);
        Contexts_List = view.findViewById(R.id.contests_list);
        Search_View = view.findViewById(R.id.search);
        Contexts_List.setHasFixedSize(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        mLayoutManager.setReverseLayout(true);
        mLayoutManager.setStackFromEnd(true);
        Contexts_List.setLayoutManager(mLayoutManager);
        contextsList = new ArrayList<>();
        contextsAdapter = new ContestsAdapter(getContext(), contextsList, this);
        Contexts_List.setAdapter(contextsAdapter);

        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        Search_View.setSearchableInfo(searchManager
                .getSearchableInfo(getActivity().getComponentName()));
        Search_View.setMaxWidth(Integer.MAX_VALUE);

        mContextsDatabase = FirebaseDatabase.getInstance().getReference().child("Contests");
        mContextsDatabase.keepSynced(true);

        Search_View.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                contextsAdapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                contextsAdapter.getFilter().filter(query);
                return false;
            }
        });


        readEvents();

        return view;
    }

    private void readEvents() {

        mContextsDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                contextsList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Contests contexts = snapshot.getValue(Contests.class);
                    contextsList.add(contexts);
                }
                contextsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onSearchSelected(Contests contests) {

    }
}
