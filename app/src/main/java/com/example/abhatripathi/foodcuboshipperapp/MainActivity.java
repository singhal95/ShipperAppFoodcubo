package com.example.abhatripathi.foodcuboshipperapp;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.abhatripathi.foodcuboshipperapp.Model.Shipper;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;

import info.hoang8f.widget.FButton;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MainActivity extends AppCompatActivity {
    FButton btn_sign_in;
    MaterialEditText edt_phone, edt_password;
    FirebaseDatabase database;
    DatabaseReference shippers;
    public static final String PREFS_FILE_NAME = "sharedPreferences";
    private static final int MY_PERMISION_CODE = 10;

    public static void firstTimeAskingPermission(Context context, String permission, boolean isFirstTime) {
        SharedPreferences sharedPreference = context.getSharedPreferences(PREFS_FILE_NAME, MODE_PRIVATE);
        sharedPreference.edit().putBoolean(permission, isFirstTime).apply();
    }

    public static boolean isFirstTimeAskingPermission(Context context, String permission) {
        return context.getSharedPreferences(PREFS_FILE_NAME, MODE_PRIVATE).getBoolean(permission, true);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISION_CODE: {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(MainActivity.this, "Enable Permissions to access the features of this App", Toast.LENGTH_LONG).show();
                    getLocation();
                }else{
                    login(edt_phone.getText().toString(),edt_password.getText().toString());
                }
            }
        }

    }
    private void showAlert() {
        final android.support.v7.app.AlertDialog.Builder dialog = new android.support.v7.app.AlertDialog.Builder(this);
        dialog.setTitle("Enable Location")
                .setMessage("Your Locations Settings are OFF \nPlease Enable Location")
                .setPositiveButton("Allow", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {


                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION},
                                MY_PERMISION_CODE);


                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        Toast.makeText(MainActivity.this, "Enable Permissions to access the features of this App", Toast.LENGTH_LONG).show();

                    }
                });
        dialog.show();
    }
    private void getLocation(){

        if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    ACCESS_FINE_LOCATION) && ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    ACCESS_COARSE_LOCATION)) {
                showAlert();

            } else {

                if (isFirstTimeAskingPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                    firstTimeAskingPermission(MainActivity.this,
                            Manifest.permission.ACCESS_FINE_LOCATION, false);
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION},
                            MY_PERMISION_CODE);
                } else {
                    Toast.makeText(MainActivity.this, "You won't be able to access the features of this App", Toast.LENGTH_LONG).show();
                }


            }
        }else{
            login(edt_phone.getText().toString(),edt_password.getText().toString());
        }

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn_sign_in=findViewById(R.id.btnSignIn);
        edt_password=findViewById(R.id.edtPassword);
        edt_phone=findViewById(R.id.edtPhone);
        // firebase
        FirebaseApp.initializeApp(MainActivity.this);
        database=FirebaseDatabase.getInstance();
        shippers=database.getReference(Common.SHIPPER_TABLE);
        btn_sign_in.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    getLocation();
            }
        });

    }

    private void login(String phone, final String password) {
        System.out.println("cominghere");
        shippers.child(phone)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            Shipper shipper=dataSnapshot.getValue(Shipper.class);
                            if(shipper.getPassword().equals(password)){
                              startActivity(new Intent(MainActivity.this,HomeMainActivity.class));
                              Common.currentShipper=shipper;
                              finish();
                            }
                            else{
                                Toast.makeText(MainActivity.this,"Wrong password",Toast.LENGTH_SHORT).show();
                            }
                        }
                        else{
                            Toast.makeText(MainActivity.this,"Your shipper's phone does not exist",Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }
}
