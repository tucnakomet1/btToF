package com.main.rpbt.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.main.rpbt.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    private static Context context;
    private final ArrayList<String[]> arr;
    private final OnItemClickListener onItemClickListener;
    private final OnItemLongClickListener onItemLongClickListener;

    public RecyclerViewAdapter(Context context, ArrayList<String[]> arr, OnItemClickListener onItemClickListener, OnItemLongClickListener onItemLongClickListener) {
        this.context = context;
        this.arr = arr;
        this.onItemClickListener = onItemClickListener;
        this.onItemLongClickListener = onItemLongClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.camera_player_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String[] data = arr.get(position);

        // normal click
        holder.bind(data, onItemClickListener);

        // long click
        holder.itemView.setOnLongClickListener(v -> {
            onItemLongClickListener.onItemLongClick(position);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return arr.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textName, textSize, textDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.camera_player_file_name);
            textSize = itemView.findViewById(R.id.camera_player_file_size);
            textDate = itemView.findViewById(R.id.camera_player_file_time);
        }

        @SuppressLint("SetTextI18n")
        public void bind(final String[] data, final OnItemClickListener listener) {
            JsonHelper jsonHelper = new JsonHelper(RecyclerViewAdapter.context);

            textName.setText(data[0]);
            textSize.setText(data[1]);
            textDate.setText(jsonHelper.getFromJson(data[0]) + " ");
            itemView.setOnClickListener(v -> listener.onItemClick(getAdapterPosition()));
        }
    }

    // removes an item after 1 second of holding it
    public void removeItem(int position, String fileName) {
        arr.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, arr.size());

        JsonHelper jsonHelper = new JsonHelper(context);
        jsonHelper.removeFromJson(fileName);
    }


    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(int position);
    }
}
