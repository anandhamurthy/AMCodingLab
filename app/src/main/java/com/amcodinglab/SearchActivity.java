package com.amcodinglab;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RelativeLayout;

import com.amcodinglab.Adapter.ContestsAdapter;
import com.amcodinglab.Adapter.ProblemAdapter;
import com.amcodinglab.Adapter.TipsAdapter;
import com.amcodinglab.Model.Contests;
import com.amcodinglab.Model.Programs;
import com.amcodinglab.Model.Tips;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import static java.security.AccessController.getContext;

public class SearchActivity extends AppCompatActivity implements ProblemAdapter.SearchAdapterListener, TipsAdapter.SearchAdapterListener, ContestsAdapter.SearchAdapterListener {

    private RecyclerView Search_List;
    private SearchView Search_View;

    private ProblemAdapter programAdapter;
    private TipsAdapter tipsAdapter;
    private ContestsAdapter contestsAdapter;

    private List<Programs> programList;
    private List<Tips> tipsList;
    private List<Contests> contestsList;

    private RelativeLayout mNoActivity;

    private String key;

    private DatabaseReference mContextsDatabase,mProgramsDatabase,mTipsDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        Intent intent = getIntent();
        key = intent.getStringExtra("id");

        Search_List = findViewById(R.id.search_list);
        Search_View = findViewById(R.id.search);
        mNoActivity=findViewById(R.id.no_activity);

        Search_List.setHasFixedSize(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(SearchActivity.this);
        Search_List.setLayoutManager(mLayoutManager);

        programList = new ArrayList<>();
        tipsList = new ArrayList<>();
        contestsList = new ArrayList<>();

        programAdapter = new ProblemAdapter(this, programList, this);
        tipsAdapter = new TipsAdapter(this, tipsList, this);
        contestsAdapter = new ContestsAdapter(this, contestsList, this);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        Search_View.setSearchableInfo(searchManager
                .getSearchableInfo(getComponentName()));
        Search_View.setMaxWidth(Integer.MAX_VALUE);

        mContextsDatabase = FirebaseDatabase.getInstance().getReference().child("Contests");
        mContextsDatabase.keepSynced(true);

        mProgramsDatabase = FirebaseDatabase.getInstance().getReference().child("Programs");
        mProgramsDatabase.keepSynced(true);

        mTipsDatabase = FirebaseDatabase.getInstance().getReference().child("Tips");
        mTipsDatabase.keepSynced(true);

        if (key.equals("tip")){
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
            Search_List.setAdapter(tipsAdapter);
            readTips();
        }
        if (key.equals("contest")){
            Search_View.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    contestsAdapter.getFilter().filter(query);
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String query) {
                    contestsAdapter.getFilter().filter(query);
                    return false;
                }
            });
            Search_List.setAdapter(contestsAdapter);
            readContests();
        }
        if (key.equals("program")){
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
            Search_List.setAdapter(programAdapter);
            readPrograms();
        }
    }


    private void readPrograms() {

        mProgramsDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                programList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Programs program = snapshot.getValue(Programs.class);
                    if (program.getUser_id().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                        mNoActivity.setVisibility(View.INVISIBLE);
                        Search_List.setVisibility(View.VISIBLE);
                        programList.add(program);
                    }else{
                        mNoActivity.setVisibility(View.VISIBLE);
                        Search_List.setVisibility(View.VISIBLE);
                    }
                }
                programAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    private void readContests() {

        mContextsDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                contestsList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Contests contexts = snapshot.getValue(Contests.class);
                    if (contexts.getUser_id().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                        mNoActivity.setVisibility(View.INVISIBLE);
                        Search_List.setVisibility(View.VISIBLE);
                        contestsList.add(contexts);
                    }else {
                        mNoActivity.setVisibility(View.VISIBLE);
                        Search_List.setVisibility(View.INVISIBLE);
                    }
                }
                contestsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    private void readTips() {

        mTipsDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                tipsList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Tips tips = snapshot.getValue(Tips.class);
                    if (tips.getUser_id().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                        mNoActivity.setVisibility(View.INVISIBLE);
                        Search_List.setVisibility(View.VISIBLE);
                        tipsList.add(tips);
                    }else{
                        mNoActivity.setVisibility(View.VISIBLE);
                        Search_List.setVisibility(View.INVISIBLE);
                    }
                }
                tipsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onSearchSelected(Programs programs) {

    }

    @Override
    public void onSearchSelected(Contests contests) {

    }

    @Override
    public void onSearchSelected(Tips tips) {

    }
}
