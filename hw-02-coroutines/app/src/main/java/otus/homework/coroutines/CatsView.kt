package otus.homework.coroutines

import android.content.Context
import android.util.AttributeSet
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.squareup.picasso.Picasso
import kotlinx.coroutines.launch

class CatsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private lateinit var viewModel: CatsViewModel

    override fun onFinishInflate() {
        super.onFinishInflate()
        findViewById<Button>(R.id.button).setOnClickListener {
            viewModel.getCatData()
        }
    }

    fun setViewModel(lifecycleOwner: LifecycleOwner, viewModel: CatsViewModel) {
        this.viewModel = viewModel

        lifecycleOwner.lifecycleScope.launch {
            viewModel.catState.collect { state ->
                when (state) {
                    is Result.Success<*> -> populate(state.data as CatData)
                    is Result.Error -> showToast(state.message)
                }
            }
        }
    }

    private fun populate(data: CatData) {
        findViewById<TextView>(R.id.fact_textView).text = data.fact
        Picasso.get()
            .load(data.imageUrl)
            .into(findViewById<ImageView>(R.id.cat_imageView))
    }

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}
