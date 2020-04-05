package com.amcodinglab.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.amcodinglab.Model.Contests;
import com.amcodinglab.Model.Programs;
import com.amcodinglab.Model.User;
import com.amcodinglab.R;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ContestsAdapter extends RecyclerView.Adapter<ContestsAdapter.ImageViewHolder> implements Filterable {

    private Context mContext;
    private List<Contests> mcontests;
    private DatabaseReference mcontestsDatabase, mUsersDatabase;
    private List<Contests> mDefaultContestList;
    private FirebaseAuth mAuth;
    private FirebaseUser mFirebaseUser;
    private FirebaseStorage mStorage;
    private String mCurrentUserId;
    private ContestsAdapter.SearchAdapterListener listener;

    public ContestsAdapter(Context context, List<Contests> contests, ContestsAdapter.SearchAdapterListener listener) {
        mContext = context;
        mcontests = contests;
        this.mDefaultContestList = contests;
        this.listener=listener;
    }


    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.single_layout_contest, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ImageViewHolder holder, final int position) {

        mAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mAuth.getCurrentUser();
        mCurrentUserId = mFirebaseUser.getUid();
        mStorage = FirebaseStorage.getInstance();
        mcontestsDatabase = FirebaseDatabase.getInstance().getReference().child("Contests");
        mcontestsDatabase.keepSynced(true);

        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mUsersDatabase.keepSynced(true);

        final Contests contests = mcontests.get(position);
        UserInformation(holder.Contests_Profile_Image, contests.getUser_id());
        Glide.with(mContext).load(contests.getImage()).placeholder(R.drawable.image_placeholder).into(holder.Contests_Image);
        holder.Contests_Desc.setText(contests.getDescription());
        holder.Contests_Name.setText(contests.getTitle());
        holder.Contests_Date.setText(contests.getTimestamp());
        holder.Contests_Company_Name.setText(contests.getOrganization_name());
        holder.Contests_Web.setText("Website : "+contests.getWebsite());
        if (contests.getUser_id().equals(mCurrentUserId)) {
            holder.Contests_More.setVisibility(View.VISIBLE);
        }else{
            holder.Contests_More.setVisibility(View.INVISIBLE);
        }
        holder.Contests_More.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                PopupMenu popupMenu = new PopupMenu(mContext, view);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case R.id.delete:
                                mcontestsDatabase.child(contests.getContest_id()).removeValue()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull final Task<Void> task) {
                                                if (task.isComplete()) {

                                                    if (!contests.getImage().isEmpty()) {
                                                        StorageReference storageRef = mStorage.getReference();
                                                        StorageReference desertRef = storageRef.child("contest_images");
                                                        StorageReference photoRef = desertRef.getStorage().getReferenceFromUrl(contests.getImage());
                                                        photoRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                if (task.isComplete()) {
                                                                    Toast.makeText(mContext, "Deleted!", Toast.LENGTH_SHORT).show();
                                                                }

                                                            }
                                                        });
                                                    }else{
                                                        Toast.makeText(mContext, "Deleted!", Toast.LENGTH_SHORT).show();
                                                    }

                                                }
                                            }
                                        });
                                return true;
                            default:
                                return false;
                        }
                    }
                });
                popupMenu.inflate(R.menu.menu_post);
                if (!contests.getUser_id().equals(mCurrentUserId)) {
                    popupMenu.getMenu().findItem(R.id.delete).setVisible(false);
                }
                popupMenu.show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mcontests.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString().toLowerCase();
                if (charString.isEmpty()) {
                    mcontests = mDefaultContestList;
                } else {
                    List<Contests> filteredList = new ArrayList<>();
                    for (Contests row : mDefaultContestList) {

                        if (row.getTitle().toLowerCase().contains(charString) ||
                                row.getOrganization_name().toLowerCase().contains(charSequence) ||
                                row.getTimestamp().toLowerCase().contains(charSequence) ) {
                            filteredList.add(row);
                        }
                    }

                    mcontests = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = mcontests;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                mcontests = (ArrayList<Contests>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }


    public class ImageViewHolder extends RecyclerView.ViewHolder {

        public ImageView Contests_Profile_Image, Contests_Image, Contests_More;
        public TextView Contests_Name, Contests_Desc, Contests_Date, Contests_Web, Contests_Company_Name;
        public ImageViewHolder(View itemView) {
            super(itemView);

            Contests_Profile_Image = itemView.findViewById(R.id.contests_profile_image);
            Contests_Name = itemView.findViewById(R.id.contests_name);
            Contests_Image = itemView.findViewById(R.id.contests_image);
            Contests_Desc = itemView.findViewById(R.id.contests_desc);
            Contests_Date = itemView.findViewById(R.id.contests_date);
            Contests_Company_Name = itemView.findViewById(R.id.contests_company_name);
            Contests_Web = itemView.findViewById(R.id.contests_web);
            Contests_More = itemView.findViewById(R.id.contests_more);

        }
    }

    private void UserInformation(final ImageView Profile_Image, String user_id) {

        mUsersDatabase.child(user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (user.getProfile_image().isEmpty())
                    Glide.with(mContext).load(R.drawable.profile_image).into(Profile_Image);
                else
                    Glide.with(mContext).load(user.getProfile_image()).placeholder(R.drawable.profile_placeholder).into(Profile_Image);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public interface SearchAdapterListener {
        void onSearchSelected(Contests contests);
    }
}