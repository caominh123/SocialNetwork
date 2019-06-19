package ntduong.socialnetwork.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;
import ntduong.socialnetwork.Model.Posts;
import ntduong.socialnetwork.R;


public class MyPostsActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private RecyclerView myPostsList;
    private FirebaseAuth mAuth;
    private DatabaseReference postsRef;
    private String currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_posts);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();

        postsRef = FirebaseDatabase.getInstance().getReference().child("Posts");

        mToolbar = findViewById(R.id.my_posts_bar_layout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("My Posts");


        myPostsList = findViewById(R.id.my_all_posts_list);
        myPostsList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        myPostsList.setLayoutManager(linearLayoutManager);


        displayMyAllPost();
    }

    private void displayMyAllPost() {

        Query myPostsQuery = postsRef.orderByChild("uid")
                .startAt(currentUserID).endAt(currentUserID + "\uf8ff");

        FirebaseRecyclerOptions<Posts> firebaseRecyclerOptions = new FirebaseRecyclerOptions.Builder<Posts>()
                .setQuery(myPostsQuery, Posts.class)
                .build();

        FirebaseRecyclerAdapter<Posts, MyPostsViewHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<Posts, MyPostsViewHolder>(firebaseRecyclerOptions)
        {
            @Override
            protected void onBindViewHolder(@NonNull MyPostsViewHolder myPostsViewHolder, int i, @NonNull Posts posts) {

                final String postKey =getRef(i).getKey();

                myPostsViewHolder.setFullname(posts.getFullname());
                myPostsViewHolder.setTime(posts.getTime());
                myPostsViewHolder.setDate(posts.getDate());
                myPostsViewHolder.setDescription(posts.getDescription());
                myPostsViewHolder.setProfileimage(posts.getProfileimage());
                myPostsViewHolder.setPostimage(posts.getPostimage());

                myPostsViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent clickPostIntent = new Intent(MyPostsActivity.this,ClickPostActivity.class);
                        clickPostIntent.putExtra("PostKey",postKey);
                        startActivity(clickPostIntent);
                    }
                });
            }

            @NonNull
            @Override
            public MyPostsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_posts_layout, parent, false);

                return new MyPostsViewHolder(view);
            }
        };

        firebaseRecyclerAdapter.startListening();
        myPostsList.setAdapter(firebaseRecyclerAdapter);
    }


    public static class  MyPostsViewHolder extends RecyclerView.ViewHolder{

        View mView;

        public MyPostsViewHolder(@NonNull View itemView) {
            super(itemView);

            mView = itemView;
        }

        public void setFullname(String fullname) {
            TextView username = mView.findViewById(R.id.post_user_name);
            username.setText(fullname);
        }

        public void setProfileimage(String profileimage) {
            CircleImageView imageView = mView.findViewById(R.id.post_profile_image);
            Picasso.get().load(profileimage).into(imageView);
        }

        public void setTime(String time) {
            TextView PostTime = mView.findViewById(R.id.post_time);
            PostTime.setText("  " + time);
        }

        public void setDate(String date) {
            TextView PostDate = mView.findViewById(R.id.post_date);
            PostDate.setText("  " + date);
        }

        public void setDescription(String description) {
            TextView PostDiscription = mView.findViewById(R.id.click_post_description);
            PostDiscription.setText(description);
        }

        public void setPostimage(String postimage) {
            ImageView PostImage = mView.findViewById(R.id.click_post_image);
            Picasso.get().load(postimage).into(PostImage);
        }
    }
}
