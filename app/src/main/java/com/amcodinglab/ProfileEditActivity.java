package com.amcodinglab;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.TextView;
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

import java.util.ArrayList;
import java.util.HashMap;

public class ProfileEditActivity extends AppCompatActivity {

    private ImageView Profile_Edit_Profile_Image;
    private TextView Profile_Edit_Email_Address, Profile_Edit_Name, Change_Image;
    private FloatingActionButton Profile_Edit_Save;
    private EditText Profile_Edit_Place, Profile_Edit_Github;
    private AutoCompleteTextView Profile_Edit_Profession;
    private RadioButton Edit_Profile_Male, Edit_Profile_Female, Edit_Profile_Others;

    private FirebaseUser mFirebaseUser;
    private String mCurrentUserId;
    private Uri mImageUri;
    private String Gender="Male";
    private StorageTask mUploadTask;
    private StorageReference mProfileImageStorage;
    private DatabaseReference mUsersDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);
        Change_Image = findViewById(R.id.profile_edit_change_profile_image_text);
        Profile_Edit_Profile_Image = findViewById(R.id.profile_edit_profile_image);
        Profile_Edit_Save = findViewById(R.id.profile_edit_save);
        Profile_Edit_Name = findViewById(R.id.profile_edit_name);
        Profile_Edit_Email_Address = findViewById(R.id.profile_edit_email_address);

        Profile_Edit_Place = findViewById(R.id.profile_edit_place);
        Profile_Edit_Github = findViewById(R.id.profile_edit_github);
        Profile_Edit_Profession = findViewById(R.id.profile_edit_profession);
        Edit_Profile_Male = findViewById(R.id.edit_profile_male);
        Edit_Profile_Female = findViewById(R.id.edit_profile_female);
        Edit_Profile_Others = findViewById(R.id.edit_profile_others);
        setSuggestions();

        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        mCurrentUserId = mFirebaseUser.getUid();
        mProfileImageStorage = FirebaseStorage.getInstance().getReference("profile_images");

        mUsersDatabase = FirebaseDatabase.getInstance().getReference("Users").child(mCurrentUserId);
        mUsersDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                Profile_Edit_Name.setText(user.getName());
                Profile_Edit_Github.setText(user.getGithub());
                Profile_Edit_Place.setText(user.getPlace());
                Profile_Edit_Profession.setText(user.getProfession());
                Profile_Edit_Email_Address.setText(user.getEmail_id());
                if (user.getProfile_image().isEmpty())
                    Glide.with(getApplicationContext()).load(R.drawable.profile_image).into(Profile_Edit_Profile_Image);
                else
                    Glide.with(getApplicationContext()).load(user.getProfile_image()).placeholder(R.drawable.profile_placeholder).into(Profile_Edit_Profile_Image);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        Edit_Profile_Male.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    Gender="Male";
                }
            }
        });
        Edit_Profile_Female.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    Gender="Female";
                }
            }
        });
        Edit_Profile_Others.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    Gender="Others";
                }
            }
        });

        Profile_Edit_Save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (isEmpty(Profile_Edit_Place.getText().toString(), Profile_Edit_Profession.getText().toString())) {

                    UpdateProfile(Profile_Edit_Place.getText().toString(),
                            Profile_Edit_Profession.getText().toString(), Gender, Profile_Edit_Github.getText().toString());
                }

            }
        });

        Change_Image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CropImage.activity()
                        .setAspectRatio(1, 1)
                        .start(ProfileEditActivity.this);
            }
        });

        Profile_Edit_Profile_Image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CropImage.activity()
                        .setAspectRatio(1, 1)
                        .start(ProfileEditActivity.this);
            }
        });
    }

    private void UpdateProfile(String place, String profession, String gender, String github) {

        HashMap<String, Object> map = new HashMap<>();
        map.put("gender", gender);
        map.put("place", place);
        map.put("github", github);
        map.put("profession", profession);

        mUsersDatabase.updateChildren(map);

        Toast.makeText(ProfileEditActivity.this, "Successfully Updated!", Toast.LENGTH_SHORT).show();
    }

    private String getFileExtension(Uri uri) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    private void UploadImage() {
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Uploading");
        pd.show();
        if (mImageUri != null) {
            final StorageReference fileReference = mProfileImageStorage.child(System.currentTimeMillis()
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
                        String miUrlOk = downloadUri.toString();

                        HashMap<String, Object> map1 = new HashMap<>();
                        map1.put("profile_image", "" + miUrlOk);
                        mUsersDatabase.updateChildren(map1);

                        pd.dismiss();

                    } else {
                        Toast.makeText(ProfileEditActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(ProfileEditActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        } else {
            Toast.makeText(ProfileEditActivity.this, "No image selected", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {


            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            mImageUri = result.getUri();

            UploadImage();

        } else {
            Toast.makeText(this, "Something gone wrong!", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isEmpty(String place, String profession) {
        if (place.isEmpty() || profession.isEmpty()) {
            Toast.makeText(ProfileEditActivity.this, "Complete All Details!", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseAuth auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() != null) {
            mUsersDatabase = FirebaseDatabase.getInstance().getReference("Users").child(auth.getCurrentUser().getUid().toString());
            mUsersDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);
                    if (user.getProfession().equals("true")) {
                        Intent setupIntent = new Intent(ProfileEditActivity.this, SplashActivity.class);
                        setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(setupIntent);
                        finish();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        } else {
            startActivity(new Intent(ProfileEditActivity.this, LoginActivity.class));
            finish();
        }
    }

    void setSuggestions() {
        String items[];
        items = getResources().getStringArray(R.array.Program_Language);
        ArrayList<String> list = new ArrayList<String>();
        for (int i = 0; i < items.length; i++) {
            list.add(items[i]);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                ProfileEditActivity.this, R.layout.single_layout_text_view, list);

        Profile_Edit_Profession.setAdapter(adapter);
        Profile_Edit_Profession.setThreshold(1);

    }
}
