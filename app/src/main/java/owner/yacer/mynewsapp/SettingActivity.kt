package owner.yacer.mynewsapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import kotlinx.android.synthetic.main.activity_setting.*
import kotlinx.android.synthetic.main.setting_app_bar.*

class SettingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)
        btn_exitSetting.setOnClickListener{
            finish()
        }
        btn_saveSetting.setOnClickListener{
            Toast.makeText(this,"تم حفظ التعديلات",Toast.LENGTH_SHORT).show()
        }
    }
}