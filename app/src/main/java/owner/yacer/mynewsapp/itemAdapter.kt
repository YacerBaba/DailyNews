package owner.yacer.mynewsapp

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.article_row.view.*


class itemAdapter :
    RecyclerView.Adapter<itemAdapter.ViewHolder>() {
    private val db = Firebase.firestore
    var user = Firebase.auth.currentUser

    private val differCallBack = object : DiffUtil.ItemCallback<Item>() {
        override fun areItemsTheSame(oldItem: Item, newItem: Item): Boolean {
            return oldItem.article.publishedAt == newItem.article.publishedAt
        }

        override fun areContentsTheSame(oldItem: Item, newItem: Item): Boolean {
            return oldItem == newItem
        }
    }
    private val differ = AsyncListDiffer(this, differCallBack)
    var articleList: List<Item>
        get() = differ.currentList
        set(value) {
            differ.submitList(value)
        }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val articleCardView = view.findViewById<CardView>(R.id.articleCardView)
        val articleTitle = view.findViewById<TextView>(R.id.article_title)
        val articleImage = view.findViewById<ImageView>(R.id.article_img)
        val articleDate = view.findViewById<TextView>(R.id.article_date)
        val btnFav = view.btn_fav
        init {
            itemView.setOnClickListener {
                val uri = Uri.parse(articleList[adapterPosition].article.url)
                Intent(Intent.ACTION_VIEW, uri).also {
                    startActivity(itemView.context,it,null)
                }
            }

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.article_row,
            parent,
            false
        )
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemView.apply {
            Glide.with(this).load(articleList[position].article.urlToImage).placeholder(R.drawable.ic_img_box_svgrepo_com)
                .priority(Priority.HIGH)
                .dontAnimate().into(holder.articleImage)
        }
        holder.articleTitle.text = articleList[position].article.title
        holder.articleDate.text = articleList[position].article.publishedAt
        holder.btnFav.isChecked = articleList[position].liked
        holder.btnFav.setOnClickListener {
            if (holder.btnFav.isChecked) {
                if (user != null) {
                    articleList[position].liked = true
                    val article = articleList[position].article
                    val map = mapOf(
                        article.publishedAt to article
                    )
                    db.collection("root").document(user!!.uid)
                        .set(map, SetOptions.merge()).addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(
                                    it.context, "تم إضافة الخبر إلى أخبارك المفضلة",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                Log.e("msg", task.exception?.message!!)
                            }
                        }
                } else {
                    Toast.makeText(
                        it.context,
                        "قم بتسحيل الدخول ليتم حفظ الخبر في قائمتك المفضلة",
                        Toast.LENGTH_SHORT
                    ).show()
                    holder.btnFav.isChecked = false
                }
            }else{
                if(user!=null){
                    articleList[position].liked = false
                    val update = mapOf(articleList[position].article.publishedAt to FieldValue.delete())
                    db.collection("root").document(user!!.uid).update(update).addOnCompleteListener {task->
                        if(task.isSuccessful){
                            Toast.makeText(
                                it.context,
                                "تم إزالة الخبر من أخبارك المفضلة",
                                Toast.LENGTH_SHORT
                            ).show()
                        }else{
                            Log.e("msgHeadAdapter",task.exception?.message!!)
                        }
                    }

                }
            }
        }

        }
        override fun getItemCount(): Int {
            return differ.currentList.size
        }
    }