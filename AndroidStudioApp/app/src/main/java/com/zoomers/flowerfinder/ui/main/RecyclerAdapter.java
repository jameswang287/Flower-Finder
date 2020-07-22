package com.zoomers.flowerfinder.ui.main;

import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.zoomers.flowerfinder.ui.main.PictureContent.PictureItem;

import com.zoomers.flowerfinder.R;

import java.util.List;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {

    private final List<PictureItem> mValues;
    private OnPicListener mOnPicListener;

    public RecyclerAdapter(List<PictureItem> items, OnPicListener onPicListener) {
        this.mValues = items;
        this.mOnPicListener = onPicListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.history_item, parent, false);
        return new ViewHolder(view, mOnPicListener);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.mItem = mValues.get(position);
        holder.mImageView.setImageURI(mValues.get(position).uri);
        // TODO: set different text views for detection result and lat, lng
        holder.mDateView.setText(mValues.get(position).result);

//        holder.mView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mListener.onHistoryItemClick(position);
//                Log.d("Recycler onClick", "here");
//            }
//        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public interface OnPicListener {
        void onPicClick(int position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final View mView;
        public final ImageView mImageView;
        public final TextView mDateView;
        public PictureItem mItem;
        OnPicListener onPicListener;

        public ViewHolder(View view, OnPicListener onPicListener) {
            super(view);
            mView = view;
            mImageView = view.findViewById(R.id.item_image_view);
            mDateView = view.findViewById(R.id.item_result);
            view.setOnClickListener(this);
            this.onPicListener = onPicListener;
        }

        @Override
        public void onClick(View view) {
            onPicListener.onPicClick(getAdapterPosition());
        }
    }
}