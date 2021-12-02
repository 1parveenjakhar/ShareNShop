package com.cometchat.pro.uikit.ui_components.shared.cometchatStickers.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.SpannableStringBuilder;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.List;


public class StickerTabAdapter extends FragmentStatePagerAdapter {
    private Drawable myDrawable;
    private SpannableStringBuilder sb;
    private final List<Fragment> mFragmentList = new ArrayList<>();
    private final List<String> mFragmentTitleList = new ArrayList<>();
    private final List<String> mFragmentIconList = new ArrayList<>();
    private Context context;
    public StickerTabAdapter(Context context,FragmentManager fm) {
        super(fm);
        this.context = context;
    }

    @Override
    public Fragment getItem(int position) {
       return mFragmentList.get(position);

    }
    public void addFragment(Fragment fragment, String title) {
        mFragmentList.add(fragment);
        mFragmentTitleList.add(title);
    }

    public void addFragment(Fragment fragment, String title,String icon) {
        mFragmentList.add(fragment);
        mFragmentTitleList.add(title);
        mFragmentIconList.add(icon);
    }

    public String getPageIcon(int position) { return mFragmentIconList.get(position); }
    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        sb = new SpannableStringBuilder("");
        return sb;
    }
    @Override
    public int getCount() {
        return mFragmentList.size();
    }
}
