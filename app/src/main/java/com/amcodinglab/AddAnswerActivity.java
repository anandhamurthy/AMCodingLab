package com.amcodinglab;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import br.tiagohm.codeview.CodeView;
import br.tiagohm.codeview.Language;
import br.tiagohm.codeview.Theme;
import me.testica.codeeditor.Editor;
import me.testica.codeeditor.SyntaxHighlightRule;

public class AddAnswerActivity extends AppCompatActivity implements br.tiagohm.codeview.CodeView.OnHighlightListener {

    private ImageView Image;
    private TextView Add_Answer_Statement, Add_Answer_Description;
    private FloatingActionButton Done, Add_Image, Add_Code;
    private EditText Add_Answer_Explanation;
    private AutoCompleteTextView Add_Answer_Language;
    private CodeView Code_View;

    private String Statement, Description, User_Id, Question_Id;

    private FirebaseUser mFirebaseUser;
    private String mCurrentUserId;
    private Uri mImageUri;
    private String Code="";

    private StorageTask mUploadTask;
    private StorageReference mProgramImageStorage;
    private DatabaseReference mUsersDatabase;
    private DatabaseReference mAnswersDatabase, mAnswerCheckDatabase;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_answer);

        Intent intent = getIntent();
        Statement = intent.getStringExtra("statement");
        Description = intent.getStringExtra("description");
        User_Id = intent.getStringExtra("user_id");
        Question_Id = intent.getStringExtra("question_id");

        Image=findViewById(R.id.image);
        Add_Answer_Statement=findViewById(R.id.add_answer_statement);
        Add_Answer_Description=findViewById(R.id.add_answer_desc);
        Done=findViewById(R.id.add);
        Add_Answer_Explanation=findViewById(R.id.explanation);
        Add_Answer_Language=findViewById(R.id.language);
        setSuggestions();
        Add_Image=findViewById(R.id.add_solution_image);
        Add_Code=findViewById(R.id.add_solution_editor);
        Code_View=findViewById(R.id.code_view);

        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        mCurrentUserId = mFirebaseUser.getUid();
        mProgramImageStorage = FirebaseStorage.getInstance().getReference("answer_images");

        mUsersDatabase = FirebaseDatabase.getInstance().getReference("Users");
        mUsersDatabase.keepSynced(true);
        mAnswersDatabase = FirebaseDatabase.getInstance().getReference("Answers");
        mAnswersDatabase.keepSynced(true);
        mAnswerCheckDatabase = FirebaseDatabase.getInstance().getReference().child("Answer Check");
        mAnswerCheckDatabase.keepSynced(true);

        Add_Answer_Statement.setText(Statement);
        Add_Answer_Description.setText(Description);

        Add_Image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Code_View.setVisibility(View.GONE);
                Image.setVisibility(View.VISIBLE);
                CropImage.activity()
                        .start(AddAnswerActivity.this);

            }
        });

        Add_Code.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Code_View.setVisibility(View.VISIBLE);
                Image.setVisibility(View.GONE);
                final Dialog dialog = new Dialog(AddAnswerActivity.this);
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
                        Code_View.setOnHighlightListener(AddAnswerActivity.this)
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

        Done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!Add_Answer_Explanation.getText().toString().isEmpty() && !Add_Answer_Language.getText().toString().isEmpty()) {
                    if (mImageUri != null || !Code.isEmpty()) {
                        Post();
                    } else {
                        Toast.makeText(AddAnswerActivity.this,
                                "Add Image or Type Code!", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(AddAnswerActivity.this,
                            "Complete All Details!", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }


    private String getFileExtension(Uri uri) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    private void Post() {
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Posting");
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

                        String key =mAnswersDatabase.push().getKey();
                        HashMap<String, Object> map = new HashMap<>();
                        map.put("answer_id", key);
                        map.put("user_id", mCurrentUserId);
                        map.put("explanation", Add_Answer_Explanation.getText().toString());
                        map.put("language", Add_Answer_Language.getText().toString());
                        map.put("image", image);
                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy" );
                        Calendar calendar = Calendar.getInstance();
                        map.put("timestamp", dateFormat.format(calendar.getTime()));
                        map.put("question_id", Question_Id);
                        map.put("code", Code+"\n");

                        mAnswersDatabase.child(key).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    mAnswerCheckDatabase.child(Question_Id).child(mCurrentUserId).setValue(true).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isComplete()){
                                                Toast.makeText(AddAnswerActivity.this, "Successfully Posted!", Toast.LENGTH_SHORT).show();
                                                startActivity(new Intent(AddAnswerActivity.this, InterstitialAdActivity.class));
                                                pd.dismiss();
                                                finish();
                                            }
                                        }
                                    });

                                }

                            }
                        });


                    } else {
                        Toast.makeText(AddAnswerActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(AddAnswerActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        }else{
            String key =mAnswersDatabase.push().getKey();
            HashMap<String, Object> map = new HashMap<>();
            map.put("answer_id", key);
            map.put("user_id", mCurrentUserId);
            map.put("explanation", Add_Answer_Explanation.getText().toString());
            map.put("language", Add_Answer_Language.getText().toString());
            map.put("image", "");
            map.put("question_id", Question_Id);
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy" );
            Calendar calendar = Calendar.getInstance();
            map.put("timestamp", dateFormat.format(calendar.getTime()));
            map.put("code", Code);

            mAnswersDatabase.child(key).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        mAnswerCheckDatabase.child(Question_Id).child(mCurrentUserId).setValue(true).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isComplete()){
                                    Toast.makeText(AddAnswerActivity.this, "Successfully Posted!", Toast.LENGTH_SHORT).show();
                                    pd.dismiss();
                                    finish();
                                }
                            }
                        });
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

    void setSuggestions() {
        String items[];
        items = getResources().getStringArray(R.array.Program_Language);
        ArrayList<String> list = new ArrayList<String>();
        for (int i = 0; i < items.length; i++) {
            list.add(items[i]);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                AddAnswerActivity.this, R.layout.single_layout_text_view, list);

        Add_Answer_Language.setAdapter(adapter);
        Add_Answer_Language.setThreshold(1);

    }
}
