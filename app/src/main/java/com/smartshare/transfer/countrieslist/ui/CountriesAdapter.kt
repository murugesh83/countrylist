package com.smartshare.transfer.countrieslist.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.smartshare.transfer.countrieslist.R
import com.smartshare.transfer.countrieslist.data.Country

class CountriesAdapter : ListAdapter<Country, CountriesAdapter.CountryViewHolder>(DiffCallback) {

    object DiffCallback : DiffUtil.ItemCallback<Country>() {
        override fun areItemsTheSame(oldItem: Country, newItem: Country): Boolean {
            return oldItem.code == newItem.code && oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: Country, newItem: Country): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CountryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_country, parent, false)
        return CountryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CountryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class CountryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameRegionText: TextView = itemView.findViewById(R.id.nameRegionText)
        private val codeText: TextView = itemView.findViewById(R.id.codeText)
        private val capitalText: TextView = itemView.findViewById(R.id.capitalText)

        fun bind(country: Country) {
            nameRegionText.text = "${country.name}, ${country.region}"
            codeText.text = country.code
            capitalText.text = country.capital
        }
    }
}


