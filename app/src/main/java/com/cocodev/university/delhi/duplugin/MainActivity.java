package com.cocodev.university.delhi.duplugin;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.widget.Toast;

import com.cocodev.university.delhi.duplugin.EH.EventsHolder;
import com.cocodev.university.delhi.duplugin.Utility.Article;
import com.cocodev.university.delhi.duplugin.Utility.Event;
import com.cocodev.university.delhi.duplugin.Utility.Notice;
import com.cocodev.university.delhi.duplugin.Utility.Utility;
import com.cocodev.university.delhi.duplugin.articles.ArticleHolder;
import com.cocodev.university.delhi.duplugin.notices.Notices;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.Target;
import com.google.android.gms.appinvite.AppInvite;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, GoogleApiClient.OnConnectionFailedListener {
    private String LAST_SYNC_ARTICLE= "lastSyncTime";
    private final int REQUEST_CODE_SETTINGS_ACTIVITY = 1001;
    String[] submenus = {"Events","Articles","Notices"};
    private String CURRENT_FRAGMENT = "currentFragment";
    private final int HOME_FRAGMENT =0;
    private final int NOTICE_BOARD_FRAGMENT =1;
    private Menu menu;
    List<Query> queryArticle = new ArrayList<Query>();
    List<Query> queryEvent = new ArrayList<Query>();
    List<Query> queryNotices = new ArrayList<Query>();
    Toolbar toolbar;
    public static final String TAG = "check";
    DatabaseReference databaseReference;

    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Normal app init code...



        checkDynamicLink();
        setContentView(R.layout.activity_main);


        setNotifications();

         toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        showToolbarShowcaseView();
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        final NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        menu = navigationView.getMenu();
        for (final String subMenu:submenus
             ) {

            final SubMenu  sM = menu.addSubMenu(subMenu);

            databaseReference = FirebaseDatabase.getInstance().getReference().child("CategoryList").child(subMenu);
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(final DataSnapshot dataSnapshot) {
                    Iterator<DataSnapshot> iterator = dataSnapshot.getChildren().iterator();
                    while(iterator.hasNext()){
                        final String SubMenuItemTitle=iterator.next().getKey();
                        MenuItem menuItem = sM.add(SubMenuItemTitle);
                        menuItem.setCheckable(true);
                        menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem menuItem) {

                                if(subMenu.equals("Articles")){

                                    ArticleHolder articleHolder =  ArticleHolder.newInstance(menuItem.getTitle().toString());

                                    getSupportFragmentManager().beginTransaction().replace(
                                            R.id.fragment_layout,
                                            articleHolder,
                                            null).commit();
                                }else
                                if(subMenu.equals("Notices")){
                                    Notices notices =  Notices.newInstance(menuItem.getTitle().toString());
                                    getSupportFragmentManager().beginTransaction().replace(
                                            R.id.fragment_layout,
                                            notices,
                                            null).commit();
                                } else
                                if(subMenu.equals("Events")){
                                    EventsHolder events =  EventsHolder.newInstance(menuItem.getTitle().toString());
                                    getSupportFragmentManager().beginTransaction().replace(
                                            R.id.fragment_layout,
                                            events,
                                            null).commit();
                                }
                                getSupportActionBar().setTitle(menuItem.getTitle().toString()+" "+subMenu);
                                return false;
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }

        if(getSupportFragmentManager().findFragmentById(R.id.fragment_layout)==null){
            getSupportFragmentManager().beginTransaction().replace(
                    R.id.fragment_layout,
                    new Events()
            ).commit();
            navigationView.setCheckedItem(R.id.events);
        }

        if(PreferenceManager.getDefaultSharedPreferences(MainActivity.this).getBoolean(getString(R.string.sp_detailButton),true)){
            Toast.makeText(this,"Click on any item to open details.",Toast.LENGTH_SHORT).show();
            PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit().putBoolean(getString(R.string.sp_detailButton),false).commit();
        }

    }

    private void checkDynamicLink() {
        Intent intent = getIntent();
        String action = intent.getAction();
        Uri data = intent.getData();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this,this)
                .addApi(AppInvite.API)
                .build();


    }

    @Override
    protected void onStart() {
        FirebaseDynamicLinks.getInstance()
                .getDynamicLink(getIntent())
                .addOnSuccessListener(new OnSuccessListener<PendingDynamicLinkData>() {
                    Uri deepLink;
                    @Override
                    public void onSuccess(PendingDynamicLinkData pendingDynamicLinkData) {
                        if(pendingDynamicLinkData!=null){

                            deepLink = pendingDynamicLinkData.getLink();
                            String type = deepLink.getQueryParameter(getString(R.string.DYNAMIC_LINK_TYPE));
                            String uid = deepLink.getQueryParameter(getString(R.string.DYNAMIC_LINK_UID));
                            if(type.equals("article")){
                                Intent intent = new Intent(MainActivity.this,ArticleDetails.class);
                                intent.putExtra(ArticleDetails.key,uid);
                                if(uid!=null) {
                                    startActivity(intent);
                                }
                            }else if(type.equals("events")){
                                Intent intent = new Intent(MainActivity.this,events_details.class);
                                intent.putExtra(events_details.key,uid);
                                if(uid!=null) {
                                    startActivity(intent);
                                }

                            }
                        }
                    }
                });

        super.onStart();
    }

    private void showToolbarShowcaseView() {
        if(PreferenceManager.getDefaultSharedPreferences(MainActivity.this).getBoolean(getString(R.string.sp_menuButton),true)){
            new ShowcaseView.Builder(MainActivity.this)
                    .setTarget(new Target() {
                        @Override
                        public Point getPoint() {
                            int actionBarSize = toolbar.getHeight();
                            int x = actionBarSize / 2;
                            int y = actionBarSize / 2;
                            return new Point(x, y);
                        }
                    })
                    .withHoloShowcase()
                    .setStyle(R.style.TextAppearance_ShowcaseView_Title)
                    .setContentTitle("Press Menu Button To See Articles and Notices.")
                    .hideOnTouchOutside()
                    .build();
            PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit().putBoolean(getString(R.string.sp_menuButton),false).commit();
        }
    }



    private void setNotifications() {

        String lastSyncArticle = getSharedPreferences(this.getLocalClassName(),MODE_PRIVATE).getString(LAST_SYNC_ARTICLE,FirebaseDatabase.getInstance().getReference().push().getKey());

        String college = (PreferenceManager.getDefaultSharedPreferences(MainActivity.this).getString(SA.KEY_COLLEGE,""));


        SharedPreferences sharedPreferences = this.getSharedPreferences(SA.fileName_HP, Context.MODE_PRIVATE);
        if(sharedPreferences!=null){
            Map<String ,?> map = sharedPreferences.getAll();
            for(Map.Entry<String,?>entry:map.entrySet()){
                if(entry.getValue().toString().equals("true")){
                   String category = entry.getKey();
                    DatabaseReference tempDatabaseReference = FirebaseDatabase.getInstance().getReference()
                            .child("Categories")
                            .child("Articles")
                            .child(category);
                    Query qr = tempDatabaseReference
                                .orderByKey()
                            .startAt(lastSyncArticle);
                    Query qr2 = FirebaseDatabase.getInstance().getReference()
                            .child("College Content")
                            .child(college)
                            .child("Categories")
                            .child("Articles")
                            .child(category)
                            .orderByKey()
                            .startAt(lastSyncArticle);
                    qr.addChildEventListener(notificationCELArticle);
                    qr2.addChildEventListener(notificationCELArticle);
                    queryArticle.add(qr2);
                    queryArticle.add(qr);
                }
            }
        }

        SharedPreferences eventPrefrences = this.getSharedPreferences(SA.fileName_EP,Context.MODE_PRIVATE);
        if(sharedPreferences!=null){
            Map<String ,?> map = eventPrefrences.getAll();
            for(Map.Entry<String,?>entry:map.entrySet()){
                if(entry.getValue().toString().equals("true")){
                    String category = entry.getKey();
                    DatabaseReference tempDatabaseReference = FirebaseDatabase.getInstance().getReference()
                            .child("Categories")
                            .child("Events")
                            .child(category);
                    Query qr = tempDatabaseReference.orderByKey().startAt(lastSyncArticle);
                    Query qr2 = FirebaseDatabase.getInstance().getReference()
                            .child("College Content")
                            .child(college)
                            .child("Categories")
                            .child("Events")
                            .child(category)
                            .orderByKey()
                            .startAt(lastSyncArticle);

                    qr2.addChildEventListener(notificationCELEvent);
                    qr.addChildEventListener(notificationCELEvent);
                    queryEvent.add(qr2);
                    queryEvent.add(qr);
                }
            }
        }

        Query qr = FirebaseDatabase.getInstance().getReference()
                .child("Notices")
                .orderByKey()
                .startAt(lastSyncArticle);
        qr.addChildEventListener(notificationCELNotice);
        queryNotices.add(qr);
        qr= FirebaseDatabase.getInstance().getReference()
                .child("College Content")
                .child(college)
                .child("Notices")
                .orderByKey()
                .startAt(lastSyncArticle);
        qr.addChildEventListener(notificationCELNotice);
        queryNotices.add(qr);

    }

    ChildEventListener notificationCELNotice = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            if(!PreferenceManager.getDefaultSharedPreferences(MainActivity.this).getBoolean(SA.KEY_NOTIFY,true))
                return;
            Notice notice = dataSnapshot.getValue(Notice.class);
            if(notice==null){
                return;
            }
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(MainActivity.this);
            mBuilder.setSmallIcon(R.mipmap.ic_launcher);
            mBuilder.setContentTitle(Html.fromHtml(notice.getDescription()));
            mBuilder.setContentInfo(Utility.getTimeAgo(getApplicationContext(),notice.getDeadline()));
            mBuilder.setAutoCancel(true);
            mBuilder.setTicker("DUplugIn "+Html.fromHtml(notice.getDescription()));
            mBuilder.setDefaults(Notification.DEFAULT_ALL);
            Intent resultIntent = new Intent(MainActivity.this, NoticeDetails.class);
            resultIntent.putExtra(NoticeDetails.key,notice.getUid());
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(MainActivity.this);
            stackBuilder.addParentStack(NoticeDetails.class);

            // Adds the Intent that starts the Activity to the top of the stack
            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);
            mBuilder.setContentIntent(resultPendingIntent);
            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            // notificationID allows you to update the notification later on.
            mNotificationManager.notify(notice.getUid(),0, mBuilder.build());


        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
        }
    };
    ChildEventListener notificationCELEvent = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            String Uid = dataSnapshot.getKey();

            if(!PreferenceManager.getDefaultSharedPreferences(MainActivity.this).getBoolean(SA.KEY_NOTIFY,true)) {
                 return;
            }

            ValueEventListener valueEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Event event = dataSnapshot.getValue(Event.class);
                    if(event==null){
                        return;
                    }
                    NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(MainActivity.this);
                    mBuilder.setSmallIcon(R.mipmap.ic_launcher);
                    mBuilder.setContentTitle(Html.fromHtml(event.getTitle()));
                    mBuilder.setContentText(Html.fromHtml(event.getDescription()));
                    mBuilder.setAutoCancel(true);
                    mBuilder.setTicker("DUplugIn "+Html.fromHtml(event.getTitle()));
                    mBuilder.setDefaults(Notification.DEFAULT_ALL);
                    Intent resultIntent = new Intent(MainActivity.this, events_details.class);
                    resultIntent.putExtra(events_details.key,event.getUID());
                    TaskStackBuilder stackBuilder = TaskStackBuilder.create(MainActivity.this);
                    stackBuilder.addParentStack(events_details.class);

                    // Adds the Intent that starts the Activity to the top of the stack
                    stackBuilder.addNextIntent(resultIntent);
                    PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);
                    mBuilder.setContentIntent(resultPendingIntent);
                    NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                    // notificationID allows you to update the notification later on.
                    mNotificationManager.notify(event.getUID(),0, mBuilder.build());
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };

            FirebaseDatabase.getInstance().getReference().child("Events").child(Uid).addListenerForSingleValueEvent(valueEventListener);
            FirebaseDatabase.getInstance().getReference()
                    .child("College Content")
                    .child(PreferenceManager.getDefaultSharedPreferences(MainActivity.this).getString(SA.KEY_COLLEGE,""))
                    .child("Events")
                    .child(Uid)
                    .addListenerForSingleValueEvent(valueEventListener)
            ;
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
        }
    };
    ChildEventListener notificationCELArticle = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            String Uid = dataSnapshot.getKey();
            if(!PreferenceManager.getDefaultSharedPreferences(MainActivity.this).getBoolean(SA.KEY_NOTIFY,true))
                return;

            ValueEventListener valueEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Article article = dataSnapshot.getValue(Article.class);
                    if(article==null){
                        return;
                    }
                    NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(MainActivity.this);
                    mBuilder.setSmallIcon(R.mipmap.ic_launcher);
                    mBuilder.setContentTitle(Html.fromHtml(article.getTitle()));
                    mBuilder.setContentText(Html.fromHtml(article.getDescription()));
                    mBuilder.setAutoCancel(true);
                    mBuilder.setTicker("DUplugIn "+ Html.fromHtml(article.getTitle()));
                    mBuilder.setDefaults(Notification.DEFAULT_ALL);
                    Intent resultIntent = new Intent(MainActivity.this, ArticleDetails.class);
                    resultIntent.putExtra(ArticleDetails.key,article.getUID());
                    TaskStackBuilder stackBuilder = TaskStackBuilder.create(MainActivity.this);
                    stackBuilder.addParentStack(ArticleDetails.class);

                    // Adds the Intent that starts the Activity to the top of the stack
                    stackBuilder.addNextIntent(resultIntent);
                    PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);
                    mBuilder.setContentIntent(resultPendingIntent);
                    NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                    // notificationID allows you to update the notification later on.
                    mNotificationManager.notify(article.getUID(),0, mBuilder.build());
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };
            FirebaseDatabase.getInstance().getReference().child("Articles").child(Uid).addListenerForSingleValueEvent(valueEventListener);
            FirebaseDatabase.getInstance().getReference()
                    .child("College Content")
                    .child(PreferenceManager.getDefaultSharedPreferences(MainActivity.this).getString(SA.KEY_COLLEGE,""))
                    .child("Articles")
                    .child(Uid)
                    .addListenerForSingleValueEvent(valueEventListener);
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
        }
    };

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement

        if (id == R.id.action_settings) {
            Intent intent = new Intent(this,SA.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
            return true;
        }

        return true;

    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
         String TAG_UPDATES = "Updates";
         String TAG_NOTICES = "Notices";
         String TAG_EVENTS = "Events";
        if (id == R.id.home) {
            // Handle the camera action
            Home home = new Home();
            Bundle bundle = new Bundle();
            FragmentManager manager = getSupportFragmentManager();

            manager.beginTransaction().replace(R.id.fragment_layout,home,TAG_UPDATES).commit();

        } else if (id == R.id.notices) {
            FragmentManager manager = getSupportFragmentManager();
            NoticeBoard noticeBoard = new NoticeBoard();


            manager.beginTransaction().replace(R.id.fragment_layout, noticeBoard, TAG_NOTICES).commit();

        }else if (id == R.id.events) {
            Events events = new Events();
            FragmentManager manager = getSupportFragmentManager();
            manager.beginTransaction().replace(R.id.fragment_layout, events, TAG_EVENTS).commit();

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onDestroy() {
        setLastSyncArticle();
        for (Query q:queryArticle
                ) {
            q.removeEventListener(notificationCELArticle);
        }
        for (Query q:queryEvent
                ) {
            q.removeEventListener(notificationCELEvent);
        }
        for (Query q:queryNotices
                ) {
            q.removeEventListener(notificationCELNotice);
        }
        super.onDestroy();

    }

    private void setLastSyncArticle() {
        getSharedPreferences(MainActivity.this.getLocalClassName(), Context.MODE_PRIVATE).edit().putString(LAST_SYNC_ARTICLE,databaseReference.push().getKey()).commit();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
