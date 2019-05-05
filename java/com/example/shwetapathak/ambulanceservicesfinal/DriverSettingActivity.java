package com.example.shwetapathak.ambulanceservicesfinal;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class DriverSettingActivity extends AppCompatActivity {

    private EditText mnamefield,mcontactfield,ambulancefield;
    private Button mback,mconfirm;
    private FirebaseAuth mauth;
    private DatabaseReference mdriverdatabase;
    private String user_id;
    private  String mname;
    private  String mProfileimageUrl;
    private String mcontact;
    private String typeofambulance;
    private  String service;
    private ImageView mProfileimage;
    private  Uri resultUri;
    private RadioGroup mradioGroup;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_setting);

        mnamefield = (EditText)findViewById(R.id.name);
        mcontactfield = (EditText)findViewById(R.id.contact);
        ambulancefield = (EditText)findViewById(R.id.typeofambulance);
        mback = (Button)findViewById(R.id.back);
        mconfirm = (Button)findViewById(R.id.confirm);
        mProfileimage = (ImageView)findViewById(R.id.profileimage);
        mradioGroup = (RadioGroup) findViewById(R.id.radiogroup);

        mauth = FirebaseAuth.getInstance();
        user_id = mauth.getCurrentUser().getUid();

        mdriverdatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(user_id);
        getuserinfo();

        mProfileimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent,1);

            }
        });
        mconfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveuserinfo();
                Intent i= new Intent(DriverSettingActivity.this,DriverMapActivity.class);
                startActivity(i);

            }
        });
        mback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i= new Intent(DriverSettingActivity.this,DriverMapActivity.class);
                startActivity(i);
            }
        });
    }

    private void getuserinfo(){
        mdriverdatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){

                    Map <String,Object> Map = (Map<String, Object>) dataSnapshot.getValue();
                    if(Map.get("name")!=null){
                        mname = Map.get("name").toString();
                        mnamefield.setText(mname);

                    }
                    if(Map.get("contact")!=null){
                        mcontact = Map.get("contact").toString();
                        mcontactfield.setText(mcontact);

                    }

                    if(Map.get("typeofambulance")!=null){
                        typeofambulance = Map.get("typeofambulance").toString();
                        ambulancefield.setText(typeofambulance);

                    }

                    if(Map.get("service")!=null){
                        service = Map.get("service").toString();
                        switch (service){
                            case "Simple":
                                mradioGroup.check(R.id.simple);
                                break;

                            case "Cardiac":
                                mradioGroup.check(R.id.cardiac);
                                break;

                        }
                    }

                    Glide.clear(mProfileimage);
                    if(Map.get("profileImageUrl")!=null) {
                        mProfileimageUrl= Map.get("profileImageUrl").toString();
                        Glide.with(getApplication()).load(mProfileimageUrl).into(mProfileimage);
                    }

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void saveuserinfo() {
        mname = mnamefield.getText().toString();
        mcontact = mcontactfield.getText().toString();
        typeofambulance = ambulancefield.getText().toString();

        int selectId = mradioGroup.getCheckedRadioButtonId();
        final RadioButton radioButton = (RadioButton) findViewById(selectId);
        if (radioButton.getText()==null){
            return;
        }

        service  = radioButton.getText().toString();

        Map userinfo = new HashMap();
        userinfo.put("name",mname);
        userinfo.put("contact",mcontact);
        userinfo.put("typeofambulance",typeofambulance);
        userinfo.put("services",service);

        mdriverdatabase.updateChildren(userinfo);

        if(resultUri!= null) {
            StorageReference filePath = FirebaseStorage.getInstance().getReference().child("profile_images").child(user_id);
            Bitmap bitmap = null;

            try {
                bitmap = MediaStore.Images.Media.getBitmap(getApplication().getContentResolver(), resultUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            ByteArrayOutputStream boas = new ByteArrayOutputStream();

            bitmap.compress(Bitmap.CompressFormat.JPEG, 20, boas);

            byte[] data = boas.toByteArray();
            UploadTask uploadTask = filePath.putBytes(data);

            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    finish();
                    return;
                }
            });
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    Task<Uri> downloadUrl = taskSnapshot.getStorage().getDownloadUrl();

                    Map newImage=new HashMap();
                    newImage.put("profileImageUrl",downloadUrl.toString());
                    mdriverdatabase.updateChildren(newImage);
                    finish();
                    return;

                }
            });

        }else {
            finish();
        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==1 && resultCode== Activity.RESULT_OK){
            final Uri imageUri = data.getData();
            resultUri = imageUri;
            mProfileimage.setImageURI(resultUri);

        }
    }
}
