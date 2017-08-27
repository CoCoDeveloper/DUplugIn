package com.cocodev.university.delhi.duplugin.EH;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.cocodev.university.delhi.duplugin.R;
import com.cocodev.university.delhi.duplugin.SA;
import com.cocodev.university.delhi.duplugin.Utility.Event;
import com.cocodev.university.delhi.duplugin.Utility.RefListAdapterQuery;
import com.cocodev.university.delhi.duplugin.events_details;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;
import com.squareup.picasso.Picasso;

import static com.cocodev.university.delhi.duplugin.Utility.Utility.formatDate;
import static com.cocodev.university.delhi.duplugin.Utility.Utility.getTimeAgo;

/**
 * A simple {@link Fragment} subclass.
 */
public class EventsHolder extends Fragment {

    public static String key = "type";
    public final static String TYPE_HOME = "All";
    private final String LAST_SCROLL_STATE = "lastScrollState";
    private String typeString;
    private ListView mListView;
    private View view;
    private FirebaseDatabase firebaseDatabase;
    private Query databaseReference;
    private RefListAdapterQuery mAdapter;

    public EventsHolder() {
        // Required empty public constructor
    }
    public static EventsHolder newInstance(String type){
        EventsHolder e = new EventsHolder();
        e.setTypeString(type);
        return  e;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState!=null){
            typeString = savedInstanceState.getString(key);
        }
        firebaseDatabase = FirebaseDatabase.getInstance();
        if(typeString!=TYPE_HOME) {
            databaseReference = firebaseDatabase.getReference()
                    .child("Categories").child("Events").child(getTypeString());
        }else{
            databaseReference = firebaseDatabase.getReference()
                    .child("Events")
            .orderByChild("date")
            .startAt(System.currentTimeMillis());
        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view= inflater.inflate(R.layout.fragment_events_holder, container, false);

        if(typeString!=TYPE_HOME) {
            DatabaseReference temp =   FirebaseDatabase.getInstance().getReference().child("College Content")
                    .child(PreferenceManager.getDefaultSharedPreferences(getContext()).getString(SA.KEY_COLLEGE,""))
                    .child("Categories")
                    .child("Events")
                    .child(getTypeString());
            temp.keepSynced(true);
            mAdapter = new RefListAdapterQuery<String>(
                    getActivity(),
                    String.class,
                    R.layout.events_adapter,
                    new Query[]{databaseReference
                    , temp}
            ) {
                @Override
                public void setChildEventListener() {
                    this.childEventListener = new ChildEventListener() {

                        @Override
                        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
//                    if(!lastArticle.equals(dataSnapshot.getKey())) {
//                    lastArticle = dataSnapshot.getKey();
                            final String t = dataSnapshot.getValue(String.class);
                            DatabaseReference dbref = firebaseDatabase.getReference().child("Events")
                                    .child(t);
                            DatabaseReference dbref2 = firebaseDatabase.getReference().child("College Content")
                                    .child(PreferenceManager.getDefaultSharedPreferences(getContext()).getString(SA.KEY_COLLEGE,""))
                                    .child("Events")
                                    .child(t);
                            ValueEventListener valueEventListener = new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    Event event = dataSnapshot.getValue(Event.class);
                                    if(event == null)
                                        return;
                                    if(event.getDate()<System.currentTimeMillis())
                                        return;
                                    insert(t,0);

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    Log.e("TAG", "onCancelled --> addValueEventListener --> populateView" + databaseError.toString());
                                }
                            };
                            dbref.addListenerForSingleValueEvent(valueEventListener);
                            dbref2.addListenerForSingleValueEvent(valueEventListener);

                            //}
                        }

                        @Override
                        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                            final String t = dataSnapshot.getValue(String.class);
                            final int position = getPosition(t);
                            remove(t);
                            DatabaseReference dbref = firebaseDatabase.getReference().child("Events")
                                    .child(t);
                            DatabaseReference dbref2 = firebaseDatabase.getReference().child("College Content")
                                    .child(PreferenceManager.getDefaultSharedPreferences(getContext()).getString(SA.KEY_COLLEGE,null))
                                    .child("Events")
                                    .child(t);
                            ValueEventListener valueEventListener = new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    Event event = dataSnapshot.getValue(Event.class);
                                    if(event == null)
                                        return;
                                    if(event.getDate()<System.currentTimeMillis())
                                        return;
                                    insert(t,0);

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    Log.e("TAG", "onCancelled --> addValueEventListener --> populateView" + databaseError.toString());
                                }
                            };

                            dbref.addListenerForSingleValueEvent(valueEventListener);
                            dbref2.addListenerForSingleValueEvent(valueEventListener);

                            notifyDataSetChanged();
                        }

                        @Override
                        public void onChildRemoved(DataSnapshot dataSnapshot) {
                            String t = dataSnapshot.getValue(String.class);
                            remove(t);
                            notifyDataSetChanged();
                        }

                        @Override
                        public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                            //do Nothing
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            //error in connection
                        }

                    };
                }
                @Override
                protected void populateView(View v, String model, int position) {
                    final TextView title = (TextView) v.findViewById(R.id.event_title);
                    final TextView venue = (TextView) v.findViewById(R.id.event_venue);
                    final TextView time  = (TextView) v.findViewById(R.id.event_time);
                    final TextView UID  = (TextView) v.findViewById(R.id.event_UID);
                    final ImageView imageView = (ImageView) v.findViewById(R.id.event_image);
                    final ImageButton imageButton = (ImageButton) v.findViewById(R.id.event_shareButton);


                    DatabaseReference dbref = firebaseDatabase.getReference().child("Events")
                            .child(model);
                    DatabaseReference dbref2 = firebaseDatabase.getReference()
                            .child("College Content")
                            .child(PreferenceManager.getDefaultSharedPreferences(getContext()).getString(SA.KEY_COLLEGE,""))
                            .child("Events")
                            .child(model);

                    dbref2.keepSynced(true);
                    ValueEventListener valueEventListener = new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            final Event event = dataSnapshot.getValue(Event.class);
                            if(event==null)
                                return;
                            time.setText(getTimeAgo(getContext(),event.getDate()));
                            venue.setText(event.getVenue());
                            title.setText(event.getTitle());
                            UID.setText(event.getUID());
                            imageButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    Uri uri = Uri.parse("https://duPlugin.com/").buildUpon()
                                            .appendQueryParameter(getString(R.string.DYNAMIC_LINK_TYPE),"events")
                                            .appendQueryParameter(getString(R.string.DYNAMIC_LINK_UID),event.getUID()).build();
                                    Task<ShortDynamicLink> shortLinkTask = FirebaseDynamicLinks.getInstance().createDynamicLink()
                                            .setLink(uri)
                                            .setDynamicLinkDomain("kys79.app.goo.gl")
                                            .setAndroidParameters(new DynamicLink.AndroidParameters.Builder().build())
                                            .setSocialMetaTagParameters(new DynamicLink.SocialMetaTagParameters.Builder()
                                                    .setTitle(event.getTitle())
                                                    .setImageUrl(Uri.parse(event.getUrl()))
                                                    .setDescription(formatDate(event.getTime())+"\n"+fromHtml(event.getVenue()))
                                                    .build())
                                            .buildShortDynamicLink()
                                            .addOnCompleteListener(getActivity(), new OnCompleteListener<ShortDynamicLink>() {
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
                                }
                            });
                            if(!event.getUrl().equals("")) {
                                Picasso.with(getContext()).load(event.getUrl()).placeholder(R.drawable.event_place_holder)
                                        .fit().centerCrop().into(imageView);
                            }


                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.e("TAG", "onCancelled --> addValueEventListener --> populateView" + databaseError.toString());
                        }
                    };
                    dbref.addListenerForSingleValueEvent(valueEventListener);
                    dbref2.addValueEventListener(valueEventListener);
                }


            };


        }else{
            mAdapter = new RefListAdapterQuery<Event>(
                    getActivity(),
                    Event.class,
                    R.layout.events_adapter,
                    new Query[]{databaseReference}
            ) {


                @Override
                protected void populateView(View v,final  Event event, int position) {
                    TextView title = (TextView) v.findViewById(R.id.event_title);
                    TextView venue = (TextView) v.findViewById(R.id.event_venue);
                    TextView time = (TextView) v.findViewById(R.id.event_time);
                    TextView UID = (TextView) v.findViewById(R.id.event_UID);
                    ImageView imageView = (ImageView) v.findViewById(R.id.event_image);
                    time.setText(getTimeAgo(getContext(),event.getDate()));
                    venue.setText(fromHtml(event.getVenue()));
                    title.setText(fromHtml(event.getTitle()));
                    UID.setText(event.getUID());

                    final ImageButton imageButton = (ImageButton) v.findViewById(R.id.event_shareButton);
                    imageButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            Uri uri = Uri.parse("https://duPlugin.com/").buildUpon()
                                    .appendQueryParameter(getString(R.string.DYNAMIC_LINK_TYPE),"events")
                                    .appendQueryParameter(getString(R.string.DYNAMIC_LINK_UID),event.getUID()).build();
                            Task<ShortDynamicLink> shortLinkTask = FirebaseDynamicLinks.getInstance().createDynamicLink()
                                    .setLink(uri)
                                    .setDynamicLinkDomain("kys79.app.goo.gl")
                                    .setAndroidParameters(new DynamicLink.AndroidParameters.Builder().build())
                                    .setSocialMetaTagParameters(new DynamicLink.SocialMetaTagParameters.Builder()
                                            .setTitle(event.getTitle())
                                            .setImageUrl(Uri.parse(event.getUrl()))
                                            .setDescription(formatDate(event.getTime())+"\n"+fromHtml(event.getVenue()))
                                            .build())
                                    .buildShortDynamicLink()
                                    .addOnCompleteListener(getActivity(), new OnCompleteListener<ShortDynamicLink>() {
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
                        }
                    });

                    if (!event.getUrl().equals("")){
                        Picasso.with(getContext()).load(event.getUrl()).placeholder(R.drawable.event_place_holder)
                                .fit().centerCrop().into(imageView);
                    }

                }

            };
        }

        mListView = (ListView) view.findViewById(R.id.eventsHolder_listView);

        TextView textView = (TextView) view.findViewById(R.id.eventsHolder_emptyView);
        textView.setText("There are currently no Events under this Category.");
        mListView.setEmptyView(textView);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(onItemClickListener);
        return view;
    }


    @Override
    public void onResume() {
        super.onResume();
        Log.e("tag1",Integer.toString(mListView.getCount()));

    }

    AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            String UID = (String) ((TextView) view.findViewById(R.id.event_UID)).getText();
            Intent intent = new Intent(getContext(),events_details.class);
            intent.putExtra("uid",UID);
            Pair<View,String> pair1 = Pair.create(view.findViewById(R.id.event_image),getString(R.string.event_share_image));
            ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(
                    getActivity(),
                    pair1
            );
            startActivity(intent,optionsCompat.toBundle());
        }
    };

    public void setTypeString(String typeString) {
        this.typeString = typeString;
    }

    public String getTypeString(){
        return typeString;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(key,typeString);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mListView = null;
        mAdapter.removeListener();
        databaseReference =null;
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
