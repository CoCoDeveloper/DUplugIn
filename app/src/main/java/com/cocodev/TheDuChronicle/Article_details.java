package com.cocodev.TheDuChronicle;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.cocodev.TheDuChronicle.Utility.Article;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Article_details extends AppCompatActivity {

    private Article article;
    public static final String key = "article";

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        supportRequestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_details);
        Intent intent = getIntent();
        String UID = intent.getStringExtra(key);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        actionBar.setStackedBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        actionBar.setTitle("");
        actionBar.setDisplayHomeAsUpEnabled(true);

        final TextView titleView= (TextView) findViewById(R.id.article_title);;
        final TextView timeView= (TextView) findViewById(R.id.article_time);;
        final TextView authorView= (TextView) findViewById(R.id.article_author);;
        final TextView descriptionView= (TextView) findViewById(R.id.article_description);

        ImageView imageView = (ImageView) findViewById(R.id.articleImage);


        DatabaseReference reference =FirebaseDatabase.getInstance().getReference().child("Articles")
                .child(UID);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                article = dataSnapshot.getValue(Article.class);

                timeView.setText(article.getTime());
                titleView.setText(article.getTitle());
                authorView.setText(article.getAuthor());
                descriptionView.setText(article.getDescription());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //
            }
        });





    }

    @Override
    public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
        return super.onCreateView(parent, name, context, attrs);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
}
}
