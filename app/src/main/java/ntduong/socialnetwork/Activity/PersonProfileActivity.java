package ntduong.socialnetwork.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;
import ntduong.socialnetwork.R;

public class PersonProfileActivity extends AppCompatActivity {

    enum STATE {
        NOT_FRIEND, WAITING_FOR_FEEDBACK, BE_FRIEND, RECEIVED_REQUEST
    }

    TextView userName, userProfileName, userStatus, userCountry, userDateOfBirth, userGender, userRelation;
    CircleImageView userProfileImage;
    Button sendFriendRequestBtn, declineFriendRequestBtn;

    DatabaseReference friendRequestRef, userRef, friendsRef;
    FirebaseAuth mAuth;
    String senderUserID, receiverUserID, saveCurrentDate;
    STATE current_state;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_profile);

        mAuth = FirebaseAuth.getInstance();
        receiverUserID = getIntent().getExtras().get("visit_userID").toString();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        friendRequestRef = FirebaseDatabase.getInstance().getReference().child("FriendRequests");
        friendsRef = FirebaseDatabase.getInstance().getReference().child("FriendsList");
        senderUserID = mAuth.getCurrentUser().getUid();

        InitializeFields();

        userRef.child(receiverUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String username = dataSnapshot.child("userName").getValue().toString();
                    String fullName = dataSnapshot.child("fullName").getValue().toString();
                    String status = dataSnapshot.child("status").getValue().toString();
                    String country = dataSnapshot.child("countryName").getValue().toString();
                    String dob = dataSnapshot.child("dob").getValue().toString();
                    String gender = dataSnapshot.child("gender").getValue().toString();
                    String relationshipstatus = dataSnapshot.child("relationshipstatus").getValue().toString();
                    String profileImage = dataSnapshot.child("profileimage").getValue().toString();

                    Picasso.get().load(profileImage).placeholder(R.drawable.profile).into(userProfileImage);
                    userName.setText("@" + username);
                    userProfileName.setText(fullName);
                    userStatus.setText(status);
                    userCountry.setText("Quốc tịch: " + country);
                    userDateOfBirth.setText("Ngày sinh: " + dob);
                    userGender.setText("Giới tính: " + gender);
                    userRelation.setText("Tình trạng hôn nhân: " + relationshipstatus);

                    MaintenanceOfButtons();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        declineFriendRequestBtn.setVisibility(View.INVISIBLE);
        declineFriendRequestBtn.setEnabled(false);

        if (!senderUserID.equals(receiverUserID)) {
            sendFriendRequestBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    sendFriendRequestBtn.setEnabled(false);
                    if (current_state == STATE.NOT_FRIEND) {
                        SendFriendRequestToPerson();
                    }
                    if (current_state == STATE.WAITING_FOR_FEEDBACK) {
                        CancelFriendRequest();
                    }
                    if (current_state == STATE.RECEIVED_REQUEST) {
                        AcceptFriendRequest();
                    }
                    if(current_state == STATE.BE_FRIEND){
                        UnFriendAnExistingFriend();
                    }
                }
            });
        } else {
            declineFriendRequestBtn.setVisibility(View.INVISIBLE);
            sendFriendRequestBtn.setVisibility(View.INVISIBLE);
        }
    }


    private void UnFriendAnExistingFriend() {
        friendsRef.child(senderUserID).child(receiverUserID)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            friendsRef.child(receiverUserID).child(senderUserID)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                sendFriendRequestBtn.setEnabled(true);
                                                current_state = STATE.NOT_FRIEND;
                                                sendFriendRequestBtn.setText("Mời kết bạn");

                                                declineFriendRequestBtn.setVisibility(View.INVISIBLE);
                                                declineFriendRequestBtn.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void AcceptFriendRequest() {
        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MM-yyyy");
        saveCurrentDate = currentDate.format(calForDate.getTime());

        // Thiết lập mối quan hệ bạn bè
        friendsRef.child(senderUserID).child(receiverUserID).child("date").setValue(saveCurrentDate)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            friendsRef.child(receiverUserID).child(senderUserID).child("date").setValue(saveCurrentDate)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            // Kết bạn thành công , xóa lời mời kết bạn trong CSDL
                                            friendRequestRef.child(senderUserID).child(receiverUserID)
                                                    .removeValue()
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()) {
                                                                friendRequestRef.child(receiverUserID).child(senderUserID)
                                                                        .removeValue()
                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                if (task.isSuccessful()) {
                                                                                    sendFriendRequestBtn.setEnabled(true);
                                                                                    current_state = STATE.BE_FRIEND;
                                                                                    sendFriendRequestBtn.setText("Hủy kết bạn");

                                                                                    declineFriendRequestBtn.setVisibility(View.INVISIBLE);
                                                                                    declineFriendRequestBtn.setEnabled(false);
                                                                                }
                                                                            }
                                                                        });
                                                            }
                                                        }
                                                    });
                                        }
                                    });
                        }
                    }
                });

    }

    // Phuong thuc huy ket ban
    private void CancelFriendRequest() {
        friendRequestRef.child(senderUserID).child(receiverUserID)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            friendRequestRef.child(receiverUserID).child(senderUserID)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                sendFriendRequestBtn.setEnabled(true);
                                                current_state = STATE.NOT_FRIEND;
                                                sendFriendRequestBtn.setText("Mời kết bạn");

                                                declineFriendRequestBtn.setVisibility(View.INVISIBLE);
                                                declineFriendRequestBtn.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    // Phương thức kiểm tra trạng thái bạn bè
    private void MaintenanceOfButtons() {
        friendRequestRef.child(senderUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(receiverUserID)) {
                    String request_type = dataSnapshot.child(receiverUserID).child("request_type").getValue().toString();
                    if (request_type.equals("sent")) {
                        current_state = STATE.WAITING_FOR_FEEDBACK;
                        sendFriendRequestBtn.setText("Huỷ mời kết bạn");

                        declineFriendRequestBtn.setVisibility(View.INVISIBLE);
                        declineFriendRequestBtn.setEnabled(false);
                    } else if (request_type.equals("received")) {
                        current_state = STATE.RECEIVED_REQUEST;
                        sendFriendRequestBtn.setText("Chấp nhận kết bạn");

                        declineFriendRequestBtn.setVisibility(View.VISIBLE);
                        declineFriendRequestBtn.setEnabled(true);

                        declineFriendRequestBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                CancelFriendRequest();
                            }
                        });
                    }
                } else {
                    friendsRef.child(senderUserID).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.hasChild(receiverUserID)){
                                current_state = STATE.BE_FRIEND;
                                sendFriendRequestBtn.setText("Hủy kết bạn");

                                declineFriendRequestBtn.setVisibility(View.INVISIBLE);
                                declineFriendRequestBtn.setEnabled(false);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    // Phương thức tạo lòi mời kết bạn
    private void SendFriendRequestToPerson() {
        friendRequestRef.child(senderUserID).child(receiverUserID)
                .child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            friendRequestRef.child(receiverUserID).child(senderUserID)
                                    .child("request_type").setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                sendFriendRequestBtn.setEnabled(true);
                                                current_state = STATE.WAITING_FOR_FEEDBACK;
                                                sendFriendRequestBtn.setText("Hủy mời kết bạn");

                                                declineFriendRequestBtn.setVisibility(View.INVISIBLE);
                                                declineFriendRequestBtn.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void InitializeFields() {

        userName = findViewById(R.id.person_profile_username);
        userProfileName = findViewById(R.id.person_profile_full_name);
        userStatus = findViewById(R.id.person_profile_status);
        userCountry = findViewById(R.id.person_profile_country);
        userDateOfBirth = findViewById(R.id.person_profile_dob);
        userGender = findViewById(R.id.person_profile_gender);
        userRelation = findViewById(R.id.person_profile_relationship_status);
        userProfileImage = findViewById(R.id.person_profile_image);
        sendFriendRequestBtn = findViewById(R.id.person_send_friend_request_btn);
        declineFriendRequestBtn = findViewById(R.id.person_decline_friend_btn);

        current_state = STATE.NOT_FRIEND;
    }
}
