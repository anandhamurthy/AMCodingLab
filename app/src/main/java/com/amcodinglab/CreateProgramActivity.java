package com.amcodinglab;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.UUID;

import br.tiagohm.codeview.CodeView;
import br.tiagohm.codeview.Language;
import br.tiagohm.codeview.Theme;
import me.testica.codeeditor.Editor;
import me.testica.codeeditor.SyntaxHighlightRule;


public class CreateProgramActivity extends AppCompatActivity implements br.tiagohm.codeview.CodeView.OnHighlightListener {

    private EditText Statement, Description, Explanation;
    private AutoCompleteTextView Language;
    private Switch Need_Solution;
    private TextView Add_Solution_Title, Name;
    private FloatingActionButton Add_Solution_Image, Add_Solution_Editor, Save;
    private LinearLayout Add_Solution;
    private Boolean value=true;
    private ImageView Image;
    private CodeView Code_View;

    private FirebaseUser mFirebaseUser;
    private String mCurrentUserId;
    private Uri mImageUri;
    private String Code="";

    private StorageTask mUploadTask;
    private StorageReference mProgramImageStorage;
    private DatabaseReference mUsersDatabase;
    private DatabaseReference mProgramDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_program);

        Statement = findViewById(R.id.statement);
        Description = findViewById(R.id.description);
        Language = findViewById(R.id.language);
        setSuggestions();
        Explanation = findViewById(R.id.explanation);

        Need_Solution = findViewById(R.id.need_solution);
        Add_Solution_Title = findViewById(R.id.add_solution_title);
        Add_Solution_Image = findViewById(R.id.add_solution_image);
        Add_Solution_Editor = findViewById(R.id.add_solution_editor);

        Add_Solution = findViewById(R.id.add_solution);

        Save = findViewById(R.id.save);
        Image=findViewById(R.id.image);
        Code_View=findViewById(R.id.code_view);
        Name=findViewById(R.id.name);

        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        mCurrentUserId = mFirebaseUser.getUid();
        mProgramImageStorage = FirebaseStorage.getInstance().getReference("program_images");

        mUsersDatabase = FirebaseDatabase.getInstance().getReference("Users").child(mCurrentUserId);
        mUsersDatabase.keepSynced(true);
        mProgramDatabase = FirebaseDatabase.getInstance().getReference("Programs");
        mProgramDatabase.keepSynced(true);
        mUsersDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                Name.setText("Program By : "+user.getName());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        Need_Solution.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onClick(View v) {
                if(Need_Solution.isChecked()){
                    Add_Solution.setVisibility(View.GONE);
                    Add_Solution_Title.setVisibility(View.GONE);
                    Add_Solution_Image.setVisibility(View.GONE);
                    Add_Solution_Editor.setVisibility(View.GONE);
                    Image.setVisibility(View.GONE);
                    Code_View.setVisibility(View.GONE);
                    Explanation.setVisibility(View.GONE);
                    value=false;
                }else{
                    Add_Solution.setVisibility(View.VISIBLE);
                    Add_Solution_Title.setVisibility(View.VISIBLE);
                    Add_Solution_Image.setVisibility(View.VISIBLE);
                    Add_Solution_Editor.setVisibility(View.VISIBLE);
                    Image.setVisibility(View.VISIBLE);
                    Code_View.setVisibility(View.VISIBLE);
                    Explanation.setVisibility(View.VISIBLE);
                    value=true;
                }
            }
        });
        Add_Solution_Image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Code_View.setVisibility(View.GONE);
                Image.setVisibility(View.VISIBLE);
                CropImage.activity()
                        .start(CreateProgramActivity.this);

            }
        });

        Add_Solution_Editor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Code_View.setVisibility(View.VISIBLE);
                Image.setVisibility(View.GONE);
                final Dialog dialog = new Dialog(CreateProgramActivity.this);
                dialog.setContentView(R.layout.activity_editor);
                dialog.setTitle("AMCoding Lab Editor");
                final Editor editor = dialog.findViewById(R.id.editor);
                editor.setSyntaxHighlightRules(
                        new SyntaxHighlightRule("[0-9]*", "#00838f"),
                        new SyntaxHighlightRule("/\\\\*(?:.|[\\\\n\\\\r])*?\\\\*/|(?<!:)//.*", "#9ea7aa")
                );
                editor.getNumLinesView().setBackgroundColor(Color.GRAY);
                editor.getNumLinesView().setTextColor(Color.WHITE);
                Button Done = dialog.findViewById(R.id.done);
                Done.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Code=editor.getText();
                        Code_View.setOnHighlightListener(CreateProgramActivity.this)
                                .setTheme(Theme.AGATE)
                                .setCode(Code+"\n")
                                .setLanguage(br.tiagohm.codeview.Language.AUTO)
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
                if (value){
                    if (isEmpty(Statement.getText().toString(), Description.getText().toString(), Language.getText().toString())){
                        if (!Explanation.getText().toString().isEmpty()) {
                            if (mImageUri != null || !Code.isEmpty()) {
                                Post(Explanation.getText().toString());
                            } else {
                                Toast.makeText(CreateProgramActivity.this,
                                        "Add Image or Type Code!", Toast.LENGTH_SHORT).show();
                            }
                        }else{
                            Toast.makeText(CreateProgramActivity.this,
                                    "Complete All Details!", Toast.LENGTH_SHORT).show();
                        }
                    }
                }else{
                    if (isEmpty(Statement.getText().toString(), Description.getText().toString(), Language.getText().toString())){
                        Post("");
                    }
                }

            }
        });
    }

    private boolean isEmpty(String title, String dsc, String lang) {
        if (title.isEmpty() || dsc.isEmpty() || lang.isEmpty()) {
            Toast.makeText(CreateProgramActivity.this, "Complete All Details!", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }


    private String getFileExtension(Uri uri) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    private void Post(final String s) {
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Posting");
        pd.setCanceledOnTouchOutside(false);
        pd.show();
        if (mImageUri != null) {
            final StorageReference fileReference = mProgramImageStorage.child(System.currentTimeMillis()
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

                        String key =mProgramDatabase.push().getKey();
                        HashMap<String, Object> map = new HashMap<>();
                        map.put("program_id", key);
                        map.put("user_id", mCurrentUserId);
                        map.put("statement", Statement.getText().toString());
                        map.put("code", Code+"\n");
                        map.put("explanation", s);
                        map.put("need_solution", value);
                        map.put("image", image);
                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy" );
                        Calendar calendar = Calendar.getInstance();
                        map.put("timestamp", dateFormat.format(calendar.getTime()));
                        map.put("description", Description.getText().toString());
                        map.put("language", Language.getText().toString());

                        mProgramDatabase.child(key).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    Toast.makeText(CreateProgramActivity.this, "Successfully Posted!", Toast.LENGTH_SHORT).show();
                                    pd.dismiss();
                                    finish();
                                }

                            }
                        });


                    } else {
                        Toast.makeText(CreateProgramActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(CreateProgramActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        } else {
            String key =mProgramDatabase.push().getKey();
            HashMap<String, Object> map = new HashMap<>();
            map.put("program_id", key);
            map.put("user_id", mCurrentUserId);
            map.put("statement", Statement.getText().toString());
            map.put("code", Code);
            map.put("need_solution", value);
            map.put("explanation", s);
            String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
            String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
            map.put("timestamp", currentDate+" @ "+currentTime);
            map.put("image", "");
            map.put("description", Description.getText().toString());
            map.put("language", Language.getText().toString());

            mProgramDatabase.child(key).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        Toast.makeText(CreateProgramActivity.this, "Successfully Posted!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(CreateProgramActivity.this, InterstitialAdActivity.class));
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
    public void onLanguageDetected(br.tiagohm.codeview.Language language, int i) {

    }

    @Override
    public void onFontSizeChanged(int i) {

    }

    @Override
    public void onLineClicked(int i, String s) {

    }

    void setSuggestions() {
        String items[];
        items = getResources().getStringArray(R.array.Program_Language);
        ArrayList<String> list = new ArrayList<String>();
        for (int i = 0; i < items.length; i++) {
            list.add(items[i]);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                CreateProgramActivity.this, R.layout.single_layout_text_view, list);

        Language.setAdapter(adapter);
        Language.setThreshold(1);

    }
}