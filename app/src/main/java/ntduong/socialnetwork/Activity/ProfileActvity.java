package ntduong.socialnetwork.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;
import ntduong.socialnetwork.R;

public class ProfileActvity extends AppCompatActivity {

    TextView userName, userProfileName, userStatus, userCountry, userDateOfBirth, userGender, userRelation;
    CircleImageView userProfileImage;

    DatabaseReference profileUserRef, friendsRef, postsRef;
    FirebaseAuth mAuth;
    String currentUserID;
    int countFriends = 0, countPosts = 0;

    Button myPosts, myFriends;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_actvity);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        profileUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);
        friendsRef = FirebaseDatabase.getInstance().getReference().child("FriendsList");
        postsRef = FirebaseDatabase.getInstance().getReference().child("Posts");

        userName = findViewById(R.id.my_profile_username);
        userProfileName = findViewById(R.id.my_profile_full_name);
        userStatus = findViewById(R.id.my_profile_status);
        userCountry = findViewById(R.id.my_profile_country);
        userDateOfBirth = findViewById(R.id.my_profile_dob);
        userGender = findViewById(R.id.my_profile_gender);
        userRelation = findViewById(R.id.my_profile_relationship_status);
        userProfileImage = findViewById(R.id.my_profile_image);
        myFriends = findViewById(R.id.my_friends_button);
        myPosts = findViewById(R.id.my_post_button);


        myFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SendUserToFriendsListActivity();
            }
        });

        myPosts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SendUserToMyPostsListActivity();
            }
        });

        postsRef.orderByChild("uid")
                .startAt(currentUserID)
                .endAt(currentUserID + "\uf8ff")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if(dataSnapshot.exists()){

                            countPosts = (int) dataSnapshot.getChildrenCount();
                            myPosts.setText(countPosts + " Posts");
                        }else {

                            myPosts.setText("0 Posts");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

        friendsRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists()){

                    countFriends = (int) dataSnapshot.getChildrenCount();
                    myFriends.setText(countFriends + " friends");
                }else {

                    myFriends.setText("0 friends");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        profileUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    String username = dataSnapshot.child("userName").getValue().toString();
                    String fullName = dataSnapshot.child("fullName").getValue().toString();
                    String status = dataSnapshot.child("status").getValue().toString();
                    String country = dataSnapshot.child("countryName").getValue().toString();
                    String dob = dataSnapshot.child("dob").getValue().toString();
                    String gender = dataSnapshot.child("gender").getValue().toString();
                    String relationshipstatus = dataSnapshot.child("relationshipstatus").getValue().toString();
                    String profileImage = dataSnapshot.child("profileimage").getValue().toString();

                    Picasso.get().load(profileImage).placeholder(R.drawable.profile).into(userProfileImage);
                    userName.setText("@"+username);
                    userProfileName.setText(fullName);
                    userStatus.setText(status);
                    userCountry.setText("Quốc tịch: "+country);
                    userDateOfBirth.setText("Ngày sinh: "+dob);
                    userGender.setText("Giới tính: "+gender);
                    userRelation.setText("Tình trạng hôn nhân: "+relationshipstatus);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void SendUserToFriendsListActivity() {
        Intent friendListIntent = new Intent(ProfileActvity.this, FriendsListActivity.class);
        startActivity(friendListIntent);
    }

    private void SendUserToMyPostsListActivity() {
        Intent friendListIntent = new Intent(ProfileActvity.this, MyPostsActivity.class);
        startActivity(friendListIntent);
    }
}
