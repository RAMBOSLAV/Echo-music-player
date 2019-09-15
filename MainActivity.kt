package com.example.echo.activities

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import com.example.echo.R
import com.example.echo.adapters.NavigationDrawerAdapter
import com.example.echo.fragments.MainScreenFragment
import com.example.echo.fragments.SongPlayingFragment

class MainActivity : AppCompatActivity()
{
    var navigationDrawerIconList: ArrayList<String> = ArrayList()
    var imagesForNavigationDrawer = intArrayOf(R.drawable.navigation_allsongs,
        R.drawable.navigation_favorites,
        R.drawable.navigation_settings, R.drawable.navigation_aboutus)
    object Statified{

        var drawerLayout: DrawerLayout? = null
        var notificationManager: NotificationManager? = null
    }
    var trackNotificationBulder: Notification? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        MainActivity.Statified.drawerLayout = findViewById(R.id.drawer_layout)

        navigationDrawerIconList.add("All Songs")
        navigationDrawerIconList.add("Favourits")
        navigationDrawerIconList.add("Settings")
        navigationDrawerIconList.add("About Us")
        val toggle = ActionBarDrawerToggle(this@MainActivity, MainActivity.Statified.drawerLayout, toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )

        MainActivity.Statified.drawerLayout?.addDrawerListener(toggle)
        toggle.syncState()

        val mainScreenFragment = MainScreenFragment()
        this.supportFragmentManager
            .beginTransaction()
            .add(R.id.details_fragment, mainScreenFragment, "MainScreenFragment")
            .commit()

        var _navigationAdapter = NavigationDrawerAdapter(navigationDrawerIconList, imagesForNavigationDrawer, this)
        _navigationAdapter.notifyDataSetChanged()

        var navigationRecyclerView = findViewById<RecyclerView>(R.id.navigation_recycler_view)
        navigationRecyclerView.layoutManager = LinearLayoutManager(this)
        navigationRecyclerView.itemAnimator = DefaultItemAnimator()
        navigationRecyclerView.adapter = _navigationAdapter
        navigationRecyclerView.setHasFixedSize(true)

        val intent = Intent(this@MainActivity, MainActivity::class.java)
        val pIntent = PendingIntent.getActivities(this@MainActivity, System.currentTimeMillis().toInt(),
            arrayOf(intent), 0)
        trackNotificationBulder = Notification.Builder(this)
            .setContentTitle("A track is playing in the background")
            .setSmallIcon(R.drawable.echo_logo)
            .setContentIntent(pIntent)
            .setOngoing(true)
            .setAutoCancel(true)
            .build()
        Statified.notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager


    }

    override fun onStart() {
        super.onStart()
        try {
            Statified.notificationManager?.cancel(1998)
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    override fun onStop() {
        super.onStop()
        try {
            if (SongPlayingFragment.stat.mediaPlayer?.isPlaying as Boolean){
                Statified.notificationManager?.notify(1998, trackNotificationBulder)
            }
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            Statified.notificationManager?.cancel(1998)
        }catch (e: Exception){
            e.printStackTrace()
        }
    }
}
