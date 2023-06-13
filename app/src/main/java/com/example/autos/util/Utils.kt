package com.example.autos.util

import android.widget.EditText
import com.example.autos.domain.DomainCoche
import com.example.autos.domain.DomainRefueling
import com.google.android.material.textfield.TextInputLayout
import java.math.RoundingMode
import java.text.NumberFormat

private const val TAG = "xxUtils"

fun localNumberFormat(number: Int): String{
    return NumberFormat.getInstance().format(number)
}

fun localFloataFormat(float: Float): String{
    return NumberFormat.getInstance().format(float)
}

fun redondeaDecimales(float: Float, decimales: Int): Float{
    return float.toBigDecimal()
        .setScale(decimales, RoundingMode.HALF_UP)
        .toFloat()
}

fun flipDate(date: String): String{
    var partes = date.split("/")
    partes = partes.reversed()
    val template = "%s/%s/%s"
    return String.format(template, partes[0], partes[1], partes[2])
}

fun standardizeDate(date: String): String{
    val partes = date.split("/").toMutableList()
    if (partes[0].length < 2)     partes[0] = "0${partes[0]}"
    if (partes[1].length < 2)     partes[1] = "0${partes[1]}"
//    Log.d(TAG,"fecha estandarizada: ${partes.joinToString("/")}")
    return partes.joinToString("/")
}

fun validacion(campos: List<EditText>): Boolean {
    var ok = true
    campos.forEach { campo ->
        if (campo.text.toString().trim().isBlank()){
            ok = false
            (campo.parent.parent as TextInputLayout).error = " "
        } else {
            (campo.parent.parent as TextInputLayout).error = ""
        }
    }
    return ok
}


class AutoListener(val clickListener: (auto: DomainCoche) -> Unit){
    fun onClick(auto: DomainCoche) = clickListener(auto)
}

class RefuelListener(val clickListener: (refuel: DomainRefueling) -> Unit){
    fun onClick(refuel: DomainRefueling) = clickListener(refuel)
}