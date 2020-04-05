package com.amcodinglab.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.amcodinglab.Model.Answers;
import com.amcodinglab.Model.User;
import com.amcodinglab.Notification.APIService;
import com.amcodinglab.Notification.Client;
import com.amcodinglab.Notification.Data;
import com.amcodinglab.Notification.MyResponse;
import com.amcodinglab.Notification.Sender;
import com.amcodinglab.Notification.Token;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

import br.tiagohm.codeview.CodeView;
import br.tiagohm.codeview.Language;
import br.tiagohm.codeview.Theme;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AnswersAdapter extends RecyclerView.Adapter<AnswersAdapter.ImageViewHolder> {

    private Context mContext;
    APIService apiService;
    private FirebaseAuth mAuth;
    private FirebaseUser mFirebaseUser;
    private String mCurrentUserId;
    private List<Answers> mAnswers;
    private FirebaseStorage mStorage;
    private DatabaseReference mAnswerDatabase, mUsersDatabase, mLikesDatabase, mAnswerCheckDatabase;

    public AnswersAdapter(Context context, List<Answers> answers) {
        mContext = context;
        mAnswers = answers;
    }


    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.single_layout_answer, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ImageViewHolder holder, final int position) {

        mAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mAuth.getCurrentUser();
        mCurrentUserId = mFirebaseUser.getUid();

        mAnswerDatabase = FirebaseDatabase.getInstance().getReference().child("Answers");
        mAnswerDatabase.keepSynced(true);
        mStorage = FirebaseStorage.getInstance();

        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mUsersDatabase.keepSynced(true);

        mLikesDatabase = FirebaseDatabase.getInstance().getReference().child("Likes");
        mLikesDatabase.keepSynced(true);

        mAnswerCheckDatabase = FirebaseDatabase.getInstance().getReference().child("Answer Check");
        mAnswerCheckDatabase.keepSynced(true);

        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);

        final Answers answers = mAnswers.get(position);
        UserInformation(holder.Answers_Profile_Image, answers.getUser_id(), holder.Answers_Name);
        holder.Answers_Date.setText(answers.getTimestamp());
        if (!answers.getImage().isEmpty()){
            holder.Answers_Code.setVisibility(View.GONE);
            holder.Answers_Image.setVisibility(View.VISIBLE);
            Glide.with(mContext).load(answers.getImage()).placeholder(R.drawable.image_placeholder).into(holder.Answers_Image);
        }
        if (!answers.getCode().isEmpty()){
            holder.Answers_Code.setVisibility(View.VISIBLE);
            holder.Answers_Image.setVisibility(View.GONE);
            holder.Answers_Code
                    .setTheme(Theme.AGATE)
                    .setCode(answers.getCode())
                    .setLanguage(Language.AUTO)
                    .setWrapLine(true)
                    .setFontSize(14)
                    .setZoomEnabled(false)
                    .setShowLineNumber(true)
                    .setStartLineNumber(1)
                    .apply();
        }
        getRateCounts(answers.getAnswer_id(), holder.Answers_Like_Count);
        if (answers.getUser_id().equals(mCurrentUserId)) {
            holder.Ansers_More.setVisibility(View.VISIBLE);
        }else{
            holder.Ansers_More.setVisibility(View.INVISIBLE);
        }
        isLiked(answers.getAnswer_id(), holder.Answers_Like);
        holder.Answers_Explanation.setText(answers.getExplanation());
        holder.Answers_Date.setText(answers.getTimestamp());
        holder.Answers_Language.setText(answers.getLanguage());
        holder.Answers_Like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.Answers_Like.getTag().equals("like")) {
                    mLikesDatabase.child(answers.getAnswer_id()).child(mCurrentUserId).setValue(true);
                    if (!answers.getUser_id().equals(mCurrentUserId)) {
                        mUsersDatabase.child(mCurrentUserId).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                User user = dataSnapshot.getValue(User.class);
                                sendNotification(answers.getUser_id(), user.getName(), "Liked your Answer", mCurrentUserId);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                } else {
                    mLikesDatabase.child(answers.getAnswer_id()).child(mCurrentUserId).removeValue();
                }
            }
        });


        holder.Ansers_More.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                PopupMenu popupMenu = new PopupMenu(mContext, view);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case R.id.delete:
                                mAnswerDatabase.child(answers.getAnswer_id()).removeValue()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull final Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    mAnswerCheckDatabase.child(answers.getQuestion_id()).child(mCurrentUserId).setValue(false).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull final Task<Void> task) {
                                                            if (task.isComplete()){
                                                                if (!answers.getImage().isEmpty()) {
                                                                    StorageReference storageRef = mStorage.getReference();
                                                                    StorageReference desertRef = storageRef.child("answer_images");
                                                                    StorageReference photoRef = desertRef.getStorage().getReferenceFromUrl(answers.getImage());
                                                                    photoRef.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if (task.isComplete())
                                                                                Toast.makeText(mContext, "Deleted!", Toast.LENGTH_SHORT).show();
                                                                        }
                                                                    });
                                                                }else{
                                                                    Toast.makeText(mContext, "Deleted!", Toast.LENGTH_SHORT).show();
                                                                }
                                                            }

                                                        }
                                                    });


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
                if (!answers.getUser_id().equals(mCurrentUserId)) {
                    popupMenu.getMenu().findItem(R.id.delete).setVisible(false);
                }
                popupMenu.show();
            }
        });


    }


    @Override
    public int getItemCount() {
        return mAnswers.size();
    }




    public class ImageViewHolder extends RecyclerView.ViewHolder implements CodeView.OnHighlightListener{

        public ImageView Answers_Image, Answers_Profile_Image, Answers_Like, Ansers_More;
        public TextView Answers_Name, Answers_Explanation, Answers_Date, Answers_Language, Answers_Like_Count;
        public CodeView Answers_Code;


        public ImageViewHolder(View itemView) {
            super(itemView);
            Answers_Name = itemView.findViewById(R.id.answer_name);
            Answers_Explanation = itemView.findViewById(R.id.answer_explanation);
            Answers_Date = itemView.findViewById(R.id.answer_date);
            Answers_Image = itemView.findViewById(R.id.answer_image);
            Answers_Profile_Image = itemView.findViewById(R.id.answer_profile_image);
            Answers_Code = itemView.findViewById(R.id.answer_code);
            Answers_Code.setOnHighlightListener(this);
            Answers_Language = itemView.findViewById(R.id.answer_language);
            Answers_Like = itemView.findViewById(R.id.answer_like);
            Answers_Like_Count = itemView.findViewById(R.id.answer_like_count);
            Ansers_More = itemView.findViewById(R.id.answer_more);
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

    private void getRateCounts(String id, final TextView rate) {
        mLikesDatabase.child(id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() == 0) {
                    rate.setText(dataSnapshot.getChildrenCount() + " Like");
                } else if (dataSnapshot.getChildrenCount() == 1) {
                    rate.setText(dataSnapshot.getChildrenCount() + " Like");
                } else {
                    rate.setText(dataSnapshot.getChildrenCount() + " Likes");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void isLiked(final String id, final ImageView imageView) {

        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        mLikesDatabase.child(id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(firebaseUser.getUid()).exists()) {
                    imageView.setImageResource(R.drawable.heart_in);
                    imageView.setTag("liked");
                } else {
                    imageView.setImageResource(R.drawable.heart_out);
                    imageView.setTag("like");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
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
                User_Name.setText(user.getName());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void sendNotification(final String Reciever, final String User_Name, final String Message, final String user_id) {

        final DatabaseReference token = FirebaseDatabase.getInstance().getReference().child("Tokens");
        Query query = token.orderByKey().equalTo(Reciever);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    Token token1 = snapshot.getValue(Token.class);
                    Data data = new Data(user_id, R.drawable.icon, User_Name, Message, Reciever);

                    Sender sender = new Sender(data, token1.getToken());
                    apiService.sendNotification(sender).enqueue(
                            new Callback<MyResponse>() {
                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {

                                    if (response.code() == 200) {
                                        if (response.body().sucess == 1) {
                                            Toast.makeText(mContext, "Failed", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Call<MyResponse> call, Throwable t) {

                                }
                            }
                    );
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
}