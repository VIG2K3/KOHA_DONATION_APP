package com.example.navbotdialog;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.cardview.widget.CardView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class RewardsFragment extends Fragment {

    private TextView kohaPoints; // Added TextView for total points
    private DatabaseReference userPointsRef;
    private String uid;

    public RewardsFragment() { }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_rewards, container, false);

        // Find the TextView in your layout
        kohaPoints = view.findViewById(R.id.kohaPoints);

        // Firebase setup for live points
        uid = FirebaseAuth.getInstance().getUid();
        if (uid != null) {
            userPointsRef = FirebaseDatabase.getInstance(
                            "https://koha-user-points.asia-southeast1.firebasedatabase.app/")
                    .getReference("users")
                    .child(uid)
                    .child("points");

            userPointsRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Long points = snapshot.getValue(Long.class);
                    if (points != null) {
                        kohaPoints.setText("KO-POINTS: " + points);
                    } else {
                        kohaPoints.setText("KO-POINTS: 0");
                        userPointsRef.setValue(0); // initialize in DB if missing
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    kohaPoints.setText("KO-POINTS: 0");
                }
            });
        } else {
            kohaPoints.setText("KO-POINTS: 0");
        }

        // ---------------------- Reward Cards ----------------------
        // KFC Reward
        CardView kfcCard = view.findViewById(R.id.kfcCard);
        kfcCard.setOnClickListener(v -> openReward("KFC (100 KO-Points)", R.drawable.kfc));

        // TNG Reward
        CardView tngCard = view.findViewById(R.id.tngCard);
        tngCard.setOnClickListener(v -> openReward("TNG (500 KO-Points)", R.drawable.tng));

        // XOX Reward
        CardView xoxCard = view.findViewById(R.id.xoxCard);
        xoxCard.setOnClickListener(v -> openReward("XOX (200 KO-Points)", R.drawable.xox));

        // Grab Reward
        CardView grabCard = view.findViewById(R.id.grabCard);
        grabCard.setOnClickListener(v -> openReward("GRAB (300 KO-Points)", R.drawable.grab));

        // DIY Reward
        CardView diyCard = view.findViewById(R.id.diyCard);
        diyCard.setOnClickListener(v -> openReward("DIY (400 KO-Points)", R.drawable.diy));

        // Lotus Reward
        CardView lotusCard = view.findViewById(R.id.lotusCard);
        lotusCard.setOnClickListener(v -> openReward("LOTUS (400 KO-Points)", R.drawable.lotus));

        return view;
    }

    // Helper function to open RewardDash with reward details
    private void openReward(String name, int imageRes) {
        Intent intent = new Intent(getActivity(), RewardDash.class);
        intent.putExtra("reward_name", name);
        intent.putExtra("reward_image", imageRes);
        startActivity(intent);
    }
}
