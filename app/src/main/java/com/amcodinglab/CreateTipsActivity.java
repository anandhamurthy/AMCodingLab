package com.amcodinglab;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import br.tiagohm.codeview.CodeView;
import br.tiagohm.codeview.Language;
import br.tiagohm.codeview.Theme;
import de.hdodenhof.circleimageview.CircleImageView;
import me.testica.codeeditor.Editor;
import me.testica.codeeditor.SyntaxHighlightRule;

public class CreateTipsActivity extends AppCompatActivity implements CodeView.OnHighlightListener{
    private EditText Title, Explanation;
    private TextView Name;
    private FloatingActionButton Add_Image, Add_Code, Save;
    private CircleImageView Profile_Image;
    private ImageView Image;
    private CodeView Code_View;

    private FirebaseUser mFirebaseUser;
    private String mCurrentUserId;
    private Uri mImageUri;
    private String Code="";
    private StorageTask mUploadTask;
    private StorageReference mContestImageStorage;
    private DatabaseReference mUsersDatabase;
    private DatabaseReference mTipDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_tips);

        Title=findViewById(R.id.title);
        Explanation=findViewById(R.id.explanation);
        Name=findViewById(R.id.name);
        Add_Image=findViewById(R.id.add_tip_image);
        Add_Code=findViewById(R.id.add_tip_editor);
        Save=findViewById(R.id.save);
        Profile_Image=findViewById(R.id.profile_image);
        Image=findViewById(R.id.image);
        Code_View=findViewById(R.id.code_view);

        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        mCurrentUserId = mFirebaseUser.getUid();
        mContestImageStorage = FirebaseStorage.getInstance().getReference("tip_images");

        mUsersDatabase = FirebaseDatabase.getInstance().getReference("Users").child(mCurrentUserId);
        mUsersDatabase.keepSynced(true);
        mTipDatabase = FirebaseDatabase.getInstance().getReference("Tips");
        mTipDatabase.keepSynced(true);
        mUsersDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                Name.setText("Tip By : "+user.getName());
                if (user.getProfile_image().isEmpty())
                    Glide.with(CreateTipsActivity.this).load(R.drawable.profile_image).into(Profile_Image);
                else
                    Glide.with(CreateTipsActivity.this).load(user.getProfile_image()).placeholder(R.drawable.profile_placeholder).into(Profile_Image);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        Add_Image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Image.setVisibility(View.VISIBLE);
                Code_View.setVisibility(View.GONE);
                CropImage.activity()
                        .start(CreateTipsActivity.this);

            }
        });

        Add_Code.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Image.setVisibility(View.GONE);
                Code_View.setVisibility(View.VISIBLE);
                final Dialog dialog = new Dialog(CreateTipsActivity.this);
                dialog.setContentView(R.layout.activity_editor);
                dialog.setTitle("AMCoding Lab Editor");
                final Editor editor = dialog.findViewById(R.id.editor);
                editor.setSyntaxHighlightRules(
                        new SyntaxHighlightRule("[0-9]*", "#00838f"),
                        new SyntaxHighlightRule("/\\\\*(?:.|[\\\\n\\\\r])*?\\\\*/|(?<!:)//.*", "#9ea7aa")
                );
                editor.getNumLinesView().setBackgroundColor(Color.GRAY);
                editor.getNumLinesView().setTextColor(Color.WHITE);

                editor.getEditText().setPadding(10, 0, 0, 0);
                Button Done = dialog.findViewById(R.id.done);
                Done.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Code=editor.getText();
                        Code_View.setOnHighlightListener(CreateTipsActivity.this)
                                .setTheme(Theme.AGATE)
                                .setCode(Code+"\n")
                                .setLanguage(Language.AUTO)
                                .setWrapLine(true)
                                .setFontSize(14)
                                .setZoomEnabled(false)
                                .setShowLineNumber(true)
                                .setStartLineNumber(1)
                                .apply();
                        dialog.dismiss();
                    }
                });

                dialog.show();
            }
        });

        Save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isEmpty(Title.getText().toString(), Explanation.getText().toString())){
                    if (mImageUri!=null || !Code.isEmpty()){
                        UploadImage();
                    }else{
                        Toast.makeText(CreateTipsActivity.this, "Add Image or Type Code!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    private boolean isEmpty(String title, String explain) {
        if (title.isEmpty() || explain.isEmpty()) {
            Toast.makeText(CreateTipsActivity.this, "Complete All Details!", Toast.LENGTH_SHORT).show();
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

                        String key =mTipDatabase.push().getKey();
                        HashMap<String, Object> map = new HashMap<>();
                        map.put("tip_id", key);
                        map.put("user_id", mCurrentUserId);
                        map.put("title", Title.getText().toString());
                        map.put("code", Code+"\n");
                        String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
                        String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
                        map.put("timestamp", currentDate+" @ "+currentTime);
                        map.put("explanation", Explanation.getText().toString());
                        map.put("image", image);

                        mTipDatabase.child(key).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    Toast.makeText(CreateTipsActivity.this, "Successfully Posted!", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(CreateTipsActivity.this, InterstitialAdActivity.class));
                                    pd.dismiss();
                                    finish();
                                }

                            }
                        });


                    } else {
                        Toast.makeText(CreateTipsActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(CreateTipsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        } else {
            String key =mTipDatabase.push().getKey();
            HashMap<String, Object> map = new HashMap<>();
            map.put("tip_id", key);
            map.put("user_id", mCurrentUserId);
            map.put("title", Title.getText().toString());
            map.put("code", Code+"\n");
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy" );
            Calendar calendar = Calendar.getInstance();
            map.put("timestamp", dateFormat.format(calendar.getTime()));
            map.put("explanation", Explanation.getText().toString());
            map.put("image", "");

            mTipDatabase.child(key).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        Toast.makeText(CreateTipsActivity.this, "Successfully Posted!", Toast.LENGTH_SHORT).show();
                        pd.dismiss();
                        finish();
                    }

                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                mImageUri = result.getUri();
                Image.setImageURI(mImageUri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception error = result.getError();
                Toast.makeText(this, "Something gone wrong! "+error, Toast.LENGTH_SHORT).show();

            }
        }

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
