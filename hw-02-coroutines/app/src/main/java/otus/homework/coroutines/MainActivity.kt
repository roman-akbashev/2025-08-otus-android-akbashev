package otus.homework.coroutines

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val diContainer = DiContainer()

    lateinit var catsPresenter: CatsPresenter

    private val catsViewModel: CatsViewModel by viewModels {
        CatsViewModel.CatsViewModelFactory(diContainer.catsService)
    }

    private val isPresenter = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val view = layoutInflater.inflate(R.layout.activity_main, null) as CatsView
        setContentView(view)

        if (isPresenter) {
            catsPresenter = CatsPresenter(diContainer.catsService)
            catsPresenter.attachView(view)
            view.setOnButtonClickAction {
                catsPresenter.onInitComplete()
            }
        } else {
            view.setOnButtonClickAction {
                catsViewModel.onInitComplete()
                lifecycleScope.launch {
                    catsViewModel.catState.collect { state ->
                        when (state) {
                            is Result.Success<*> -> view.populate(state.data as CatData)
                            is Result.Error -> view.showToast(state.message)
                        }
                    }
                }
            }
        }
    }

    override fun onStop() {
        if (isFinishing && isPresenter) {
            catsPresenter.detachView()
        }
        super.onStop()
    }
}