package com.example.smarthomecontrolapp;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {
BottomNavigationView bottomNav;
DrawerLayout drawerLayout;
NavigationView navigationView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        init();
        setupDrawerHeader();
        setupDrawer();
        setupBottomNav();
        if(savedInstanceState==null)
        {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,new HomeFragment()).commit();
            bottomNav.setSelectedItemId(R.id.nav_home);
        }
    }

    private void setupBottomNav() {
        bottomNav.setOnItemSelectedListener(item->
        {Fragment selectedFragment=null;
            int id=item.getItemId();
            if(id==R.id.nav_home)
            {selectedFragment=new HomeFragment();

            }
            else if(id==R.id.nav_rooms)
            {
                selectedFragment=new RoomsFragment();
            }
            else if(id==R.id.nav_energy)
            {
                selectedFragment=new EnergyFragment();
            }
            else if(id==R.id.nav_details)
            {
                selectedFragment=new DetailsFragment();
            }
            if (selectedFragment != null) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }
return true;
        });
    }

    private void setupDrawer() {
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            Fragment selected = null;

            if (id == R.id.drawer_home) {
                selected = new HomeFragment();
                bottomNav.setSelectedItemId(R.id.nav_home);

            } else if (id == R.id.drawer_rooms) {
                selected = new RoomsFragment();
                bottomNav.setSelectedItemId(R.id.nav_rooms);

            } else if (id == R.id.drawer_energy) {
                selected = new EnergyFragment();
                bottomNav.setSelectedItemId(R.id.nav_energy);

            } else if (id == R.id.drawer_details) {
                selected = new DetailsFragment();
                bottomNav.setSelectedItemId(R.id.nav_details);

            } else if (id == R.id.drawer_profile) {
                selected = new ProfileFragment();

            } else if (id == R.id.drawer_logout) {
                new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("Logout")
                        .setMessage("Are you sure you want to logout?")
                        .setPositiveButton("Logout", (dialog, which) -> {
                            FirebaseAuth.getInstance().signOut();
                            android.content.Intent intent = new android.content.Intent(this, LoginActivity.class);
                            intent.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK |
                                    android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }

            if (selected != null) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, selected)
                        .commit();
            }

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }
    private void setupDrawerHeader() {
        View headerView=navigationView.getHeaderView(0);
        TextView navUserName=headerView.findViewById(R.id.navUserName);
        TextView navUserEmail = headerView.findViewById(R.id.navUserEmail);
        FirebaseUser user= FirebaseAuth.getInstance().getCurrentUser();
        if(user!=null)
        {
            navUserEmail.setText(user.getEmail());
        }
        FirebaseDatabase.getInstance().getReference("users").child(user.getUid()).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name=snapshot.getValue(String.class);
                if(name!=null)
                {
                    navUserName.setText(name);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    public void openDrawer()
    {
        drawerLayout.openDrawer(GravityCompat.START);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void init()
    {
        bottomNav=findViewById(R.id.bottom_navigation);
        drawerLayout=findViewById(R.id.drawerLayout);
        navigationView=findViewById(R.id.navigationView);
    }

}