package owner.yacer.mynewsapp

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {
    var dialog: Dialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()
        checkInternetConnection()
    }

    private fun checkInternetConnection(){
        dialog = Dialog(this)
        var hasNet: Boolean
        Handler().postDelayed({
            hasNet = isInternetAvailable()
            Log.e("msg", "hasNet = $hasNet")
            if (!hasNet) {
                Log.e("msg", "You entred if block")
                dialog?.setContentView(R.layout.check_net)
                dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                dialog?.setCancelable(false)
                dialog?.show()
            } else {
                Intent(this, HomePage::class.java).also {
                    startActivity(it)
                    finish()
                }
            }
        }, 1000)
    }

    fun isInternetAvailable(): Boolean {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)!!.state
                == NetworkInfo.State.CONNECTED
                || connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)!!.state
                == NetworkInfo.State.CONNECTED)
    }


    fun onRetry(view: View) {
        Intent(applicationContext, MainActivity::class.java).also {
            startActivity(it)
        }
    }
}