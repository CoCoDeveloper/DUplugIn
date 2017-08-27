package com.cocodev.university.delhi.duplugin;

import android.content.Intent;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cocodev.university.delhi.duplugin.Utility.Article;
import com.cocodev.university.delhi.duplugin.Utility.Comment;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;
import com.squareup.picasso.Picasso;

import static com.cocodev.university.delhi.duplugin.Utility.Utility.getTimeAgo;


public class ArticleDetails extends AppCompatActivity{
    private int preLast = 0;
    private Article article;
    private String articleUid;
    public static final String key = "article";
    private View emptyFooterView;
    boolean check = true;
    ListView mListView;
    View mFooterView;
    private DatabaseReference mCommentRefrence;
    private FirebaseListAdapter<Comment> commentAdapter;
    String title;
    String imageUrl;
    String UID;

    Button toggleShowHideComments;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_share:

                Uri uri = Uri.parse("https://duPlugin.com/").buildUpon()
                        .appendQueryParameter(getString(R.string.DYNAMIC_LINK_TYPE),"article")
                        .appendQueryParameter(getString(R.string.DYNAMIC_LINK_UID),UID).build();
                Task<ShortDynamicLink> shortLinkTask = FirebaseDynamicLinks.getInstance().createDynamicLink()
                        .setLink(uri)
                        .setDynamicLinkDomain("kys79.app.goo.gl")
                        .setAndroidParameters(new DynamicLink.AndroidParameters.Builder().build())
                        .setSocialMetaTagParameters(new DynamicLink.SocialMetaTagParameters.Builder()
                                .setTitle(title)
                                .setImageUrl(Uri.parse(imageUrl))
                                .build())
                        .buildShortDynamicLink()
                        .addOnCompleteListener(ArticleDetails.this, new OnCompleteListener<ShortDynamicLink>() {
                            @Override
                            public void onComplete(@NonNull Task<ShortDynamicLink> task) {
                                if (task.isSuccessful()) {
                                    // Short link created
                                    Uri shortLink = task.getResult().getShortLink();
                                    Uri flowchartLink = task.getResult().getPreviewLink();

                                    Intent shareIntent = new Intent();
                                    shareIntent.setAction(Intent.ACTION_SEND);
                                    shareIntent.setType("text/plain");
                                    shareIntent.putExtra(Intent.EXTRA_TEXT,shortLink.toString());
                                    startActivity(Intent.createChooser(shareIntent,"Choose"));
                                } else {
                                    // Error
                                    // ...
                                }
                            }
                        });
                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        supportRequestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_details);
        final Intent intent = getIntent();
        UID = intent.getStringExtra(key);
        articleUid=UID;
        initActionBar();

        final View headerView = LayoutInflater.from(this).inflate(R.layout.comment_list_header, null);
        mListView = (ListView) findViewById(R.id.articleComment_listView);
        mListView.addHeaderView(headerView);


        final ImageView imageView = (ImageView) findViewById(R.id.articleImage);
        final TextView titleView = (TextView) headerView.findViewById(R.id.noticeDetails_title);
        final TextView timeView = (TextView) headerView.findViewById(R.id.noticeDetails_time);
        final TextView authorView = (TextView) headerView.findViewById(R.id.article_author);
        final TextView descriptionView = (TextView) headerView.findViewById(R.id.notice_details_description);


        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Articles")
                .child(UID);
        DatabaseReference reference2 = FirebaseDatabase.getInstance().getReference()
                .child("College Content")
                .child(PreferenceManager.getDefaultSharedPreferences(this).getString(SA.KEY_COLLEGE,null))
                .child("Articles")
                .child(UID);
        reference2.keepSynced(true);
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                article = dataSnapshot.getValue(Article.class);

                if(article==null) {
                    if(check){
                        check=false;
                    }else{
                        Toast.makeText(ArticleDetails.this, "This Article has been deleted.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                    return;
                }
                title = String.valueOf(fromHtml(article.getTitle()));
                imageUrl=article.getImageUrl();
                final String imageUrl = article.getImageUrl();

                timeView.setText(getTimeAgo(ArticleDetails.this,article.getTime()));
                titleView.setText(fromHtml(article.getTitle()));
                descriptionView. setMovementMethod(LinkMovementMethod.getInstance());
                authorView.setText(fromHtml(article.getAuthor()));
                descriptionView.setText(fromHtml(article.getDescription()));
                Picasso.with(ArticleDetails.this).load(article.getImageUrl()).fit().into(imageView);

//                appCompatImageButton.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//
//

//                    }
//                });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //
            }
        };
        reference.addListenerForSingleValueEvent(valueEventListener);
        reference2.addListenerForSingleValueEvent(valueEventListener);

        mCommentRefrence = FirebaseDatabase.getInstance().getReference().child("comments").child(articleUid);
        commentAdapter = new FirebaseListAdapter<Comment>(
                this,
                Comment.class,
                R.layout.comment_layout,
                mCommentRefrence
        ) {
            @Override
            protected void populateView(View v, Comment model, int position) {
                TextView comment = (TextView) v.findViewById(R.id.commentLayout_comment);
                Button like = (Button) v.findViewById(R.id.commentLayout_like);
                Button dislike = (Button) v.findViewById(R.id.commentLayout_dislike);
                TextView time = (TextView) v.findViewById(R.id.comment_timeStamp);
                comment.setText(model.getComment());
                time.setText(Long.toString(model.getTimestamp()));
            }


        };

        mListView.setAdapter(null);

        final FloatingActionButton writeComment = (FloatingActionButton) headerView.findViewById(R.id.articleDetails_postComment);
        toggleShowHideComments = (Button) headerView.findViewById(R.id.articleDetails_toggleShowHideComments);
        final EditText comment = (EditText) headerView.findViewById(R.id.articleDetails_comment_EditText);
        final Button submitComment = (Button) headerView.findViewById(R.id.articleDetails_submitComment);
        emptyFooterView = LayoutInflater.from(ArticleDetails.this).inflate(R.layout.empty_view,null);
//
//        toggleShowHideComments.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Toast.makeText(ArticleDetails.this,Integer.toString(mListView.getSelectedItemPosition()), Toast.LENGTH_SHORT).show();
//                if(writeComment.getVisibility()==View.GONE) {
//                    writeComment.setVisibility(View.VISIBLE);
//                    toggleShowHideComments.setText("Hide Comments");
//                    mListView.setAdapter(commentAdapter);
//                    if(commentAdapter.getCount()==0){
//                        mListView.addFooterView(emptyFooterView);
//                    }
//
//                }else{
//
//                    comment.setVisibility(View.GONE);
//                    submitComment.setVisibility(View.GONE);
//                    writeComment.setImageDrawable(ContextCompat.getDrawable(ArticleDetails.this,android.R.drawable.ic_menu_edit));
//                    writeComment.setVisibility(View.GONE);
//                    toggleShowHideComments.setText("Show Comments");
//                    mListView.setAdapter(null);
//                    mListView.removeFooterView(emptyFooterView);
//                }
//            }
//        });

        commentAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                if(commentAdapter.getCount()==0){
                   if(writeComment.getVisibility()==View.VISIBLE){
                        mListView.addFooterView(emptyFooterView);
                   }
                }else{
                    mListView.removeFooterView(emptyFooterView);
                }
            }
        });

        writeComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(comment.getVisibility()==View.GONE) {
                    comment.setVisibility(View.VISIBLE);
                    submitComment.setVisibility(View.VISIBLE);
                    writeComment.setImageDrawable(ContextCompat.getDrawable(ArticleDetails.this, android.R.drawable.ic_menu_close_clear_cancel));

                }else{
                    comment.setVisibility(View.GONE);
                    submitComment.setVisibility(View.GONE);
                    writeComment.setImageDrawable(ContextCompat.getDrawable(
                            ArticleDetails.this, android.R.drawable.ic_menu_edit));

                }
            }
        });

        submitComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(comment.getText().equals("")){
                    Toast.makeText(ArticleDetails.this, "You can not submit an empty comment", Toast.LENGTH_SHORT).show();
                }else{
                    String commentUid = mCommentRefrence.push().getKey();
                    Comment temp = new Comment(
                            articleUid,
                            null,
                            null,
                            0,0,
                            System.currentTimeMillis(),
                            comment.getText().toString(),
                            commentUid
                            );
                    mCommentRefrence.child(commentUid).setValue(temp).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(ArticleDetails.this, "Your comment was submitted Successfully", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(ArticleDetails.this, "There is a problem establishing connection to the server.", Toast.LENGTH_SHORT).show();
                        }
                    });
                    comment.setText("");
                    writeComment.callOnClick();
                }

            }
        });


    }

    private void initActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#10000000")));
        actionBar.setStackedBackgroundDrawable(new ColorDrawable(Color.parseColor("#10000000")));
        actionBar.setTitle("");
        actionBar.setDisplayHomeAsUpEnabled(true);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        commentAdapter.cleanup();
    }

    @Override
    public void onBackPressed() {
        if(isTaskRoot()){
            Intent intent = new Intent(this,MainActivity.class);
            startActivity(intent);
        }
        finish();

    }


    @SuppressWarnings("deprecation")
    public static Spanned fromHtml(String html){
        Spanned result;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            result = Html.fromHtml(html,Html.FROM_HTML_MODE_LEGACY);
        } else {
            result = Html.fromHtml(html);
        }
        return result;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.share_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

}
