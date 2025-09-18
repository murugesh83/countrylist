package com.smartshare.transfer.countrieslist

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import com.smartshare.transfer.countrieslist.ui.CountriesAdapter
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: CountriesViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var errorText: TextView
    private val adapter = CountriesAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerView)
        progressBar = findViewById(R.id.progressBar)
        errorText = findViewById(R.id.errorText)

        recyclerView.adapter = adapter

        viewModel = ViewModelProvider(this)[CountriesViewModel::class.java]

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE
                    if (state.errorMessage != null) {
                        errorText.visibility = View.VISIBLE
                        errorText.text = "Failed to load countries.\n${'$'}{state.errorMessage}\nTap to retry"
                    } else {
                        errorText.visibility = View.GONE
                    }
                    adapter.submitList(state.countries)
                }
            }
        }

        errorText.setOnClickListener {
            viewModel.retry()
        }

        viewModel.loadCountries()
    }
}