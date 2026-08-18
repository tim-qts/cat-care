package com.tim.ui

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.tim.data.model.CatEntity
import com.tim.data.repository.CatRepository
import com.tim.ui.components.AddCatDialog
import com.tim.ui.components.CatDailyChecklistCard
import com.tim.ui.components.CsvExportDialog
import com.tim.ui.components.DateNavigator
import com.tim.ui.components.DeleteCatDialog
import com.tim.ui.components.EditCatDialog
import com.tim.ui.components.HeaderBanner
import com.tim.ui.components.TrendsView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: CatViewModel) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val cats by viewModel.cats.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val selectedCatIdForTrends by viewModel.selectedCatIdForTrends.collectAsState()
    val logsForDate by viewModel.logsForSelectedDate.collectAsState()
    val logsForSelectedCat by viewModel.logsForSelectedCat.collectAsState()

    val isAddCatOpen by viewModel.isAddCatDialogOpen.collectAsState()
    val isCsvExportOpen by viewModel.isCsvExportDialogOpen.collectAsState()
    val catToDelete by viewModel.catToDelete.collectAsState()
    val catToEdit by viewModel.catToEdit.collectAsState()
    val csvContent by viewModel.generatedCsvText.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }

    val logsMap = remember(logsForDate) { logsForDate.associateBy { it.catId } }

    // Local list state for reordering
    var localCats by remember(cats) { mutableStateOf(cats) }
    var draggedCatId by remember { mutableStateOf<Int?>(null) }
    var draggingOffsetY by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(cats) {
        if (draggedCatId == null) {
            localCats = cats
        }
    }

    LaunchedEffect(Unit) {
        viewModel.snackbarEvent.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    icon = { Icon(imageVector = Icons.Default.Today, contentDescription = "Daily Checklist") },
                    label = { Text("Daily Care", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.testTag("nav_daily_care")
                )

                NavigationBarItem(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    icon = { Icon(imageVector = Icons.Default.Analytics, contentDescription = "Individual Trends") },
                    label = { Text("Trends", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.testTag("nav_trends")
                )

                NavigationBarItem(
                    selected = selectedTabIndex == 2,
                    onClick = { selectedTabIndex = 2 },
                    icon = { Icon(imageVector = Icons.Default.Pets, contentDescription = "Manage Cats") },
                    label = { Text("My Cats (${cats.size})", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.testTag("nav_manage_cats")
                )
            }
        },
        floatingActionButton = {
            if (selectedTabIndex == 0 || selectedTabIndex == 2) {
                ExtendedFloatingActionButton(
                    onClick = { viewModel.isAddCatDialogOpen.value = true },
                    icon = { Icon(imageVector = Icons.Default.Add, contentDescription = null) },
                    text = { Text("Add Cat", fontWeight = FontWeight.Bold) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.testTag("fab_add_cat")
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Header Hero Banner
            item {
                HeaderBanner(
                    catCount = cats.size,
                    onExportCsvClick = { viewModel.prepareCsvExport() }
                )
            }

            when (selectedTabIndex) {
                0 -> {
                    // Daily Care Checklist View
                    item {
                        DateNavigator(
                            selectedDateString = selectedDate,
                            onPreviousDayClick = { viewModel.changeDateByDays(-1) },
                            onNextDayClick = { viewModel.changeDateByDays(1) },
                            onTodayClick = { viewModel.setSelectedDate(java.util.Calendar.getInstance()) }
                        )
                    }

                    if (cats.isEmpty()) {
                        item {
                            EmptyCatsCard(onAddCatClick = { viewModel.isAddCatDialogOpen.value = true })
                        }
                    } else {
                        items(cats, key = { it.id }) { cat ->
                            val log = logsMap[cat.id]
                            CatDailyChecklistCard(
                                cat = cat,
                                log = log,
                                onToggleTask = { taskType ->
                                    viewModel.toggleTask(cat, taskType)
                                },
                                onNotesChanged = { notes ->
                                    viewModel.updateNotes(cat.id, notes)
                                },
                                onDeleteCatClick = {
                                    viewModel.confirmDeleteCat(cat)
                                },
                                onEditCatClick = {
                                    viewModel.openEditCatDialog(cat)
                                }
                            )
                        }
                    }
                }

                1 -> {
                    // Individual Cat Trends & History View
                    item {
                        TrendsView(
                            cats = cats,
                            selectedCatId = selectedCatIdForTrends,
                            logsForCat = logsForSelectedCat,
                            onSelectCat = { catId -> viewModel.selectCatForTrends(catId) }
                        )
                    }
                }

                2 -> {
                    // Manage Cats List View with Drag-to-Reorder & Editing
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "↕️", fontSize = 22.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "My Cats — Drag & Reorder",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        text = "Drag ≡ up or down to reorder. Tap ✏️ to edit icon, color & details.",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                                    )
                                }
                            }
                        }
                    }

                    if (localCats.isEmpty()) {
                        item {
                            EmptyCatsCard(onAddCatClick = { viewModel.isAddCatDialogOpen.value = true })
                        }
                    } else {
                        items(localCats, key = { it.id }) { cat ->
                            val isDragging = draggedCatId == cat.id

                            ManageCatItem(
                                cat = cat,
                                isDragging = isDragging,
                                onDragStart = {
                                    draggedCatId = cat.id
                                    draggingOffsetY = 0f
                                },
                                onDrag = { dragY ->
                                    val draggingId = draggedCatId
                                    if (draggingId != null) {
                                        draggingOffsetY += dragY
                                        val stepPx = 140f
                                        val currIdx = localCats.indexOfFirst { it.id == draggingId }
                                        if (currIdx != -1) {
                                            if (draggingOffsetY > stepPx && currIdx < localCats.size - 1) {
                                                val mutable = localCats.toMutableList()
                                                val moved = mutable.removeAt(currIdx)
                                                mutable.add(currIdx + 1, moved)
                                                localCats = mutable
                                                draggingOffsetY -= stepPx
                                            } else if (draggingOffsetY < -stepPx && currIdx > 0) {
                                                val mutable = localCats.toMutableList()
                                                val moved = mutable.removeAt(currIdx)
                                                mutable.add(currIdx - 1, moved)
                                                localCats = mutable
                                                draggingOffsetY += stepPx
                                            }
                                        }
                                    }
                                },
                                onDragEnd = {
                                    if (draggedCatId != null) {
                                        viewModel.reorderCats(localCats)
                                    }
                                    draggedCatId = null
                                    draggingOffsetY = 0f
                                },
                                onEditClick = { viewModel.openEditCatDialog(cat) },
                                onDeleteClick = { viewModel.confirmDeleteCat(cat) },
                                modifier = if (isDragging) {
                                    Modifier
                                        .graphicsLayer {
                                            translationY = draggingOffsetY
                                            scaleX = 1.02f
                                            scaleY = 1.02f
                                            shadowElevation = 24f
                                        }
                                        .zIndex(20f)
                                } else Modifier.zIndex(1f)
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    // Dialogs
    if (isAddCatOpen) {
        AddCatDialog(
            onDismiss = { viewModel.isAddCatDialogOpen.value = false },
            onAddCat = { name, breed, colorHex, avatarEmoji, requiresMedication, medicationNotes ->
                viewModel.addCat(name, breed, colorHex, avatarEmoji, requiresMedication, medicationNotes)
            }
        )
    }

    if (catToEdit != null) {
        EditCatDialog(
            cat = catToEdit!!,
            onDismiss = { viewModel.closeEditCatDialog() },
            onSaveCat = { updatedCat ->
                viewModel.updateCat(updatedCat)
            }
        )
    }

    if (catToDelete != null) {
        DeleteCatDialog(
            cat = catToDelete!!,
            onDismiss = { viewModel.catToDelete.value = null },
            onConfirmDelete = { viewModel.deleteCatConfirmed() }
        )
    }

    if (isCsvExportOpen && csvContent != null) {
        CsvExportDialog(
            csvContent = csvContent!!,
            onDismiss = { viewModel.isCsvExportDialogOpen.value = false },
            onShareCsv = { content ->
                viewModel.shareCsvFile(context, content)
            }
        )
    }
}

@Composable
fun EmptyCatsCard(onAddCatClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "🐾", fontSize = 48.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "No Cats Registered",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Add your feline companions to start tracking their daily meals, poop, and medication!",
                textAlign = TextAlign.Center,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onAddCatClick,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Add Your First Cat 🐱")
            }
        }
    }
}

@Composable
fun ManageCatItem(
    cat: CatEntity,
    isDragging: Boolean,
    onDragStart: () -> Unit,
    onDrag: (Float) -> Unit,
    onDragEnd: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentOnDragStart by rememberUpdatedState(onDragStart)
    val currentOnDrag by rememberUpdatedState(onDrag)
    val currentOnDragEnd by rememberUpdatedState(onDragEnd)

    val catColor = try {
        Color(android.graphics.Color.parseColor(cat.colorHex))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }

    val elevation by animateDpAsState(
        targetValue = if (isDragging) 12.dp else 2.dp,
        label = "elevation"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDragging) catColor.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surface
        ),
        border = if (isDragging) BorderStroke(2.dp, catColor) else null,
        elevation = CardDefaults.cardElevation(defaultElevation = elevation)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Drag Handle
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isDragging) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .pointerInput(cat.id) {
                            detectDragGestures(
                                onDragStart = { currentOnDragStart() },
                                onDragEnd = { currentOnDragEnd() },
                                onDragCancel = { currentOnDragEnd() },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    currentOnDrag(dragAmount.y)
                                }
                            )
                        }
                        .padding(10.dp)
                        .testTag("drag_handle_${cat.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DragHandle,
                        contentDescription = "Drag handle for ${cat.name}",
                        tint = if (isDragging) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Avatar Icon
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(catColor.copy(alpha = 0.2f))
                        .border(1.5.dp, catColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = cat.avatarEmoji, fontSize = 24.sp)
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Cat Details
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = cat.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (cat.breed.isNotBlank()) {
                        Text(
                            text = cat.breed,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (cat.requiresMedication && cat.medicationNotes.isNotBlank()) {
                        Text(
                            text = "💊 ${cat.medicationNotes}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }

            // Action Buttons
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier.testTag("edit_cat_item_${cat.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Cat Details",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.testTag("delete_cat_item_${cat.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Remove Cat",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
