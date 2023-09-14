package com.example.autos.ui.composables

import android.view.LayoutInflater
import android.widget.Button
import android.widget.DatePicker
import android.widget.TextView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.autos.NumberType
import com.example.autos.R
import com.example.autos.util.Limits
import com.example.autos.util.localNumberFormat
import com.example.autos.util.numeroValido
import com.example.autos.util.standardizeDate
import com.example.autos.util.textEmpty

private const val TAG = "xxAuxC"

@Composable
fun DatePickerView(
    head: String,
    modifier: Modifier = Modifier,
    fieldInput: (String) -> Unit,
    changeDate: (Boolean) -> Unit
) {
    AndroidView(
        factory = { context ->
            val view = LayoutInflater.from(context).inflate(R.layout.datepicker_layout, null, false)

            view
        },
        modifier = modifier.background(
            color = MaterialTheme.colorScheme.surfaceVariant
        ),
    ) {
        val headTv = it.findViewById<TextView>(R.id.date_picker_head)
        headTv.text = head
        val dateSpinner: DatePicker = it.findViewById(R.id.date_Picker)

        val okBtn = it.findViewById<Button>(R.id.ok_btn)
        okBtn.setOnClickListener {
            val dateString = "${dateSpinner.dayOfMonth}/${dateSpinner.month + 1}/${dateSpinner.year}"
            fieldInput( standardizeDate(dateString) )
            changeDate( false )
        }
    }
}

@Composable
fun DateInput(
    label: String,
    modifier: Modifier = Modifier,
    focusManager: FocusManager,
    dateInput: String,
    onDataChange: (String) -> Unit
) {
    val dateFocus = rememberSaveable { mutableStateOf(false) }
    val error = remember { mutableStateOf(false) }

    if (dateFocus.value){
        DatePickerView(
            head = label,
            modifier = modifier,
            fieldInput = onDataChange,
            changeDate = { dateFocus.value = it}
        )
    } else {
        OutlinedTextField(
            value = dateInput,
            onValueChange = {
                onDataChange(it)
                error.value = textEmpty(it)
            },
            label = { Text(text = label) },
            trailingIcon = @Composable {
                IconButton(onClick = { dateFocus.value = true }) {
                    Icon(
                        Icons.Outlined.DateRange,
                        contentDescription = stringResource(id = R.string.date_adjust)
                    )
                }
            },
            isError = error.value,
            modifier = modifier
                .onFocusChanged { state ->
                    if (state.isFocused) {
                        dateFocus.value = true
                    }
                },
            keyboardOptions = KeyboardOptions( imeAction = ImeAction.Next ),
            keyboardActions = KeyboardActions(
                onNext = {
                    error.value = textEmpty(dateInput)
                    focusManager.moveFocus(FocusDirection.Next)
                }
            ),
            readOnly = true
        )
    }
}

@Composable
fun Dato(
    modifier: Modifier = Modifier,
    value: String,
    label: String,
    valueAtEnd: Boolean = false,
    labelModifier: Modifier = Modifier,
    valueModifier: Modifier = Modifier
) {
    Row(modifier) {
        Text(
            text = label,
            modifier = labelModifier,
            fontWeight = FontWeight.Bold,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1
        )
        Text(
            text = value,
            modifier = valueModifier,
            textAlign = if (valueAtEnd) TextAlign.End
                        else TextAlign.Start,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun DatoNumericoInput(
    label: String,
    numberType: NumberType,
    modifier: Modifier = Modifier,
    focusManager: FocusManager,
    focusRequest: Boolean = false,
    value: String,
    lastValue: String,
    lastInput: Boolean = false,
    auxFunc: (() -> Unit)? = null,
    onDataChange: (String) -> Unit
){
    val error = rememberSaveable{ mutableStateOf(false) }
    var focusRequester: FocusRequester? = null
    if (focusRequest)   focusRequester = remember { FocusRequester() }

    Row(
        modifier = modifier
            .fillMaxWidth()
    ) {
        Text(
            text = label,
            modifier = Modifier
                .fillMaxWidth(0.35f)
                .align(Alignment.CenterVertically)
        )
        OutlinedTextField(
            value = TextFieldValue(value, TextRange(value.length)),
            onValueChange = {
                onDataChange(it.text)
                error.value = if (numberType == NumberType.INT){
                                    !numeroValido(it.text, numberType, lastValue.toInt())
                                } else {
                                    !numeroValido(it.text, numberType)
                                }
            },
            modifier =
                if (!focusRequest) {
                    Modifier
                        .fillMaxWidth()
                        .align(Alignment.CenterVertically)
                } else {
                    Modifier
                        .fillMaxWidth()
                        .align(Alignment.CenterVertically)
                        .focusRequester(focusRequester!!)
                },
            label = { Text( lastValue ) },
            isError = error.value,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next,
                keyboardType = KeyboardType.Number
            ),
            keyboardActions = KeyboardActions(
                onNext = {
                    error.value = textEmpty(value)
                    if (lastInput)  focusManager.clearFocus()
                    else            focusManager.moveFocus(FocusDirection.Next)
                    if (auxFunc != null) {
                        auxFunc()
                    }
                }
            )
        )
        if (focusRequest) {
            LaunchedEffect(Unit) {
                focusRequester!!.requestFocus()
            }
        }
    }
}

@Composable
fun StringInput(
    label: String,
    modifier: Modifier = Modifier,
    focusManager: FocusManager,
    focusRequest: Boolean = false,
    stringInput: String,
    lastInput: Boolean = false,
    onDataChange: (String) -> Unit
) {
    val error = rememberSaveable { mutableStateOf(false) }
    var focusRequester: FocusRequester? = null
    if (focusRequest)   focusRequester = remember { FocusRequester() }

    OutlinedTextField(
        value = TextFieldValue(stringInput, TextRange(stringInput.length)),
        modifier =
            if (!focusRequest) {
                modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            } else {
                modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .focusRequester(focusRequester!!)
            },
        onValueChange = {
            onDataChange(it.text)
            error.value = textEmpty(it.text)
        },
        isError = error.value,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Sentences,
            imeAction = ImeAction.Next
        ),
        keyboardActions = KeyboardActions(
            onNext = {
                error.value = textEmpty(stringInput)
                if (lastInput)  focusManager.clearFocus()
                else            focusManager.moveFocus(FocusDirection.Next)
            }
        )
    )
    if (focusRequest) {
        LaunchedEffect(Unit) {
            focusRequester!!.requestFocus()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestoreDialog(
    show: Boolean,
    onAccept: () -> Unit
) {
    MaterialTheme {
        val openDialog = remember { mutableStateOf(show) }
        if (openDialog.value) {
            AlertDialog(
                onDismissRequest = { },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.inversePrimary)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(
                        text = stringResource(R.string.restore_dialog_msg),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(vertical = 12.dp),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Button(
                            onClick = {
                                        openDialog.value = false
                                      },
                            modifier = Modifier
                                .weight(0.5f, true)
                                .padding(end = 16.dp)
                        ) {
                            Text(stringResource(id = android.R.string.cancel))
                        }
                        Button(
                            onClick = {
                                        openDialog.value = false
                                        onAccept()
                                      },
                            modifier = Modifier
                                .weight(0.5f, true)
                                .padding(start = 16.dp)
                        ) {
                            Text(stringResource(id = android.R.string.ok))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Notice(
    modifier: Modifier = Modifier,
    actualData: State<Int?> = mutableStateOf(0),
    headText: String = "",
    limits: Limits,
    icon: Int = android.R.drawable.alert_light_frame
) {
    val backColor: Color
    val textColor: Color
    val borderColor: Color

    when{
        (actualData.value ?: 0) > limits.highLimit -> {
            borderColor = MaterialTheme.colorScheme.onTertiaryContainer
            backColor = colorResource(id = R.color.top_notice)
            textColor = colorResource(id = R.color.on_top)
        }
        (actualData.value ?: 0) > limits.lowLimit -> {
            borderColor = MaterialTheme.colorScheme.onTertiaryContainer
            backColor = colorResource(id = R.color.bottom_notice)
            textColor = colorResource(id = R.color.on_bottom)
        }
        else -> {
            borderColor = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.15f)
            backColor = colorResource(id = android.R.color.transparent)
            textColor = colorResource(id = android.R.color.transparent)
        }
    }
    Column(
        modifier = modifier
            .width(76.dp)
            .heightIn(76.dp)
            .background(
                backColor,
                RoundedCornerShape(10.dp)
            )
            .border(
                4.dp,
                color = borderColor,
                shape = RoundedCornerShape(10.dp)
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if ((actualData.value ?: 0) > limits.lowLimit) {
            Text(
                text = headText,
                modifier = Modifier.padding(start = 4.dp, top = 2.dp, end = 4.dp),
                color = textColor,
                fontWeight = FontWeight.Bold,
                lineHeight = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Icon(
                painter = painterResource(icon),
                contentDescription = "Uso de ${limits.name} $actualData kms",
                tint = textColor
            )
            Text(
                text = "${localNumberFormat(actualData.value ?: 0)} ${stringResource(id = R.string.kms_)}",
                modifier = Modifier.padding(start = 4.dp, bottom = 1.dp, end = 4.dp),
                color = textColor,
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                lineHeight = 10.sp,
                minLines = 2
            )
        }
    }
}

@Preview
@Composable
fun BadgePreview() {
    val actualData = remember{ mutableStateOf(40500) }
    Notice(
        actualData = actualData,
        headText = "Delanteras",
        limits = Limits.TIRES,
        icon = R.drawable.wheel,
    )
}

//@Preview
//@Composable
//fun ShowRestoreDialog() {
//    RestoreDialog(
//        show = true,
//        onAccept = {}
//    )
//}

//@Preview(showBackground = true)
//@Composable
//fun PreviewDatePickerView(){
//    DatePickerView(
//        head = "Prueba de DatePickerView",
//        fieldInput = {},
//        changeDate = {}
//    )
//}