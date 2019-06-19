package ntduong.socialnetwork.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import ntduong.socialnetwork.Model.Friend;
import ntduong.socialnetwork.R;

public class FriendsListActivity extends AppCompatActivity {

    RecyclerView myFriendList;
    DatabaseReference friendsRef, usersRef;
    FirebaseAuth mAuth;
    String onlineUserID, currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_list);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        friendsRef = FirebaseDatabase.getInstance().getReference().child("FriendsList").child(currentUserID);
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");


        myFriendList = findViewById(R.id.friend_list);
        myFriendList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        myFriendList.setLayoutManager(linearLayoutManager);

        DisplayAllFriends();
    }


    public void updateUserStatus(String state){


        String saveCurrentDate, saveCurrentTime;
        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate = currentDate.format(calForDate.getTime());

        Calendar calForTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calForTime.getTime());

        Map currentStateMap = new HashMap();
        currentStateMap.put("time", saveCurrentTime);
        currentStateMap.put("date", saveCurrentDate);
        currentStateMap.put("type", state);

        usersRef.child(currentUserID).child("userState")
                .updateChildren(currentStateMap);
    }


    @Override
    protected void onStart() {
        super.onStart();

        updateUserStatus("online");
    }


    @Override
    protected void onStop() {
        super.onStop();

        updateUserStatus("offline");
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        updateUserStatus("offline");
    }

    private void DisplayAllFriends() {

        FirebaseRecyclerOptions<Friend> options =
                new FirebaseRecyclerOptions.Builder<Friend>()
                        .setQuery(friendsRef, Friend.class)
                        .build();

        FirebaseRecyclerAdapter<Friend, FriendsListViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Friend, FriendsListViewHolder>(options) {

                    @NonNull
                    @Override
                    public FriendsListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_users_display_layout, parent, false);

                        return new FriendsListViewHolder(view);
                    }

                    @Override
                    public void onBindViewHolder(@NonNull final FriendsListViewHolder friendsListViewHolder, int position, @NonNull Friend friend) {

                        friendsListViewHolder.setDate(friend.getDate());

                        final String friendID = getRef(position).getKey();

                        usersRef.child(friendID).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    final String fullName = dataSnapshot.child("fullName").getValue().toString();
                                    String profileImage = dataSnapshot.child("profileimage").getValue().toString();
                                    final String type;

                                    if(dataSnapshot.hasChild("userState")){

                                        type = dataSnapshot.child("userState").child("type").getValue().toString();

                                        if(type.equals("online")){

                                            friendsListViewHolder.onlineStatusView.setVisibility(View.VISIBLE);
                                        }else {

                                            friendsListViewHolder.onlineStatusView.setVisibility(View.INVISIBLE);
                                        }
                                    }

                                    friendsListViewHolder.setFullName(fullName);
                                    friendsListViewHolder.setProfileImage(profileImage);

                                    friendsListViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            CharSequence options[] = new CharSequence[]{
                                                    fullName+"'s Profile",
                                                    "Send Message"
                                            };
                                            AlertDialog.Builder builder = new AlertDialog.Builder(FriendsListActivity.this);
                                            builder.setTitle("Select Option");

                                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int position) {
                                                    if(position == 0){
                                                        Intent profileIntent = new Intent(FriendsListActivity.this,PersonProfileActivity.class);
                                                        profileIntent.putExtra("visit_userID",friendID);
                                                        startActivity(profileIntent);
                                                    }
                                                    if(position == 1){
                                                        Intent chatIntent = new Intent(FriendsListActivity.this, ChatActivity.class);
                                                        chatIntent.putExtra("visit_userID",friendID);
                                                        chatIntent.putExtra("fullName",fullName);
                                                        startActivity(chatIntent);
                                                    }
                                                }
                                            });
                                            builder.show();
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }

                };
        firebaseRecyclerAdapter.startListening();
        myFriendList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class FriendsListViewHolder extends RecyclerView.ViewHolder {

        View mView;
        ImageView onlineStatusView;

        public FriendsListViewHolder(@NonNull View itemView) {
            super(itemView);
            this.mView = itemView;
            onlineStatusView = itemView.findViewById(R.id.all_user_online_icon);
        }

        public void setProfileImage(String profileImage) {
            CircleImageView myImage = mView.findViewById(R.id.all_users_profile_image);
            Picasso.get().load(profileImage).placeholder(R.drawable.profile).into(myImage);
        }

        public void setFullName(String fullname) {
            TextView myName = mView.findViewById(R.id.all_users_profile_full_name);
            myName.setText(fullname);
        }

        public void setDate(String date) {
            TextView myDate = mView.findViewById(R.id.all_users_status);
            String info ="Ngày kết bạn: "+date;
            myDate.setText(info);
        }

    }


}
