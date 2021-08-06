package com.app.sendemailinback;

import android.content.Context;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class SMSListAdapter extends RecyclerView.Adapter<SMSListAdapter.MyViewHolder>{
    List<SMSModel> smsModelList;
    private Context context;
    onItemClickListener onItemClickListener;

    public SMSListAdapter(Context context, List<SMSModel> smsModelList, onItemClickListener onItemClickListener) {
        this.smsModelList = smsModelList;
        this.context = context;
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public SMSListAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.sms_row_item, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(SMSListAdapter.MyViewHolder holder, final int position) {
        SMSModel smsModel = smsModelList.get(position);
        holder.tvSMSFrom.setText(smsModel.getSmsFromNumber());
        holder.tvSMSDate.setText(smsModel.getSmsDate());
        holder.tvSMSBody.setText(smsModel.getSmsBody());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClickListener.onItemClick(smsModel);
            }
        });
    }

    @Override
    public int getItemCount() {
        return smsModelList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public ImageView imgSMS;
        public TextView tvSMSFrom;
        public TextView tvSMSBody;
        public TextView tvSMSDate;

        public MyViewHolder(View view) {
            super(view);
            imgSMS =  view.findViewById(R.id.imgSMS);
            tvSMSFrom =  view.findViewById(R.id.tvSMSFrom);
            tvSMSBody =  view.findViewById(R.id.tvSMSBody);
            tvSMSDate = view.findViewById(R.id.tvSMSDate);
        }
    }

    public void removeItem(int position) {
        smsModelList.remove(position);
        notifyItemRemoved(position);
    }

    public void restoreItem(SMSModel item, int position) {
        smsModelList.add(position, item);
        notifyItemInserted(position);
    }

    public List<SMSModel> getData() {
        return smsModelList;
    }

}
