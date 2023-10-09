package owner.yacer.mynewsapp.Activities

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_home_page.*
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import okhttp3.Cache
import okhttp3.OkHttpClient
import owner.yacer.mynewsapp.Fragments.FavoriteFragment.Companion.adapter
import owner.yacer.mynewsapp.Fragments.*
import owner.yacer.mynewsapp.Models.Api
import owner.yacer.mynewsapp.Models.Article
import owner.yacer.mynewsapp.Models.BottomNavCallback
import owner.yacer.mynewsapp.Adapters.FavoriteNewsAdapter
import owner.yacer.mynewsapp.R
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*


class HomeActivity : AppCompatActivity() {
    private var auth = Firebase.auth
    var user = auth.currentUser
    var db = Firebase.firestore
    lateinit var progressBoxHome: ProgressDialog
    var callback = object : BottomNavCallback {
        override fun update() {
            setUpBottomNavigationView()
        }
    }

    companion object {
        lateinit var myBigList:List<Article>
    }

    lateinit var toggleBtn: ActionBarDrawerToggle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_page)
        val navView = nav_bar
        val headerView = navView.getHeaderView(0)
        val imgView = headerView.findViewById<ImageView>(R.id.iv_user)

        progressBoxHome = ProgressDialog(this)
        with(progressBoxHome) {
            setMessage("يرجى الإنتظار...")
            setCancelable(false)
        }
        setUpNavigationDrawer()
        setUpBottomNavigationView()
        val myCache = Cache(this.cacheDir, cacheSize.toLong())
        val okHttpClient = OkHttpClient.Builder()
            .cache(myCache)
            .addInterceptor { chain ->
                var request = chain.request()
                if (hasNetwork(this)!!) {
                    request =
                        request.newBuilder().header("Cache-Control", "public, max-age=" + 600000)
                            .build()
                } else
                    request.newBuilder().header(
                        "Cache-Control",
                        "public, only-if-cached, max-stale=" + 60 * 60 * 24 * 7
                    ).build()
                chain.proceed(request)
            }
            .build()
        val myRetrofitInstance = Retrofit.Builder()
            .baseUrl("https://newsapi.org")
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
            .create(Api::class.java)
        progressBoxHome.show()
        lifecycleScope.async {
            getFavoriteNews()
        }
        if (user != null && user?.photoUrl != null) {
            Glide.with(this).load(user!!.photoUrl).into(imgView)
        }
        val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
            throwable.printStackTrace()
        }
        lifecycleScope.launch(Dispatchers.IO) {
            val response =
                myRetrofitInstance.getNews("aljazeera.net", "1f4d893e8d9e4be08eb1b92964905f53")
            var photoUrl = user?.photoUrl
            Log.e("msg", "request end")
            if (response.isSuccessful && response.body() != null) {
                myBigList = response.body()!!.articles
                withContext(Dispatchers.Main) {
                    Log.e("msg", myBigList[0].title)
                    progressBoxHome.dismiss()
                    setFragment(HomeFragment())
                }
            } else {
                Log.e("msg", "Something went wrong")
                progressBoxHome.dismiss()
            }
        }
    }

    private fun getFavoriteNews() {
        lifecycleScope.async {
            db.collection("root").document(user!!.uid).get().addOnCompleteListener {
                if (it.isSuccessful) {
                    var articleList = LinkedList<Article>()
                    var result = it.result.data as HashMap<String, Any>
                    Log.e("msg", result.toString())
                    for (entry: Map.Entry<String, Any> in result.entries) {
                        var map = entry.value as HashMap<String, Any>
                        var article = mapperToArticle(map)
                        articleList.add(article)
                    }
                    FavoriteNewsAdapter.articleFavList = articleList
                    adapter.notifyDataSetChanged()
                }
            }.await()
        }

    }

    private fun mapperToArticle(map: Map<String, Any>): Article {
        return Article(
            map["author"] as String,
            map["content"] as String,
            map["description"] as String,
            map["publishedAt"] as String,
            null,
            map["title"] as String,
            map["url"] as String,
            map["urlToImage"] as String
        )
    }

    private fun setUpBottomNavigationView() {
        val user = auth.currentUser
        bottomNavigationView2.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.miHome -> {
                    setFragment(HomeFragment())
                }
                R.id.miFavorite -> if (user != null) {
                    setFragment(FavoriteFragment())
                } else {
                    setFragment(FavFragment_notSignIN())
                }
                R.id.miAccount -> if (user != null) {
                    setFragment(AccountFragment(callback))
                } else {
                    setFragment(SignInFragment(callback))
                }
            }
            true
        }
    }

    private fun setUpNavigationDrawer() {
        nav_bar.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.miSetting ->
                    Toast.makeText(
                        applicationContext,
                        "ستتوفر بعض الإعدادات عند إطلاق التطبيق ان شاء الله ..",
                        Toast.LENGTH_SHORT
                    ).show()
                R.id.miHelp -> Toast.makeText(
                    applicationContext,
                    "سيتوفر الدعم الفني عند إطلاق التطبيق ان شاء الله ..",
                    Toast.LENGTH_SHORT
                ).show()
                R.id.miFollowUs -> {
                    var uri = Uri.parse("https://www.linkedin.com/in/yacer-babaouamer-7b12b1229/")
                    Intent(Intent.ACTION_VIEW, uri).also {
                        startActivity(it)
                    }
                }
            }
            true
        }
    }

    private fun setFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.flfragment, fragment)
            commit()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggleBtn.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun hasNetwork(context: Context): Boolean? {
        var isConnected: Boolean? = false
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo
        if (activeNetwork != null && activeNetwork.isConnected)
            isConnected = true
        return isConnected
    }


}