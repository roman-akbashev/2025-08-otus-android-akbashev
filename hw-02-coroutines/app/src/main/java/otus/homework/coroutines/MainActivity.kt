package otus.homework.coroutines

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private val diContainer = DiContainer()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val view = layoutInflater.inflate(R.layout.activity_main, null) as CatsView
        setContentView(view)
        val catsViewModel = CatsViewModel(diContainer.catsService)
        view.setViewModel(this, catsViewModel)
    }

    override fun onStop() {
        super.onStop()
    }
}