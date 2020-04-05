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

import com.amcodinglab.Model.Tips;
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

import br.tiagohm.codeview.CodeView;
import br.tiagohm.codeview.Language;
import br.tiagohm.codeview.Theme;

public class TipsAdapter extends RecyclerView.Adapter<TipsAdapter.ImageViewHolder> implements Filterable {

    private Context mContext;
    private List<Tips> mTips;
    private DatabaseReference mTipsDatabase, mUsersDatabase;
    private FirebaseStorage mStorage;
    private FirebaseAuth mAuth;
    private FirebaseUser mFirebaseUser;
    private String mCurrentUserId;
    private List<Tips> mDefaultTipList;
    private TipsAdapter.SearchAdapterListener listener;

    public TipsAdapter(Context context, List<Tips> tips, TipsAdapter.SearchAdapterListener listener) {
        mContext = context;
        mTips = tips;
        this.mDefaultTipList = tips;
        this.listener=listener;
    }


    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.single_layout_tip, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ImageViewHolder holder, final int position) {

        mAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mAuth.getCurrentUser();
        mCurrentUserId = mFirebaseUser.getUid();

        mTipsDatabase = FirebaseDatabase.getInstance().getReference().child("Tips");
        mTipsDatabase.keepSynced(true);

        mStorage = FirebaseStorage.getInstance();

        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mUsersDatabase.keepSynced(true);

        final Tips tips = mTips.get(position);
        UserInformation(holder.Tips_Profile_Image, tips.getUser_id(), holder.Tips_User_Name);
        if (!tips.getImage().isEmpty()){
            holder.Tips_Code.setVisibility(View.GONE);
            holder.Tips_Image.setVisibility(View.VISIBLE);
            Glide.with(mContext).load(tips.getImage()).placeholder(R.drawable.image_placeholder).into(holder.Tips_Image);
        }
        if (tips.getUser_id().equals(mCurrentUserId)) {
            holder.Tips_More.setVisibility(View.VISIBLE);
        }else{
            holder.Tips_More.setVisibility(View.INVISIBLE);
        }
        if (!tips.getCode().isEmpty()){
            holder.Tips_Code.setVisibility(View.VISIBLE);
            holder.Tips_Image.setVisibility(View.GONE);
            holder.Tips_Code
                    .setTheme(Theme.AGATE)
                    .setCode(tips.getCode())
                    .setLanguage(Language.AUTO)
                    .setWrapLine(true)
                    .setFontSize(14)
                    .setZoomEnabled(false)
                    .setShowLineNumber(true)
                    .setStartLineNumber(1)
                    .apply();
        }
        holder.Tips_Explanation.setText(tips.getExplanation());
        holder.Tips_Date.setText(tips.getTimestamp());
        holder.Tips_Name.setText(tips.getTitle());

        holder.Tips_More.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                PopupMenu popupMenu = new PopupMenu(mContext, view);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case R.id.delete:
                                mTipsDatabase.child(tips.getTip_id()).removeValue()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull final Task<Void> task) {
                                                if (task.isSuccessful()) {

                                                    if (!tips.getImage().isEmpty()) {
                                                        StorageReference storageRef = mStorage.getReference();
                                                        StorageReference desertRef = storageRef.child("tip_images");
                                                        StorageReference photoRef = desertRef.getStorage().getReferenceFromUrl(tips.getImage());
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
                if (!tips.getUser_id().equals(mCurrentUserId)) {
                    popupMenu.getMenu().findItem(R.id.delete).setVisible(false);
                }
                popupMenu.show();
            }
        });
    }


    @Override
    public int getItemCount() {
        return mTips.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString().toLowerCase();
                if (charString.isEmpty()) {
                    mTips = mDefaultTipList;
                } else {
                    List<Tips> filteredList = new ArrayList<>();
                    for (Tips row : mDefaultTipList) {

                        if (row.getTitle().toLowerCase().contains(charString) ||
                                row.getExplanation().toLowerCase().contains(charSequence) ||
                                row.getTimestamp().toLowerCase().contains(charSequence)) {
                            filteredList.add(row);
                        }
                    }

                    mTips = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = mTips;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                mTips = (ArrayList<Tips>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }


    public class ImageViewHolder extends RecyclerView.ViewHolder implements CodeView.OnHighlightListener{

        public ImageView Tips_Image, Tips_Profile_Image, Tips_More;
        public TextView Tips_Name, Tips_Explanation, Tips_Date, Tips_User_Name;
        public CodeView Tips_Code;

        public ImageViewHolder(View itemView) {
            super(itemView);
            Tips_Name = itemView.findViewById(R.id.tips_name);
            Tips_Explanation = itemView.findViewById(R.id.tips_explanation);
            Tips_Date = itemView.findViewById(R.id.tips_date);
            Tips_Image = itemView.findViewById(R.id.tips_image);
            Tips_Profile_Image = itemView.findViewById(R.id.tips_profile_image);
            Tips_Code = itemView.findViewById(R.id.tips_code);
            Tips_Code.setOnHighlightListener(this);
            Tips_User_Name = itemView.findViewById(R.id.tips_user_name);
            Tips_More = itemView.findViewById(R.id.tips_more);
        }

        @Override
        public void onStartCodeHighlight() {

        }

        @Override
        public void onFinishCodeHighlight() {

        }

        @Override
        public void onLanguageDetected(Language language, int i) {

        }

        @Override
        public void onFontSizeChanged(int i) {

        }

        @Override
        public void onLineClicked(int i, String s) {

        }
    }
    private void UserInformation(final ImageView Profile_Image, final String user_id, final TextView User_Name) {

        mUsersDatabase.child(user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (user.getProfile_image().isEmpty())
                    Glide.with(mContext).load(R.drawable.profile_image).into(Profile_Image);
                else
                    Glide.with(mContext).load(user.getProfile_image()).placeholder(R.drawable.profile_placeholder).into(Profile_Image);
                User_Name.setText("By : "+user.getName());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public interface SearchAdapterListener {
        void onSearchSelected(Tips tips);
    }
}