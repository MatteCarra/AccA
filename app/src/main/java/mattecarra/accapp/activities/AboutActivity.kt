package mattecarra.accapp.activities

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
import mattecarra.accapp.R
import mattecarra.accapp.acc.Acc
import mattecarra.accapp.databinding.ActivityAboutBinding
import mattecarra.accapp.utils.LogExt

class AboutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        LogExt().d(javaClass.simpleName, "onCreate()")

        super.onCreate(savedInstanceState)

        val binding=ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)

       supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Set appropriate version numbers
        val app = applicationContext.packageManager.getPackageInfo(packageName, 0)
        binding.aboutAccaVersionTv.text = String.format("%s (%s)", app.versionName, app.versionCode.toString())
        binding.aboutAccDaemonVersionTv.text = Acc.getAccVersionToStr()
        binding.aboutAccApiVersionTv.text = Acc.instance.version.toString()
    }

    companion object {
        fun launch(context: Context) {
            context.startActivity(Intent(context, AboutActivity::class.java))
        }
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

    private fun openUrl(url: String) {
        try {
            val tabsBuilder = CustomTabsIntent.Builder()
            val tabs: CustomTabsIntent = tabsBuilder.build()
            tabs.launchUrl(this, Uri.parse(url))
        } catch (anfEx: ActivityNotFoundException) {
            Toast.makeText(this, R.string.toast_no_browser_installed, Toast.LENGTH_LONG).show()
        }
    }

    fun accaGitHubOnClick(view: View) {
        openUrl("https://github.com/MatteCarra/AccA")
    }

    fun accaTelegramOnClick(v: View) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("tg://resolve?domain=acc_group")))
        } catch (ignored: Exception) {
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/acc_group")))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun vr25GitHubOnClick(view: View) {
        openUrl("https://github.com/VR-25")
    }
    fun vr25WebsiteOnClick(view: View) {
        openUrl("https://forum.xda-developers.com/member.php?u=5228676")
    }

    fun matteGitHubOnClick(view: View) {
        openUrl("https://github.com/MatteCarra")
    }
    fun matteWebsiteOnClick(view: View) {
        openUrl("https://forum.xda-developers.com/member.php?u=9731715")
    }

    fun squabbiGitHubOnClick(view: View) {
        openUrl("https://github.com/squabbi")
    }
    fun squabbiWebsiteOnClick(view: View) {
        openUrl("https://squabbi.com/")
    }
}
