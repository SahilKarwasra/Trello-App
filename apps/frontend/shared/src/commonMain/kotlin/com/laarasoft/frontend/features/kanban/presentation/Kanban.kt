package com.laarasoft.frontend.features.kanban.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.laarasoft.frontend.core.utils.ObserveAsEvents
import com.laarasoft.frontend.core.utils.ui.shimmer
import com.laarasoft.frontend.features.kanban.domain.models.Issue
import com.laarasoft.frontend.features.kanban.domain.models.Section
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.roundToInt

// ── Root composable ───────────────────────────────────────────────────────────

@Composable
fun KanbanRoot(
    boardId: String = "",
    boardTitle: String = "",
    navigateBack: () -> Unit = {},
    navigateToSwitchBoard: () -> Unit = {},
    viewModel: KanbanViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(boardId, boardTitle) {
        if (boardId.isNotBlank()) {
            viewModel.onAction(KanbanAction.Init(boardId = boardId, boardTitle = boardTitle))
        }
    }

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is KanbanEvents.NavigateBack -> navigateBack()
            is KanbanEvents.NavigateToSwitchBoard -> navigateToSwitchBoard()
        }
    }

    KanbanScreen(state = state, onAction = viewModel::onAction)
}

// ── Screen ────────────────────────────────────────────────────────────────────

@Composable
fun KanbanScreen(
    state: KanbanState,
    onAction: (KanbanAction) -> Unit,
) {
    val dragDropState = rememberKanbanDragDropState()
    val scrollState = rememberScrollState()

    // Smooth horizontal edge auto-scrolling during drag
    LaunchedEffect(dragDropState.isDragging) {
        if (dragDropState.isDragging) {
            while (isActive) {
                val delta = dragDropState.calculateHorizontalScrollDelta(
                    dragDropState.boardSize.width.toFloat()
                )
                if (delta != 0f) {
                    scrollState.scrollBy(delta)
                }
                delay(16)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .onGloballyPositioned { coordinates ->
                dragDropState.registerBoardCoordinates(coordinates)
            }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar
            KanbanTopBar(
                boardTitle = state.boardTitle,
                onBackClick = { onAction(KanbanAction.OnBackClick) },
                onRefreshClick = { onAction(KanbanAction.OnRefresh) },
                isRefreshing = state.isRefreshing,
            )

            // Board area
            Box(modifier = Modifier.weight(1f)) {
                when {
                    state.isLoadingSections -> KanbanLoadingSkeleton()
                    state.sections.isEmpty() && !state.isLoadingSections -> KanbanEmptyState(
                        onAddSection = { onAction(KanbanAction.OnShowCreateSectionDialog) }
                    )
                    else -> KanbanBoard(
                        sections = state.sections,
                        dragDropState = dragDropState,
                        scrollState = scrollState,
                        onAction = onAction,
                        onAddSection = { onAction(KanbanAction.OnShowCreateSectionDialog) },
                        onAddIssue = { sectionId ->
                            onAction(KanbanAction.OnShowCreateIssueDialog(sectionId))
                        }
                    )
                }
            }
        }

        // ── Floating Dragged Issue Overlay (Neo-Brutalist) ──
        if (dragDropState.isDraggingIssue) {
            val issue = dragDropState.draggedIssue
            if (issue != null) {
                val pos = dragDropState.floatingCardPosition
                Box(
                    modifier = Modifier
                        .offset {
                            IntOffset(pos.x.roundToInt(), pos.y.roundToInt())
                        }
                        .width(260.dp)
                        .graphicsLayer {
                            rotationZ = -3f
                            scaleX = 1.05f
                            scaleY = 1.05f
                            shadowElevation = 24f
                        }
                        .zIndex(9999f)
                ) {
                    FloatingIssueCard(issue = issue)
                }
            }
        }

        // ── Floating Dragged Section Overlay (Neo-Brutalist) ──
        if (dragDropState.isDraggingSection) {
            val section = dragDropState.draggedSection
            if (section != null) {
                val pos = dragDropState.floatingSectionPosition
                Box(
                    modifier = Modifier
                        .offset {
                            IntOffset(pos.x.roundToInt(), pos.y.roundToInt())
                        }
                        .width(260.dp)
                        .graphicsLayer {
                            rotationZ = -2f
                            scaleX = 1.03f
                            scaleY = 1.03f
                            shadowElevation = 28f
                        }
                        .zIndex(9999f)
                ) {
                    FloatingSectionCard(section = section)
                }
            }
        }

        // Dialogs
        if (state.showCreateSectionDialog) {
            CreateSectionDialog(
                title = state.newSectionTitle,
                isCreating = state.isCreatingSection,
                onTitleChange = { onAction(KanbanAction.OnNewSectionTitleChange(it)) },
                onConfirm = { onAction(KanbanAction.OnCreateSection) },
                onDismiss = { onAction(KanbanAction.OnDismissCreateSectionDialog) }
            )
        }

        if (state.showCreateIssueDialog) {
            CreateIssueDialog(
                title = state.newIssueTitle,
                description = state.newIssueDescription,
                isCreating = state.isCreatingIssue,
                onTitleChange = { onAction(KanbanAction.OnNewIssueTitleChange(it)) },
                onDescriptionChange = { onAction(KanbanAction.OnNewIssueDescriptionChange(it)) },
                onConfirm = { onAction(KanbanAction.OnCreateIssue) },
                onDismiss = { onAction(KanbanAction.OnDismissCreateIssueDialog) }
            )
        }
    }
}

// ── Top Bar (Neo-Brutalism) ───────────────────────────────────────────────────

@Composable
private fun KanbanTopBar(
    boardTitle: String,
    isRefreshing: Boolean,
    onBackClick: () -> Unit,
    onRefreshClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = 2.dp,
                color = MaterialTheme.colorScheme.onBackground,
                shape = RoundedCornerShape(0.dp)
            )
            .padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = boardTitle.ifBlank { "Kanban Board" },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            IconButton(
                onClick = onRefreshClick,
                modifier = Modifier.size(36.dp)
            ) {
                if (isRefreshing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = "Refresh",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

// ── Board horizontal scroll with Section Drag & Drop ──────────────────────────

@Composable
private fun KanbanBoard(
    sections: List<Section>,
    dragDropState: KanbanDragDropState,
    scrollState: androidx.compose.foundation.ScrollState,
    onAction: (KanbanAction) -> Unit,
    onAddSection: () -> Unit,
    onAddIssue: (sectionId: String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .horizontalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        sections.forEachIndexed { originalIndex, section ->
            key(section.id) {
                val isThisSectionDragged = dragDropState.isDraggingSection && dragDropState.draggedSection?.id == section.id

                val candidateIndex = if (dragDropState.isDraggingSection) {
                    val candidateList = sections.filter { it.id != dragDropState.draggedSection?.id }
                    candidateList.indexOfFirst { it.id == section.id }
                } else originalIndex

                // Show drop placeholder before this section if hovered here
                if (dragDropState.isDraggingSection && dragDropState.targetSectionIndex == candidateIndex && !isThisSectionDragged) {
                    DropSectionIndicator()
                }

                KanbanColumn(
                    section = section,
                    sectionIndex = originalIndex,
                    allSections = sections,
                    dragDropState = dragDropState,
                    isBeingDragged = isThisSectionDragged,
                    onAction = onAction,
                    onAddIssue = { onAddIssue(section.id) }
                )
            }
        }

        // Show drop placeholder at the end of columns if target is at or after the end
        val otherSectionCount = if (dragDropState.isDraggingSection) {
            sections.count { it.id != dragDropState.draggedSection?.id }
        } else {
            sections.size
        }
        if (dragDropState.isDraggingSection && dragDropState.targetSectionIndex >= otherSectionCount) {
            DropSectionIndicator()
        }

        // Add section column
        AddSectionColumn(onClick = onAddSection)
    }
}

// ── Kanban Column (Neo-Brutalism with Drag Gestures) ──────────────────────────

@Composable
private fun KanbanColumn(
    section: Section,
    sectionIndex: Int,
    allSections: List<Section>,
    dragDropState: KanbanDragDropState,
    isBeingDragged: Boolean,
    onAction: (KanbanAction) -> Unit,
    onAddIssue: () -> Unit,
) {
    val shape = RoundedCornerShape(12.dp)
    val isHoveredByIssue = dragDropState.isDraggingIssue && dragDropState.hoveredSectionId == section.id

    Box(
        modifier = Modifier
            .width(IntrinsicSize.Max)
            .onGloballyPositioned { coordinates ->
                dragDropState.registerSectionCoordinates(section.id, coordinates)
            }
            .alpha(if (isBeingDragged) 0.25f else 1f)
    ) {
        // Offset shadow (neo-brutalism)
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = 4.dp, y = 4.dp)
                .background(
                    if (isHoveredByIssue) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
                    shape
                )
        )

        Column(
            modifier = Modifier
                .width(260.dp)
                .clip(shape)
                .background(MaterialTheme.colorScheme.surface, shape)
                .border(
                    width = if (isHoveredByIssue) 2.5.dp else 2.dp,
                    color = if (isHoveredByIssue) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
                    shape = shape
                )
        ) {
            // ── Column header (Long-press to drag and reorder column) ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .pointerInput(section.id) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = { offset ->
                                dragDropState.startDraggingSection(
                                    section = section,
                                    sourceIndex = sectionIndex,
                                    grabOffset = offset,
                                    itemSize = size,
                                    allSections = allSections
                                )
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                dragDropState.onDrag(dragAmount, allSections)
                            },
                            onDragEnd = {
                                dragDropState.onDragEnd(
                                    onMoveIssue = { fromSec, toSec, issId, targetIdx ->
                                        onAction(KanbanAction.MoveIssue(fromSec, toSec, issId, targetIdx))
                                    },
                                    onReorderSections = { fromIdx, toIdx ->
                                        onAction(KanbanAction.ReorderSections(fromIdx, toIdx))
                                    }
                                )
                            },
                            onDragCancel = { dragDropState.onDragCancel() }
                        )
                    }
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Section Title
                Text(
                    text = section.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Issues count badge
                if (section.issues.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f),
                                RoundedCornerShape(4.dp)
                            )
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.3f),
                                RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 1.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${section.issues.size}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                }

                // Drag indicator icon
                Icon(
                    imageVector = Icons.Filled.DragIndicator,
                    contentDescription = "Hold to drag column",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f),
                    modifier = Modifier.size(18.dp)
                )
            }

            // ── Divider ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(MaterialTheme.colorScheme.onBackground)
            )

            // ── Issue cards with Drop Target Indicator ──
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val issues = section.issues.sortedBy { it.position }

                if (issues.isEmpty()) {
                    if (isHoveredByIssue) {
                        DropIssueIndicator()
                    } else {
                        EmptyColumnPlaceholder()
                    }
                } else {
                    issues.forEachIndexed { originalIndex, issue ->
                        key(issue.id) {
                            val isThisCardDragged = dragDropState.isDraggingIssue && dragDropState.draggedIssue?.id == issue.id

                            val candidateIndex = if (dragDropState.isDraggingIssue) {
                                val candidateList = issues.filter { it.id != dragDropState.draggedIssue?.id }
                                candidateList.indexOfFirst { it.id == issue.id }
                            } else originalIndex

                            // Show drop indicator before this card
                            if (isHoveredByIssue && dragDropState.targetIssueIndex == candidateIndex && !isThisCardDragged) {
                                DropIssueIndicator()
                            }

                            IssueCard(
                                issue = issue,
                                index = originalIndex,
                                section = section,
                                allSections = allSections,
                                dragDropState = dragDropState,
                                isBeingDragged = isThisCardDragged,
                                onAction = onAction
                            )
                        }
                    }

                    // Show drop indicator at the bottom of the column if target index is at or after the end
                    val otherIssueCount = if (dragDropState.isDraggingIssue) {
                        issues.count { it.id != dragDropState.draggedIssue?.id }
                    } else {
                        issues.size
                    }
                    if (isHoveredByIssue && dragDropState.targetIssueIndex >= otherIssueCount) {
                        DropIssueIndicator()
                    }
                }
            }

            // ── Add card footer ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onAddIssue)
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Add card",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Add a card",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

// ── Issue Card (Neo-Brutalism with Drag Support) ──────────────────────────────

@Composable
private fun IssueCard(
    issue: Issue,
    index: Int,
    section: Section,
    allSections: List<Section>,
    dragDropState: KanbanDragDropState,
    isBeingDragged: Boolean,
    onAction: (KanbanAction) -> Unit,
) {
    val shape = RoundedCornerShape(8.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .onGloballyPositioned { coordinates ->
                dragDropState.registerIssueCoordinates(issue.id, coordinates)
            }
            .alpha(if (isBeingDragged) 0.25f else 1f)
            .pointerInput(issue.id) {
                detectDragGesturesAfterLongPress(
                    onDragStart = { offset ->
                        dragDropState.startDraggingIssue(
                            issue = issue,
                            sourceSecId = section.id,
                            sourceIndex = index,
                            grabOffset = offset,
                            itemSize = size,
                            allSections = allSections
                        )
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        dragDropState.onDrag(dragAmount, allSections)
                    },
                    onDragEnd = {
                        dragDropState.onDragEnd(
                            onMoveIssue = { fromSec, toSec, issId, targetIdx ->
                                onAction(KanbanAction.MoveIssue(fromSec, toSec, issId, targetIdx))
                            },
                            onReorderSections = { fromIdx, toIdx ->
                                onAction(KanbanAction.ReorderSections(fromIdx, toIdx))
                            }
                        )
                    },
                    onDragCancel = { dragDropState.onDragCancel() }
                )
            }
    ) {
        // Offset shadow (neo-brutalism)
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = 3.dp, y = 3.dp)
                .background(MaterialTheme.colorScheme.onBackground, shape)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(MaterialTheme.colorScheme.background, shape)
                .border(1.5.dp, MaterialTheme.colorScheme.onBackground, shape)
                .padding(10.dp),
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = issue.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (issue.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = issue.description,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Visual drag handle icon
            Icon(
                imageVector = Icons.Filled.DragHandle,
                contentDescription = "Hold to drag card",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier
                    .size(16.dp)
                    .padding(start = 4.dp)
            )
        }
    }
}

// ── Floating Elevated Issue Card (Rendered during drag) ───────────────────────

@Composable
private fun FloatingIssueCard(issue: Issue) {
    val shape = RoundedCornerShape(8.dp)
    Box(modifier = Modifier.fillMaxWidth()) {
        // Deep shadow
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = 6.dp, y = 6.dp)
                .background(MaterialTheme.colorScheme.onBackground, shape)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(MaterialTheme.colorScheme.surface, shape)
                .border(2.5.dp, MaterialTheme.colorScheme.onBackground, shape)
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = issue.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (issue.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = issue.description,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Icon(
                imageVector = Icons.Filled.DragIndicator,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

// ── Floating Elevated Section Preview (Rendered during column drag) ───────────

@Composable
private fun FloatingSectionCard(section: Section) {
    val shape = RoundedCornerShape(12.dp)
    Box(modifier = Modifier.width(260.dp)) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = 8.dp, y = 8.dp)
                .background(MaterialTheme.colorScheme.onBackground, shape)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(MaterialTheme.colorScheme.surface, shape)
                .border(2.5.dp, MaterialTheme.colorScheme.onBackground, shape)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = section.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Icon(
                    imageVector = Icons.Filled.DragIndicator,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(20.dp)
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(MaterialTheme.colorScheme.onBackground)
            )
            Text(
                text = "${section.issues.size} cards",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(14.dp)
            )
        }
    }
}

// ── Drop Target Indicators ────────────────────────────────────────────────────

@Composable
private fun DropIssueIndicator() {
    val shape = RoundedCornerShape(8.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(shape)
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
            .border(
                width = 2.dp,
                color = MaterialTheme.colorScheme.primary,
                shape = shape
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "Drop card here",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun DropSectionIndicator() {
    val shape = RoundedCornerShape(12.dp)
    Box(
        modifier = Modifier
            .width(260.dp)
            .height(200.dp)
            .clip(shape)
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f))
            .border(
                width = 2.5.dp,
                color = MaterialTheme.colorScheme.primary,
                shape = shape
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Dashboard,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            Text(
                text = "Drop list here",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

// ── Empty column placeholder ──────────────────────────────────────────────────

@Composable
private fun EmptyColumnPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(
                width = 1.5.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(8.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "No cards yet",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ── Add Section column ────────────────────────────────────────────────────────

@Composable
private fun AddSectionColumn(onClick: () -> Unit) {
    val shape = RoundedCornerShape(12.dp)
    Box {
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = 4.dp, y = 4.dp)
                .background(MaterialTheme.colorScheme.outline, shape)
        )
        Column(
            modifier = Modifier
                .width(200.dp)
                .clip(shape)
                .background(MaterialTheme.colorScheme.background, shape)
                .border(2.dp, MaterialTheme.colorScheme.onBackground, shape)
                .clickable(onClick = onClick)
                .padding(vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        MaterialTheme.colorScheme.primary,
                        RoundedCornerShape(8.dp)
                    )
                    .border(
                        1.5.dp,
                        MaterialTheme.colorScheme.onBackground,
                        RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Add section",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Add list",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

// ── Loading skeleton ──────────────────────────────────────────────────────────

@Composable
private fun KanbanLoadingSkeleton() {
    val shape = RoundedCornerShape(12.dp)
    val scrollState = rememberScrollState()
    Row(
        modifier = Modifier
            .fillMaxSize()
            .horizontalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        repeat(3) { index ->
            Box {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .offset(x = 4.dp, y = 4.dp)
                        .background(
                            MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                            shape
                        )
                )
                Column(
                    modifier = Modifier
                        .width(260.dp)
                        .clip(shape)
                        .background(MaterialTheme.colorScheme.surface, shape)
                        .border(
                            2.dp,
                            MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                            shape
                        )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .shimmer()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .background(MaterialTheme.colorScheme.outline)
                    )

                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        repeat(if (index == 0) 3 else if (index == 1) 1 else 2) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .shimmer()
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Empty state ───────────────────────────────────────────────────────────────

@Composable
private fun KanbanEmptyState(onAddSection: () -> Unit) {
    val shape = RoundedCornerShape(16.dp)
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Box {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(x = 5.dp, y = 5.dp)
                    .background(MaterialTheme.colorScheme.onBackground, shape)
            )
            Column(
                modifier = Modifier
                    .clip(shape)
                    .background(MaterialTheme.colorScheme.surface, shape)
                    .border(2.dp, MaterialTheme.colorScheme.onBackground, shape)
                    .padding(horizontal = 40.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            MaterialTheme.colorScheme.primaryContainer,
                            RoundedCornerShape(14.dp)
                        )
                        .border(
                            2.dp,
                            MaterialTheme.colorScheme.onBackground,
                            RoundedCornerShape(14.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Dashboard,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Text(
                    text = "No lists yet",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Add your first list to get started",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                val btnShape = RoundedCornerShape(8.dp)
                Box {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .offset(x = 3.dp, y = 3.dp)
                            .background(MaterialTheme.colorScheme.onBackground, btnShape)
                    )
                    Row(
                        modifier = Modifier
                            .clip(btnShape)
                            .background(MaterialTheme.colorScheme.primary, btnShape)
                            .border(2.dp, MaterialTheme.colorScheme.onBackground, btnShape)
                            .clickable(onClick = onAddSection)
                            .padding(horizontal = 20.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                        Text(
                            text = "Add a list",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
    }
}

// ── Create Section Dialog ─────────────────────────────────────────────────────

@Composable
private fun CreateSectionDialog(
    title: String,
    isCreating: Boolean,
    onTitleChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = { if (!isCreating) onDismiss() },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
        title = {
            Text(
                text = "Add a list",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            OutlinedTextField(
                value = title,
                onValueChange = onTitleChange,
                label = { Text("List name") },
                placeholder = { Text("e.g. To Do, In Progress") },
                singleLine = true,
                enabled = !isCreating,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                )
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = title.isNotBlank() && !isCreating,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                if (isCreating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Add list", fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isCreating
            ) {
                Text("Cancel", fontWeight = FontWeight.Medium)
            }
        }
    )
}

// ── Create Issue Dialog ───────────────────────────────────────────────────────

@Composable
private fun CreateIssueDialog(
    title: String,
    description: String,
    isCreating: Boolean,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = { if (!isCreating) onDismiss() },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
        title = {
            Text(
                text = "Add a card",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = onTitleChange,
                    label = { Text("Card title") },
                    placeholder = { Text("e.g. Fix login bug") },
                    singleLine = true,
                    enabled = !isCreating,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    )
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = onDescriptionChange,
                    label = { Text("Description (optional)") },
                    placeholder = { Text("Details or acceptance criteria") },
                    maxLines = 3,
                    enabled = !isCreating,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = title.isNotBlank() && !isCreating,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                if (isCreating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Add card", fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isCreating
            ) {
                Text("Cancel", fontWeight = FontWeight.Medium)
            }
        }
    )
}