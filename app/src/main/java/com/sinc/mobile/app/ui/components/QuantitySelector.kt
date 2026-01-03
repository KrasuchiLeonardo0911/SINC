package com.sinc.mobile.app.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuantitySelector(
    modifier: Modifier = Modifier,
    label: String,
    quantity: String,
    onQuantityChange: (String) -> Unit
) {
    val currentQuantity = quantity.toIntOrNull() ?: 0

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.width(16.dp))

        SoftQuantityInput(
            modifier = Modifier.weight(1f),
            quantity = quantity,
            onQuantityChange = onQuantityChange,
            currentQuantity = currentQuantity
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SoftQuantityInput(
    modifier: Modifier = Modifier,
    quantity: String,
    onQuantityChange: (String) -> Unit,
    currentQuantity: Int,
) {
    Row(
        modifier = modifier
            .height(56.dp)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(24.dp) // Corrected to 24.dp
            )
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Decrement Button
        IconButton(
            onClick = {
                if (currentQuantity > 0) {
                    onQuantityChange((currentQuantity - 1).toString())
                }
            },
            enabled = currentQuantity > 0
        ) {
            Icon(
                imageVector = Icons.Default.Remove,
                contentDescription = "Disminuir cantidad",
                tint = if (currentQuantity > 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }

        // Quantity TextField
        OutlinedTextField(
            value = quantity,
            onValueChange = { newValue ->
                val filteredValue = newValue.filter { it.isDigit() }
                val newNum = filteredValue.toIntOrNull() ?: 0
                if (newNum >= 0) {
                    onQuantityChange(filteredValue)
                } else if (filteredValue.isEmpty()) {
                    onQuantityChange("")
                }
            },
            modifier = Modifier.weight(1f),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                disabledBorderColor = Color.Transparent,
                cursorColor = MaterialTheme.colorScheme.onSurface
            ),
            singleLine = true,
            textStyle = LocalTextStyle.current.copy(
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            ),
            placeholder = {
                Text(
                    text = "0",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        )

        // Increment Button
        IconButton(onClick = { onQuantityChange((currentQuantity + 1).toString()) }) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Aumentar cantidad",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}