package com.example.shwetapathak.ambulanceservicesfinal;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
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
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

public class DriverMapActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener,com.google.android.gms.location.LocationListener,RoutingListener {

    private GoogleMap mMap;
    GoogleApiClient kGoogleApiClient;
    Location kLastLocation;
    LocationRequest kLOcationRequest;
    private Button mlogout,settings,droppatient;

    private  LatLng pickuplatlng;
    //Marker mcurrent;


    private  String customer_id = "";
    GeoQuery geoQuery;

    private Boolean isLoggingOut= false;

    private  SupportMapFragment mapFragment;

    private LinearLayout mcustomerinfo;

    //private LatLng driverdroppedpatientlatlng;

    //private Switch driverworkingswitch;

    private  int status = 0;

    private ImageView mcustomerprofileimage;
    private TextView mcustomername,mcustomerphone;

    private List<Polyline> polylines;
    private static final int[] COLORS = new int[]{R.color.primary_dark_material_light};
    //private LatLng pickuplatlng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_map2);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        polylines = new ArrayList<>();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mcustomerinfo = (LinearLayout) findViewById(R.id.customerinfo);
        mcustomerprofileimage = (ImageView) findViewById(R.id.customerprofileimage);
        mcustomername = (TextView) findViewById(R.id.customername);
        mcustomerphone = (TextView) findViewById(R.id.customerphone);

        settings = (Button) findViewById(R.id.settings);
        //driverworkingswitch = (Switch)findViewById(R.id.switch2);
        mlogout = (Button) findViewById(R.id.logout);
        //droppatient = (Button) findViewById(R.id.dropppatient);
/*
        driverworkingswitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){

                    startlocation();
                    display();
                    Toast.makeText(getApplicationContext(),"You are online",Toast.LENGTH_SHORT).show();
                }
                else {
                    stoplocationupdates();
                    Toast.makeText(getApplicationContext(),"You are offline",Toast.LENGTH_SHORT).show();
                    mcurrent.remove();
                }
            }
        });

*/
/*
        droppatient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference deletecustomer = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(customer_id).child("customerrequest").child("customerid");
                deletecustomer.child(customer_id).removeValue();
            }
        });

        */
        mlogout.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isLoggingOut = true;
                disconnectdriver();

                FirebaseAuth.getInstance().signOut();
                Intent i = new Intent(DriverMapActivity.this, MainActivity.class);
                startActivity(i);
                finish();
                return;
            }
        });

        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(DriverMapActivity.this, DriverSettingActivity.class);
                startActivity(i);
                finish();
                return;
            }
        });


        getAssignedcustomer();
    }
/*
    private void stoplocationupdates() {


    }

    private void display() {

    }

    private void startlocation() {

    }
*/
    private  void getAssignedcustomer(){
        String driver_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference assigncustomerref = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driver_id).child("customerId");
        assigncustomerref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){

                        customer_id = dataSnapshot.getValue().toString();
                        getaccidentpickuplocation();
                        getaccidentclientinfo();



                                   }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }


        });

    }



    Marker pickupmarker;
    private DatabaseReference assignedcustomerpickuplocationref;
    private  ValueEventListener assignedcustomerpickuplocationreflistener;
    private void  getaccidentpickuplocation() {
        assignedcustomerpickuplocationref = FirebaseDatabase.getInstance().getReference().child("customerrequest").child(customer_id).child("l");
         assignedcustomerpickuplocationref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() ){
                    List<Object> map = (List<Object>)dataSnapshot.getValue();
                    double locationlat = 0;
                    double locationlng = 0;
                    if(map.get(0)!=null){
                        locationlat = Double.parseDouble(map.get(0).toString());
                    }
                    if(map.get(1)!=null){
                        locationlng = Double.parseDouble(map.get(1).toString());
                    }
                    LatLng pickuplatlng = new LatLng(locationlat,locationlng);
                    mMap.addMarker(new MarkerOptions().position(pickuplatlng).title("Accident Here"));
                    getRoutetomarker(pickuplatlng);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void getRoutetomarker(LatLng pickuplatlng ) {
        Routing routing = new Routing.Builder()
                .key("@string/google_maps_key")
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .alternativeRoutes(false)
                .waypoints(new LatLng(kLastLocation.getLatitude(),kLastLocation.getLongitude()),pickuplatlng)
                .build();
        routing.execute();
    }




    private void getaccidentclientinfo(){
         mcustomerinfo.setVisibility(View.VISIBLE);
         DatabaseReference mcustomerdatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(customer_id);
         mcustomerdatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){

                    Map <String,Object> Map = (Map<String, Object>) dataSnapshot.getValue();
                    if(Map.get("name")!=null){
                        mcustomername.setText( Map.get("name").toString());

                    }
                    if(Map.get("contact")!=null){
                        mcustomerphone.setText(Map.get("contact").toString());

                    }


                    if(Map.get("profileImageUrl")!=null) {

                        Glide.with(getApplication()).load(Map.get("profileImageUrl").toString()).into(mcustomerprofileimage);
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

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.INTERNET)!=PackageManager.PERMISSION_GRANTED){
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
        if (getApplicationContext()!=null){

            kLastLocation = location;
            LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(16));
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference refavailable = FirebaseDatabase.getInstance().getReference("driverAvailable");
            DatabaseReference refworking = FirebaseDatabase.getInstance().getReference("driverworking");
            GeoFire geofireavailable = new GeoFire(refavailable);
            GeoFire geofireworking = new GeoFire(refworking);
            switch (customer_id){
                case  "":
                    geofireworking.removeLocation(userId);
                    geofireavailable.setLocation(userId,new GeoLocation(location.getLatitude(),location.getLongitude()));
                    break;

                default:
                    geofireavailable.removeLocation(userId);
                    geofireworking.setLocation(userId,new GeoLocation(location.getLatitude(),location.getLongitude()));
                    break;
            }

        }

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

    private  void disconnectdriver(){

        LocationServices.FusedLocationApi.removeLocationUpdates(kGoogleApiClient,this);
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("driverAvailable");

        GeoFire geofire = new GeoFire(ref);
        geofire.removeLocation(userId);

    }


    @Override
    protected  void onStop(){
        super.onStop();

        if(!isLoggingOut){

            disconnectdriver();
        }

    }

    @Override
    public void onRoutingFailure(RouteException e) {

        if(e != null) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }else {
            Toast.makeText(this, "Something went wrong, Try again", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {

        if(polylines.size()>0) {
            for (Polyline poly : polylines) {
                poly.remove();
            }
        }

        polylines = new ArrayList<>();
        //add route(s) to the map.
        for (int i = 0; i <route.size(); i++) {

            //In case of more than 5 alternative routes
            int colorIndex = i % COLORS.length;

            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(getResources().getColor(COLORS[colorIndex]));
            polyOptions.width(10 + i * 3);
            polyOptions.addAll(route.get(i).getPoints());
            Polyline polyline = mMap.addPolyline(polyOptions);
            polylines.add(polyline);

            Toast.makeText(getApplicationContext(),"Route "+ (i+1) +": distance - "+ route.get(i).getDistanceValue()+": duration - "+ route.get(i).getDurationValue(),Toast.LENGTH_SHORT).show();
        }


    }

    @Override
    public void onRoutingCancelled() {

    }


    private  void erasepolylines(){
        for (Polyline line : polylines){
            line.remove();
        }
        polylines.clear();
    }
}
