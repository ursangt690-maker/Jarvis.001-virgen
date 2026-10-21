package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.JarvisCardBorder
import com.example.ui.theme.JarvisSurface
import com.example.ui.theme.JarvisSurfaceElevated

@Composable
fun JarvisCard(
    modifier: Modifier = Modifier,
    borderColor: Color = JarvisCardBorder,
    borderWidth: Dp = 1.dp,
    cornerRadius: Dp = 16.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)

    val surfaceModifier = if (onClick != null) {
        modifier.clickable { onClick() }
    } else {
        modifier
    }

    Surface(
        modifier = surfaceModifier,
        shape = shape,
        color = JarvisSurface,
        border = BorderStroke(borderWidth, borderColor),
        tonalElevation = 2.dp
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            JarvisSurfaceElevated.copy(alpha = 0.55f),
                            JarvisSurface
                        )
                    )
                )
                .padding(16.dp)
        ) {
            content()
        }
    }
}
