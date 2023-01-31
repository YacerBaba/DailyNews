package owner.yacer.mynewsapp

data class ResponseObject(
    val articles: List<Article>,
    val status: String,
    val totalResults: Int
)