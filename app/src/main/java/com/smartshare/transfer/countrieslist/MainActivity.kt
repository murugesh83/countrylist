/*
 MainActivity: Hosts the Countries screen.
 - Configures edge-to-edge and dark system bars.
 - Applies system bar insets so content sits below status bar and above nav bar.
 - Binds a RecyclerView with CountriesAdapter.
 - Observes CountriesViewModel's StateFlow and renders loading/success/error.
 - Provides tap-to-retry on error.
 @Murugesan Sagadevan
*/
package com.smartshare.transfer.countrieslist

import android.os.Bundle
import android.view.View
import android.graphics.Color
import android.app.AlertDialog
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
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
    private lateinit var statusBarOverlay: View
    private lateinit var titleText: TextView
    private val adapter = CountriesAdapter()
    private var hasShownNoNetworkDialog: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        // Make system bars dark with light icons
        window.statusBarColor = Color.BLACK
        window.navigationBarColor = Color.BLACK
        val ic = WindowCompat.getInsetsController(window, window.decorView)
        ic.isAppearanceLightStatusBars = false
        ic.isAppearanceLightNavigationBars = false
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerView)
        progressBar = findViewById(R.id.progressBar)
        errorText = findViewById(R.id.errorText)
        statusBarOverlay = findViewById(R.id.statusBarOverlay)
        titleText = findViewById(R.id.titleText)

        recyclerView.adapter = adapter

        // Apply system bar insets as padding so content sits within safe area
        ViewCompat.setOnApplyWindowInsetsListener(recyclerView) { v, insets ->
            val sysBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(bottom = sysBars.bottom)
            statusBarOverlay.layoutParams.height = sysBars.top
            statusBarOverlay.requestLayout()
            insets
        }

        viewModel = ViewModelProvider(this)[CountriesViewModel::class.java]

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE
                    if (state.errorMessage != null) {
                        errorText.visibility = View.VISIBLE
                        errorText.text = "Failed to load countries.\n${'$'}{state.errorMessage}\nTap to retry"
                        if (!isNetworkAvailable() && !hasShownNoNetworkDialog) {
                            showNoNetworkDialog()
                            hasShownNoNetworkDialog = true
                        }
                    } else {
                        errorText.visibility = View.GONE
                        hasShownNoNetworkDialog = false
                    }
                    adapter.submitList(state.countries)
                }
            }
        }

        errorText.setOnClickListener {
            if (!isNetworkAvailable()) {
                showNoNetworkDialog()
                hasShownNoNetworkDialog = true
            } else {
                viewModel.retry()
            }
        }

        if (!isNetworkAvailable()) {
            errorText.visibility = View.VISIBLE
            errorText.text = "No network available. Tap to retry"
            showNoNetworkDialog()
            hasShownNoNetworkDialog = true
        } else {
            viewModel.loadCountries()
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    private fun showNoNetworkDialog() {
        AlertDialog.Builder(this)
            .setTitle("No network")
            .setMessage("No network available. Please check your connection and try again.")
            .setPositiveButton("Retry") { dialog, _ ->
                dialog.dismiss()
                if (isNetworkAvailable()) {
                    viewModel.retry()
                } else {
                    // Show again to prompt the user until network is back
                    showNoNetworkDialog()
                }
            }
            .setNegativeButton("Dismiss") { dialog, _ -> dialog.dismiss() }
            .show()
    }
}