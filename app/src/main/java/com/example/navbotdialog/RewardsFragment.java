package com.example.navbotdialog;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.cardview.widget.CardView;

public class RewardsFragment extends Fragment {

    public RewardsFragment() {

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_rewards, container, false);

        // KFC Reward
        CardView kfcCard = view.findViewById(R.id.kfcCard);
        kfcCard.setOnClickListener(v -> openReward("KFC (100 KP)", R.drawable.kfc));

        // TNG Reward
        CardView tngCard = view.findViewById(R.id.tngCard);
        tngCard.setOnClickListener(v -> openReward("TNG (500 KP)", R.drawable.tng));

        // XOX Reward
        CardView xoxCard = view.findViewById(R.id.xoxCard);
        xoxCard.setOnClickListener(v -> openReward("XOX (200 KP)", R.drawable.xox));

        // Grab Reward
        CardView grabCard = view.findViewById(R.id.grabCard);
        grabCard.setOnClickListener(v -> openReward("GRAB (300 KP)", R.drawable.grab));

        // DIY Reward
        CardView diyCard = view.findViewById(R.id.diyCard);
        diyCard.setOnClickListener(v -> openReward("DIY (400 KP)", R.drawable.diy));

        // Lotus Reward
        CardView lotusCard = view.findViewById(R.id.lotusCard);
        lotusCard.setOnClickListener(v -> openReward("LOTUS (400 KP)", R.drawable.lotus));

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
