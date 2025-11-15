package com.example.navbotdialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.android.material.bottomappbar.BottomAppBar;

public class MainActivity extends AppCompatActivity {

    FloatingActionButton fab;
    DrawerLayout drawerLayout;
    BottomNavigationView bottomNavigationView;
    BottomAppBar bottomAppBar;

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Initialize Views
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        fab = findViewById(R.id.fab);
        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        Toolbar toolbar = findViewById(R.id.toolbar);
        bottomAppBar = findViewById(R.id.bottomAppBar);

        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.open_nav, R.string.close_nav);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Default Fragment
        if (savedInstanceState == null) {
            replaceFragment(new HomeFragment());
            navigationView.setCheckedItem(R.id.nav_home);
            showBottomBar(true);
        }

        // Drawer Navigation Handling
        navigationView.setNavigationItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                selectedFragment = new HomeFragment();
                showBottomBar(true);
            } else if (id == R.id.nav_profile) {
                selectedFragment = new ProfileFragment();
                showBottomBar(false);
            } else if (id == R.id.nav_change_password) {
                selectedFragment = new ChangePasswordFragment();
                showBottomBar(false);
            } else if (id == R.id.nav_about) {
                selectedFragment = new AboutFragment();
                showBottomBar(false);
            } else if (id == R.id.nav_logout) {
                showLogoutDialog();
                return true;
            }

            if (selectedFragment != null) {
                replaceFragment(selectedFragment);
            }

            drawerLayout.closeDrawers();
            return true;
        });

        // Bottom Navigation Handling
        bottomNavigationView.setBackground(null);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.bottom_home) {
                replaceFragment(new HomeFragment());
            } else if (itemId == R.id.rewards) {
                replaceFragment(new RewardsFragment());
            } else if (itemId == R.id.location) {
                replaceFragment(new LocationFragment());
            } else if (itemId == R.id.service) {
                replaceFragment(new ServiceRequestFragment());
            }

            showBottomBar(true); // Always show for bottom tabs
            return true;
        });

        // Floating Action Button
        fab.setOnClickListener(view -> showBottomDialog());
    }

    // Replace fragments dynamically
    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }

    //  Function to Show / Hide Bottom Bar
    private void showBottomBar(boolean show) {
        if (bottomAppBar == null || fab == null || bottomNavigationView == null)
            return;

        if (show) {
            bottomAppBar.setVisibility(View.VISIBLE);
            fab.setVisibility(View.VISIBLE);
            bottomNavigationView.setVisibility(View.VISIBLE);
        } else {
            bottomAppBar.setVisibility(View.GONE);
            fab.setVisibility(View.GONE);
            bottomNavigationView.setVisibility(View.GONE);
        }
    }

    //  Logout confirmation dialog
    private void showLogoutDialog() {
        new AlertDialog.Builder(MainActivity.this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    FirebaseAuth.getInstance().signOut();
                    Toast.makeText(MainActivity.this, "User logged out", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(MainActivity.this, OpeningActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    //  Custom Bottom Sheet Dialog
    private void showBottomDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.bottomsheetlayout);

        LinearLayout qrLayout = dialog.findViewById(R.id.layoutQR);
        LinearLayout scanLayout = dialog.findViewById(R.id.layoutScan);

        qrLayout.setOnClickListener(v -> {
            dialog.dismiss();
            Intent intent = new Intent(MainActivity.this, UserQRCode.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_up, 0);
        });

        scanLayout.setOnClickListener(v -> {
            dialog.dismiss();
            Intent intent = new Intent(MainActivity.this, QRScanner.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_up, 0);
        });

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
    }

}

