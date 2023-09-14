package com.example.autos.util

import com.example.autos.NumberType
import java.math.RoundingMode
import java.text.NumberFormat

private const val TAG = "xxUtils"

enum class Limits(val lowLimit: Int, val highLimit: Int){
    // KMS
    OIL ( 10000, 15000 ),
    AIR (15000, 25000 ),
    TIRES (20000, 30000)
}

fun localNumberFormat(number: Int): String{
    return NumberFormat.getInstance().format(number)
}

fun localFloatFormat(float: Float): String{
    return NumberFormat.getInstance().format(float)
}

fun redondeaDecimales(float: Float, decimales: Int): Float{
    return float.toBigDecimal()
        .setScale(decimales, RoundingMode.HALF_UP)
        .toFloat()
}

fun flipDate(date: String?): String{
    return if (date != null) {
                var partes = date.split("/")
                partes = partes.reversed()
                val template = "%s/%s/%s"
                String.format(template, partes[0], partes[1], partes[2])
            } else ""
}

fun flipAndSortDate(date: String): String {
    val partes = date.split("/")
    val template = "%s/%s"
    return String.format(template, partes[2], partes[1])
}

fun standardizeDate(date: String): String{
    val partes = date.split("/").toMutableList()
    if (partes[0].length < 2)                       partes[0] = "0${partes[0]}"
    if (partes[1].length < 2)    partes[1] = "0${partes[1]}"

    return partes.joinToString("/")
}

fun validacion(campos: List<String>): Boolean {
    var ok = true
    campos.forEach { campo ->
        if (campo.isBlank()){
            ok = false
            return ok
        }
    }
    return ok
}

fun textEmpty(textValue: String): Boolean{
    return textValue.isBlank()
}

fun numeroValido(value: String, type: NumberType, minValue: Int = 0): Boolean{
    when(type){
        NumberType.INT -> {
            val patern = Regex("[0-9]{1,7}")
            return patern.matches(value) && value.toInt() > minValue
        }
        NumberType.FLOAT2 -> {
            val patern = Regex("""[0-9]{1,3}([.,][0-9]{1,2})?""")
            return patern.matches(value)
        }
        NumberType.FLOAT3 -> {
            val patern = Regex("""[0-9]?[.,][0-9]{1,3}""")
            return patern.matches(value)
        }
    }
}