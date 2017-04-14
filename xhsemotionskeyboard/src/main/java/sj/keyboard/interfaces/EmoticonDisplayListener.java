package sj.keyboard.interfaces;

import android.view.ViewGroup;

import sj.keyboard.adapter.EmoticonsAdapter;

public interface EmoticonDisplayListener<T> {

    void onBindView(int position, ViewGroup parent, EmoticonsAdapter.ViewHolder viewHolder, T t, boolean isDelBtn);

}
