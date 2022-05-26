package com.example.cryptobiz

object Converters {
    fun currencyDoubleToInt(value: Double, precision: Int = 2): Int {
        var exp = 1
        for (i in 1..precision) {
            exp *= 10
        }
        return (value * exp).toInt()
    }

    fun currencyIntToString(value: Int, precision: Int = 2): String {
        var str = value.toString()
        while (str.length <= precision) {
            str = "0$str"
        }
        return "${str.dropLast(precision)}.${str.takeLast(precision)}"
    }
}