package com.example.kohasignuplogin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class DonationCategoriesFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_donation_categories, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageView backArrow = view.findViewById(R.id.back_arrow);

        backArrow.setOnClickListener(v -> {
            HomeFragment homeFragment = new HomeFragment();

            requireActivity().getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(
                            R.anim.slide_in_left, // enter
                            R.anim.slide_out_right, // exit
                            R.anim.slide_in_left, // pop enter
                            R.anim.slide_out_right // pop exit
                    )
                    .replace(R.id.frame_layout, homeFragment)
                    .addToBackStack(null)
                    .commit();
        });
    }
}
