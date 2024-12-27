package com.example.labproject;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.TextView;
import com.example.labproject.R;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.example.labproject.databinding.ActivityMainScreenBinding;

public class MainScreenActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainScreenBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPrefManager sharedPrefManager = SharedPrefManager.getInstance(this);
        boolean isDarkMode = sharedPrefManager.readBoolean("isDarkMode", false); // Default to light mode

        if (isDarkMode) {
            setTheme(R.style.AppTheme_Dark);
        } else {
            setTheme(R.style.AppTheme_Light);
        }

        super.onCreate(savedInstanceState);

        binding = ActivityMainScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMainScreen.toolbar);

        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_logout, R.id.nav_addnewtask, R.id.nav_updateprofile,R.id.nav_alltasks,R.id.nav_searchtask,R.id.nav_importtask, R.id.nav_showcompletedtasks)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main_screen);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);





        DataBaseHelper dataBaseHelper = new DataBaseHelper(this, "DB", null, 1);

        View Menu = binding.navView.getHeaderView(0);
        TextView emailMenu = Menu.findViewById(R.id.navmenuEmail);
        TextView nameMenu = Menu.findViewById(R.id.navmenuName);

        String email=(String) getIntent().getSerializableExtra("email");
        Log.d("MainScreenActivity",email);
        emailMenu.setText(email);

        String fullName = dataBaseHelper.getUserFullName(email);
        nameMenu.setText(fullName);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
            getMenuInflater().inflate(R.menu.main_screen, menu);
            return true;
    }


    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main_screen);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        SharedPrefManager sharedPrefManager = SharedPrefManager.getInstance(this);

        if (id == R.id.action_light_mode) {
            sharedPrefManager.writeBoolean("isDarkMode", false);
            applyTheme(false);
            return true;
        } else if (id == R.id.action_dark_mode) {
            sharedPrefManager.writeBoolean("isDarkMode", true);
            applyTheme(true);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void applyTheme(boolean isDarkMode) {
        if (isDarkMode) {
            setTheme(R.style.AppTheme_Dark);
        } else {
            setTheme(R.style.AppTheme_Light);
        }
        recreate();
    }

}