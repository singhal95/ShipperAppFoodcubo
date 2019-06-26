package com.example.abhatripathi.foodcuboshipperapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.location.Location;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.example.abhatripathi.foodcuboshipperapp.Model.Request;
import com.example.abhatripathi.foodcuboshipperapp.Model.Shipper;
import com.example.abhatripathi.foodcuboshipperapp.Model.ShippingInformation;
import com.example.abhatripathi.foodcuboshipperapp.Remote.IGeoCoordinates;
import com.example.abhatripathi.foodcuboshipperapp.Remote.RetrofitClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Common {

    public static  Request currentRequest;//object of request
    public static  String currentKey;
    public static final String ORDER_NEED_SHIP_TABLE="OrdersNeedShip";
    public static final String SHIPPER_INFO_TABLE="ShippingOrders";
    public static final String SHIPPER_TABLE="Shippers";
    public static Shipper currentShipper;
    public static final int REQUEST_CODE=1000;
    private static final String baseUrl = "https://maps.googleapis.com";
    public static final String LEGACY_SERVER_KEY="AAAAcomtHlQ:APA91bHEA5V7XlHkNuMg3CXemd60rlFoNOIKb--7kBsuvZKhlc5pXN9nHgYbNLE7h4bXcn8S4n47J54blm_ySGxqa4toBKPFLnqRykE4Bwi_5fxeo0aUKYUeWL-CFmvcSa_c_ne5kXa9BAygmsBbY9iAUGBSTHopGg";

    public static String convertCodeToStatus(String code){
        switch (code) {
            case "0":
                return "Placed";
            case "1":
                return "On My Way";
            case "2":
                return "Shipping";
            default:
                return "Shipped!";
        }
    }
    public static String getDate(long time){
        Calendar calendar=  Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(time);
        StringBuilder date=new StringBuilder(
                android.text.format.DateFormat.format("dd-MM-yyyy HH:mm",calendar).toString()//android.text.format
                //KJava This package contains alternative classes for some text formatting classe
        );
        return date.toString();
    }

    public static void createShippingOrder(String key, String phone, Location mlastlocation) {
        ShippingInformation shippingInformation=new ShippingInformation();
        shippingInformation.setOrderId(key);
        shippingInformation.setShipperPhone(phone);
        shippingInformation.setLat(mlastlocation.getLatitude());
        shippingInformation.setLng(mlastlocation.getLongitude());

        //create new item on shipper information table
        FirebaseDatabase.getInstance().getReference(SHIPPER_INFO_TABLE)//where is table?me
                .child(key)
                .setValue(shippingInformation).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("ERROR",e.getMessage());
            }
        });



    }
    public static IGeoCoordinates getGeoCodeService(){
        return RetrofitClient.getClient(baseUrl).create(IGeoCoordinates.class);
    }
    public static Bitmap scaleBitmap(Bitmap bitmap, int newWidth, int newHeight){
        Bitmap scaledBitmap = Bitmap.createBitmap(newWidth,newHeight, Bitmap.Config.ARGB_8888);
        float scaleX = newWidth/(float)bitmap.getWidth();
        float scaleY = newHeight/(float)bitmap.getHeight();
        float pivotX=0,pivotY=0;
        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(scaleX,scaleY,pivotX,pivotY);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bitmap,0,0,new Paint(Paint.FILTER_BITMAP_FLAG));
        return scaledBitmap;
    }

    public static void updateShippingInformation(String currentKey, Location mlastlocation) {
        Map<String,Object> update_location=new HashMap<>();
        update_location.put("lat",mlastlocation.getLatitude());
        update_location.put("lng",mlastlocation.getLongitude());

        FirebaseDatabase.getInstance()
                .getReference(SHIPPER_INFO_TABLE)
                .child(currentKey)
                .updateChildren(update_location)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                     Log.d("ERROR",e.getMessage());
                    }
                });
    }
}
