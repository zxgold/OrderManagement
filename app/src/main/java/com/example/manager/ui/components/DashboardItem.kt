package com.example.manager.ui.components // 确保包名正确

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun DashboardItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier // 允许外部传入 Modifier 以便灵活控制
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .padding(horizontal = 4.dp, vertical = 8.dp) // 给卡片之间留出一些间距
            .width(100.dp)
            .aspectRatio(1f), // 保持宽高比为1:1，使其看起来像个方块
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = MaterialTheme.shapes.medium // 使用主题定义的中等圆角
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp), // 卡片内部的 padding
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center // 使图标和文字在卡片内居中
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(36.dp), // 图标大小
                tint = MaterialTheme.colorScheme.primary // 给图标一个主题色
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge, // 使用更合适的标签文本样式
                textAlign = TextAlign.Center,
                maxLines = 2 // 允许标签文字换行，最多两行
            )
        }
    }
}