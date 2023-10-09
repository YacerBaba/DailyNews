package owner.yacer.mynewsapp.Utils

import java.io.FileInputStream
import java.util.Properties

class Utils {
    companion object{
        fun getAPIKey():String{
            val properties = Properties()
            val inputStream = FileInputStream("local.properties")
            properties.load(inputStream)
            val apiKey = properties.getProperty("api.key")
            inputStream.close()
            return apiKey
        }
    }
}