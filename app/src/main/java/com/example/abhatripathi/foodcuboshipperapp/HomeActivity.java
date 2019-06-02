package com.example.abhatripathi.foodcuboshipperapp;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.abhatripathi.foodcuboshipperapp.Model.Request;
import com.example.abhatripathi.foodcuboshipperapp.Model.Token;
import com.example.abhatripathi.foodcuboshipperapp.ViewHolder.OrderViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

public class HomeActivity extends AppCompatActivity {
    FusedLocationProviderClient fusedLocationProviderClient;
    LocationCallback locationCallback;
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    FirebaseDatabase database;
    DatabaseReference shipperOrders;
    LocationRequest locationRequest;
    Location mlastlocation;
     FirebaseRecyclerAdapter<Request,OrderViewHolder> adapter;
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        //check permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED ) {
            requestPermissions(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.CALL_PHONE
            }, Common.REQUEST_CODE);
        } else {
            buildLocationRequest();
            buildLocationCallback();
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
        }
        mLoadingProgress = findViewById(R.id.progress);
        setCallDialog();
        //init firebase
        database=FirebaseDatabase.getInstance();
        shipperOrders=database.getReference(Common.ORDER_NEED_SHIP_TABLE);

        //views
        recyclerView=findViewById(R.id.recycler_order);
        recyclerView.setHasFixedSize(true);
        layoutManager=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        if(Common.currentShipper!=null)
            loadAllOrderNeedShip(Common.currentShipper.getPhone());
        else{
            finish();
            startActivity(new Intent(HomeActivity.this,MainActivity.class));
        }
    }

    public Dialog mCallDialog;
    TextView tv_phoneno;
    public String restaurantNumber;
    private static final int CALL_PHONE_REQUEST_CODE=9999;
    ProgressBar mLoadingProgress;


    @Override
    public void onBackPressed() {
        if(mLoadingProgress.getVisibility()==View.VISIBLE){
            mLoadingProgress.setVisibility(View.GONE);
        }else{
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mLoadingProgress.setVisibility(View.GONE);
    }

    private void setCallDialog(){

        mCallDialog = new Dialog(HomeActivity.this);
        mCallDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mCallDialog.setContentView(R.layout.dialog_call_restaurant);
        mCallDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        tv_phoneno = mCallDialog.findViewById(R.id.tv_phoneno);
        TextView cancel = mCallDialog.findViewById(R.id.tv_cancel);
        TextView ok = mCallDialog.findViewById(R.id.tv_call);
        cancel.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                mCallDialog.dismiss();
            }
        });
        ok.setOnClickListener(new View.OnClickListener() {

            @TargetApi(16)
            public void onClick(View view) {
                if(ActivityCompat.checkSelfPermission(HomeActivity.this, Manifest.permission.CALL_PHONE)
                        != PackageManager.PERMISSION_GRANTED
                        ){
                    ActivityCompat.requestPermissions(HomeActivity.this,new String[]{
                            Manifest.permission.CALL_PHONE
                    },CALL_PHONE_REQUEST_CODE);

                }
                else {
                    mLoadingProgress.setVisibility(View.VISIBLE);

                                Intent intent = new Intent(Intent.ACTION_CALL);
                                intent.setData(Uri.parse("tel:" + restaurantNumber));
                                startActivityForResult(intent,0);

                }
                mCallDialog.dismiss();
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case  CALL_PHONE_REQUEST_CODE:
            {
                if(grantResults.length >0 && grantResults[0] ==PackageManager.PERMISSION_GRANTED){
                    mLoadingProgress.setVisibility(View.VISIBLE);
                    Intent intent = new Intent(Intent.ACTION_CALL);
                    intent.setData(Uri.parse("tel:" + restaurantNumber));
                    startActivityForResult(intent,0);

                }
            }
            case Common.REQUEST_CODE: {
                if (grantResults.length > 0) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        buildLocationRequest();
                        buildLocationCallback();
                        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
                        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
                    }
                    else{
                        Toast.makeText(this,"You should assign permission first!",Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
            default:
                break;
        }
    }
    private void loadAllOrderNeedShip(String phone) {
        DatabaseReference orderInChildOfShipper = shipperOrders.child(phone);

        FirebaseRecyclerOptions<Request> listOrders=new FirebaseRecyclerOptions.Builder<Request>()
                .setQuery(orderInChildOfShipper,Request.class)
                .build();

        adapter=new FirebaseRecyclerAdapter<Request, OrderViewHolder>(listOrders) {
            @Override
            protected void onBindViewHolder(@NonNull OrderViewHolder viewHolder, final int position, @NonNull final Request model) {
                viewHolder.txtOrderId.setText(adapter.getRef(position).getKey());
                viewHolder.txtOrderStatus.setText(Common.convertCodeToStatus(model.getStatus()));
                viewHolder.txtOrderAddress.setText(model.getAddress());
                viewHolder.txtOrderphone.setText(model.getPhone());
                viewHolder.txtOrderDate.setText(Common.getDate(Long.parseLong(adapter.getRef(position).getKey())));


                viewHolder.btn_call_restaurant.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mLoadingProgress.setVisibility(View.VISIBLE);
                        restaurantNumber=model.getRestaurantPhone();
                        tv_phoneno.setText(restaurantNumber);
                        mCallDialog.show();

                    }
                });

                viewHolder.btnShipping.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                      Common.createShippingOrder(adapter.getRef(position).getKey(),
                      Common.currentShipper.getPhone(),
                      mlastlocation);
                      Common.currentRequest=model;
                      Common.currentKey=adapter.getRef(position).getKey();
                      startActivity(new Intent(HomeActivity.this,TrackingOrder.class));

                                          }
                });
            }

            @NonNull
            @Override
            public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View itemView= LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.order_view_layout,parent,false);
                return new OrderViewHolder(itemView);
            }
        };
        adapter.startListening();
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(Common.currentShipper!=null)
            loadAllOrderNeedShip(Common.currentShipper.getPhone());
        else{
            finish();
            startActivity(new Intent(HomeActivity.this,MainActivity.class));
        }
     }

    @Override
    protected void onStop() {
        if(adapter!=null)
            adapter.stopListening();
        if(fusedLocationProviderClient!=null){
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        }
        super.onStop();
    }

    private void buildLocationRequest() {

        locationRequest= new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setSmallestDisplacement(10f);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);

    }
    private void buildLocationCallback(){
        locationCallback=new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                mlastlocation=locationResult.getLastLocation();

            }
        };



    }

}
