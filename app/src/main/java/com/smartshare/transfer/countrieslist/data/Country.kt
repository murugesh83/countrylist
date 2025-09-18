/*
 Country: Immutable model representing a country record from the JSON feed.
 - name: Display name of the country
 - region: Geographic region code from data
 - code: ISO-like country code shown on the right
 - capital: Capital city name
 @Murugesan Sagadevan
*/
package com.smartshare.transfer.countrieslist.data

data class Country(
    val name: String,
    val region: String,
    val code: String,
    val capital: String
)


