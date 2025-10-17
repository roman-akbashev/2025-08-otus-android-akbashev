package otus.homework.customview

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import otus.homework.customview.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val payloads = parseDataFromFile(this, R.raw.payload)
        binding.pieChartView.setData(payloads)
    }

    private fun parseDataFromFile(context: Context, rawResId: Int): List<Expense> {
        context.resources.openRawResource(rawResId).use { inputStream ->
            val json = inputStream.bufferedReader().readText()
            val type = object : TypeToken<List<Expense>>() {}.type
            return Gson().fromJson(json, type)
        }
    }
}