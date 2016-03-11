package com.example.ivansv.issatest;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class MainListAdapter extends RecyclerView.Adapter<MainListAdapter.MainViewHolder> {
    private ArrayList<String> titles;

    public MainListAdapter(ArrayList<String> titles) {
        this.titles = titles;
    }

    @Override
    public MainViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final View v = inflater.inflate(R.layout.list_item, parent, false);
        return new MainViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MainViewHolder holder, int position) {
        holder.title = titles.get(position);
        holder.textViewTitle.setText(holder.title);
    }

    @Override
    public int getItemCount() {
        return titles.size();
    }

    public class MainViewHolder extends RecyclerView.ViewHolder {
        TextView textViewTitle;
        String title;

        public MainViewHolder(View itemView) {
            super(itemView);
            textViewTitle = (TextView) itemView.findViewById(R.id.title);
        }
    }
}
