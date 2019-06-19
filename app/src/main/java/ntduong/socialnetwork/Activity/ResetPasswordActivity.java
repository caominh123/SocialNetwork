package ntduong.socialnetwork.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import ntduong.socialnetwork.R;

public class ResetPasswordActivity extends AppCompatActivity {

    Toolbar mToolbar;

    EditText resetEmail;
    Button sendEmailButton;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        mAuth = FirebaseAuth.getInstance();

        mToolbar = findViewById(R.id.forget_password_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Quên mật khẩu");

        resetEmail = findViewById(R.id.reset_password_email);
        sendEmailButton = findViewById(R.id.reset_password_button);

        sendEmailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userEmail = resetEmail.getText().toString();

                if(TextUtils.isEmpty(userEmail)){
                    Toast.makeText(ResetPasswordActivity.this,"Vui lòng nhập email !!!",Toast.LENGTH_SHORT).show();
                }
                else{
                    mAuth.sendPasswordResetEmail(userEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(ResetPasswordActivity.this, "Đã gửi thư đến hộp thư của bạn \n Vui lòng kiểm tra hộp thư của bạn",
                                        Toast.LENGTH_SHORT).show();
                                SendUserToLoginActivity();
                            }
                            else{
                                Toast.makeText(ResetPasswordActivity.this, "Xảy ra lỗi !!!"+task.getException().getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });

    }

    void SendUserToLoginActivity(){
        Intent loginIntent = new Intent(ResetPasswordActivity.this, LoginActivity.class);
        startActivity(loginIntent);
    }

}
