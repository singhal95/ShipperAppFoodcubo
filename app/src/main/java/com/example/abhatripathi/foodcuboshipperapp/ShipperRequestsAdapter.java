package com.example.abhatripathi.foodcuboshipperapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.abhatripathi.foodcuboshipperapp.Model.Request;
import com.example.abhatripathi.foodcuboshipperapp.Model.Token;
import com.example.abhatripathi.foodcuboshipperapp.ViewHolder.OrderViewHolder;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;

import static android.content.Context.MODE_PRIVATE;

public class ShipperRequestsAdapter extends RecyclerView.Adapter<OrderViewHolder>{

    private final AcceptOrdersActivity mContext;
    private final ArrayList<Request> requestslist;
    private final ArrayList<String> keyslist;
    public double latString;
    public double longstring;
    DatabaseReference requests;
    boolean isClickable=true;

    public ShipperRequestsAdapter(AcceptOrdersActivity context, ArrayList<Request> requestslist,ArrayList<String> keyslist){
        this.mContext= context;
        this.keyslist=keyslist;
        this.requestslist=requestslist;

        requests = FirebaseDatabase.getInstance().getReference("Requests");

    }

    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView= LayoutInflater.from(parent.getContext())
                .inflate(R.layout.order_view_layout,parent,false);
        return new OrderViewHolder(itemView);
    }


    @Override
    public void onBindViewHolder(@NonNull final OrderViewHolder viewHolder, final int position) {
        final Request model = requestslist.get(viewHolder.getAdapterPosition());
        final String key = keyslist.get(viewHolder.getAdapterPosition());
        viewHolder.txtOrderId.setText(key);
        viewHolder.txtOrderStatus.setText(Common.convertCodeToStatus(model.getStatus()));
        viewHolder.txtOrderAddress.setText(model.getAddress());
        viewHolder.txtOrderphone.setText(model.getPhone());
        viewHolder.txtOrderDate.setText(Common.getDate(Long.parseLong(key)));
        viewHolder.btnShipping.setText("ACCEPT");
        viewHolder.btnShipping.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mContext.getLocation(key,model);
            }
        });

    }
    public void buttonClickAction(final String key, final Request model){
        if(isClickable) {
            isClickable=false;
            mContext.progress.setVisibility(View.VISIBLE);
            DatabaseReference shipperorders = FirebaseDatabase.getInstance().getReference(Common.ORDER_NEED_SHIP_TABLE);
            shipperorders.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot shipperSnapshot : dataSnapshot.getChildren()) {
                        if (shipperSnapshot.child(key).exists()) {
                            FirebaseDatabase.getInstance().getReference(Common.ORDER_NEED_SHIP_TABLE)
                                    .child(shipperSnapshot.getKey()).child(key).removeValue();
                        }

                    }

                    model.setStatus("1");


                    FirebaseDatabase.getInstance().getReference("Restaurants")
                            .child(model.getRestaurantId())
                            .child("Requests")
                            .child(key)
                            .child("status")
                            .setValue("1");

                    FirebaseDatabase.getInstance().getReference("Restaurants")
                            .child(model.getRestaurantId())
                            .child("Requests")
                            .child(key)
                            .child("tempShipper")
                            .setValue(Common.currentShipper.getPhone());

                    requests.child(key).setValue(model);
                    FirebaseDatabase.getInstance().getReference(Common.ORDER_NEED_SHIP_TABLE)
                            .child(Common.currentShipper.getPhone())
                            .child(key)
                            .setValue(model);
                    sendAcceptancetoAdmin("0001",
                            model, key, "Hello Admin");
                    sendAcceptancetoAdmin(model.getPhone(),
                            model, key, "Hello ");
                    sendSms("Hello Admin"+Common.currentShipper.getPhone()
                            +" accepted the order "+key,"0001");
                    sendSms("Hello "+Common.currentShipper.getPhone()
                            +" accepted the order "+key,model.getPhone());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }


    public String sendSms(String msg,String phonenumber) {
        try {
            // Construct data
            String apiKey = "apikey=" + "uRlkT7rVE04-N0fmmUN5Q23xwENdH63iejlr4NO7k0";
            String message = "&message=" + "Message from FOOD CUBO "+msg;
            String sender = "&sender=" + "TXTLCL";
            String numbers = "&numbers=" + phonenumber;
            // Send data
            HttpURLConnection conn = (HttpURLConnection) new URL("https://api.textlocal.in/send/?").openConnection();
            String data = apiKey + numbers + message + sender;
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Length", Integer.toString(data.length()));
            conn.getOutputStream().write(data.getBytes("UTF-8"));
            final BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            final StringBuffer stringBuffer = new StringBuffer();
            String line;
            while ((line = rd.readLine()) != null) {
                System.out.println("FOOD CUBO " + line);
                stringBuffer.append(line);
            }
            rd.close();

            return stringBuffer.toString();
        } catch (Exception e) {
            System.out.println("Error SMS " + e);
            return "Error " + e;
        }
    }

    @Override
    public int getItemCount() {
        return requestslist.size();
    }

    private void sendAcceptancetoAdmin(final String phone, final Request item
            , final String requestKey, final String msg ) {
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
        tokens.child(phone)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists()) {
                            final Token token = dataSnapshot.getValue(Token.class);
                            final MediaType JSON
                                    = MediaType.parse("application/json; charset=utf-8");
                            new AsyncTask<Void,Void,Boolean>(){

                                @Override
                                protected Boolean doInBackground(Void... params) {
                                    try {
                                        OkHttpClient client = new OkHttpClient();
                                        JSONObject json=new JSONObject();
                                        JSONObject dataJson=new JSONObject();
                                        dataJson.put("body",Common.currentShipper.getPhone()
                                                +" accepted the order "+requestKey);
                                        dataJson.put("title",msg);
                                        json.put("notification",dataJson);
                                        json.put("to",token.getToken());
                                        RequestBody body = RequestBody.create(JSON, json.toString());
                                        okhttp3.Request request = new okhttp3.Request.Builder()
                                                .header("Authorization","key="+Common.LEGACY_SERVER_KEY)
                                                .url("https://fcm.googleapis.com/fcm/send")
                                                .post(body)
                                                .build();
                                        okhttp3.Response response = client.newCall(request).execute();
                                        if (response.code() == 200) {
                                            if (response.isSuccessful()) {
                                                item.setTempShipper(Common.currentShipper.getPhone());
                                                requests.child(requestKey).setValue(item);
                                                return true;
                                            } else {
                                                return true;

                                            }
                                        }
                                    }catch (Exception e){
                                        e.printStackTrace();
                                        return false;
                                    }
                                    return false;
                                }

                                @Override
                                protected void onPostExecute(Boolean aVoid) {
                                    super.onPostExecute(aVoid);
                                    if(aVoid){
                                        notifyDataSetChanged();
                                                if(requestslist.size()==0)
                                                    mContext.tv_no_orders.setVisibility(View.VISIBLE);
                                                else
                                                    mContext.tv_no_orders.setVisibility(View.GONE);
                                                Toast.makeText(mContext, "Order Accepted and added to your orders!", Toast.LENGTH_SHORT).show();
                                        mContext.progress.setVisibility(View.GONE);
                                        isClickable=true;
                                    }
                                }
                            }.execute();

                        }


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }


}
