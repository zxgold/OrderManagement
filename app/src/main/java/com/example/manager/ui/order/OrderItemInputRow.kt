// 可以放在 ui/order 或 ui/components
package com.example.manager.ui.order

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.manager.viewmodel.TempOrderItem

@Composable
fun OrderItemInputRow(
    item: TempOrderItem,
    onQuantityChange: (String, Int) -> Unit,
    onPriceChange: (String, Double) -> Unit,
    onRemoveClick: (String) -> Unit
) {
    var quantity by remember(item.tempId) { mutableStateOf(item.quantity.toString()) }
    var price by remember(item.tempId) { mutableStateOf(item.actualUnitPrice.toString()) }

    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(item.productName, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                IconButton(onClick = { onRemoveClick(item.tempId) }) {
                    Icon(Icons.Filled.Delete, contentDescription = "移除")
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = quantity,
                    onValueChange = {
                        quantity = it
                        onQuantityChange(item.tempId, it.toIntOrNull() ?: 1)
                    },
                    label = { Text("数量") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = price,
                    onValueChange = {
                        price = it
                        onPriceChange(item.tempId, it.toDoubleOrNull() ?: 0.0)
                    },
                    label = { Text("单价") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }
        }
    }
}
