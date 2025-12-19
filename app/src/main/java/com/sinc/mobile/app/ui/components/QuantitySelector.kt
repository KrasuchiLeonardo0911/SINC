package com.sinc.mobile.app.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sinc.mobile.ui.theme.CozyDivider
import com.sinc.mobile.ui.theme.CozyTextMain
import com.sinc.mobile.ui.theme.InactiveGray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuantitySelector(
    modifier: Modifier = Modifier,
    label: String,
    quantity: String,
    onQuantityChange: (String) -> Unit
) {
    val currentQuantity = quantity.toIntOrNull() ?: 0

    Column(modifier = modifier) {
        Text(
            text = label,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = CozyTextMain
        )
        Spacer(Modifier.height(8.dp))

        SoftQuantityInput(
            quantity = quantity,
            onQuantityChange = onQuantityChange,
            currentQuantity = currentQuantity
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SoftQuantityInput(
    quantity: String,
    onQuantityChange: (String) -> Unit,
    currentQuantity: Int,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .border(
                width = 1.dp,
                color = CozyDivider,
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
                tint = if (currentQuantity > 0) CozyTextMain else InactiveGray
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
                cursorColor = CozyTextMain
            ),
            singleLine = true,
            textStyle = LocalTextStyle.current.copy(
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = CozyTextMain,
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
                tint = CozyTextMain
            )
        }
    }
}
