package com.app.sendemailinback;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SMSListAdapter extends RecyclerView.Adapter<SMSListAdapter.MyViewHolder>{
    List<SMSModel> smsModelList;
    private Context context;

    public SMSListAdapter(Context context, List<SMSModel> smsModelList) {
        this.smsModelList = smsModelList;
        this.context = context;
    }

    @Override
    public SMSListAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.sms_row_item, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(SMSListAdapter.MyViewHolder holder, final int position) {

        holder.tvSMSFrom.setText(smsModelList.get(position).getSmsFromNumber());
        holder.tvSMSBody.setText(smsModelList.get(position).getSmsBody());

    }

    @Override
    public int getItemCount() {
        return smsModelList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public ImageView imgSMS;
        public TextView tvSMSFrom;
        public TextView tvSMSBody;

        public MyViewHolder(View view) {
            super(view);
            imgSMS =  view.findViewById(R.id.imgSMS);
            tvSMSFrom =  view.findViewById(R.id.tvSMSFrom);
            tvSMSBody =  view.findViewById(R.id.tvSMSBody);
        }
    }

}
