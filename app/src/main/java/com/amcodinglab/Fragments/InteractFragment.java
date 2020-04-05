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

import com.amcodinglab.Adapter.ProblemAdapter;
import com.amcodinglab.Model.Programs;
import com.amcodinglab.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class InteractFragment extends Fragment implements ProblemAdapter.SearchAdapterListener {

    private RecyclerView Program_List;
    private SearchView Search_View;
    private ProblemAdapter programAdapter;
    private List<Programs> programList;
    private DatabaseReference mProgramsDatabase;


    public InteractFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_programs, container, false);
        Program_List = view.findViewById(R.id.program_list);
        Search_View = view.findViewById(R.id.search);
        Program_List.setHasFixedSize(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        mLayoutManager.setReverseLayout(true);
        mLayoutManager.setStackFromEnd(true);
        Program_List.setLayoutManager(mLayoutManager);
        programList = new ArrayList<>();
        programAdapter = new ProblemAdapter(getContext(), programList, this);
        Program_List.setAdapter(programAdapter);

        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        Search_View.setSearchableInfo(searchManager
                .getSearchableInfo(getActivity().getComponentName()));
        Search_View.setMaxWidth(Integer.MAX_VALUE);


        mProgramsDatabase = FirebaseDatabase.getInstance().getReference().child("Programs");
        mProgramsDatabase.keepSynced(true);

        Search_View.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                programAdapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                programAdapter.getFilter().filter(query);
                return false;
            }
        });

        readPrograms();

        return view;
    }

    private void readPrograms() {

        mProgramsDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                programList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Programs program = snapshot.getValue(Programs.class);
                    programList.add(program);
                }
                programAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onSearchSelected(Programs programs) {

    }
}
