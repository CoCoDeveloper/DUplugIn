package com.cocodev.university.delhi.duplugin.Utility;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.Html;

import com.cocodev.university.delhi.duplugin.ArticleDetails;
import com.cocodev.university.delhi.duplugin.NoticeDetails;
import com.cocodev.university.delhi.duplugin.R;
import com.cocodev.university.delhi.duplugin.SA;
import com.cocodev.university.delhi.duplugin.events_details;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by manav on 8/6/17.
 */

public class NotificationService extends Service {
    private String LAST_SYNC_ARTICLE= "lastSyncTime";
    private List<Query> queryArticle = new ArrayList<Query>();
    private List<Query> queryEvent = new ArrayList<Query>();
    private List<Query> queryNotices = new ArrayList<Query>();
    private String lastSyncArticle;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
         super.onStartCommand(intent, flags, startId);
         lastSyncArticle = intent.getStringExtra("LastSyncArticle");
         setNotifications();
         return START_NOT_STICKY;
    }

    private void setNotifications() {

        String college = (PreferenceManager.getDefaultSharedPreferences(NotificationService.this).getString(SA.KEY_COLLEGE,""));
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

            if(!PreferenceManager.getDefaultSharedPreferences(NotificationService.this).getBoolean(SA.KEY_NOTIFY,true))
                return;
            Notice notice = dataSnapshot.getValue(Notice.class);
            if(notice==null){
                return;
            }
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(NotificationService.this);
            mBuilder.setSmallIcon(R.mipmap.ic_launcher);
            mBuilder.setContentTitle(Html.fromHtml(notice.getDescription()));
            mBuilder.setContentInfo(Utility.getTimeAgo(getApplicationContext(),notice.getDeadline()));
            mBuilder.setAutoCancel(true);
            mBuilder.setTicker("DUplugIn "+Html.fromHtml(notice.getDescription()));
            mBuilder.setDefaults(Notification.DEFAULT_ALL);
            Intent resultIntent = new Intent(NotificationService.this, NoticeDetails.class);
            resultIntent.putExtra(NoticeDetails.key,notice.getUid());
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(NotificationService.this);
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

            if(!PreferenceManager.getDefaultSharedPreferences(NotificationService.this).getBoolean(SA.KEY_NOTIFY,true)) {
                return;
            }

            ValueEventListener valueEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Event event = dataSnapshot.getValue(Event.class);
                    if(event==null){
                        return;
                    }
                    NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(NotificationService.this);
                    mBuilder.setSmallIcon(R.mipmap.ic_launcher);
                    mBuilder.setContentTitle(Html.fromHtml(event.getTitle()));
                    mBuilder.setContentText(Html.fromHtml(event.getDescription()));
                    mBuilder.setAutoCancel(true);
                    mBuilder.setTicker("DUplugIn "+Html.fromHtml(event.getTitle()));
                    mBuilder.setDefaults(Notification.DEFAULT_ALL);
                    Intent resultIntent = new Intent(NotificationService.this, events_details.class);
                    resultIntent.putExtra(events_details.key,event.getUID());
                    TaskStackBuilder stackBuilder = TaskStackBuilder.create(NotificationService.this);
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
                    .child(PreferenceManager.getDefaultSharedPreferences(NotificationService.this).getString(SA.KEY_COLLEGE,""))
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
            if(!PreferenceManager.getDefaultSharedPreferences(NotificationService.this).getBoolean(SA.KEY_NOTIFY,true))
                return;

            ValueEventListener valueEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Article article = dataSnapshot.getValue(Article.class);
                    if(article==null){
                        return;
                    }
                    NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(NotificationService.this);
                    mBuilder.setSmallIcon(R.mipmap.ic_launcher);
                    mBuilder.setContentTitle(Html.fromHtml(article.getTitle()));
                    mBuilder.setContentText(Html.fromHtml(article.getDescription()));
                    mBuilder.setAutoCancel(true);
                    mBuilder.setTicker("DUplugIn "+ Html.fromHtml(article.getTitle()));
                    mBuilder.setDefaults(Notification.DEFAULT_ALL);
                    Intent resultIntent = new Intent(NotificationService.this, ArticleDetails.class);
                    resultIntent.putExtra(ArticleDetails.key,article.getUID());
                    TaskStackBuilder stackBuilder = TaskStackBuilder.create(NotificationService.this);
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
                    .child(PreferenceManager.getDefaultSharedPreferences(NotificationService.this).getString(SA.KEY_COLLEGE,""))
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
    public void onDestroy() {
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
}
