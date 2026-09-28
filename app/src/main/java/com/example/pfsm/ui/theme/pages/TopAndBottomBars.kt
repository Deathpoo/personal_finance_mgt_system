package com.example.pfsm.ui.theme.pages

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.pfsm.R
import com.example.pfsm.ui.theme.design.PFSMTheme

data class BottomNavItem(
    val title: String,
    val outlinedIcon: ImageVector,
    val filledIcon: ImageVector,
    val selectedColor: Color
)


val navigationNavItems: List<BottomNavItem>
    @Composable
    get() = listOf(
        BottomNavItem(
            title = stringResource(R.string.home),
            outlinedIcon = Icons.Outlined.Home,
            filledIcon = Icons.Filled.Home,
            selectedColor = Color(0xFFFFC107)
        ),
        BottomNavItem(
            title = stringResource(R.string.budget),
            outlinedIcon = Icons.Outlined.AccountBalanceWallet,
            filledIcon = Icons.Filled.AccountBalanceWallet,
            selectedColor = Color(0xFF4CAF50)
        ),
        BottomNavItem(
            title = stringResource(R.string.dash_board),
            outlinedIcon = Icons.Outlined.BarChart,
            filledIcon = Icons.Filled.BarChart,
            selectedColor = Color(0xFF2196F3)
        ),
        BottomNavItem(
            title = stringResource(R.string.transactions),
            outlinedIcon = Icons.Outlined.ReceiptLong,
            filledIcon = Icons.Filled.ReceiptLong,
            selectedColor = Color(0xFFFF5722)
        )
    )

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    profileImage: ImageBitmap? = null,
    onProfileClick: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        actions = {
            Box(
                modifier = Modifier
                    .padding(end = 12.dp)
                    .size(40.dp)
                    .clip(CircleShape)
                    .clickable { onProfileClick() }
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                if (profileImage != null) {
                    Image(
                        bitmap = profileImage,
                        contentDescription = stringResource(R.string.profile),
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = stringResource(R.string.profile),
                        tint = MaterialTheme.colorScheme.scrim,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    )
}

@Composable
fun BottomAppBar(
    selectedItem: Int,
    onItemSelected: (Int) -> Unit
) {
    val items = navigationNavItems

    HorizontalDivider(
        color = Color.Gray,
        thickness = 0.2.dp,
        modifier = Modifier.padding(horizontal = 8.dp)
    )

    NavigationBar(
        containerColor = Color.Transparent,
    ) {
        items.forEachIndexed { index, item ->
            val isSelected = selectedItem == index

            val iconScale by animateFloatAsState(
                targetValue = if (isSelected) 1.2f else 1.0f,
                animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
                label = stringResource(R.string.iconScale)
            )

            val animatedColor by animateColorAsState(
                targetValue = if (isSelected) item.selectedColor else MaterialTheme.colorScheme.onSurfaceVariant,
                animationSpec = tween(220),
                label = stringResource(R.string.iconColor)
            )

            NavigationBarItem(
                selected = isSelected,
                onClick = { onItemSelected(index) },
                icon = {
                    Icon(
                        imageVector = if (isSelected) item.filledIcon else item.outlinedIcon,
                        contentDescription = item.title,
                        tint = animatedColor,
                        modifier = Modifier
                            .scale(iconScale)
                            .size(24.dp)
                    )
                },
                label = {
                    Text(
                        text = item.title,
                        color = animatedColor,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = item.selectedColor,
                    selectedTextColor = item.selectedColor,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}

@Composable
fun NavigationRailBar(
    selectedItem: Int,
    profileImage: ImageBitmap? = null,
    onProfileClick: () -> Unit,
    onItemSelected: (Int) -> Unit
) {
    val items = navigationNavItems

    NavigationRail(
        header = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .size(44.dp)
                    .clip(CircleShape)
                    .clickable { onProfileClick() }
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                if (profileImage != null) {
                    Image(
                        bitmap = profileImage,
                        contentDescription = stringResource(R.string.profile),
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = stringResource(R.string.profile),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    ) {
        items.forEachIndexed { index, item ->
            val isSelected = selectedItem == index

            val iconScale by animateFloatAsState(
                targetValue = if (isSelected) 1.2f else 1.0f,
                animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
                label = stringResource(R.string.iconScale)
            )

            val animatedColor by animateColorAsState(
                targetValue = if (isSelected) item.selectedColor else MaterialTheme.colorScheme.onSurfaceVariant,
                animationSpec = tween(220),
                label = stringResource(R.string.iconColor)
            )

            NavigationRailItem(
                selected = isSelected,
                onClick = { onItemSelected(index) },
                icon = {
                    Icon(
                        imageVector = if (isSelected) item.filledIcon else item.outlinedIcon,
                        contentDescription = item.title,
                        tint = animatedColor,
                        modifier = Modifier
                            .scale(iconScale)
                            .size(24.dp)
                    )
                },
                colors = NavigationRailItemDefaults.colors(
                    selectedIconColor = item.selectedColor,
                    selectedTextColor = item.selectedColor,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NavigationRailBarPreview() {
    PFSMTheme {
        NavigationRailBar(
            selectedItem = 0,
            onProfileClick = {},
            onItemSelected = {}
        )
    }
}