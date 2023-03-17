package owner.yacer.mynewsapp.Fragments

import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_home_page.*
import kotlinx.android.synthetic.main.fragment_favorite.*
import kotlinx.android.synthetic.main.fragment_favorite.view.*
import kotlinx.coroutines.async
import owner.yacer.mynewsapp.Models.Article
import owner.yacer.mynewsapp.Adapters.FavoriteNewsAdapter
import owner.yacer.mynewsapp.Activities.HomePage
import owner.yacer.mynewsapp.R
import java.util.*


class FavoriteFragment : Fragment(R.layout.fragment_favorite) {
    var db = Firebase.firestore
    var user = Firebase.auth.currentUser
    var articleList = LinkedList<Article>()
    lateinit var progressBox: ProgressDialog
    companion object{
        var adapter = FavoriteNewsAdapter()
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.async {
            getFavoriteNews()
        }
        val drawer = (activity as HomePage).mDrawerLayout
        more_btn_fav.setOnClickListener {
            drawer.openDrawer(GravityCompat.START)
        }
        var layoutManager = LinearLayoutManager(context)
        view.tv_thereIsNoNews.isVisible = adapter.itemCount==0
        val observer = object : RecyclerView.AdapterDataObserver(){
            override fun onChanged() {
                super.onChanged()
                val nbr = adapter.itemCount
                view.tv_thereIsNoNews.isVisible = nbr==0
            }
        }
        adapter.registerAdapterDataObserver(observer)
        rv_favorites.adapter = adapter
        rv_favorites.layoutManager = layoutManager

    }
    private fun getFavoriteNews() {
        db.collection("root").document(user!!.uid).addSnapshotListener { value, _ ->
            if (value?.exists() == true) {
                var articleList = LinkedList<Article>()
                var result = value.data //as java.util.HashMap<String, Any>
                Log.e("msg", result.toString())
                for (entry: Map.Entry<String, Any> in result!!.entries) {
                    var map = entry.value as java.util.HashMap<String, Any>
                    var article = mapperToArticle(map)
                    articleList.add(article)
                }
                //fav_progressBar.isVisible = false
                FavoriteNewsAdapter.articleFavList = articleList
                adapter.notifyDataSetChanged()
            }
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
}
