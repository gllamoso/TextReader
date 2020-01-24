package gtcl.dev.textreader

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.preference.PreferenceManager
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import gtcl.dev.textreader.mainfragments.CameraFragment

class MainActivity : AppCompatActivity() {

    lateinit var adapter: PagerAdapter
    lateinit var tabLayout: TabLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tabLayout = findViewById(R.id.tab_layout)
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.ic_photo_camera_white_24dp))
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.ic_image_24dp))

        val viewPager = findViewById<ViewPager>(R.id.view_pager)
        adapter = PagerAdapter(
            supportFragmentManager,
            FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT,
            tabLayout.tabCount
        )
        viewPager.adapter = adapter

        viewPager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))
        tabLayout.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener{
            override fun onTabReselected(p0: TabLayout.Tab?) {}

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab != null) {
                    viewPager.currentItem = tab.position
                }
            }

        })
        PreferenceManager.setDefaultValues(this, R.xml.root_preferences, false)

        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        val darkMode = sharedPref.getString("dark mode", "0")
        val preferStorage = sharedPref.getBoolean("prefer storage", false)
        if(preferStorage) selectTab(STORAGE_FRAGMENT_INDEX)
        when(darkMode){
            "0" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            "1" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            "2" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val fragments = supportFragmentManager.fragments
        for(fragment in fragments)
            fragment.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val fragments = supportFragmentManager.fragments
        for(fragment in fragments)
            fragment.onActivityResult(requestCode,resultCode,data)
    }

    fun getFragment(index: Int): Fragment{
        return adapter.getItem(index)
    }

    fun selectTab(index: Int){
        tabLayout.getTabAt(index)!!.select()
    }

    companion object {
        var CAMERA_FRAGMENT_INDEX = 0
        var STORAGE_FRAGMENT_INDEX = 1
    }
}
