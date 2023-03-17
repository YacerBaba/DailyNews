package owner.yacer.mynewsapp.Fragments

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.facebook.login.LoginManager
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.activity_home_page.*
import kotlinx.android.synthetic.main.fragment_account.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import owner.yacer.mynewsapp.*
import owner.yacer.mynewsapp.Activities.HomePage
import owner.yacer.mynewsapp.Models.Article
import owner.yacer.mynewsapp.Models.BottomNavCallback
import owner.yacer.mynewsapp.Adapters.FavoriteNewsAdapter
import java.util.*

const val REQUEST_CODE = 1

class AccountFragment(val callback: BottomNavCallback) : Fragment(R.layout.fragment_account) {
    var db = Firebase.firestore
    var user = Firebase.auth.currentUser
    var auth = Firebase.auth
    var photoUri: Uri? = null
    var currentName:String? =null
    lateinit var progressDialog:ProgressDialog
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        var drawable = 0
        super.onViewCreated(view, savedInstanceState)
        progressDialog = ProgressDialog(context)
        var progressDialogChangeName = ProgressDialog(context)
        with(progressDialogChangeName) {
            setMessage(" يتم تغيير اسم الحساب...")
        }


        with(progressDialog) {
            setCancelable(false)
            setMessage("تسجيل الخروج...")
        }

        val drawer = (activity as HomePage).mDrawerLayout
        more_btn_account.setOnClickListener {
            drawer.openDrawer(GravityCompat.START)
        }

        lifecycleScope.async {
            getUserCredentials()
        }
        iv_pickPhoto.setOnClickListener {
            openGallery()
        }
        btn_account_signOut.setOnClickListener {
            progressDialog.show()
            lifecycleScope.async {
                auth.signOut()
                LoginManager.getInstance().logOut()
                delay(1500)
                FavoriteNewsAdapter.articleFavList = LinkedList<Article>()
                updateUI()
                callback.update()
            }
        }
        btn_account_editName.setOnClickListener{
            if(drawable == 0){
                currentName = et_account_username.text.toString()
                et_account_username.isEnabled = true
                et_account_username.requestFocus()
                btn_account_editName.setBackgroundResource(R.drawable.ic_confirm_svgrepo_com)
                drawable = 1
            }else{
                if(et_account_username.text.toString().isEmpty()){
                    et_account_username.error = "يجب ملئ خانة الإسم"
                    et_account_username.requestFocus()
                    return@setOnClickListener
                }
                if(et_account_username.text.toString() == currentName){
                    drawable = 0
                    et_account_username.isEnabled = false
                    btn_account_editName.setBackgroundResource(R.drawable.ic_edit_svgrepo_com)
                    return@setOnClickListener
                }
                progressDialogChangeName.show()
                btn_account_editName.setBackgroundResource(R.drawable.ic_edit_svgrepo_com)
                val name = et_account_username.text.toString()
                CoroutineScope(Dispatchers.IO).async {
                    progressDialogChangeName.show()
                    updateProfileName(name)
                    progressDialogChangeName.dismiss()
                    drawable = 0
                    progressDialogChangeName.hide()
                    et_account_username.isEnabled = false
                }
            }
        }
    }
    private fun updateUI() {
        progressDialog.dismiss()
        callback.update()
        setFragment(SignInFragment(callback))
        HomeFragment.myFirstAdapter.user = auth.currentUser
        HomeFragment.mySecondAdapter.user = auth.currentUser
    }
    private fun getUserCredentials() {
        var dateMap = db.collection("Dates").document(user!!.uid).get().addOnCompleteListener {
            if (it.isSuccessful) {
                val dateMap = it.result.data
                et_account_username.setText(user!!.displayName)
                tv_account_email!!.text = user!!.email
                tv_account_date!!.text = dateMap?.get("date").toString()
                if (user!!.photoUrl != null) {
                    Glide.with(this).load(user!!.photoUrl).priority(Priority.HIGH).into(iv_account_profilePic)
                }
            }
        }
    }

    private fun openGallery() {
        var intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_CODE);
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            var progressBoxAcc = ProgressDialog(context)
            with(progressBoxAcc) {
                setCancelable(false)
                setMessage("تحديث صورة الملف الشخصي...")
            }

            var storageRef = Firebase.storage.reference
            photoUri = data.data
            Glide.with(this).load(data.data).into(iv_account_profilePic)
            var photoRef = storageRef.child(user!!.uid)
            progressBoxAcc.show()
            photoRef.putFile(data.data!!).addOnCompleteListener {
                if (it.isSuccessful) {
                    photoRef.downloadUrl.addOnSuccessListener { uri ->
                        updateProfilePic(uri)
                        progressBoxAcc.hide()
                    }
                }else{
                    Toast.makeText(context,it.exception?.message!!,Toast.LENGTH_SHORT).show()
                    progressBoxAcc.hide()
                }
            }
        }
    }
    private fun updateProfileName(name:String) {
        val mProfile = UserProfileChangeRequest.Builder().setDisplayName(name)
            .build()
        Toast.makeText(this.context, "تم تغيير اسم الحساب بنجاح", Toast.LENGTH_SHORT).show()
        user!!.updateProfile(mProfile)
    }
    private fun updateProfilePic(uri: Uri?) {
        if (uri != null) {
            val mProfile = UserProfileChangeRequest.Builder().setPhotoUri(uri)
                .build()
            Toast.makeText(this.context, "تم تغيير صورة الحساب بنجاح", Toast.LENGTH_SHORT).show()
            user!!.updateProfile(mProfile)
        }
    }

    private fun setFragment(nextFragment: Fragment) {
        val fragmentTransaction: FragmentTransaction = requireFragmentManager().beginTransaction()
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.hide(this@AccountFragment)
        fragmentTransaction.add(R.id.flfragment, nextFragment)
        fragmentTransaction.commit()
    }
}
