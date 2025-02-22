package com.pchmn.materialchips.views;


import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.os.Build;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AlphaAnimation;
import android.widget.Filter;
import android.widget.RelativeLayout;

import com.pchmn.materialchips.ChipsInput;
import com.pchmn.materialchips.R;
import com.pchmn.materialchips.adapter.FilterableAdapter;
import com.pchmn.materialchips.model.ChipInterface;
import com.pchmn.materialchips.util.ViewUtil;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener;

import java.util.Comparator;
import java.util.List;

public class FilterableListView extends RelativeLayout {

    private static final String TAG = FilterableListView.class.toString();
    private Context mContext;
    // list
    RecyclerView mRecyclerView;
    private FilterableAdapter mAdapter;
    private List<? extends ChipInterface> mFilterableList;
    // others
    private ChipsInput mChipsInput;

    public FilterableListView(Context context) {
        super(context);
        mContext = context;
        init();
    }

    private void init() {
        // inflate layout
        View view = inflate(getContext(), R.layout.list_filterable_view, this);
        mRecyclerView = view.findViewById(R.id.recycler_view);

        // recycler
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));

        // hide on first
        setVisibility(GONE);

        keyboardListeners();
    }

    public void build(List<? extends ChipInterface> filterableList, ChipsInput chipsInput, ColorStateList backgroundColor, ColorStateList textColor, Comparator<ChipInterface> comparator) {
        mFilterableList = filterableList;
        mChipsInput = chipsInput;

        // adapter
        mAdapter = new FilterableAdapter(mContext, mRecyclerView, filterableList, chipsInput, backgroundColor, textColor, comparator);
        mRecyclerView.setAdapter(mAdapter);
        if(backgroundColor != null)
            mRecyclerView.getBackground().setColorFilter(backgroundColor.getDefaultColor(), PorterDuff.Mode.SRC_ATOP);

        // listen to change in the tree
        mChipsInput.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {

                // position
                ViewGroup rootView = (ViewGroup) mChipsInput.getRootView();

                // size
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                        ViewUtil.getWindowWidth(mContext),
                        ViewGroup.LayoutParams.MATCH_PARENT);

                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);

                if(mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
                    layoutParams.bottomMargin = ViewUtil.getNavBarHeight(mContext);
                }


                // add view
                rootView.addView(FilterableListView.this, layoutParams);

                // remove the listener:
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                    mChipsInput.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                } else {
                    mChipsInput.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            }

        });
    }

    public void filterList(CharSequence text) {
        mAdapter.getFilter().filter(text, new Filter.FilterListener() {
            @Override
            public void onFilterComplete(int count) {
                // show if there are results
                if(mAdapter.getItemCount() > 0)
                    fadeIn();
                else
                    fadeOut();
            }
        });
    }

    private void keyboardListeners() {
        KeyboardVisibilityEvent.setEventListener(
                (Activity) (getContext()),
                new KeyboardVisibilityEventListener() {
                    @Override
                    public void onVisibilityChanged(boolean isOpen) {
                        ViewGroup.MarginLayoutParams layoutParams = (MarginLayoutParams) getLayoutParams();
                        if (layoutParams == null)
                            return;

                        final View rootView = getRootView();
                        Rect r = new Rect();
                        rootView.getWindowVisibleDisplayFrame(r);

                        // visible height
                        layoutParams.bottomMargin = rootView.getHeight() - r.bottom;

                        setLayoutParams(layoutParams);
                    }
                });
    }

    /**
     * Fade in
     */
    public void fadeIn() {
        if(getVisibility() == VISIBLE)
            return;

        // get visible window (keyboard shown)
        final View rootView = getRootView();
        Rect r = new Rect();
        rootView.getWindowVisibleDisplayFrame(r);

        int[] coord = new int[2];
        mChipsInput.getLocationInWindow(coord);
        ViewGroup.MarginLayoutParams layoutParams = (MarginLayoutParams) getLayoutParams();
        layoutParams.topMargin = coord[1] + mChipsInput.getHeight();
        // height of the keyboard
        layoutParams.bottomMargin = rootView.getHeight() - r.bottom;
        setLayoutParams(layoutParams);

        AlphaAnimation anim = new AlphaAnimation(0.0f, 1.0f);
        anim.setDuration(200);
        startAnimation(anim);
        setVisibility(VISIBLE);
    }

    /**
     * Fade out
     */
    public void fadeOut() {
        if(getVisibility() == GONE)
            return;

        AlphaAnimation anim = new AlphaAnimation(1.0f, 0.0f);
        anim.setDuration(200);
        startAnimation(anim);
        setVisibility(GONE);
    }
}
