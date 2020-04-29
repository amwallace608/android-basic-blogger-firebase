package com.amwallace.basicblogger.Data;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.amwallace.basicblogger.Activities.PostDetailActivity;
import com.amwallace.basicblogger.Model.BlogPost;
import com.amwallace.basicblogger.R;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

public class BlogRecyclerAdapter extends RecyclerView.Adapter<BlogRecyclerAdapter.ViewHolder> {

    private Context context;
    private List<BlogPost> blogPostList;

    public BlogRecyclerAdapter(Context context, List<BlogPost> blogPostList) {
        this.context = context;
        this.blogPostList = blogPostList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate post item view
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.post_item, parent, false);
        return new ViewHolder(view, context);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        //bind post data to UI elements
        //get current post
        BlogPost blogPost = blogPostList.get(position);
        String imageUrl = null;

        holder.title.setText(blogPost.getTitle());
        holder.description.setText(blogPost.getDescription());
        //holder.timeStamp.setText(blogPost.getTimestamp());

        DateFormat dateFormat = DateFormat.getDateInstance();
        holder.timeStamp.setText(dateFormat.
                format(new Date(Long.valueOf(blogPost.getTimestamp())).getTime()));

        imageUrl = blogPost.getImage();
        //load image with picasso
        Picasso.with(context).load(imageUrl).into(holder.postImg);

    }

    @Override
    public int getItemCount() {
        return blogPostList.size();
    }

    //inner ViewHolder class for blog recycler adapter
    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView title, description, timeStamp;
        public ImageView postImg;
        String userId;

        public ViewHolder(View view, Context ctx){
            super(view);
            context = ctx;
            //setup ui of post view
            title = (TextView) view.findViewById(R.id.postFeedTitle);
            description = (TextView) view.findViewById(R.id.postFeedTxt);
            timeStamp = (TextView) view.findViewById(R.id.postFeedTime);
            userId = null;
            postImg = (ImageView) view.findViewById(R.id.postFeedImg);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //todo go to view post activity, send post details with intent
                    //get clicked post info
                    BlogPost clickedPost = blogPostList.get(getAdapterPosition());
                    Intent intent = new Intent(context, PostDetailActivity.class);
                    //send post to detail activity
                    intent.putExtra("blogpost", clickedPost);
                    context.startActivity(intent);

                }
            });

        }
    }
}
