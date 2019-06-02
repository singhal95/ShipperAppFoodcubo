package com.example.abhatripathi.foodcuboshipperapp;

import android.Manifest;
import android.content.DialogInterface;
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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.ArrayList;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static com.example.abhatripathi.foodcuboshipperapp.MainActivity.firstTimeAskingPermission;
import static com.example.abhatripathi.foodcuboshipperapp.MainActivity.isFirstTimeAskingPermission;

public class AcceptOrdersActivity extends AppCompatActivity {
    FusedLocationProviderClient fusedLocationProviderClient;
    LocationCallback locationCallback;
    RecyclerView recyclerView;
    TextView tv_no_orders;
    RecyclerView.LayoutManager layoutManager;
    FirebaseDatabase database;
    DatabaseReference shipperRequests;
    LocationRequest locationRequest;
    Location mlastlocation;
    private ArrayList<Request> shipperRequestsList = new ArrayList<>();
    private ArrayList<String> shipperRequestsKeyList = new ArrayList<>();
    ShipperRequestsAdapter adapter;
    int comefirst = 0;
    ProgressBar progress;
    private static final int MY_PERMISION_CODE = 10;


    private void showAlert() {
        final android.support.v7.app.AlertDialog.Builder dialog = new android.support.v7.app.AlertDialog.Builder(this);
        dialog.setTitle("Enable Location")
                .setMessage("Your Locations Settings are OFF \nPlease Enable Location")
                .setPositiveButton("Allow", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {

                        ActivityCompat.requestPermissions(AcceptOrdersActivity.this,
                                new String[]{ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION},
                                MY_PERMISION_CODE);


                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        Toast.makeText(AcceptOrdersActivity.this, "Enable Permissions to access the features of this App", Toast.LENGTH_LONG).show();

                    }
                });
        dialog.show();
    }


    public String tempkey;
    public Request tempmodel;

    public void getLocation(final String key, final Request model) {
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        tempkey = key;
        tempmodel = model;
        if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(AcceptOrdersActivity.this,
                    ACCESS_FINE_LOCATION) && ActivityCompat.shouldShowRequestPermissionRationale(AcceptOrdersActivity.this,
                    ACCESS_COARSE_LOCATION)) {
                showAlert();

            } else {
                    ActivityCompat.requestPermissions(AcceptOrdersActivity.this,
                            new String[]{ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION},
                            MY_PERMISION_CODE);
            }
        } else {

            mFusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {

                    if (location != null) {
                        adapter.latString = location.getLatitude();
                        adapter.longstring = location.getLongitude();
                        adapter.buttonClickAction(key, model);
                    } else {
                        Toast.makeText(getApplicationContext(), "Error in fetching the location", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        //check permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
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
        //init firebase
        database = FirebaseDatabase.getInstance();
        shipperRequests = database.getReference("Requests");
        //views
        progress = findViewById(R.id.progress);
        tv_no_orders = findViewById(R.id.tv_no_orders);
        recyclerView = findViewById(R.id.recycler_order);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        progress.setVisibility(View.VISIBLE);
        if (Common.currentShipper != null)
            loadRequests(Common.currentShipper.getPhone());
        else {
            finish();
            startActivity(new Intent(AcceptOrdersActivity.this, MainActivity.class));
        }
    }

    private void loadRequests(final String currentShipper) {
        shipperRequests.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                shipperRequestsKeyList.removeAll(shipperRequestsKeyList);
                shipperRequestsKeyList.clear();
                shipperRequestsList.removeAll(shipperRequestsKeyList);
                shipperRequestsList.clear();
                System.out.println("lol..........." + dataSnapshot.getChildrenCount());
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    System.out.println("lol...........ggggg" + snapshot.child("tempShipper") + "....." + currentShipper);
                    if (snapshot.child("tempShipper").exists()) {
                        if (snapshot.child("tempShipper").getValue(String.class).equals(currentShipper)
                                && snapshot.child("status").getValue(String.class).equals("0")) {
                            Request request = snapshot.getValue(Request.class);
                            shipperRequestsKeyList.add(snapshot.getKey());
                            shipperRequestsList.add(request);
                        }
                    }

                }
                System.out.println("lol...........gtgtgt" + shipperRequestsKeyList.size());
                if (shipperRequestsKeyList.size() > 0) {
                    adapter = new ShipperRequestsAdapter(AcceptOrdersActivity.this, shipperRequestsList, shipperRequestsKeyList);
                    recyclerView.setAdapter(adapter);
                }
                if (comefirst == 0) {
                    comefirst++;
                    if (shipperRequestsKeyList.size() == 0)
                        tv_no_orders.setVisibility(View.VISIBLE);
                    else
                        tv_no_orders.setVisibility(View.GONE);
                }
                progress.setVisibility(View.GONE);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    /*   private void loadAllOrderNeedShip(String phone) {
           DatabaseReference orderInChildOfShipper = shipperRequests.child(phone);

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
                   viewHolder.btnShipping.setOnClickListener(new View.OnClickListener() {
                       @Override
                       public void onClick(View view) {
                         Common.createShippingOrder(adapter.getRef(position).getKey(),
                           Common.currentShipper.getPhone(),
                           mlastlocation);
                         Common.currentRequest=model;
                         Common.currentKey=adapter.getRef(position).getKey();
                         startActivity(new Intent(AcceptOrdersActivity.this,TrackingOrder.class));





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
           adapter.notifyDataSetChanged();
           recyclerView.setAdapter(adapter);
       }
   */
    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {

        if (fusedLocationProviderClient != null) {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        }
        super.onStop();
    }

    private FusedLocationProviderClient mFusedLocationProviderClient;
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
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
                    } else {
                        Toast.makeText(this, "You should assign permission first!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
            case MY_PERMISION_CODE: {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                    mFusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {

                            if (location != null) {
                                adapter.latString = location.getLatitude();
                                adapter.longstring = location.getLongitude();
                                adapter.buttonClickAction(tempkey, tempmodel);
                            } else {
                                Toast.makeText(getApplicationContext(), "Error in fetching the location", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else {
                    Toast.makeText(AcceptOrdersActivity.this, "Enable Permissions to access the features of this App", Toast.LENGTH_LONG).show();
                    getLocation(tempkey, tempmodel);
                }
            }
            break;
            default:
                break;
        }
    }

    private void buildLocationRequest() {

        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setSmallestDisplacement(10f);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);

    }

    private void buildLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                mlastlocation = locationResult.getLastLocation();

            }
        };


    }

}
