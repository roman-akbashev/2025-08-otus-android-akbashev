package ru.otus.cryptosample.coins.feature

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.coroutines.launch
import ru.otus.cryptosample.CoinsSampleApp
import ru.otus.cryptosample.coins.feature.adapter.CoinsAdapter
import ru.otus.cryptosample.coins.feature.di.DaggerCoinListComponent
import ru.otus.cryptosample.databinding.FragmentCoinListBinding
import javax.inject.Inject

class CoinListFragment : Fragment() {

    private var _binding: FragmentCoinListBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var factory: CoinListViewModelFactory

    private val viewModel: CoinListViewModel by viewModels { factory }

    private lateinit var coinsAdapter: CoinsAdapter

    override fun onAttach(context: Context) {
        super.onAttach(context)

        val appComponent = (activity?.applicationContext as CoinsSampleApp).appComponent

        DaggerCoinListComponent.factory()
            .create(appComponent)
            .inject(this)
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCoinListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupChipToggle()
        subscribeUI()
    }

    private fun setupRecyclerView() {
        coinsAdapter = CoinsAdapter()

        val gridLayoutManager = GridLayoutManager(requireContext(), 2)
        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return when (coinsAdapter.getItemViewType(position)) {
                    0 -> 2 // Category header spans full width
                    1 -> 1 // Coin item spans half width
                    else -> 1
                }
            }
        }

        binding.recyclerView.apply {
            layoutManager = gridLayoutManager
            adapter = coinsAdapter
        }
    }

    private fun setupChipToggle() {
        binding.highlightChip.setOnCheckedChangeListener { _, isChecked ->
            viewModel.onHighlightMoversToggled(isChecked)
        }

        binding.showAllChip.setOnCheckedChangeListener { _, isChecked ->
            viewModel.onShowAllToggled(isChecked)
        }
    }

    private fun subscribeUI() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    renderState(state)
                }
            }
        }
    }

    private fun renderState(state: CoinsScreenState) {
        coinsAdapter.setData(state.categories)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
