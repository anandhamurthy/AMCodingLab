package com.amcodinglab;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.amcodinglab.Model.Answers;
import com.amcodinglab.Model.Contests;
import com.amcodinglab.Model.Programs;
import com.amcodinglab.Model.Tips;
import com.amcodinglab.Model.User;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.HashMap;

public class ProfileActivity extends AppCompatActivity {

    private ImageView Profile_Image;
    private TextView Edit_Profile, Name, Tips_Count, Contests_Count, Programs_Count;
    private FirebaseUser mFirebaseUser;
    private String mCurrentUserId;
    private DatabaseReference mUsersDatabase;
    private DatabaseReference mContextsDatabase,mProgramsDatabase,mTipsDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        mCurrentUserId = mFirebaseUser.getUid();

        Profile_Image=findViewById(R.id.profile_profile_image);
        Edit_Profile=findViewById(R.id.profile_edit_profile);
        Name=findViewById(R.id.profile_name);
        Tips_Count=findViewById(R.id.profile_tips_count);
        Contests_Count=findViewById(R.id.profile_contests_count);
        Programs_Count=findViewById(R.id.profile_programs_count);

        mUsersDatabase = FirebaseDatabase.getInstance().getReference("Users").child(mCurrentUserId);
        mUsersDatabase.keepSynced(true);

        mContextsDatabase = FirebaseDatabase.getInstance().getReference().child("Contests");
        mContextsDatabase.keepSynced(true);

        mProgramsDatabase = FirebaseDatabase.getInstance().getReference().child("Programs");
        mProgramsDatabase.keepSynced(true);

        mTipsDatabase = FirebaseDatabase.getInstance().getReference().child("Tips");
        mTipsDatabase.keepSynced(true);

        mUsersDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                Name.setText(user.getName());
                if (user.getProfile_image().isEmpty())
                    Glide.with(getApplicationContext()).load(R.drawable.profile_image).into(Profile_Image);
                else
                    Glide.with(getApplicationContext()).load(user.getProfile_image()).placeholder(R.drawable.profile_placeholder).into(Profile_Image);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        Edit_Profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ProfileActivity.this, ProfileEditActivity.class));
            }
        });

        Tips_Count.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, SearchActivity.class);
                intent.putExtra("id","tip");
                startActivity(intent);
            }
        });

        Contests_Count.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, SearchActivity.class);
                intent.putExtra("id","contest");
                startActivity(intent);
            }
        });

        Programs_Count.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, SearchActivity.class);
                intent.putExtra("id","program");
                startActivity(intent);
            }
        });

        mTipsDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long count=0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Tips tips = snapshot.getValue(Tips.class);
                    if (tips.getUser_id().equals(mCurrentUserId)) {
                        count++;
                    }
                }
                Tips_Count.setText("" + count);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mContextsDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long count=0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Contests contests = snapshot.getValue(Contests.class);
                    if (contests.getUser_id().equals(mCurrentUserId)) {
                        count++;
                    }
                }
                Contests_Count.setText("" + count);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mProgramsDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long count=0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Programs programs = snapshot.getValue(Programs.class);
                    if (programs.getUser_id().equals(mCurrentUserId)) {
                        count++;
                    }
                }
                Programs_Count.setText("" + count);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
