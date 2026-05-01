package com.example.smarthomecontrolapp;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
BottomNavigationView bottomNav;
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
        bottomNav.setOnItemSelectedListener(item->
        {
            Fragment selectedFragment= null;
            int id= item.getItemId();
            if(id==R.id.nav_home)
            {
                selectedFragment=new HomeFragment();
            }
            else if(id== R.id.nav_energy)
            {
                selectedFragment=new EnergyFragment();

            }
            else if(id==R.id.nav_rooms)
            {
                selectedFragment=new RoomsFragment();
            }
            else if (id==R.id.nav_profile)
            {
                selectedFragment=new ProfileFragment();
            }
            if(selectedFragment!= null)
            {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,selectedFragment).commit();
            }
            return true;
        });
        if(savedInstanceState==null)
        {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,new HomeFragment()).commit();
        }
    }
    private void init()
    {
        bottomNav=findViewById(R.id.bottom_navigation);
    }

}