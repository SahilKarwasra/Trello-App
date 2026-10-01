package com.laarasoft.frontend.features.kanban.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.IntSize
import com.laarasoft.frontend.features.kanban.domain.models.Issue
import com.laarasoft.frontend.features.kanban.domain.models.Section
import kotlin.math.abs

enum class DragType {
    NONE,
    ISSUE,
    SECTION
}

@Stable
class KanbanDragDropState {
    var activeDragType by mutableStateOf(DragType.NONE)
        private set

    val isDragging: Boolean
        get() = activeDragType != DragType.NONE

    val isDraggingIssue: Boolean
        get() = activeDragType == DragType.ISSUE

    val isDraggingSection: Boolean
        get() = activeDragType == DragType.SECTION

    // Issue drag state
    var draggedIssue by mutableStateOf<Issue?>(null)
        private set
    var sourceSectionId by mutableStateOf<String?>(null)
        private set
    var sourceIssueIndex by mutableStateOf(-1)
        private set
    var hoveredSectionId by mutableStateOf<String?>(null)
        private set
    var targetIssueIndex by mutableStateOf(-1)
        private set

    // Section drag state
    var draggedSection by mutableStateOf<Section?>(null)
        private set
    var sourceSectionIndex by mutableStateOf(-1)
        private set
    var targetSectionIndex by mutableStateOf(-1)
        private set

    // Coordinate & pointer tracking
    var currentTouchInBoard by mutableStateOf(Offset.Zero)
        private set
    var touchGrabOffset by mutableStateOf(Offset.Zero)
        private set
    var draggedItemSize by mutableStateOf(IntSize.Zero)
        private set

    var boardOffsetInRoot by mutableStateOf(Offset.Zero)
        private set
    var boardSize by mutableStateOf(IntSize.Zero)
        private set

    val sectionBounds = mutableStateMapOf<String, Rect>()
    val issueBounds = mutableStateMapOf<String, Rect>()

    val floatingCardPosition: Offset
        get() = currentTouchInBoard - touchGrabOffset

    val floatingSectionPosition: Offset
        get() = currentTouchInBoard - touchGrabOffset

    fun registerBoardCoordinates(coordinates: LayoutCoordinates) {
        boardOffsetInRoot = coordinates.positionInRoot()
        boardSize = coordinates.size
    }

    fun registerSectionCoordinates(sectionId: String, coordinates: LayoutCoordinates) {
        if (!coordinates.isAttached) return
        val posInRoot = coordinates.positionInRoot()
        val offsetInBoard = posInRoot - boardOffsetInRoot
        val size = coordinates.size
        sectionBounds[sectionId] = Rect(
            offsetInBoard,
            Size(size.width.toFloat(), size.height.toFloat())
        )
    }

    fun registerIssueCoordinates(issueId: String, coordinates: LayoutCoordinates) {
        if (!coordinates.isAttached) return
        val posInRoot = coordinates.positionInRoot()
        val offsetInBoard = posInRoot - boardOffsetInRoot
        val size = coordinates.size
        issueBounds[issueId] = Rect(
            offsetInBoard,
            Size(size.width.toFloat(), size.height.toFloat())
        )
    }

    fun startDraggingIssue(
        issue: Issue,
        sourceSecId: String,
        sourceIndex: Int,
        grabOffset: Offset,
        itemSize: IntSize,
        allSections: List<Section>
    ) {
        draggedIssue = issue
        sourceSectionId = sourceSecId
        sourceIssueIndex = sourceIndex
        hoveredSectionId = sourceSecId
        targetIssueIndex = sourceIndex
        touchGrabOffset = grabOffset
        draggedItemSize = itemSize

        val cardRect = issueBounds[issue.id]
        currentTouchInBoard = if (cardRect != null) {
            cardRect.topLeft + grabOffset
        } else {
            grabOffset
        }

        activeDragType = DragType.ISSUE
        updateIssueHoverTargets(allSections)
    }

    fun startDraggingSection(
        section: Section,
        sourceIndex: Int,
        grabOffset: Offset,
        itemSize: IntSize,
        allSections: List<Section>
    ) {
        draggedSection = section
        sourceSectionIndex = sourceIndex
        targetSectionIndex = sourceIndex
        touchGrabOffset = grabOffset
        draggedItemSize = itemSize

        val secRect = sectionBounds[section.id]
        currentTouchInBoard = if (secRect != null) {
            secRect.topLeft + grabOffset
        } else {
            grabOffset
        }

        activeDragType = DragType.SECTION
        updateSectionHoverTargets(allSections)
    }

    fun onDrag(dragAmount: Offset, allSections: List<Section>) {
        currentTouchInBoard += dragAmount
        when (activeDragType) {
            DragType.ISSUE -> updateIssueHoverTargets(allSections)
            DragType.SECTION -> updateSectionHoverTargets(allSections)
            DragType.NONE -> {}
        }
    }

    private fun updateIssueHoverTargets(allSections: List<Section>) {
        if (activeDragType != DragType.ISSUE || allSections.isEmpty()) return

        // 1. Detect hovered section
        var bestSection: Section? = null
        var minDistanceX = Float.MAX_VALUE

        for (sec in allSections) {
            val rect = sectionBounds[sec.id] ?: continue
            // Check if touch is horizontally within section
            if (currentTouchInBoard.x >= rect.left && currentTouchInBoard.x <= rect.right) {
                bestSection = sec
                break
            }
            val centerX = (rect.left + rect.right) / 2f
            val dist = abs(currentTouchInBoard.x - centerX)
            if (dist < minDistanceX) {
                minDistanceX = dist
                bestSection = sec
            }
        }

        val targetSec = bestSection ?: allSections.firstOrNull() ?: return
        hoveredSectionId = targetSec.id

        // 2. Detect target drop index in that section
        val candidateIssues = targetSec.issues.filter { it.id != draggedIssue?.id }
        if (candidateIssues.isEmpty()) {
            targetIssueIndex = 0
            return
        }

        var targetIdx = candidateIssues.size
        for (i in candidateIssues.indices) {
            val iss = candidateIssues[i]
            val rect = issueBounds[iss.id]
            if (rect != null) {
                val centerY = (rect.top + rect.bottom) / 2f
                if (currentTouchInBoard.y < centerY) {
                    targetIdx = i
                    break
                }
            }
        }
        targetIssueIndex = targetIdx
    }

    private fun updateSectionHoverTargets(allSections: List<Section>) {
        if (activeDragType != DragType.SECTION || allSections.isEmpty()) return

        val candidateSections = allSections.filter { it.id != draggedSection?.id }
        if (candidateSections.isEmpty()) {
            targetSectionIndex = 0
            return
        }

        var targetIdx = candidateSections.size
        for (i in candidateSections.indices) {
            val sec = candidateSections[i]
            val rect = sectionBounds[sec.id]
            if (rect != null) {
                val centerX = (rect.left + rect.right) / 2f
                if (currentTouchInBoard.x < centerX) {
                    targetIdx = i
                    break
                }
            }
        }
        targetSectionIndex = targetIdx
    }

    fun onDragEnd(
        onMoveIssue: (fromSecId: String, toSecId: String, issueId: String, targetIdx: Int) -> Unit,
        onReorderSections: (fromIdx: Int, toIdx: Int) -> Unit
    ) {
        when (activeDragType) {
            DragType.ISSUE -> {
                val issue = draggedIssue
                val fromSec = sourceSectionId
                val toSec = hoveredSectionId
                val targetIdx = targetIssueIndex
                if (issue != null && fromSec != null && toSec != null && targetIdx >= 0) {
                    onMoveIssue(fromSec, toSec, issue.id, targetIdx)
                }
            }
            DragType.SECTION -> {
                val fromIdx = sourceSectionIndex
                val toIdx = targetSectionIndex
                if (fromIdx >= 0 && toIdx >= 0 && fromIdx != toIdx) {
                    onReorderSections(fromIdx, toIdx)
                }
            }
            DragType.NONE -> {}
        }
        reset()
    }

    fun onDragCancel() {
        reset()
    }

    fun calculateHorizontalScrollDelta(viewportWidth: Float): Float {
        if (activeDragType == DragType.NONE || viewportWidth <= 0f) return 0f
        val edgeThreshold = 90f // pixels
        val maxSpeed = 24f // pixels per frame
        val touchX = currentTouchInBoard.x

        return when {
            touchX > viewportWidth - edgeThreshold -> {
                val ratio = ((touchX - (viewportWidth - edgeThreshold)) / edgeThreshold).coerceIn(0f, 1f)
                ratio * maxSpeed
            }
            touchX < edgeThreshold -> {
                val ratio = ((edgeThreshold - touchX) / edgeThreshold).coerceIn(0f, 1f)
                -ratio * maxSpeed
            }
            else -> 0f
        }
    }

    private fun reset() {
        activeDragType = DragType.NONE
        draggedIssue = null
        sourceSectionId = null
        sourceIssueIndex = -1
        hoveredSectionId = null
        targetIssueIndex = -1
        draggedSection = null
        sourceSectionIndex = -1
        targetSectionIndex = -1
        currentTouchInBoard = Offset.Zero
        touchGrabOffset = Offset.Zero
        draggedItemSize = IntSize.Zero
    }
}

@Composable
fun rememberKanbanDragDropState(): KanbanDragDropState {
    return remember { KanbanDragDropState() }
}
