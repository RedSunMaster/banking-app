@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)

package com.mcnut.banking.helpers

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.github.skydoves.colorpicker.compose.ColorEnvelope
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import com.mcnut.banking.R
import com.mcnut.banking.types.CategoryItem

@Composable
fun EditCategoryDialog(openDialog: Boolean, currentCategoryItem: CategoryItem, onSubmit: (newCategoryText: String, newCategoryColour: String, categoryId: Int) -> Unit, onDismiss: () -> Unit) {
    var newCategoryText by remember { mutableStateOf("") }
    var newCategoryColour by remember { mutableStateOf("")}
    val rowPadding = 10.dp
    val keyboardController = LocalSoftwareKeyboardController.current
    newCategoryText = currentCategoryItem.category

    if (openDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
        ) {
            Surface(
                modifier = Modifier
                    .wrapContentWidth()
                    .wrapContentHeight(),
                shape = MaterialTheme.shapes.large,
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top=rowPadding)
                    ) {
                        Icon(
                            painterResource(id = R.drawable.ic_description),
                            contentDescription = "Money icon",
                            modifier = Modifier.padding(end = 16.dp)
                        )
                        OutlinedTextField(
                            value = newCategoryText,
                            onValueChange = {newCategoryText = it},
                            label = { Text("New Category") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {keyboardController?.hide()}
                            ),
                        )
                    }
                    val controller = rememberColorPickerController()
                    var selectedColor by remember { mutableStateOf(currentCategoryItem.colour) }
                    Row {
                        HsvColorPicker(
                            modifier = Modifier
                                .weight(1f)
                                .height(200.dp)
                                .padding(10.dp),
                            controller = controller,
                            initialColor = currentCategoryItem.colour,
                            onColorChanged = { colorEnvelope: ColorEnvelope ->
                                val hexCodeWithoutAlpha = "#" + colorEnvelope.hexCode.substring(2)
                                selectedColor = Color(android.graphics.Color.parseColor(hexCodeWithoutAlpha))
                                newCategoryColour = "#" + Integer.toHexString(selectedColor.toArgb()).substring(2).uppercase()
                            }
                        )
                        Column (
                            modifier = Modifier
                                .wrapContentSize(Alignment.Center)
                                .align(Alignment.CenterVertically)
                                .weight(1f)
                                .width(80.dp)
                                ){
                            Box(
                                modifier = Modifier
                                    .width(50.dp)
                                    .height(50.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(selectedColor)
                            )
                            Text(text = "#" + Integer.toHexString(selectedColor.toArgb()).substring(2).uppercase())

                        }
                    }
                    Row(
                        modifier = Modifier.padding(all = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Spacer(modifier = Modifier.weight(1f))
                        Button(
                            onClick = {onSubmit(newCategoryText, newCategoryColour, currentCategoryItem.id)}
                        ) {
                            Text("Update Category")
                        }
                    }
                }
            }
        }
    }
}
