package owner.yacer.mynewsapp

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_signin.*
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.util.*


const val TAG = "msgFB"

class SignInFragment(var callback: BottomNavCallback) : Fragment(R.layout.fragment_signin) {

    var db = Firebase.firestore
    var auth = Firebase.auth
    lateinit var progressDialog: ProgressDialog
    lateinit var gso: GoogleSignInOptions
    lateinit var mGoogleSignInClient: GoogleSignInClient
    var callbackManager = CallbackManager.Factory.create()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        progressDialog = ProgressDialog(context)
        with(progressDialog) {
            setCancelable(true)
            setMessage("يرجى الإنتظار...")
        }
        createRequest()

        btn_signIn.setOnClickListener {
            loginUser()
        }
        btn_register.setOnClickListener {
            Intent(context, RegisterActivity::class.java).also {
                startActivity(it)
            }
        }
        btn_signin_fb.setOnClickListener {
            progressDialog.show()
            val loginManager = LoginManager.getInstance()
            // Set the permissions that the user will need to grant
            val permissions: List<String> = listOf("email", "public_profile")
            // Initiate the login process
            loginManager.logInWithReadPermissions(this, permissions)
            loginManager.registerCallback(callbackManager, object :
                FacebookCallback<LoginResult> {
                override fun onSuccess(loginResult: LoginResult) {
                    Log.d(TAG, "facebook:onSuccess:$loginResult")
                    handleFacebookAccessToken(loginResult.accessToken)
                }
                override fun onCancel() {
                    Log.d(TAG, "facebook:onCancel")
                    progressDialog.dismiss()
                }
                override fun onError(error: FacebookException) {
                    Log.d(TAG, "facebook:onError", error)
                    progressDialog.dismiss()
                }
            })
        }
        btn_signin_google.setOnClickListener {
            progressDialog.show()
            signIn()
        }
    }

    private fun createRequest() {
        gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestIdToken("814524973969-tf9nfn2ghnjt2e4dnk7rbmljg5kqj46k.apps.googleusercontent.com")
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient((activity as HomePage).applicationContext, gso)
    }

    private fun signIn() {
        val intent = mGoogleSignInClient.signInIntent
        startActivityForResult(intent, 101)
    }

    private fun loginUser() {
        if (ti_email.editText?.text.toString().isEmpty()) {
            ti_email.error = "يرجى إدخال البريد الإلكتروني"
            ti_email.requestFocus()
            return
        }
        if (ti_password.editText?.text.toString().isEmpty()) {
            ti_password.error = "يرجى إدخال كلمة المرور"
            ti_password.requestFocus()
            return
        }
        var email = ti_email.editText?.text.toString()
        var pass = ti_password.editText?.text.toString()
        progressDialog.show()
        auth.signInWithEmailAndPassword(email, pass).addOnCompleteListener {
            if (it.isSuccessful) {
                progressDialog.dismiss()
                Toast.makeText(context, "تم تسجيل الدخول بنجاح", Toast.LENGTH_LONG).show()
                CoroutineScope(Dispatchers.Main).launch {
                    delay(1000)
                    updateUI()
                    callback.update()
                }
            } else {
                progressDialog.dismiss()
                Toast.makeText(context, it.exception?.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun handleFacebookAccessToken(token: AccessToken) {
        Log.d(TAG, "handleFacebookAccessToken:$token")
        val credential = FacebookAuthProvider.getCredential(token.token)
        auth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Sign in success, update UI with the signed-in user's information
                Log.d(TAG, "signInWithCredential:success")
                val user = auth.currentUser
                val db = Firebase.firestore
                CoroutineScope(Dispatchers.Main).async {
                    if (!db.collection("Dates")
                            .document(auth.currentUser!!.uid).get().await().exists()
                    ) {
                        Log.e(TAG, "inside if statement")
                        val currentDate = Calendar.getInstance().time
                        db.collection("Dates").document(auth.currentUser!!.uid)
                            .set(mapOf("date" to currentDate.toString())).await()
                    }
                    updateUI()
                }

            } else {
                // If sign in fails, display a message to the user.
                Log.w(TAG, "signInWithCredential:failure", task.exception)
                Toast.makeText(
                    context, "Authentication failed.",
                    Toast.LENGTH_SHORT
                ).show()
                progressDialog.dismiss()
            }
        }
    }

    private fun setFragment(nextFragment: Fragment) {
        val fragmentTransaction: FragmentTransaction = requireFragmentManager().beginTransaction()
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.hide(this@SignInFragment)
        fragmentTransaction.add(R.id.flfragment, nextFragment)
        fragmentTransaction.commit()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Pass the activity result back to the Facebook SDK
        callbackManager.onActivityResult(requestCode, resultCode, data)
        // google sign in result
        if (requestCode == 101) {
            var task = GoogleSignIn.getSignedInAccountFromIntent(data)
            if (task.isSuccessful) {
                handleSignInResult(task.result)
            }else{
                progressDialog.dismiss()
            }
        }
    }

    private fun updateUI() {
        progressDialog.dismiss()
        callback.update()
        setFragment(AccountFragment(callback))
        HomeFragment.myFirstAdapter.user = auth.currentUser
        HomeFragment.mySecondAdapter.user = auth.currentUser
    }

    private fun handleSignInResult(account: GoogleSignInAccount) {
        val firebaseCredential = GoogleAuthProvider.getCredential(account.idToken, null)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                auth.signInWithCredential(firebaseCredential).await()
                Log.e("msgGoogle", "Signed in successfully")
                withContext(Dispatchers.Main) {
                    updateUI()
                    progressDialog.dismiss()
                }
            } catch (e: ApiException) {
                // The ApiException status code indicates the detailed failure reason.
                // Please refer to the GoogleSignInStatusCodes class reference for more information.
                Log.w(TAG, "signInResult:failed code=" + e.statusCode)
                progressDialog.dismiss()
            }
        }

    }
}