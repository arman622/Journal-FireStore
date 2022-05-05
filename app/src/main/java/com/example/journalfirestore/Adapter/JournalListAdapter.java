package com.example.journalfirestore.Adapter;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.journalfirestore.JournalListActivity;
import com.example.journalfirestore.Model.Journal;
import com.example.journalfirestore.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class JournalListAdapter extends RecyclerView.Adapter<JournalListAdapter.ViewHolder> {
    private Context context;
    private List<Journal> journalList;
    private OnJournalClickListener listener;

    public JournalListAdapter(Context context, List<Journal> journalList) {
        this.context = context;
        this.journalList = journalList;
    }

    @NonNull
    @Override
    public JournalListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.journal_list_row,parent,false);
        return new ViewHolder(view, context);
    }

    @Override
    public void onBindViewHolder(@NonNull JournalListAdapter.ViewHolder holder, int position) {
        //set all the TextViews

        Journal journal = journalList.get(position);

        holder.usernameTextView.setText(journal.getUserName());
        holder.titleTextView.setText(journal.getTitle());
        holder.thoughtTextView.setText(journal.getThought());

        //Get Image from journal is a url string
        Picasso.get()
                .load(journal.getImageUrl())
                .placeholder(android.R.drawable.stat_sys_download)
                .error(android.R.drawable.stat_notify_error)
                .into(holder.imageView);

        //Convert Timestamp object into String properly Example: 0 minutes ago / 1 hour ago
        long time = journal.getTimestamp().getSeconds()*1000;
        String timeAgo = (String) DateUtils.getRelativeTimeSpanString(time);
        holder.timestampTextView.setText(timeAgo);
    }

    @Override
    public int getItemCount() {
        return journalList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView usernameTextView;
        private TextView titleTextView, thoughtTextView, timestampTextView;
        private ImageView imageView;
        public ImageButton shareBtn;

        public ViewHolder(@NonNull View itemView, Context ctx) {
            super(itemView);

            context = ctx;

            usernameTextView = itemView.findViewById(R.id.username_textview_row);
            titleTextView = itemView.findViewById(R.id.title_textview_row);
            thoughtTextView = itemView.findViewById(R.id.thought_textview_row);
            timestampTextView = itemView.findViewById(R.id.timestamp_textview_row);
            imageView = itemView.findViewById(R.id.imageview_row);

            shareBtn = itemView.findViewById(R.id.share_imageview_btn_row);

            shareBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    Intent intent = new Intent(Intent.ACTION_SEND);
//                    intent.setType("image/*");

                }
            });
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if(listener != null && position != RecyclerView.NO_POSITION){
                        listener.onJournalClick(journalList.get(position));
                    }
                }
            });


        }
    }

    public interface OnJournalClickListener{
        void onJournalClick(Journal journal);
    }

    public void setOnItemClickListener(OnJournalClickListener listener){
        this.listener = listener;
    }



}
