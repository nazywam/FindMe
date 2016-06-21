package findme.findme;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class MapsActivity extends AppCompatActivity implements StartInfo.OnFragmentInteractionListener {
    private DrawerLayout drawerLayout;
    private ListView drawerList;
    private ActionBarDrawerToggle drawerToggle;

    private NavigationFragment navigationFragment;
    private PathFragment pathFragment;
    private Fragment currentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_maps);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.main_container, StartInfo.newInstance());
        transaction.commit();
        setDrawer();

        navigationFragment = NavigationFragment.newInstance();
        pathFragment = PathFragment.newInstance();
    }

    private void setDrawer() {
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        String[] drawerStrings = getResources().getStringArray(R.array.drawer_items);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerList = (ListView) findViewById(R.id.drawer);
        if(drawerList != null) {
            drawerList.setAdapter(new ArrayAdapter<>(this, R.layout.draw_list_item, drawerStrings));
            drawerList.setOnItemClickListener(new ListView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    drawerLayout.closeDrawer(drawerList);
                    switch (position) {
                        case 0:
                            startWindow();
                            break;
                        case 1:
                            navigation();
                            break;
                        case 2:
                            path();
                            break;
                        case 3:
                            about();
                            break;
                    }
                }
            });
        }
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.drawer_open, R.string.drawer_close) {
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
            }
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };
        drawerLayout.addDrawerListener(drawerToggle);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
    }

    private void startWindow() {
        StartInfo info = StartInfo.newInstance();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.main_container, info);
        transaction.commit();
        currentFragment = info;
    }

    private void navigation() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.main_container, navigationFragment);
        transaction.commit();
        currentFragment = navigationFragment;
    }

    private void about() {
        AboutFragment about = AboutFragment.newInstance();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.main_container, about);
        transaction.commit();
        currentFragment = about;
    }

    private void path() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.main_container, pathFragment);
        transaction.commit();
        currentFragment = pathFragment;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return drawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if(currentFragment != navigationFragment || !((NavigationFragment) currentFragment).backPressed())
            super.onBackPressed();
    }

    @Override
    public void onStartButtonClick() {
        navigation();
        drawerList.setItemChecked(1, true);
    }
}
