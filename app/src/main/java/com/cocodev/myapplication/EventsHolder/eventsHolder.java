package com.cocodev.myapplication.EventsHolder;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.cocodev.myapplication.Article_details;
import com.cocodev.myapplication.R;
import com.cocodev.myapplication.Utility.Article;
import com.cocodev.myapplication.Utility.Event;
import com.cocodev.myapplication.adapter.CustomArticleHolderAdapter;
import com.cocodev.myapplication.articles.ArticleHolder;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * A simple {@link Fragment} subclass.
 */
public class eventsHolder extends Fragment {

    public static String key = "type";
    private int type =-1;
    private final int TYPE_HOME = 0;
    private final int TYPE_COLLEGE = 1;
    private final int TYPE_SPORTS = 2;
    private final int TYPE_DANCE = 3;
    private final int TYPE_MUSIC = 4;
    private final int ARTICLE_LOADER = 1;
    private final String SPORTS = "Sports";
    private final String DANCE = "Dance";
    private final String MUSIC = "Music";
    private final String LAST_SCROLL_STATE = "lastScrollState";
    private ListView mListView;
    private View view;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private FirebaseListAdapter mAdapter;

    public eventsHolder() {
        // Required empty public constructor
    }
    public static eventsHolder newInstance(int type){
        eventsHolder e = new eventsHolder();
        e.setType(type);
        return  e;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState!=null){
            type = savedInstanceState.getInt(key);
        }
        firebaseDatabase = FirebaseDatabase.getInstance();
        if(type!=TYPE_HOME) {
            databaseReference = firebaseDatabase.getReference()
                    .child("Categories").child("Events").child(getTypeString());
        }else{
            databaseReference = firebaseDatabase.getReference()
                    .child("Events");
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view= inflater.inflate(R.layout.fragment_events_holder, container, false);
        if(type!=TYPE_HOME) {
            mAdapter = new FirebaseListAdapter<String>(
                    getActivity(),
                    String.class,
                    R.layout.events_adapter,
                    databaseReference
            ) {

                @Override
                protected void populateView(View v, String model, int position) {
                    final TextView title = (TextView) v.findViewById(R.id.event_title);
                    final TextView venue = (TextView) v.findViewById(R.id.event_venue);
                    final TextView time  = (TextView) v.findViewById(R.id.event_time);
                    final TextView UID  = (TextView) v.findViewById(R.id.event_UID);

                    DatabaseReference dbref = firebaseDatabase.getReference().child("Events")
                            .child(model);
                    dbref.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Event event = dataSnapshot.getValue(Event.class);
                            time.setText(event.getTime());
                            venue.setText(event.getVenue());
                            title.setText(event.getTitle());
                            UID.setText(event.getUID());
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.e("TAG", "onCancelled --> addValueEventListener --> populateView" + databaseError.toString());
                        }
                    });
                }
            };
        }else{
            mAdapter = new FirebaseListAdapter<Event>(
                    getActivity(),
                    Event.class,
                    R.layout.events_adapter,
                    databaseReference
            ) {

                @Override
                protected void populateView(View v, Event event, int position) {
                     TextView title = (TextView) v.findViewById(R.id.event_title);
                     TextView venue = (TextView) v.findViewById(R.id.event_venue);
                     TextView time  = (TextView) v.findViewById(R.id.event_time);
                     TextView UID  = (TextView) v.findViewById(R.id.event_UID);
                    time.setText(event.getTime());
                    venue.setText(event.getVenue());
                    title.setText(event.getTitle());
                    UID.setText(event.getUID());
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

    AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//            String UID = (String) ((TextView) view.findViewById(R.id.article_UID)).getText();
//            Intent intent = new Intent(getContext(),Article_details.class);
//            intent.putExtra(Article_details.key,UID);
//            startActivity(intent);

        }
    };

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getTypeString(){
        switch(type){
            case TYPE_SPORTS:
                return "Sports";
            case TYPE_DANCE:
                return "Dance";
            case TYPE_MUSIC:
                return "Music";
            case TYPE_HOME:
                return "Home";
            case TYPE_COLLEGE:
                return "College";
            default:
                return "Unknown Type";
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(key,type);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mListView = null;
        mAdapter.cleanup();
        databaseReference =null;
    }
}
