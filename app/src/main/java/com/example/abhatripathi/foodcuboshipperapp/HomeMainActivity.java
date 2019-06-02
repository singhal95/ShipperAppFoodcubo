package com.example.abhatripathi.foodcuboshipperapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class HomeMainActivity extends AppCompatActivity {
    TextView tv_acceptorders,tv_myorders;
    FirebaseDatabase database;
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_main);
        //check permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED ) {
            requestPermissions(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.CALL_PHONE
            }, Common.REQUEST_CODE);
        }
        database=FirebaseDatabase.getInstance();
        tv_acceptorders = findViewById(R.id.tv_acceptorders);
        tv_myorders = findViewById(R.id.tv_myorders);
        tv_acceptorders.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(HomeMainActivity.this,AcceptOrdersActivity.class));

            }
        });
        tv_myorders.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(HomeMainActivity.this,HomeActivity.class));
            }
        });


        if(Common.currentShipper!=null)
            updateTokenShipper(FirebaseInstanceId.getInstance().getToken());
        else{
            finish();
            startActivity(new Intent(HomeMainActivity.this,MainActivity.class));
        }
    }


    private void updateTokenShipper(String token) {
        DatabaseReference tokens=database.getReference("Tokens");
        Token data=new Token(token,false);
        tokens.child(Common.currentShipper.getPhone()).setValue(data);
    }

}
