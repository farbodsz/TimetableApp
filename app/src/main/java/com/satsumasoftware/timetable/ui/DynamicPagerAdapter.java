package com.satsumasoftware.timetable.ui;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Locale;

public class DynamicPagerAdapter extends PagerAdapter {

    private ArrayList<View> mViews = new ArrayList<>();
    private ArrayList<String> mTitles = new ArrayList<>();

    @Override
    public int getCount() {
        return mViews.size();
    }

    @Override
    public int getItemPosition(Object object) {
        int index = mViews.indexOf(object);
        if (index == -1) {
            return POSITION_NONE;
        } else {
            return index;
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        Locale l = Locale.getDefault();
        return mTitles.get(position).toUpperCase(l);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View view = mViews.get(position);
        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView(mViews.get(position));
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    public int addView(View view) {
        return addView(view, mViews.size());
    }

    public int addViewWithTitle(View view, String title) {
        return addViewWithTitle(view, mViews.size(), title);
    }

    public int addView(View view, int position) {
        return addViewWithTitle(view, position, "");
    }

    public int addViewWithTitle(View view, int position, String title) {
        mViews.add(position, view);
        mTitles.add(title);
        notifyDataSetChanged();
        return position;
    }

    public int removeView(ViewPager pager, View view) {
        return removeView(pager, mViews.indexOf(view));
    }

    public int removeView(ViewPager pager, int position) {
        pager.setAdapter(null);
        mViews.remove(position);
        pager.setAdapter(this);
        return position;
    }

    public View getView(int position) {
        return mViews.get(position);
    }

    public ArrayList<View> getAllViews() {
        return mViews;
    }

}
