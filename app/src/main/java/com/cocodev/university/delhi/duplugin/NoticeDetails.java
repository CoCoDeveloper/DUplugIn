package com.cocodev.university.delhi.duplugin;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.Spanned;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cocodev.university.delhi.duplugin.Utility.BlankFragment;
import com.cocodev.university.delhi.duplugin.Utility.CustomViewPager;
import com.cocodev.university.delhi.duplugin.Utility.Notice;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Iterator;

import static com.cocodev.university.delhi.duplugin.Utility.Utility.getTimeAgo;

public class NoticeDetails extends AppCompatActivity implements BlankFragment.PhotoTapListener{
    public static final String key = "notice_uid";
    String noticeUid;
    private CustomViewPager viewPager;
    private TextView[] dots;
    ArrayList<String> imageList = new ArrayList<String>();
    boolean check=true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notice_details);

        initActionBar();

        Intent intent = getIntent();
        noticeUid = intent.getStringExtra(key);

        viewPager = (CustomViewPager) findViewById(R.id.noticeDetails_viewFlipper);


        final TextView title = (TextView) findViewById(R.id.notice_details_title);
        final TextView description = (TextView) findViewById(R.id.notice_details_description);
        final TextView time = (TextView) findViewById(R.id.notice_deatails_time);
        final TextView deadline = (TextView) findViewById(R.id.notice_details_deadline);
        final TextView uid = (TextView) findViewById(R.id.notice_uid);
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

                imageList.add("https://pbs.twimg.com/profile_images/604644048/sign051.gif");
                imageList.add("https://pbs.twimg.com/profile_images/604644048/sign051.gif");
                Iterator<String> iterator = imageList.iterator();
                final ArrayList<BlankFragment> fragments = new ArrayList<BlankFragment>();
                while(iterator.hasNext()){
                    String photo = iterator.next();
                    BlankFragment temp = BlankFragment.newInstance(photo);
                    fragments.add(temp);
                }
                viewPager.setAdapter(new FragmentStatePagerAdapter(getSupportFragmentManager()) {
                    @Override
                    public Fragment getItem(int position) {
                        return  fragments.get(position);
                    }

                    @Override
                    public int getCount() {
                        return fragments.size();
                    }
                });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //
            }
        };
        reference.addListenerForSingleValueEvent(valueEventListener);
        reference2.addListenerForSingleValueEvent(valueEventListener);

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
    public void onPhotoTap(ImageView view, float x, float y) {


//        Intent intent = new Intent(this,ImagePagerActivty.class);
//        intent.putExtra(ImagePagerActivty.RESOURCE,imageList);
//        intent.putExtra(ImagePagerActivty.POSITION,viewPager.getCurrentItem());
//        startActivity(intent);
    }
}
