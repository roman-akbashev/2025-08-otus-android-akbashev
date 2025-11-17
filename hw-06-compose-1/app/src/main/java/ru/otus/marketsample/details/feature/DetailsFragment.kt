package ru.otus.marketsample.details.feature

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import coil.load
import kotlinx.coroutines.launch
import ru.otus.common.di.findDependencies
import ru.otus.marketsample.details.feature.di.DaggerDetailsComponent
import ru.otus.marketsample.R
import ru.otus.marketsample.databinding.FragmentDetailsBinding
import javax.inject.Inject

class DetailsFragment : Fragment() {

    private var _binding: FragmentDetailsBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var factory: DetailsViewModelFactory

    private val viewModel: DetailsViewModel by viewModels(
        factoryProducer = { factory }
    )

    private val productId by lazy { arguments?.getString("productId")!! }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        DaggerDetailsComponent.factory()
            .create(
                dependencies = findDependencies(),
                productId = productId,
            )
            .inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeUI()
    }

    private fun subscribeUI() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.state.collect { state ->
                        when {
                            state.isLoading -> showLoading()
                            state.hasError -> {
                                Toast.makeText(
                                    requireContext(),
                                    "Error wile loading data",
                                    Toast.LENGTH_SHORT
                                ).show()

                                viewModel.errorHasShown()
                            }

                            else -> showProduct(detailsState = state.detailsState)
                        }
                    }
                }
            }
        }
    }

    private fun showLoading() {
        hideAll()
        binding.progress.visibility = View.VISIBLE
    }

    private fun showProduct(detailsState: DetailsState) {
        hideAll()
        binding.image.load(detailsState.image)
        binding.image.visibility = View.VISIBLE

        binding.name.text = detailsState.name
        binding.name.visibility = View.VISIBLE

        binding.price.text = getString(R.string.price_with_arg, detailsState.price)
        binding.price.visibility = View.VISIBLE

        if (detailsState.hasDiscount) {
            binding.promo.visibility = View.VISIBLE
            binding.promo.text = detailsState.discount
        } else {
            binding.promo.visibility = View.GONE
        }

        binding.addToCart.visibility = View.VISIBLE
    }

    private fun hideAll() {
        binding.progress.visibility = View.GONE
        binding.image.visibility = View.GONE
        binding.name.visibility = View.GONE
        binding.price.visibility = View.GONE
        binding.progress.visibility = View.GONE
        binding.addToCart.visibility = View.GONE
    }
}
