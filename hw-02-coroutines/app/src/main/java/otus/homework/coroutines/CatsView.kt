package otus.homework.coroutines

import android.content.Context
import android.util.AttributeSet
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import com.squareup.picasso.Picasso

class CatsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr), ICatsView {

    private var onButtonClick: (() -> Unit)? = null

    fun setOnButtonClickAction(action: () -> Unit) {
        this.onButtonClick = action
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        findViewById<Button>(R.id.button).setOnClickListener {
            onButtonClick?.invoke()
        }
    }

    override fun populate(data: CatData) {
        findViewById<TextView>(R.id.fact_textView).text = data.fact
        Picasso.get()
            .load(data.imageUrl)
            .into(findViewById<ImageView>(R.id.cat_imageView))
    }

    override fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}

interface ICatsView {

    fun populate(data: CatData)

    fun showToast(message: String)
}