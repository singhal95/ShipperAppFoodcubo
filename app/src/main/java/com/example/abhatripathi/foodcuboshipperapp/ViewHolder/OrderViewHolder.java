package com.example.abhatripathi.foodcuboshipperapp.ViewHolder;

import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.abhatripathi.foodcuboshipperapp.R;

public class OrderViewHolder extends RecyclerView.ViewHolder  {
    public TextView txtOrderId, txtOrderStatus, txtOrderphone, txtOrderAddress,txtOrderDate;
    public Button btnShipping;
    public FloatingActionButton btn_call_restaurant;



    public OrderViewHolder(View itemView){
        super(itemView);

        txtOrderAddress = itemView.findViewById(R.id.order_address);
        txtOrderId = itemView.findViewById(R.id.order_id);
        txtOrderStatus = itemView.findViewById(R.id.order_status);
        txtOrderphone = itemView.findViewById(R.id.order_phone);
        txtOrderDate = itemView.findViewById(R.id.order_date);
        btn_call_restaurant = itemView.findViewById(R.id.btn_call_restaurant);


        btnShipping=(Button)itemView.findViewById(R.id.btnShipping);


    }

}

