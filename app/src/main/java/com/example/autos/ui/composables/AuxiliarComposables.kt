package com.example.autos.ui.composables

import android.view.LayoutInflater
import android.widget.Button
import android.widget.DatePicker
import android.widget.TextView
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.autos.R
import com.example.autos.util.standardizeDate

private const val TAG = "xxAuxC"

//@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DatePickerView(
    head: String,
    modifier: Modifier = Modifier,
    fieldInput: (String) -> Unit,
    changeDate: (Boolean) -> Unit
) {
//    val bringIntoViewRequester = remember { BringIntoViewRequester() }
//    val coroutineScope = rememberCoroutineScope()
    AndroidView(
        factory = { context ->
            val view = LayoutInflater.from(context).inflate(R.layout.datepicker_layout, null, false)

            view
        },
        modifier = modifier.background(color = MaterialTheme.colorScheme.surfaceVariant),
//                        .bringIntoViewRequester(bringIntoViewRequester)
//                        .onFocusEvent { focusState ->
//                            if (focusState.isFocused) {
//                                coroutineScope.launch {
//                                    bringIntoViewRequester.bringIntoView()
//                                }
//                            }
//                        }
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

@Preview(showBackground = true)
@Composable
fun PreviewDatePickerView(){
    DatePickerView(
        head = "Prueba de DatePickerView",
        fieldInput = {},
        changeDate = {}
    )
}

@Composable
fun Dato(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    labelModifier: Modifier = Modifier
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
            modifier = Modifier
                .padding(start = 2.dp)
                .fillMaxWidth(),
            maxLines = 1

        )
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
                            modifier = Modifier.weight(0.5f, true)
                                .padding(end = 16.dp)
                        ) {
                            Text(stringResource(id = android.R.string.cancel))
                        }
                        Button(
                            onClick = {
                                        openDialog.value = false
                                        onAccept()
                                      },
                            modifier = Modifier.weight(0.5f, true)
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

@Preview
@Composable
fun ShowRestoreDialog() {
    RestoreDialog(
        show = true,
        onAccept = {}
    )
}