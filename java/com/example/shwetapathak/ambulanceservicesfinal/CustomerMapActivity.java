package com.example.shwetapathak.ambulanceservicesfinal;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class
CustomerMapActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener,com.google.android.gms.location.LocationListener {

    private GoogleMap mMap;
    GoogleApiClient kGoogleApiClient;
    Location kLastLocation;
    LocationRequest kLOcationRequest;
    private  Marker pickupmarker;
    private Button mlogout,mrequest,msetting;
    private  LatLng accidentlocation;
    private LinearLayout mdriverinfo;
    private ImageView mdriverprofileimage;
    private TextView mdrivername,mdriverphone,mdriverambulance;
    //private  String requestservice;
    private RadioGroup mradioGroup;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mdriverinfo = (LinearLayout) findViewById(R.id.driverinfo);
        mdriverprofileimage = (ImageView) findViewById(R.id.driverprofileimage);
        mdrivername = (TextView) findViewById(R.id.drivername);
        mdriverphone = (TextView) findViewById(R.id.driverphone);

        mdriverambulance = (TextView) findViewById(R.id.driverambulance);

        mlogout = (Button)findViewById(R.id.logout);
        mrequest = (Button)findViewById(R.id.request);
        msetting = (Button)findViewById(R.id.settings);
        mlogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent i = new Intent(CustomerMapActivity.this,MainActivity.class);
                startActivity(i);
                finish();
                return;
            }
        });

        mrequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("customerrequest");
                    GeoFire geofire = new GeoFire(ref);
                    geofire.setLocation(userId,new GeoLocation(kLastLocation.getLatitude(),kLastLocation.getLongitude()));
                    accidentlocation = new LatLng(kLastLocation.getLatitude(),kLastLocation.getLongitude());
                    pickupmarker=mMap.addMarker(new MarkerOptions().position(accidentlocation).title("Accident occured here"));
                    mrequest.setText("Calling Ambulance...");
                    getClosestAmbulance();
            }
        });

        msetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent =  new Intent(CustomerMapActivity.this,CustomerSettingActivity.class);
                startActivity(intent);
                return;
            }
        });
    }

    private  int radius = 1;
    private  Boolean  driverFound=false;
    private  String driverFoundId;
    GeoQuery geoQuery;

    private  void getClosestAmbulance(){

        DatabaseReference driverlocation = FirebaseDatabase.getInstance().getReference().child("driverAvailable");
        GeoFire geofire = new GeoFire(driverlocation);
        geoQuery = geofire.queryAtLocation(new GeoLocation(accidentlocation.latitude, accidentlocation.longitude),radius);
        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if (!driverFound){
                    driverFound = true;
                    driverFoundId=key;
                    DatabaseReference driverref = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverFoundId);
                    String customer_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    HashMap map = new HashMap();
                    map.put("customerId",customer_id);
                    driverref.updateChildren(map);

                    mrequest.setText("Looking For Nearby Ambulance...");
                    getdriverlocation();
                    getDriverinfo();

                    }
            }


            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                if (!driverFound){
                    radius++;
                    getClosestAmbulance();
                }

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }


    private  Marker mdrivermarker;
    private  void  getdriverlocation(){

        DatabaseReference driverlocationref = FirebaseDatabase.getInstance().getReference().child("driverworking").child(driverFoundId).child("l");
        driverlocationref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    List<Object> map = (List<Object>) dataSnapshot.getValue();
                    double locationlat = 0;
                    double locationlng = 0;
                    mrequest.setText("Ambulance Found...");

                    if(map.get(0)!= null){
                        locationlat = Double.parseDouble(map.get(0).toString());
                    }
                    if(map.get(1)!= null){
                        locationlng = Double.parseDouble(map.get(1).toString());
                    }
                    LatLng driverlatlng = new LatLng(locationlat,locationlng);
                    if (mdrivermarker !=null){
                        mdrivermarker.remove();
                    }

                    Location loc1 = new Location("");
                    loc1.setLatitude(accidentlocation.latitude);
                    loc1.setLongitude(accidentlocation.longitude);

                    Location loc2 = new Location("");
                    loc2.setLatitude(driverlatlng.latitude);
                    loc2.setLongitude(driverlatlng.longitude);

                    float distance = loc1.distanceTo(loc2);

                    if(distance<100){
                        mrequest.setText("Ambulance Here");

                    }else {
                        mrequest.setText("Ambulance Found..."+String.valueOf(distance));
                    }
                    mdrivermarker=mMap.addMarker(new MarkerOptions().position(driverlatlng).title("Your Ambulance"));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void getDriverinfo(){
        mdriverinfo.setVisibility(View.VISIBLE);
        DatabaseReference mcustomerdatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverFoundId);
        mcustomerdatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                    Map<String,Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if(map.get("name")!=null){
                        mdrivername.setText(map.get("name").toString());

                    }
                    if(map.get("contact")!=null){
                        mdriverphone.setText(map.get("contact").toString());

                    }

                    if(map.get("typeofambulance")!=null){
                        mdriverambulance.setText(map.get("typeofambulance").toString());

                    }


                    if(map.get("profileImageUrl")!=null) {

                        Glide.with(getApplication()).load(map.get("profileImageUrl").toString()).into(mdriverprofileimage);
                    }

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.INTERNET)!=PackageManager.PERMISSION_GRANTED){
            return;
        }

        buildGoogleApiClient();
        mMap.setMyLocationEnabled(true);
    }

    protected  synchronized  void buildGoogleApiClient(){

        kGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        kGoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {

        kLastLocation = location;
        LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());

        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(16));


    }



    @Override
    public void onConnected(@Nullable Bundle bundle) {
        kLOcationRequest = new LocationRequest();
        kLOcationRequest.setInterval(1000);
        kLOcationRequest.setFastestInterval(1000);
        kLOcationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.INTERNET)!=PackageManager.PERMISSION_GRANTED){

        }
        LocationServices.FusedLocationApi.requestLocationUpdates(kGoogleApiClient,kLOcationRequest,this);

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    protected  void onStop(){
        super.onStop();

    }
}
