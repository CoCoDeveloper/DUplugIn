package com.cocodev.university.delhi.duplugin;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.Spanned;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cocodev.university.delhi.duplugin.Utility.ImagePagerAdapter;
import com.cocodev.university.delhi.duplugin.Utility.ImageViewTouchViewPager;
import com.cocodev.university.delhi.duplugin.Utility.Notice;
import com.cocodev.university.delhi.duplugin.Utility.TouchImageView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import static com.cocodev.university.delhi.duplugin.Utility.Utility.getTimeAgo;

public class NoticeDetails extends AppCompatActivity {
    public static final String key = "notice_uid";
    String noticeUid;
    private LinearLayout dotsLayout;
    private ImagePagerAdapter noticeImagePagerAdapter;
    private ImageViewTouchViewPager noticeImagePager;
    private TextView[] dots;
    private int[] resources;
    boolean check=true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notice_details);

        initActionBar();

        Intent intent = getIntent();
        noticeUid = intent.getStringExtra(key);

        resources = new int[]{R.drawable.placeholder,R.drawable.event_place_holder};


        noticeImagePager = (ImageViewTouchViewPager) findViewById(R.id.view_pager);
        dotsLayout = (LinearLayout) findViewById(R.id.layoutDots);
        noticeImagePagerAdapter = new ImagePagerAdapter(getApplicationContext(),resources);
        noticeImagePager.setAdapter(noticeImagePagerAdapter);
        noticeImagePager.setOnPageChangeListener(viewPagerPageChangeListener);
        addBottomDots(0);

        final TextView title = (TextView) findViewById(R.id.notice_details_title);
        final TextView description = (TextView) findViewById(R.id.notice_details_description);
        final TextView time = (TextView) findViewById(R.id.notice_deatails_time);
        final TextView deadline = (TextView) findViewById(R.id.notice_details_deadline);
        final TextView uid = (TextView) findViewById(R.id.notice_uid);
//        final TouchImageView touchImageViewSmall = (TouchImageView) findViewById(R.id.noticeDetails_smallImageView);
//        touchImageViewSmall.setImageResource(R.drawable.event_place_holder);
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Notices")
                .child(noticeUid);
        DatabaseReference reference2 = FirebaseDatabase.getInstance().getReference()
                .child("College Content")
                .child(PreferenceManager.getDefaultSharedPreferences(this).getString(SA.KEY_COLLEGE,null))
                .child("Notices")
                .child(noticeUid);
        reference2.keepSynced(true);

        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Notice notice = dataSnapshot.getValue(Notice.class);
                if(notice==null) {
                    if(check){
                        check=false;
                    }else{
                        Toast.makeText(NoticeDetails.this, "This Notice has been deleted.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                    return;
                }
                title.setText(fromHtml(notice.getTitle()));
                description.setText(fromHtml(notice.getDescription()));
                time.setText(fromHtml(getTimeAgo(NoticeDetails.this,notice.getTime())));
                deadline.setText(getTimeAgo(NoticeDetails.this,notice.getDeadline()));
                if(!notice.getImageUrl().equals("")) {
                    //Picasso.with(NoticeDetails.this).load(notice.getImageUrl()).fit().into(touchImageViewSmall);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //
            }
        };
        reference.addListenerForSingleValueEvent(valueEventListener);
        reference2.addListenerForSingleValueEvent(valueEventListener);

    }
    private void addBottomDots(int currentPage) {
        dots = new TextView[resources.length];

        int[] colorsActive = getResources().getIntArray(R.array.dot_active);
        int[] colorsInactive = getResources().getIntArray(R.array.dot_inactive);

        dotsLayout.removeAllViews();
        for (int i = 0; i < dots.length; i++) {
            dots[i] = new TextView(this);
            dots[i].setText(Html.fromHtml("&#8226;"));
            dots[i].setTextSize(35);
            dots[i].setTextColor(colorsInactive[currentPage]);
            dotsLayout.addView(dots[i]);
        }

        if (dots.length > 0)
            dots[currentPage].setTextColor(colorsActive[currentPage]);
    }


    private void initActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onBackPressed() {
        if(isTaskRoot()){
            Intent intent = new Intent(this,MainActivity.class);
            startActivity(intent);
        }
        finish();

    }

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

    ViewPager.OnPageChangeListener viewPagerPageChangeListener = new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            addBottomDots(position);

            // changing the next button text 'NEXT' / 'GOT IT'
            if (position == resources.length - 1) {
                // last page. make button text to GOT IT

            } else {
                // still pages are left

            }
        }
        @Override
        public void onPageScrollStateChanged(int state) {

        }
        private int getItem(int i) {
            return noticeImagePager.getCurrentItem()+i;
        }
    };



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
