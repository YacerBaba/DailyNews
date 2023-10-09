package owner.yacer.mynewsapp.Activities

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.coroutines.async
import kotlinx.coroutines.tasks.await
import owner.yacer.mynewsapp.R
import java.util.*

class RegisterActivity : AppCompatActivity() {
    var state = 0
    var state2 = 0
    var auth = Firebase.auth
    var db = Firebase.firestore
    lateinit var progressBox: ProgressDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        progressBox = ProgressDialog(this)
        with(progressBox) {
            setCancelable(false)
            setMessage("يرجى الإنتظار...")
        }
        ti_reg_pass.setStartIconOnClickListener {
            Log.e("msg", "clicked")
            if (state == 0) {
                ti_reg_pass.editText?.transformationMethod =
                    HideReturnsTransformationMethod.getInstance()
                ti_reg_pass.setStartIconDrawable(R.drawable.ic_hide_password)
                state = 1
            } else {
                ti_reg_pass.editText?.transformationMethod =
                    PasswordTransformationMethod.getInstance()
                ti_reg_pass.setStartIconDrawable(R.drawable.ic_show_password)
                state = 0
            }
        }
        ti_reg_confirmPass.setStartIconOnClickListener {
            if (state2 == 0) {
                ti_reg_confirmPass.editText?.transformationMethod =
                    HideReturnsTransformationMethod.getInstance()
                ti_reg_confirmPass.setStartIconDrawable(R.drawable.ic_hide_password)
                state2 = 1
            } else {
                ti_reg_confirmPass.editText?.transformationMethod =
                    PasswordTransformationMethod.getInstance()
                ti_reg_confirmPass.setStartIconDrawable(R.drawable.ic_show_password)
                state2 = 0
            }
        }
        btn_createNewAccount.setOnClickListener {
            createNewAccount()
        }
    }

    private fun createNewAccount() {
        if (ti_reg_username.editText?.text.toString().isEmpty()) {
            ti_reg_username.error = "يرجى إدخال إسم المستخدم"
            ti_reg_username.requestFocus()
            return
        }
        if (ti_reg_email.editText?.text.toString().isEmpty()) {
            ti_reg_email.error = "يرجى إدخال البريد الإلكتروني"
            ti_reg_email.requestFocus()
            return
        }
        if (ti_reg_pass.editText?.text.toString().isEmpty()) {
            ti_reg_pass.error = "يرجى إدخال كلمة المرور"
            ti_reg_pass.requestFocus()
            return
        }
        if (ti_reg_pass.editText?.text.toString() != ti_reg_confirmPass.editText?.text.toString()) {
            ti_reg_confirmPass.error = "كلمة المرور ليست مطابقة"
            ti_reg_confirmPass.requestFocus()
            return
        }
        progressBox.show()
        var email = ti_reg_email.editText?.text.toString()
        var pass = ti_reg_pass.editText?.text.toString()
        val userName = ti_reg_username.editText?.text.toString()
        auth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener {
            if (it.isSuccessful) {
                lifecycleScope.async {
                    auth.signInWithEmailAndPassword(email, pass).await()
                    var user = auth.currentUser
                    var mProfile = UserProfileChangeRequest.Builder().setDisplayName(userName)
                        .build()
                    user!!.updateProfile(mProfile).await()
                    var currentDate = Calendar.getInstance().time
                    db.collection("Dates").document(auth.currentUser!!.uid)
                        .set(mapOf("date" to currentDate.toString()))
                    progressBox.hide()
                    Toast.makeText(
                        this@RegisterActivity,
                        "تم إنشاء الحساب بنجاح",
                        Toast.LENGTH_LONG
                    ).show()
                    Intent(this@RegisterActivity, HomeActivity::class.java).also { intent ->
                        startActivity(intent)
                        finish()
                    }
                }
            } else {
                progressBox.hide()
                Toast.makeText(this, it.exception?.message, Toast.LENGTH_LONG).show()
                Log.e("msg", it.exception?.message!!)
            }
        }
    }
}