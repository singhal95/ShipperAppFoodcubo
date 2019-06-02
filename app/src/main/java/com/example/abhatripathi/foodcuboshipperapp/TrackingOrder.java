package com.example.abhatripathi.foodcuboshipperapp;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.abhatripathi.foodcuboshipperapp.Helper.DirectionJSONParser;
import com.example.abhatripathi.foodcuboshipperapp.Model.Request;
import com.example.abhatripathi.foodcuboshipperapp.Remote.IGeoCoordinates;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TrackingOrder extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    FusedLocationProviderClient fusedLocationProviderClient;
    LocationCallback locationCallback;
    LocationRequest locationRequest;
    Location mlastlocation;
    IGeoCoordinates mService;
    Marker mCurrentMarker;
    Polyline polyline;
    Button btn_Call, btn_Shipped;

    @Override
    protected void onResume() {
        super.onResume();


        if (Common.currentShipper == null) {
            finish();
            startActivity(new Intent(TrackingOrder.this,MainActivity.class));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking_order);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        btn_Call =(Button) findViewById(R.id.btnCall);
        btn_Shipped =(Button) findViewById(R.id.btnShipped);
        btn_Call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:"+Common.currentRequest.getPhone()));
                startActivity(intent);
            }
        });
        btn_Shipped.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Common.currentShipper!=null)
                shippedOrder();
                else{
                    finish();
                    startActivity(new Intent(TrackingOrder.this,MainActivity.class));
                }
            }
        });
        mService=Common.getGeoCodeService();
        buildLocationRequest();
        buildLocationCallback();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());

    }

    private void shippedOrder() {
        //we will delete order in taBLE
        //-orderNeedShip
        //-ShippingOrder
        //-Add update status of order to shipped
        FirebaseDatabase.getInstance().getReference(Common.ORDER_NEED_SHIP_TABLE)
                .child(Common.currentShipper.getPhone())
                .child(Common.currentKey)
                .removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                     //update status on request table
                        Map<String,Object>update_status=new HashMap<>();
                        update_status.put("status","03");
                        FirebaseDatabase.getInstance().getReference("Requests")
                                .child(Common.currentKey)
                                .updateChildren(update_status)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        //delete from shipping order
                                        FirebaseDatabase.getInstance().getReference(Common.SHIPPER_INFO_TABLE)
                                                .child(Common.currentKey)
                                                .removeValue()
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Toast.makeText(TrackingOrder.this,"Shipped",Toast.LENGTH_SHORT).show();
                                                        finish();
                                                    }
                                                });
                                    }
                                });
                    }
                });
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

                if(mCurrentMarker!=null){
                    mCurrentMarker.setPosition(new LatLng(mlastlocation.getLatitude(),mlastlocation.getLongitude()));//update location for marker
                    //update location for shipping
                    Common.updateShippingInformation(Common.currentKey,mlastlocation);
//                    mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(mlastlocation.getLatitude(),mlastlocation.getLongitude())));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mlastlocation.getLatitude(),mlastlocation.getLongitude()), 16));
                    drawRoute(new LatLng(mlastlocation.getLatitude(),mlastlocation.getLongitude()),Common.currentRequest);
                }

            }
        };


    }
    private void drawRoute(final LatLng yourLocation, Request request) {

        if(polyline!=null)
            polyline.remove();

       /* if(request.getAddress()!=null && !request.getAddress().isEmpty())
        {
            mService.getGeoCode(request.getAddress()).enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    try{
                        JSONObject jsonObject=new JSONObject(response.body().toString());
                        String lat=((JSONArray)jsonObject.get("results"))
                                .getJSONObject(0)
                                .getJSONObject("geometry")
                                .getJSONObject("location")
                                .get("lat").toString();
                        String lng=((JSONArray)jsonObject.get("results"))
                                .getJSONObject(0)
                                .getJSONObject("geometry")
                                .getJSONObject("location")
                                .get("lng").toString();

                        LatLng orderLocation = new LatLng(Double.parseDouble(lat),Double.parseDouble(lng));
                        Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.house);
                        bitmap=Common.scaleBitmap(bitmap,70,70);

                        MarkerOptions marker = new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(bitmap))
                                .title("Order of " +Common.currentRequest.getPhone())
                                .position(orderLocation);
                        mMap.addMarker((marker));
                        //draw route
                        mService.getDirections(yourLocation.latitude+","+yourLocation.longitude,
                                orderLocation.latitude+","+orderLocation.longitude)
                                .enqueue(new Callback<String>() {
                                    @Override
                                    public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                                        new ParserTask().execute(response.body().toString());
                                    }

                                    @Override
                                    public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {

                                    }
                                });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {

                }
            });
        }
        else{*/
            if(request.getLatLng()!=null&& !request.getLatLng().isEmpty()){
               String[] latLng=request.getLatLng().split(",");
               LatLng orderLocation=new LatLng(Double.parseDouble(latLng[0]),Double.parseDouble(latLng[1]));
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.house);
                bitmap=Common.scaleBitmap(bitmap,70,70);

                MarkerOptions marker = new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(bitmap))
                        .title("Order of " +Common.currentRequest.getPhone())
                        .position(orderLocation);
                mMap.addMarker((marker));

                mService.getDirections(mlastlocation.getLatitude()+","+mlastlocation.getLongitude()
                ,orderLocation.latitude+","+orderLocation.longitude)
                        .enqueue(new Callback<String>() {
                            @Override
                            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                                 new ParserTask().execute(response.body().toString());
                            }

                            @Override
                            public void onFailure(Call<String> call, Throwable t) {

                            }
                        });
            }
        //}

    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        boolean isSuccess=mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this,R.raw.uber_style));
        if(!isSuccess)
            Log.d("ERROR","Map style load failed");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                mlastlocation= location;
                LatLng yourlocation = new LatLng(location.getLatitude(), location.getLongitude());
                mCurrentMarker=mMap.addMarker(new MarkerOptions().position(yourlocation).title("Your Location"));
//                mMap.moveCamera(CameraUpdateFactory.newLatLng(yourlocation));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(yourlocation, 16));
                drawRoute(new LatLng(mlastlocation.getLatitude(),mlastlocation.getLongitude()),Common.currentRequest);

            }
        });
    }

    @Override
    protected void onStop() {
        if(fusedLocationProviderClient!=null){
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        }
        super.onStop();
    }
    private class ParserTask extends AsyncTask<String,Integer,List<List<HashMap<String,String>>>> {
        ProgressDialog mDialog= new ProgressDialog(TrackingOrder.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mDialog.setMessage("Please Wait.... ");
            mDialog.show();
        }

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... strings) {

            JSONObject jObject;
            List<List<HashMap<String,String>>> routes=null;
            try{
                jObject=new JSONObject(strings[0]);
                DirectionJSONParser parser=new DirectionJSONParser();
                routes=parser.parse(jObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> lists) {
            mDialog.dismiss();
            ArrayList points=null;
            PolylineOptions lineOptions=null;
            for(int i=0;i<lists.size();i++){
                points=new ArrayList();
                lineOptions = new PolylineOptions();
                List<HashMap<String,String>> path=lists.get(i);
                for(int j=0;j<path.size();j++){
                    HashMap<String,String> point=path.get(j);
                    double lat=Double.parseDouble(point.get("lat"));
                    double lng=Double.parseDouble(point.get("lng"));
                    LatLng position=new LatLng(lat,lng);
                    points.add(position);
                }
                lineOptions.addAll(points);
                lineOptions.width(12);
                lineOptions.color(Color.BLUE);
                lineOptions.geodesic(true);
            }
            if(lineOptions!=null)
            mMap.addPolyline(lineOptions);

        }
    }
}
