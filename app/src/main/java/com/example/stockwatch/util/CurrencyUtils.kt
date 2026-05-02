package com.example.stockwatch.util

object CurrencyUtils {
    fun getSymbol(currency: String): String {
        return when (currency.lowercase()) {
            "eur" -> "€"
            "pln" -> "zł"
            "gbp" -> "£"
            else -> "$"
        }
    }
}
