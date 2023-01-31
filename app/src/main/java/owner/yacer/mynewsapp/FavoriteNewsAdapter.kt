package owner.yacer.mynewsapp

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.article_row.view.*
import kotlinx.android.synthetic.main.fav_article_row.view.*
import java.util.*

class FavoriteNewsAdapter : RecyclerView.Adapter<FavoriteNewsAdapter.ViewHolder>() {
    companion object {
        var articleFavList = LinkedList<Article>()
    }

    var db = Firebase.firestore
    var user = Firebase.auth.currentUser

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        init {
            itemView.setOnClickListener {
                val uri = Uri.parse(articleFavList[adapterPosition].url)
                Intent(Intent.ACTION_VIEW, uri).also {
                    ContextCompat.startActivity(itemView.context, it, null)
                }
            }

        }

        val articleCardView = view.findViewById<CardView>(R.id.fav_articleCardView)
        val articleTitle = view.findViewById<TextView>(R.id.fav_article_title)
        val articleImage = view.findViewById<ImageView>(R.id.fav_article_img)
        val articleDate = view.findViewById<TextView>(R.id.fav_article_date)
        val btnDelete = view.fav_btn_delete
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): FavoriteNewsAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.fav_article_row,
            parent,
            false
        )
        return ViewHolder(view)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.btnDelete.setOnClickListener {
            db.collection("root").document(user!!.uid).update(
                articleFavList[position].publishedAt, FieldValue.delete())
                .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        it.context,
                        "تم إزالة الخبر من قائمتك المفضلة",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
        holder.itemView.apply {
            Glide.with(this).load(articleFavList[position].urlToImage).into(holder.articleImage)
        }
        holder.articleTitle.text = articleFavList[position].title
        holder.articleDate.text = articleFavList[position].publishedAt
    }

    override fun getItemCount(): Int = articleFavList.size
}