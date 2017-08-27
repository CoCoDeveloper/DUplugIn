package com.cocodev.university.delhi.duplugin.adapter;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.cocodev.university.delhi.duplugin.R;
import com.cocodev.university.delhi.duplugin.Utility.RefListAdapter;
import com.google.firebase.database.DatabaseReference;

/**
 * Created by Sudarshan on 08-06-2017.
 */

/**
 * Generic version of the Box class.
 * @param <T> the type of the value being boxed
 */
public abstract class CustomArticleHolderAdapter<T> extends RefListAdapter<T> {

    private final int COUNT_VIEW_TYPE = 2;
    private final int VIEW_TYPE_FIRST=0;
    private final int VIEW_TYPE_SECOND=1;

    public CustomArticleHolderAdapter(@NonNull Context context, Class<T> modelclass, @LayoutRes int resource, DatabaseReference[] db) {
        super(context, modelclass, resource, db);
    }



    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = newView(getContext(),position,viewGroup);
        }
        T model = getItem(position);

        populateView(view,model,position);

        return view;
    }

    @Override
    abstract protected void populateView(View v, T model, int position);

    public static class ViewHolder {

        public final TextView titleView;
        public final TextView timeView;
        public final TextView authorView;
        public final TextView UID;
        public final ImageView imageView;

        public ViewHolder(View view) {
            titleView = (TextView) view.findViewById(R.id.noticeDetails_title);
            timeView = (TextView) view.findViewById(R.id.noticeDetails_time);
            authorView = (TextView) view.findViewById(R.id.article_author);
            UID = (TextView) view.findViewById(R.id.article_UID);
            imageView = (ImageView) view.findViewById(R.id.articleImage);
        }
    }


    public View newView(Context context, int position, ViewGroup parent) {
        int viewType = getItemViewType(position);
        int layoutID = -1;
        if(viewType==VIEW_TYPE_FIRST){
            layoutID = R.layout.article_list_view_first;
        }else{
            layoutID = R.layout.article_list_view_second;
        }

        View view = LayoutInflater.from(context).inflate(layoutID,parent,false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);
        return view;
    }


    @Override
    public int getViewTypeCount() {
        return COUNT_VIEW_TYPE;
    }

    @Override
    public int getItemViewType(int position) {
        return  position == 0 ? VIEW_TYPE_FIRST : VIEW_TYPE_SECOND;
    }

}
