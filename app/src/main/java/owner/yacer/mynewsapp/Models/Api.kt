package owner.yacer.mynewsapp.Models

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface Api {
    @GET("v2/everything")
    suspend fun getNews(
        @Query("domains") domain:String = "bbc.com",
        @Query("apiKey") key:String = "YOUR_API_KEY"
    ):Response<ResponseObject>
}