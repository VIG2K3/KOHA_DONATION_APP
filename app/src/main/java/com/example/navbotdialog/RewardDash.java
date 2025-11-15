package com.example.navbotdialog;

import android.os.Bundle;
import android.text.Html;
import android.view.Gravity;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class RewardDash extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reward_dash);

        TextView rewardTitle = findViewById(R.id.clothTitle);
        ImageView rewardImage = findViewById(R.id.clothImage);
        TextView rewardDescription = findViewById(R.id.rewardDescription);
        Button redeemButton = findViewById(R.id.redeemButton);

        // Get data from intent
        String name = getIntent().getStringExtra("reward_name");
        int imageRes = getIntent().getIntExtra("reward_image", R.drawable.kfc);

        rewardTitle.setText(name);
        rewardImage.setImageResource(imageRes);

        // Set description based on reward
        String description = "";
        switch (name) {
            case "KFC (100 KP)":
                description = "<b>CRAVING SOMETHING DELICIOUS?</b><br><br>" +
                        "Redeem this exclusive KFC voucher and enjoy your favorite fried chicken meals and combos. " +
                        "Perfect for lunch, dinner, or a quick snack — all for just 100 KO-Points!";
                redeemButton.setText("Redeem for 100 KO-Points");
                break;

            case "TNG (500 KP)":
                description = "<b>STAY CASHLESS AND CONVENIENT!</b><br><br>" +
                        "Use your KO-Points to redeem a Touch 'n Go eWallet top-up worth RM10. " +
                        "Perfect for tolls, parking, and everyday purchases — only 500 KO-Points needed!";
                redeemButton.setText("Redeem for 500 KO-Points");
                break;

            case "XOX (200 KP)":
                description = "<b>RUNNING LOW ON MOBILE CREDIT?</b><br><br>" +
                        "Redeem your XOX prepaid top-up and stay connected with your friends and family anytime, anywhere. " +
                        "Get RM5 credit instantly for just 200 KO-Points!";
                redeemButton.setText("Redeem for 200 KO-Points");

                break;

            case "GRAB (300 KP)":
                description = "<b>GO ANYWHERE, EAT ANYTHING!</b><br><br>" +
                        "Redeem this Grab voucher and enjoy rides or food delivery at your convenience. " +
                        "Travel smarter and dine easier — all for 300 KO-Points!";
                redeemButton.setText("Redeem for 300 KO-Points");
                break;

            case "DIY (400 KP)":
                description = "<b>TIME TO GET CREATIVE!</b><br><br>" +
                        "Redeem a MR.DIY shopping voucher and explore a world of tools, gadgets, and home essentials. " +
                        "Upgrade your home or find something handy — all with 400 KO-Points!";
                redeemButton.setText("Redeem for 400 KO-Points");
                break;

            case "LOTUS (400 KP)":
                description = "<b>SHOP MORE, SAVE MORE!</b><br><br>" +
                        "Use your KO-Points to redeem a Lotus’s shopping voucher you can use for groceries, daily essentials, and more. " +
                        "Turn your points into savings — only 400 KO-Points!";
                redeemButton.setText("Redeem for 400 KO-Points");
                break;
        }

        // Apply the formatted description
        rewardDescription.setText(Html.fromHtml(description));
        rewardDescription.setTextSize(20);
        rewardDescription.setLineSpacing(6, 1);
        rewardDescription.setGravity(Gravity.CENTER);


        // Handle redeem button click
        redeemButton.setOnClickListener(v -> {
            new androidx.appcompat.app.AlertDialog.Builder(RewardDash.this)
                    .setTitle("Confirm Redemption?")
                    .setMessage("Are you sure you want to redeem this reward?\n\n" + redeemButton.getText().toString())
                    .setPositiveButton("Yes", (dialog, which) -> {
                        new androidx.appcompat.app.AlertDialog.Builder(RewardDash.this)
                                .setTitle("Redemption Successful! 🎉")
                                .setMessage("You have successfully redeemed " + name)
                                .setPositiveButton("OK", (d, w) -> d.dismiss())
                                .show();
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> {
                    })
                    .setIcon(R.drawable.points)
                    .show();
        });
    }
}