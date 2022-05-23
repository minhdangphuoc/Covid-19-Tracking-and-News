package com.example.covid19trackingandnews;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.text.ParseException;
import java.util.ArrayList;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.Viewholder>{
    private final Context context;
    private final ArrayList<newsitem> courseModelArrayList;



    // Constructor
    public ItemAdapter(Context context, ArrayList<newsitem> courseModelArrayList) {
        this.context = context;
        this.courseModelArrayList = courseModelArrayList;
    }

    @NonNull
    @Override
    public ItemAdapter.Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // to inflate the layout for each item of recycler view.
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.news_item, parent, false);
        return new Viewholder(view);
    }



    @Override
    public void onBindViewHolder(@NonNull ItemAdapter.Viewholder holder, int position) {
        // to set data to textview and imageview of each card layout
        newsitem model = courseModelArrayList.get(position);
        Glide.with(context)
                .load(model.getImageUrl())
                .into(holder.newsIV);
        holder.newsTitle.setText(model.getTitle());
        holder.newsAuthor.setText(model.getAuthor());
        try {
            holder.publishedTime.setText(model.getDateFromPublishDate());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        holder.itemView.setOnClickListener(v -> {
            // item clicked
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(model.getUrl()));
            context.startActivity(browserIntent);
        });
    }

    @Override
    public int getItemCount() {
        // this method is used for showing number
        // of card items in recycler view.
        return courseModelArrayList.size();
    }

    // View holder class for initializing of
    // your views such as TextView and Imageview.
    public class Viewholder extends RecyclerView.ViewHolder {
        private final ImageView newsIV;
        private final TextView newsTitle;
        private final TextView newsAuthor;
        private final TextView publishedTime;

        public Viewholder(@NonNull View itemView) {
            super(itemView);
            newsIV = itemView.findViewById(R.id.itemImage);
            newsTitle = itemView.findViewById(R.id.newsTitle);
            newsAuthor = itemView.findViewById(R.id.newsAuthor);
            publishedTime = itemView.findViewById(R.id.publishedTime);
        }
    }
}
