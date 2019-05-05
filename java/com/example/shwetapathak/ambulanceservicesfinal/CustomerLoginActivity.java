package com.example.shwetapathak.ambulanceservicesfinal;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;

public class CustomerLoginActivity extends AppCompatActivity {
    private EditText kemail, kpassword;
    private Button klogin, kregistration;
    private FirebaseAuth kauth;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;
    private Button Show;
    CheckBox Spass;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_login);

        kauth = FirebaseAuth.getInstance();
        firebaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    Intent i = new Intent(CustomerLoginActivity.this,CustomerMapActivity.class);
                    startActivity(i);
                    finish();
                    return;
                }
            }
        };
        kemail = (EditText) findViewById(R.id.Email);
        kpassword = (EditText) findViewById(R.id.Password);
        klogin=(Button)findViewById(R.id.Login);
        kregistration=(Button)findViewById(R.id.Register);
        Spass=findViewById(R.id.show);

        Spass.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    kpassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());

                }else
                {
                    kpassword.setTransformationMethod(PasswordTransformationMethod.getInstance());

                }
            }
        });





        kregistration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email=kemail.getText().toString();
                final String password=kpassword.getText().toString();
                kauth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(CustomerLoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()){

                            Toast.makeText(CustomerLoginActivity.this,"Sign Up Error",Toast.LENGTH_SHORT).show();
                        }else {
                            String user_id = kauth.getCurrentUser().getUid();
                            DatabaseReference current_user_db= FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(user_id);
                            current_user_db.setValue(true);
                        }
                    }
                });
            }
        });

        klogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = kemail.getText().toString();
                final String password = kpassword.getText().toString();
                kauth.signInWithEmailAndPassword(email,password).addOnCompleteListener(CustomerLoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()){

                            Toast.makeText(CustomerLoginActivity.this,"Sign In Error",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }
    @Override
    public  void onStart(){
        super.onStart();
        kauth.addAuthStateListener(firebaseAuthListener);
    }

    @Override
    public  void  onStop(){
        super.onStop();
        kauth.removeAuthStateListener(firebaseAuthListener);
    }
}
