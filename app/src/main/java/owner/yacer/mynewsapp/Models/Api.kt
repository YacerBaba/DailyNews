package owner.yacer.mynewsapp.Models

import okhttp3.OkHttp
import owner.yacer.mynewsapp.Utils.Utils
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface Api {
    @GET("v2/everything")
    suspend fun getNews(
        @Query("domains") domain:String = "bbc.com",
        @Query("apiKey") key:String = Utils.getAPIKey()
    ):Response<ResponseObject>
}