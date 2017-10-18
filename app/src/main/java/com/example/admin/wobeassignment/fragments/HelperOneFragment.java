package com.example.admin.wobeassignment.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.admin.wobeassignment.R;

/**
 * Created by Admin on 19-09-2017.
 */

public class HelperOneFragment extends Fragment {

    ViewPager viewPager;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_helper_one,
                container, false);
        initialiseViews(rootView);
        return rootView;
    }

    private void initialiseViews(View view){
        viewPager = (ViewPager) view.findViewById(R.id.pager);
    }

}