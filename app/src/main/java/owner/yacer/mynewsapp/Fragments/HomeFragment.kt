package owner.yacer.mynewsapp.Fragments

import android.app.ProgressDialog
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import android.view.View
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.SnapHelper
import kotlinx.android.synthetic.main.activity_home_page.*
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import owner.yacer.mynewsapp.*
import owner.yacer.mynewsapp.Activities.HomePage
import owner.yacer.mynewsapp.Adapters.FavoriteNewsAdapter
import owner.yacer.mynewsapp.Adapters.HeadLineAdapter
import owner.yacer.mynewsapp.Models.Item
import owner.yacer.mynewsapp.Adapters.itemAdapter
import java.util.*

const val cacheSize = 5 * 1024 * 1024

class HomeFragment : Fragment(R.layout.fragment_home) {
    companion object{
        lateinit var myFirstAdapter: HeadLineAdapter
        lateinit var mySecondAdapter: itemAdapter
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val progressBox = ProgressDialog(context)
        val drawer = (activity as HomePage).mDrawerLayout
        with(progressBox) {
            setCancelable(false)
            setMessage("يرجى الإنتظار")
        }
        progressBarHome.isVisible = true

        CoroutineScope(Dispatchers.Main).launch {
            delay(50)
            var itemsList = LinkedList<Item>()
            for (article in HomePage.myBigList) {
                var liked = false
                for (fav_art in FavoriteNewsAdapter.articleFavList) {
                    if (fav_art.publishedAt == article.publishedAt) {
                        liked = true
                    }
                }
                itemsList.add(Item(article, liked))
            }
            val myFirstList = itemsList.subList(0, 20)
            val mySecondList = itemsList.subList(21, itemsList.size - 1)
            setUpHeadLineRv(myFirstList)
            setUpNewsRv(mySecondList)
            progressBarHome.isVisible = false
        }
        more_btn_home.setOnClickListener {
            drawer.openDrawer(GravityCompat.START)
        }

    }

    private fun setUpHeadLineRv(list:List<Item>) {
        myFirstAdapter = HeadLineAdapter()
        myFirstAdapter.HeadLinesList = list
        var mFirstLayoutManager =
            LinearLayoutManager(this.context, LinearLayoutManager.HORIZONTAL, false)
        First_rv.adapter = myFirstAdapter
        First_rv.layoutManager = mFirstLayoutManager
        val snapHelper: SnapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(First_rv)
    }

    private fun setUpNewsRv(list:List<Item>) {
        mySecondAdapter = itemAdapter()
        mySecondAdapter.articleList = list
        val mSecondLayoutManager = LinearLayoutManager(this.context)
        Second_rv.adapter = mySecondAdapter
        Second_rv.layoutManager = mSecondLayoutManager
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

