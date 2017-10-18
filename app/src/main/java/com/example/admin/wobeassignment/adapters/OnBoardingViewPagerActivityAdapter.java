package com.example.admin.wobeassignment.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.example.admin.wobeassignment.fragments.HelperOneFragment;
import com.example.admin.wobeassignment.fragments.HelperTwoFragment;
import com.example.admin.wobeassignment.fragments.HelperThreeFragment;


/*
   Adapter to set the correct fragment on swiping the view pager.
*/
public class OnBoardingViewPagerActivityAdapter extends FragmentPagerAdapter {
    public static final int PAGE_COUNT = 3;
    private final int FRAGMENT_HELPER_ONE = 0;
    private final int FRAGMENT_HELPER_TWO = FRAGMENT_HELPER_ONE + 1;
    private final int FRAGMENT_HELPER_THREE = FRAGMENT_HELPER_TWO + 1;

    public OnBoardingViewPagerActivityAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case FRAGMENT_HELPER_ONE:
                return new HelperOneFragment();
            case FRAGMENT_HELPER_TWO:
                return new HelperTwoFragment();
            case FRAGMENT_HELPER_THREE:
                return new HelperThreeFragment();
            default:
                return new HelperOneFragment();
        }
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }
}
