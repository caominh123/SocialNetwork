package ntduong.socialnetwork.Activity;

import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import ntduong.socialnetwork.R;


public class LoginActivity extends AppCompatActivity {

    Button LoginButton;
    EditText UserEmail,UserPassword;
    TextView NeedNewAccountLink,resetPasswordLink;
    FirebaseAuth mAuth;
    private Boolean emailAddressChecker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        NeedNewAccountLink = findViewById(R.id.register_account_link);
        UserEmail = findViewById(R.id.login_email);
        UserPassword = findViewById(R.id.login_password);
        LoginButton = findViewById(R.id.login_button);
        resetPasswordLink= findViewById(R.id.forget_password_link);

        NeedNewAccountLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendUserToRegisterActivity();
            }
        });

        resetPasswordLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendUserToResetPasswordActivity();
            }
        });
        LoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AllowingUserToLogin();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser != null){
            SendUserToMainActivity();
        }
    }

    // Method Login
    private void AllowingUserToLogin(){
        String email = UserEmail.getText().toString();
        String password = UserPassword.getText().toString();

        if(TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Bạn chưa nhập Email !!!!", Toast.LENGTH_SHORT).show();
        }else if(TextUtils.isEmpty(password)){
            Toast.makeText(this, "Bạn chưa nhập mật khẩu !!!!", Toast.LENGTH_SHORT).show();
        }else{
            mAuth.signInWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){

//                                verifyEmailAddress();
                                SendUserToMainActivity();
                            }else{
                                String message = task.getException().getMessage();
                                Toast.makeText(LoginActivity.this,"Lỗi : "+message,Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }


//    private void verifyEmailAddress(){
//
//        FirebaseUser user = mAuth.getCurrentUser();
//        emailAddressChecker = user.isEmailVerified();
//
//        if(emailAddressChecker){
//
//            SendUserToMainActivity();
//        }else {
//
//            Toast.makeText(this, "please verify your account first...", Toast.LENGTH_SHORT).show();
//            mAuth.signOut();
//        }
//    }


    //
    void SendUserToRegisterActivity(){
        Intent registerIntent = new Intent(LoginActivity.this,RegisterActivity.class);
        startActivity(registerIntent);
    }

    void SendUserToResetPasswordActivity(){
        Intent resetIntent = new Intent(LoginActivity.this,ResetPasswordActivity.class);
        startActivity(resetIntent);
    }

    //
    void SendUserToMainActivity(){
        Intent mainIntent = new Intent(LoginActivity.this,MainActivity.class);
        mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}
