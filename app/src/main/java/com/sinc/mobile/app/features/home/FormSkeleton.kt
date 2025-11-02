package com.sinc.mobile.app.features.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.accompanist.placeholder.material3.placeholder

@Composable
fun FormSkeleton(
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(top = 16.dp)) {
        Spacer(modifier = Modifier.height(16.dp))
        // Placeholder for Especie Selector
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .placeholder(visible = true)
        )
        Spacer(modifier = Modifier.height(16.dp))
        // Placeholder for Categoria Selector
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .placeholder(visible = true)
        )
        Spacer(modifier = Modifier.height(16.dp))
        // Placeholder for Raza Selector
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .placeholder(visible = true)
        )
    }
}
