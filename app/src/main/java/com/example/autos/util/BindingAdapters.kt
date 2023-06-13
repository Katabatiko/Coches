package com.example.autos.util

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.example.autos.AutosStatus
import com.example.autos.R

@BindingAdapter("numberFormat")
fun TextView.formatNumber(number: Int){
    if (number != 0)
        text = localNumberFormat(number)
}

@BindingAdapter("floatFormat")
fun TextView.formatFloat(float: Float){
    if (float != 0f)
        text = localFloataFormat(float)
}

@BindingAdapter("dateFormat")
fun TextView.formatDate(date: String?){
    if (!date.isNullOrEmpty())
        text = flipDate(date)
}

/*@BindingAdapter("initialKms", "actualKms")
fun TextView.setInitialKms(initialKms: Int, actualKms: Int){
    text = localNumberFormat(actualKms - initialKms)
}*/

@BindingAdapter("nasaApiStatus")
fun bindStatusImage(statusImgView: ImageView, status: AutosStatus){
    when(status){
        AutosStatus.LOADING -> {
            statusImgView.visibility = View.VISIBLE
            statusImgView.setImageResource(R.drawable.loading_animation)
        }
        AutosStatus.ERROR -> {
            statusImgView.visibility = View.VISIBLE
            statusImgView.setImageResource(R.drawable.ic_broken_image)
        }
        AutosStatus.DONE -> {
            statusImgView.visibility = View.GONE
        }
    }
}
