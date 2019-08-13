package mattecarra.accapp.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.FragmentManager
import mattecarra.accapp.R
import mattecarra.accapp.fragments.SettingsFragment

class SettingsActivity: AppCompatActivity(), FragmentManager.OnBackStackChangedListener {
    private lateinit var mSettingsFragment: SettingsFragment

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        if (savedInstanceState == null) {
            mSettingsFragment = SettingsFragment.newInstance()
            supportFragmentManager.beginTransaction().add(R.id.settings, mSettingsFragment, "Settings").commit()
        } else {
            mSettingsFragment = supportFragmentManager.findFragmentByTag("Settings") as SettingsFragment
        }

        supportFragmentManager.addOnBackStackChangedListener(this)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val ab = supportActionBar
        ab?.setDisplayHomeAsUpEnabled(true)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId


        if (id == android.R.id.home) {
            finish()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onBackStackChanged() {
        mSettingsFragment = supportFragmentManager.findFragmentByTag("Settings") as SettingsFragment
    }

    companion object {
        fun launch(context: Context) {
            context.startActivity(Intent(context, SettingsActivity::class.java))
        }
    }
}