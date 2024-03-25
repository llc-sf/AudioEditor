package dev.android.player.widget;

import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 固定几个View 作为RecyclerView的头部 或者尾部
 */
public class FixedViewsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<View> mViews = new ArrayList<>();

    public FixedViewsAdapter(View... views) {
        this.mViews.clear();
        this.mViews.addAll(Arrays.stream(views).filter(Objects::nonNull).collect(Collectors.toList()));
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RecyclerView.ViewHolder(new FrameLayout(parent.getContext())) {
        };
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        View view = mViews.get(position);
        if (view.getParent() != null) {
            ((ViewGroup) view.getParent()).removeView(view);
        }
        if (holder.itemView instanceof ViewGroup) {
            ((ViewGroup) holder.itemView).removeAllViews();
            ((ViewGroup) holder.itemView).addView(view);
        }
    }

    @Override
    public int getItemCount() {
        return mViews.size();
    }
}
