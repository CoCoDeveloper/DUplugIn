package com.cocodev.university.delhi.duplugin;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.Spanned;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cocodev.university.delhi.duplugin.Utility.Event;
import com.google.android.gms.tasks.OnCompleteListener;
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

import static com.cocodev.university.delhi.duplugin.Utility.Utility.formatDate;
import static com.cocodev.university.delhi.duplugin.Utility.Utility.getTimeAgo;

public class events_details extends AppCompatActivity {
    public static String key = "uid";
    private String Uid = "";
    boolean check = true;
    private String uid;
    private String title;
    private String venue;
    private String url;
    private Long time;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        supportRequestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events_details);
        Intent intent = getIntent();
        Uid = intent.getStringExtra("uid");

        initActionBar();

        final ImageView imageView = (ImageView) findViewById(R.id.event_image);
        final TextView titleView = (TextView) findViewById(R.id.event_detail_title);
        final TextView timeView = (TextView) findViewById(R.id.event_detail_time);
        final TextView eventPlace = (TextView) findViewById(R.id.event_detail_place);
        final TextView descriptionView = (TextView) findViewById(R.id.event_detail_desc);


        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Events")
                .child(Uid);
        DatabaseReference reference2 = FirebaseDatabase.getInstance().getReference()
                .child("College Content")
                .child(PreferenceManager.getDefaultSharedPreferences(events_details.this).getString(SA.KEY_COLLEGE,null))
                .child("Events")
                .child(Uid);
        reference2.keepSynced(true);
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Event event = dataSnapshot.getValue(Event.class);
                if(event==null) {
                    if(check){
                        check=false;
                    }else{
                        Toast.makeText(events_details.this, "This Article has been deleted.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                    return;
                }
                time = event.getDate();
                title = fromHtml(event.getTitle()).toString();
                venue = fromHtml(event.getVenue()).toString();
                uid = event.getUID();
                url = event.getUrl();
                timeView.setText(getTimeAgo(events_details.this,event.getDate()));
                titleView.setText(fromHtml(event.getTitle()));
                eventPlace.setText(fromHtml(event.getVenue()));
                descriptionView.setText(fromHtml(event.getDescription()));
                if(!event.getUrl().equals("")) {
                    Picasso.with(events_details.this).load(event.getUrl()).placeholder(R.drawable.event_place_holder).fit().centerInside().into(imageView);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        reference.addListenerForSingleValueEvent(valueEventListener);
        reference2.addListenerForSingleValueEvent(valueEventListener);
    }

    private void initActionBar() {
        ActionBar actionBar = getSupportActionBar();

        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#10000000")));
        actionBar.setStackedBackgroundDrawable(new ColorDrawable(Color.parseColor("#10000000")));

        actionBar.setTitle("");
        actionBar.setDisplayHomeAsUpEnabled(true);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.share_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_share:

                Uri uri = Uri.parse("https://duPlugin.com/").buildUpon()
                        .appendQueryParameter(getString(R.string.DYNAMIC_LINK_TYPE),"events")
                        .appendQueryParameter(getString(R.string.DYNAMIC_LINK_UID),uid).build();
                Task<ShortDynamicLink> shortLinkTask = FirebaseDynamicLinks.getInstance().createDynamicLink()
                        .setLink(uri)
                        .setDynamicLinkDomain("kys79.app.goo.gl")
                        .setAndroidParameters(new DynamicLink.AndroidParameters.Builder().build())
                        .setSocialMetaTagParameters(new DynamicLink.SocialMetaTagParameters.Builder()
                                .setTitle(title)
                                .setImageUrl(Uri.parse(url))
                                .setDescription(formatDate(time)+"\n"+fromHtml(venue))
                                .build())
                        .buildShortDynamicLink()
                        .addOnCompleteListener(events_details.this, new OnCompleteListener<ShortDynamicLink>() {
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
    public void onBackPressed() {
        if(isTaskRoot()){
            Intent intent = new Intent(this,MainActivity.class);
            startActivity(intent);
        }
        super.onBackPressed();
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
}
