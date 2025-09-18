/*
 CountriesViewModel: Holds UI state and orchestrates data loading.
 - Exposes CountriesUiState via StateFlow for lifecycle-aware observation.
 - Loads countries from CountriesService with coroutines and handles errors.
 - Caches results to survive configuration changes; supports retry.
 @Murugesan Sagadevan
*/
package com.smartshare.transfer.countrieslist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartshare.transfer.countrieslist.data.CountriesService
import com.smartshare.transfer.countrieslist.data.Country
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CountriesUiState(
    val isLoading: Boolean = false,
    val countries: List<Country> = emptyList(),
    val errorMessage: String? = null
)

class CountriesViewModel(
    private val service: CountriesService = CountriesService()
) : ViewModel() {

    private val _uiState = MutableStateFlow(CountriesUiState(isLoading = false))
    val uiState: StateFlow<CountriesUiState> = _uiState.asStateFlow()

    private var currentJob: Job? = null

    fun loadCountries() {
        if (_uiState.value.countries.isNotEmpty() || _uiState.value.isLoading) return
        currentJob?.cancel()
        _uiState.value = CountriesUiState(isLoading = true)
        currentJob = viewModelScope.launch {
            val result = service.fetchCountries(COUNTRIES_URL)
            result.onSuccess { list ->
                _uiState.value = CountriesUiState(isLoading = false, countries = list, errorMessage = null)
            }.onFailure { e ->
                _uiState.value = CountriesUiState(isLoading = false, countries = emptyList(), errorMessage = e.message ?: "Unknown error")
            }
        }
    }

    fun retry() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
        loadCountries()
    }

    companion object {
        const val COUNTRIES_URL = "https://gist.githubusercontent.com/peymano-wmt/32dcb892b06648910ddd40406e37fdab/raw/db25946fd77c5873b0303b858e861ce724e0dcd0/countries.json"
    }
}


