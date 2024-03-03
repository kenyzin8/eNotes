package com.example.enotes;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.bitmap.CenterInside;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {
    private Context mContext;
    private List<ImageData> mImageDataList;
    private List<Integer> mImageIds;
    private int loadedImagesCount = 0;
    private OnAllImagesLoadedListener allImagesLoadedListener;
    public static List<Integer> tempImageIds;

    public ImageAdapter(Context context, List<ImageData> imageDataList, List<Integer> imageIds) {
        mContext = context;
        mImageDataList = imageDataList;
        mImageIds = imageIds;
    }

    public interface OnAllImagesLoadedListener {
        void onAllImagesLoaded();
    }

    public void setOnAllImagesLoadedListener(OnAllImagesLoadedListener listener) {
        this.allImagesLoadedListener = listener;
    }

    private void checkAllImagesLoaded() {
        if ((getItemCount() == 0 || loadedImagesCount == getItemCount()) && allImagesLoadedListener != null) {
            allImagesLoadedListener.onAllImagesLoaded();
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;
        public ProgressBar progressBar;

        public ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            progressBar = itemView.findViewById(R.id.imageLoadingProgress);
        }
    }

    @NonNull
    @Override
    public ImageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.image_item, parent, false);

        return new ViewHolder(view);
    }

    public static byte[] compressImage(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, stream); // Compress quality set to 50%
        return stream.toByteArray();
    }

    public void insertImage(ImageData imageData, int imageId) {
        mImageDataList.add(0, imageData);
        mImageIds.add(0, imageId);
        notifyItemInserted(0);
        notifyItemRangeChanged(0, mImageDataList.size());
    }

    public void removeImage(int imageId) {
        int position = mImageIds.indexOf(imageId);

        if (position != -1) {
            mImageDataList.remove(position);
            mImageIds.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, mImageDataList.size());

            String pluralHandler = mImageDataList.size() <= 1 ? " Picture" : "Pictures";
            SubjectViewActivity.tvSubjectPictures.setText(mImageDataList.size() + " " + pluralHandler);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ImageAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        int imageId = mImageIds.get(position);
        holder.imageView.setTag(imageId);
        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int imageId = (int) view.getTag();
                Intent intent = new Intent(mContext, ImageViewActivity.class);
                intent.putExtra("imageId", imageId);
                intent.putExtra("subjectId", SubjectViewActivity.subjectID);
                intent.putExtra("imagePosition", position);
                mContext.startActivity(intent);
            }
        });
        holder.progressBar.setVisibility(View.VISIBLE);
        Bitmap bitmap = BitmapFactory.decodeByteArray(mImageDataList.get(position).getByteArray(), 0, mImageDataList.get(position).getByteArray().length);
        byte[] compressedImage = compressImage(bitmap);

        Glide.with(mContext)
                .asBitmap()
                .load(compressedImage)
                .transform(new CenterInside(),new RoundedCorners(15))
                .addListener(new RequestListener<Bitmap>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                        holder.progressBar.setVisibility(View.GONE);
                        incrementAndCheckImageLoaded();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                        holder.progressBar.setVisibility(View.GONE);
                        incrementAndCheckImageLoaded();
                        return false;
                    }
                })
                .into(holder.imageView);

    }

    private synchronized void incrementAndCheckImageLoaded() {
        loadedImagesCount++;
        checkAllImagesLoaded();
    }

    @Override
    public int getItemCount() {
        return mImageDataList.size();
    }
}
