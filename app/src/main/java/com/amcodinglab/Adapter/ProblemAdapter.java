package com.amcodinglab.Adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.amcodinglab.AnswerActivity;
import com.amcodinglab.CreateProgramActivity;
import com.amcodinglab.Model.Answers;
import com.amcodinglab.Model.User;
import com.amcodinglab.R;
import com.amcodinglab.Model.Programs;
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
import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

public class ProblemAdapter extends RecyclerView.Adapter<ProblemAdapter.ImageViewHolder> implements Filterable {

    private Context mContext;
    private List<Programs> mProgram;
    private DatabaseReference mProgramDatabase, mUsersDatabase;
    private FirebaseStorage mStorage;
    private List<Programs> mDefaultProgramList;
    private SearchAdapterListener listener;
    private FirebaseAuth mAuth;
    private FirebaseUser mFirebaseUser;
    private String mCurrentUserId;

    public ProblemAdapter(Context context, List<Programs> programs, ProblemAdapter.SearchAdapterListener listener) {
        mContext = context;
        mProgram = programs;
        this.mDefaultProgramList = programs;
        this.listener=listener;

    }


    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.single_layout_program, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ImageViewHolder holder, final int position) {

        mAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mAuth.getCurrentUser();
        mCurrentUserId = mFirebaseUser.getUid();
        mStorage = FirebaseStorage.getInstance();
        mProgramDatabase = FirebaseDatabase.getInstance().getReference().child("Programs");
        mProgramDatabase.keepSynced(true);
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mUsersDatabase.keepSynced(true);

        final Programs program = mProgram.get(position);
        UserInformation(holder.Program_Profile_Image, program.getUser_id(), holder.Program_User_Name);

        holder.Program_Desc.setText(program.getDescription());
        holder.Program_Date.setText(program.getTimestamp());
        holder.Program_Language.setText(program.getLanguage());
        holder.Program_Statement.setText(program.getStatement());
        if (program.getUser_id().equals(mCurrentUserId)) {
            holder.Program_More.setVisibility(View.VISIBLE);
        }else{
            holder.Program_More.setVisibility(View.INVISIBLE);
        }
        String need_solution="";
        if (program.isNeed_solution()){
            need_solution="true";
        }else{
            need_solution="false";
        }

        final String finalNeed_solution = need_solution;
        holder.Program_Answer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, AnswerActivity.class);
                intent.putExtra("statement", program.getStatement());
                intent.putExtra("date", program.getTimestamp());
                intent.putExtra("description", program.getDescription());
                intent.putExtra("language", program.getLanguage());
                intent.putExtra("question_id", program.getProgram_id());
                intent.putExtra("need_solution", finalNeed_solution);
                intent.putExtra("user_id", program.getUser_id());
                mContext.startActivity(intent);

            }
        });

        holder.Program_More.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                PopupMenu popupMenu = new PopupMenu(mContext, view);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case R.id.delete:
                                mProgramDatabase.child(program.getProgram_id()).removeValue()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull final Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    if (!program.getImage().isEmpty()) {
                                                        StorageReference storageRef = mStorage.getReference();
                                                        StorageReference desertRef = storageRef.child("program_images");
                                                        StorageReference photoRef = desertRef.getStorage().getReferenceFromUrl(program.getImage());
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
                if (!program.getUser_id().equals(mCurrentUserId)) {
                    popupMenu.getMenu().findItem(R.id.delete).setVisible(false);
                }
                popupMenu.show();
            }
        });
    }


    @Override
    public int getItemCount() {
        return mProgram.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString().toLowerCase();
                if (charString.isEmpty()) {
                    mProgram = mDefaultProgramList;
                } else {
                    List<Programs> filteredList = new ArrayList<>();
                    for (Programs row : mDefaultProgramList) {

                        if (row.getStatement().toLowerCase().contains(charString) ||
                                row.getLanguage().toLowerCase().contains(charSequence) ||
                                row.getTimestamp().toLowerCase().contains(charSequence)) {
                            filteredList.add(row);
                        }
                    }

                    mProgram = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = mProgram;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                mProgram = (ArrayList<Programs>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }


    public class ImageViewHolder extends RecyclerView.ViewHolder {

        public ImageView Program_Profile_Image, Program_More;
        public TextView Program_Desc, Program_Date, Program_Language, Program_Statement, Program_User_Name;
        public Button Program_Answer;

        public ImageViewHolder(View itemView) {
            super(itemView);
            Program_Desc = itemView.findViewById(R.id.program_desc);
            Program_Date = itemView.findViewById(R.id.program_date);
            Program_Profile_Image = itemView.findViewById(R.id.program_profile_image);
            Program_Answer = itemView.findViewById(R.id.program_answer);
            Program_Language = itemView.findViewById(R.id.program_language);
            Program_User_Name = itemView.findViewById(R.id.program_user_name);
            Program_Statement = itemView.findViewById(R.id.program_statement);
            Program_More = itemView.findViewById(R.id.program_more);

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
        void onSearchSelected(Programs programs);
    }
}