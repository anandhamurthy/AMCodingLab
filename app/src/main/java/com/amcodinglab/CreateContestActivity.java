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
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class CreateContestActivity extends AppCompatActivity {
    private EditText Title, Description, Organization_Name, Website;
    private FloatingActionButton Save;
    private CircleImageView Profile_Image;
    private ImageView Contest_Image;

    private FirebaseUser mFirebaseUser;
    private String mCurrentUserId;
    private Uri mImageUri;
    private boolean isChanged = false;
    private StorageTask mUploadTask;
    private StorageReference mContestImageStorage;
    private DatabaseReference mUsersDatabase;
    private DatabaseReference mContestDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_contest);

        Title=findViewById(R.id.title);
        Description=findViewById(R.id.desc);
        Organization_Name=findViewById(R.id.organization_name);
        Website =findViewById(R.id.web);
        Save=findViewById(R.id.save);
        Profile_Image=findViewById(R.id.profile_image);
        Contest_Image=findViewById(R.id.image);

        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        mCurrentUserId = mFirebaseUser.getUid();
        mContestImageStorage = FirebaseStorage.getInstance().getReference("contest_images");

        mUsersDatabase = FirebaseDatabase.getInstance().getReference("Users").child(mCurrentUserId);
        mUsersDatabase.keepSynced(true);
        mContestDatabase = FirebaseDatabase.getInstance().getReference("Contests");
        mContestDatabase.keepSynced(true);
        mUsersDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                Organization_Name.setText(user.getName());
                if (user.getProfile_image().isEmpty())
                    Glide.with(CreateContestActivity.this).load(R.drawable.profile_image).into(Profile_Image);
                else
                    Glide.with(CreateContestActivity.this).load(user.getProfile_image()).placeholder(R.drawable.profile_placeholder).into(Profile_Image);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        Contest_Image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CropImage.activity()
                        .start(CreateContestActivity.this);
            }
        });

        Save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isEmpty(Title.getText().toString(), Organization_Name.getText().toString(), Description.getText().toString(), Website.getText().toString())){
                    if (mImageUri!=null){
                        if (isChanged) {
                            UploadImage();
                        }
                    }else{
                        Toast.makeText(CreateContestActivity.this, "Click to add Image", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    private boolean isEmpty(String title, String name, String desc, String web) {
        if (title.isEmpty() || name.isEmpty() || desc.isEmpty() || web.isEmpty()) {
            Toast.makeText(CreateContestActivity.this, "Complete All Details!", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }


    private String getFileExtension(Uri uri) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    private void UploadImage() {
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Posting");
        pd.setCanceledOnTouchOutside(false);
        pd.show();
        if (mImageUri != null) {
            final StorageReference fileReference = mContestImageStorage.child(System.currentTimeMillis()
                    + "." + getFileExtension(mImageUri));

            mUploadTask = fileReference.putFile(mImageUri);
            mUploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return fileReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        String image = downloadUri.toString();

                        String key =mContestDatabase.push().getKey();
                        HashMap<String, Object> map = new HashMap<>();
                        map.put("contest_id", key);
                        map.put("user_id", mCurrentUserId);
                        map.put("title", Title.getText().toString());
                        map.put("organization_name", Organization_Name.getText().toString());
                        map.put("description", Description.getText().toString());
                        map.put("image", image);
                        String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
                        String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
                        map.put("timestamp", currentDate+" @ "+currentTime);
                        map.put("website", Website.getText().toString());

                        mContestDatabase.child(key).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    Toast.makeText(CreateContestActivity.this, "Successfully Posted!", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(CreateContestActivity.this, InterstitialAdActivity.class));
                                    pd.dismiss();
                                    finish();
                                }

                            }
                        });


                    } else {
                        Toast.makeText(CreateContestActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(CreateContestActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        } else {
            Toast.makeText(CreateContestActivity.this, "No image selected", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                mImageUri = result.getUri();
                Contest_Image.setImageURI(mImageUri);

                isChanged = true;

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception error = result.getError();
                Toast.makeText(this, "Something gone wrong! "+error, Toast.LENGTH_SHORT).show();

            }
        }

    }
}
