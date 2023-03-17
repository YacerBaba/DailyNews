package owner.yacer.mynewsapp.Models

data class ResponseObject(
    val articles: List<Article>,
    val status: String,
    val totalResults: Int
)