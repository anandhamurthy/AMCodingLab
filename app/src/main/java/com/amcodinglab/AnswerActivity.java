package com.amcodinglab;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amcodinglab.Adapter.AnswersAdapter;
import com.amcodinglab.Model.Answers;
import com.amcodinglab.Model.Programs;
import com.amcodinglab.Model.User;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import br.tiagohm.codeview.CodeView;
import br.tiagohm.codeview.Language;
import br.tiagohm.codeview.Theme;

public class AnswerActivity extends AppCompatActivity implements CodeView.OnHighlightListener{

    private ImageView Profile_Image, Answer_Image;
    private TextView Answer_Statement, Answer_Date, Answer_Description, Answer_Language, Answer_User_Name;
    private String Statement, Date, Description, User_Id, Langua, Question_Id, Need_Solution;
    public CodeView Answer_Code;

    private RecyclerView Answer_List;
    private Button Answer;

    private DatabaseReference mUsersDatabase;
    private FirebaseAuth mAuth;
    private FirebaseUser mFirebaseUser;
    private String mCurrentUserId;
    private AnswersAdapter answerAdapter;
    private List<Answers> answersList;
    private DatabaseReference mAnswersDatabase, mProgramDatabase, mAnswerCheckDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_answer);

        Intent intent = getIntent();
        Statement = intent.getStringExtra("statement");
        Date = intent.getStringExtra("date");
        Description = intent.getStringExtra("description");
        User_Id = intent.getStringExtra("user_id");
        Need_Solution = intent.getStringExtra("need_solution");
        Langua = intent.getStringExtra("language");
        Question_Id = intent.getStringExtra("question_id");

        Answer_Statement = findViewById(R.id.answer_statement);
        Answer_Date = findViewById(R.id.answer_date);
        Answer_Description = findViewById(R.id.answer_desc);
        Profile_Image = findViewById(R.id.answer_profile_image);
        Answer = findViewById(R.id.answer_add_answer);
        Answer_List = findViewById(R.id.answers_list);
        Answer_Language = findViewById(R.id.answer_language);
        Answer_User_Name = findViewById(R.id.answer_user_name);
        Answer_Code = findViewById(R.id.answer_code);
        Answer_Image = findViewById(R.id.answer_image);

        mAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mAuth.getCurrentUser();
        mCurrentUserId = mFirebaseUser.getUid();

        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mUsersDatabase.keepSynced(true);

        mAnswersDatabase = FirebaseDatabase.getInstance().getReference().child("Answers");
        mAnswersDatabase.keepSynced(true);

        mProgramDatabase = FirebaseDatabase.getInstance().getReference().child("Programs");
        mProgramDatabase.keepSynced(true);

        mAnswerCheckDatabase = FirebaseDatabase.getInstance().getReference().child("Answer Check");
        mAnswerCheckDatabase.keepSynced(true);

        mUsersDatabase.child(User_Id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                Glide.with(AnswerActivity.this).load(user.getProfile_image()).into(Profile_Image);
                if (Need_Solution.equals("true"))
                    Answer_User_Name.setText("By : "+user.getName());
                else
                    Answer_User_Name.setText("Asked By : "+user.getName());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        Answer_Statement.setText(Statement);
        Answer_Date.setText(Date);
        Answer_Description.setText(Description);
        Answer_Language.setText(Langua);
        Answer_List.setHasFixedSize(true);
        answersList = new ArrayList<>();

        isAnswered(Question_Id,Answer);

        LinearLayoutManager mLayoutManager = new LinearLayoutManager(AnswerActivity.this);
        mLayoutManager.setReverseLayout(true);
        mLayoutManager.setStackFromEnd(true);
        Answer_List.setLayoutManager(mLayoutManager);
        answerAdapter = new AnswersAdapter(AnswerActivity.this, answersList);
        Answer_List.setAdapter(answerAdapter);

        if (Need_Solution.equals("false")){
            Answer.setVisibility(View.VISIBLE);
            Answer_Code.setVisibility(View.GONE);
            Answer_Image.setVisibility(View.GONE);
            Answer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (Answer.getTag().equals("answer")) {
                        Intent intent = new Intent(AnswerActivity.this, AddAnswerActivity.class);
                        intent.putExtra("statement", Statement);
                        intent.putExtra("description", Description);
                        intent.putExtra("user_id", User_Id);
                        intent.putExtra("question_id", Question_Id);
                        startActivity(intent);

                    }
                }
//                    else {
//                        mAnswerCheckDatabase.child(Question_Id).child(mCurrentUserId).removeValue();
//                    }

            });

            readAnswers();
        }else{
            Answer.setVisibility(View.INVISIBLE);
            mProgramDatabase.child(Question_Id).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    Programs programs = dataSnapshot.getValue(Programs.class);
                    if (!programs.getImage().isEmpty()){
                        Answer_Code.setVisibility(View.GONE);
                        Answer_Image.setVisibility(View.VISIBLE);
                        Glide.with(AnswerActivity.this).load(programs.getImage()).placeholder(R.drawable.image_placeholder).into(Answer_Image);
                    }
                    if (!programs.getCode().isEmpty() && !programs.getCode().equals("\n")){
                        Answer_Code.setVisibility(View.VISIBLE);
                        Answer_Image.setVisibility(View.GONE);
                        Answer_Code
                                .setOnHighlightListener(AnswerActivity.this)
                                .setTheme(Theme.AGATE)
                                .setCode(programs.getCode())
                                .setLanguage(Language.AUTO)
                                .setWrapLine(true)
                                .setFontSize(14)
                                .setZoomEnabled(false)
                                .setShowLineNumber(true)
                                .setStartLineNumber(1)
                                .apply();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }



    }

    private void readAnswers() {

        mAnswersDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                answersList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Answers answers = snapshot.getValue(Answers.class);
                    if (answers.getQuestion_id().equals(Question_Id)) {
                        answersList.add(answers);
                    }
                }
                answerAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
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

    private void isAnswered(final String id, final Button button) {

        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        mAnswerCheckDatabase.child(id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(firebaseUser.getUid()).equals(false)) {
                    button.setVisibility(View.GONE);
                    button.setTag("answered");
                } else {
                    button.setVisibility(View.VISIBLE);
                    button.setTag("answer");
                }
                Toast.makeText(AnswerActivity.this, button.getTag().toString(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
