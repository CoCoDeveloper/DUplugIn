package com.cocodev.university.delhi.duplugin.Utility;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.cocodev.university.delhi.duplugin.R;
import com.github.chrisbanes.photoview.OnPhotoTapListener;
import com.github.chrisbanes.photoview.PhotoView;
import com.squareup.picasso.Picasso;

/**
 * A simple {@link Fragment} subclass.
 */
public class BlankFragment extends Fragment {
    PhotoTapListener photoTapListener;
    private PhotoView photoView;
    private String imageUrl;
    private View mView;
    public BlankFragment() {
        // Required empty public constructor
    }

    public static BlankFragment newInstance(String Url){
        BlankFragment temp = new BlankFragment();
        temp.setImageUrl(Url);
        return temp;
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        photoTapListener=(PhotoTapListener) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        photoTapListener=null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        mView =  inflater.inflate(R.layout.fragment_blank, container, false);
        if(imageUrl!=null){
            photoView= (PhotoView) mView.findViewById(R.id.photoView);
            Picasso.with(getContext()).load(getImageUrl()).fit().centerInside().into(photoView);
            photoView.setOnPhotoTapListener(new OnPhotoTapListener() {
                @Override
                public void onPhotoTap(ImageView view, float x, float y) {
                    photoTapListener.onPhotoTap(view,x,y);
                }
            });
        }
        return mView;
    }

    public void setPhoto(String url){
        photoView= (PhotoView) mView.findViewById(R.id.photoView);
        Picasso.with(getContext()).load(url).fit().centerInside().into(photoView);
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public interface PhotoTapListener {
        public void onPhotoTap(ImageView view, float x, float y);
    }
}
