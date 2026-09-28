package com.example.ui.screens.designer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DesignerComponentItem
import com.example.data.model.DesignerWireItem
import com.example.ui.theme.CircuitGold
import com.example.ui.theme.CircuitTrace
import com.example.ui.theme.LedGreen
import com.example.ui.theme.LedRed
import kotlin.math.roundToInt

@Composable
fun CircuitDesignerScreen(
    components: List<DesignerComponentItem>,
    wires: List<DesignerWireItem>,
    onAddComponent: (String, String, String) -> Unit,
    onRemoveComponent: (String) -> Unit,
    onRotateComponent: (String) -> Unit,
    onUpdatePosition: (String, Float, Float) -> Unit,
    onClearWorkspace: () -> Unit,
    onCheckCircuit: () -> String,
    onAddWire: (String, String, String, String) -> Unit = { _, _, _, _ -> },
    onDeleteWire: (String) -> Unit = {},
    onDuplicateComponent: (String) -> Unit = {},
    onUpdateComponentValue: (String, String) -> Unit = { _, _ -> },
    onRestoreState: (List<DesignerComponentItem>, List<DesignerWireItem>) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current.density

    // Designer state
    var isFullScreen by remember { mutableStateOf(false) }
    var currentTool by remember { mutableStateOf(DesignerTool.SELECT) }
    var selectedCompId by remember { mutableStateOf<String?>(null) }
    var selectedWireId by remember { mutableStateOf<String?>(null) }
    var pendingWirePin by remember { mutableStateOf<Pair<String, String>?>(null) } // (compId, pinId)
    var showLibrarySheet by remember { mutableStateOf(false) }
    var showPropertiesSheet by remember { mutableStateOf(false) }
    var validationFeedback by remember { mutableStateOf<String?>(null) }

    // Canvas Navigation state
    var zoomLevel by remember { mutableStateOf(1.0f) }
    var panOffsetX by remember { mutableStateOf(0f) }
    var panOffsetY by remember { mutableStateOf(0f) }
    var snapToGrid by remember { mutableStateOf(true) }
    var showGridDots by remember { mutableStateOf(true) }

    // Undo / Redo history stack
    var history by remember { mutableStateOf(listOf<DesignerHistorySnapshot>()) }
    var historyIndex by remember { mutableStateOf(-1) }

    // Helper to push state
    fun recordHistory(comps: List<DesignerComponentItem>, wrs: List<DesignerWireItem>) {
        val newSnapshot = DesignerHistorySnapshot(comps, wrs)
        val trimmed = if (historyIndex >= 0 && historyIndex < history.size) {
            history.take(historyIndex + 1)
        } else {
            history
        }
        history = (trimmed + newSnapshot).takeLast(25)
        historyIndex = history.size - 1
    }

    // Initialize history once if empty
    LaunchedEffect(Unit) {
        if (history.isEmpty()) {
            history = listOf(DesignerHistorySnapshot(components, wires))
            historyIndex = 0
        }
    }

    val selectedComponent = components.firstOrNull { it.id == selectedCompId }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF070C15))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (isFullScreen) 0.dp else 12.dp)
        ) {
            // Top Control Bar (Toolbar)
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = if (isFullScreen) RoundedCornerShape(0.dp) else RoundedCornerShape(14.dp),
                border = if (isFullScreen) null else CardDefaults.outlinedCardBorder(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                    // Row 1: Title, Quick Status, Fullscreen Button, Check Circuit
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.DeveloperBoard,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isFullScreen) "Circuit Workspace" else "Circuit Designer",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "${components.size} Comps • ${wires.size} Wires",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = MaterialTheme.colorScheme.primary,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.sp
                                    ),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Check Circuit Button
                            Button(
                                onClick = {
                                    validationFeedback = onCheckCircuit()
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = CircuitTrace),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier
                                    .height(34.dp)
                                    .testTag("check_circuit_button")
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Check Circuit", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }

                            // Full Screen Toggle Button
                            FilledTonalButton(
                                onClick = { isFullScreen = !isFullScreen },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier
                                    .height(34.dp)
                                    .testTag("fullscreen_toggle_button")
                            ) {
                                Icon(
                                    imageVector = if (isFullScreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                                    contentDescription = if (isFullScreen) "Exit Full Screen" else "Full Screen",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isFullScreen) "Exit" else "Full Screen", fontSize = 11.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Row 2: Tool Palette & Action Controls (Scrollable)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Open Component Library Button
                        Button(
                            onClick = { showLibrarySheet = true },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("open_component_library_button")
                        ) {
                            Icon(Icons.Default.AddCircleOutline, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Component", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        VerticalDivider(modifier = Modifier.height(20.dp).padding(horizontal = 2.dp))

                        // Tool Selectors (Select, Move, Wire, Pan)
                        DesignerTool.values().forEach { tool ->
                            val isSelected = currentTool == tool
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    currentTool = tool
                                    if (tool != DesignerTool.WIRE) {
                                        pendingWirePin = null
                                    }
                                },
                                leadingIcon = {
                                    Icon(tool.icon, contentDescription = null, modifier = Modifier.size(14.dp))
                                },
                                label = { Text(tool.label, fontSize = 11.sp) },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(32.dp)
                            )
                        }

                        VerticalDivider(modifier = Modifier.height(20.dp).padding(horizontal = 2.dp))

                        // Undo Button
                        IconButton(
                            onClick = {
                                if (historyIndex > 0) {
                                    historyIndex--
                                    val snap = history[historyIndex]
                                    onRestoreState(snap.components, snap.wires)
                                    selectedCompId = null
                                    selectedWireId = null
                                    pendingWirePin = null
                                }
                            },
                            enabled = historyIndex > 0,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Undo,
                                contentDescription = "Undo",
                                tint = if (historyIndex > 0) MaterialTheme.colorScheme.onSurface else Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // Redo Button
                        IconButton(
                            onClick = {
                                if (historyIndex < history.size - 1) {
                                    historyIndex++
                                    val snap = history[historyIndex]
                                    onRestoreState(snap.components, snap.wires)
                                    selectedCompId = null
                                    selectedWireId = null
                                    pendingWirePin = null
                                }
                            },
                            enabled = historyIndex < history.size - 1,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Redo,
                                contentDescription = "Redo",
                                tint = if (historyIndex < history.size - 1) MaterialTheme.colorScheme.onSurface else Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        VerticalDivider(modifier = Modifier.height(20.dp).padding(horizontal = 2.dp))

                        // Zoom Controls
                        IconButton(
                            onClick = { zoomLevel = (zoomLevel * 1.15f).coerceIn(0.5f, 2.5f) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.ZoomIn, contentDescription = "Zoom In", modifier = Modifier.size(16.dp))
                        }

                        IconButton(
                            onClick = { zoomLevel = (zoomLevel / 1.15f).coerceIn(0.5f, 2.5f) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.ZoomOut, contentDescription = "Zoom Out", modifier = Modifier.size(16.dp))
                        }

                        IconButton(
                            onClick = {
                                zoomLevel = 1.0f
                                panOffsetX = 0f
                                panOffsetY = 0f
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.FitScreen, contentDescription = "Fit to screen", modifier = Modifier.size(16.dp))
                        }

                        // Snap-to-grid toggle
                        IconButton(
                            onClick = { snapToGrid = !snapToGrid },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Grid4x4,
                                contentDescription = "Snap to grid",
                                tint = if (snapToGrid) CircuitGold else Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // Clear Board
                        IconButton(
                            onClick = {
                                onClearWorkspace()
                                selectedCompId = null
                                selectedWireId = null
                                pendingWirePin = null
                                recordHistory(emptyList(), emptyList())
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = "Clear Canvas", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // Validation Feedback Banner
            AnimatedVisibility(visible = validationFeedback != null) {
                if (validationFeedback != null) {
                    val isPass = validationFeedback!!.startsWith("✅")
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isPass) LedGreen.copy(alpha = 0.15f) else Color(0xFFFF9100).copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isPass) LedGreen else Color(0xFFFF9100)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isPass) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (isPass) LedGreen else Color(0xFFFF9100),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = validationFeedback!!,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { validationFeedback = null },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Dismiss", modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }
            }

            // Active Tool / Wire Connection Instruction Bar
            if (currentTool == DesignerTool.WIRE) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF0F2537),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CircuitTrace),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Timeline, contentDescription = null, tint = CircuitTrace, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (pendingWirePin == null)
                                    "Tap any component terminal (pin) to start drawing a wire"
                                else
                                    "Now tap a terminal on another component to connect!",
                                style = MaterialTheme.typography.labelSmall.copy(color = Color.White, fontWeight = FontWeight.SemiBold)
                            )
                        }

                        if (pendingWirePin != null) {
                            TextButton(
                                onClick = { pendingWirePin = null },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(26.dp)
                            ) {
                                Text("Cancel", color = Color(0xFFFF9100), fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            // Quick Selection Inspector Bar (when component or wire selected)
            if (selectedComponent != null) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${selectedComponent.label} (${selectedComponent.value})",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Angle: ${selectedComponent.rotationDeg}°",
                                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            // Properties Dialog
                            IconButton(
                                onClick = { showPropertiesSheet = true },
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(Icons.Default.Tune, contentDescription = "Properties", tint = CircuitGold, modifier = Modifier.size(16.dp))
                            }

                            // Rotate
                            IconButton(
                                onClick = {
                                    onRotateComponent(selectedComponent.id)
                                    recordHistory(components, wires)
                                },
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(Icons.Default.RotateRight, contentDescription = "Rotate", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            }

                            // Duplicate
                            IconButton(
                                onClick = {
                                    onDuplicateComponent(selectedComponent.id)
                                    recordHistory(components, wires)
                                },
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Duplicate", tint = Color.LightGray, modifier = Modifier.size(16.dp))
                            }

                            // Delete
                            IconButton(
                                onClick = {
                                    onRemoveComponent(selectedComponent.id)
                                    selectedCompId = null
                                    recordHistory(components, wires)
                                },
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            } else if (selectedWireId != null) {
                // Wire selection inspector
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.dp, CircuitGold),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Wire Selected",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = CircuitGold)
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(
                                onClick = {
                                    onDeleteWire(selectedWireId!!)
                                    selectedWireId = null
                                    recordHistory(components, wires)
                                },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Delete Wire", color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
                            }

                            IconButton(
                                onClick = { selectedWireId = null },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Deselect", modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // ==========================================
            // Main Schematic Canvas Area
            // ==========================================
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color(0xFF070C15), if (isFullScreen) RoundedCornerShape(0.dp) else RoundedCornerShape(14.dp))
                    .border(1.dp, Color(0xFF1E293B), if (isFullScreen) RoundedCornerShape(0.dp) else RoundedCornerShape(14.dp))
                    .clip(if (isFullScreen) RoundedCornerShape(0.dp) else RoundedCornerShape(14.dp))
                    .pointerInput(currentTool) {
                        if (currentTool == DesignerTool.PAN) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                panOffsetX += dragAmount.x
                                panOffsetY += dragAmount.y
                            }
                        }
                    }
            ) {
                // Transformed Canvas Content Container
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = zoomLevel
                            scaleY = zoomLevel
                            translationX = panOffsetX
                            translationY = panOffsetY
                        }
                ) {
                    // Background Schematic Grid Dots
                    if (showGridDots) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height
                            val gridPx = 20.dp.toPx()
                            var x = 0f
                            while (x < w) {
                                var y = 0f
                                while (y < h) {
                                    drawCircle(Color(0xFF162030), radius = 1.2f, center = Offset(x, y))
                                    y += gridPx
                                }
                                x += gridPx
                            }
                        }
                    }

                    // Wire Drawing Layer (Manhattan style orthogonal traces)
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        wires.forEach { wire ->
                            val fromComp = components.firstOrNull { it.id == wire.fromComponentId }
                            val toComp = components.firstOrNull { it.id == wire.toComponentId }
                            if (fromComp != null && toComp != null) {
                                val fromPins = ComponentLibrary.getPinsForType(fromComp.type)
                                val toPins = ComponentLibrary.getPinsForType(toComp.type)
                                val p1Def = fromPins.firstOrNull { it.id == wire.fromPin } ?: fromPins.firstOrNull()
                                val p2Def = toPins.firstOrNull { it.id == wire.toPin } ?: toPins.firstOrNull()

                                val compW = 68.dp.toPx()
                                val compH = 54.dp.toPx()

                                val x1 = fromComp.x.dp.toPx() + (compW / 2) + (p1Def?.relX ?: 0f) * (compW * 0.42f)
                                val y1 = fromComp.y.dp.toPx() + (compH / 2) + (p1Def?.relY ?: 0f) * (compH * 0.42f)

                                val x2 = toComp.x.dp.toPx() + (compW / 2) + (p2Def?.relX ?: 0f) * (compW * 0.42f)
                                val y2 = toComp.y.dp.toPx() + (compH / 2) + (p2Def?.relY ?: 0f) * (compH * 0.42f)

                                val isWireSelected = wire.id == selectedWireId
                                val traceColor = if (isWireSelected) CircuitGold else CircuitTrace
                                val traceWidth = if (isWireSelected) 3.5f else 2.5f

                                // Orthogonal 90 degree Manhattan routing
                                val midX = (x1 + x2) / 2
                                val path = Path().apply {
                                    moveTo(x1, y1)
                                    lineTo(midX, y1)
                                    lineTo(midX, y2)
                                    lineTo(x2, y2)
                                }

                                drawPath(path, traceColor, style = Stroke(width = traceWidth, cap = StrokeCap.Round))

                                // Solder junction pads
                                drawCircle(CircuitGold, radius = 3.5f, center = Offset(x1, y1))
                                drawCircle(CircuitGold, radius = 3.5f, center = Offset(x2, y2))
                            }
                        }
                    }

                    // Interactive Components Layer
                    components.forEach { comp ->
                        val isSelected = comp.id == selectedCompId
                        val compPins = remember(comp.type) { ComponentLibrary.getPinsForType(comp.type) }

                        Box(
                            modifier = Modifier
                                .offset(x = comp.x.dp, y = comp.y.dp)
                                .size(width = 68.dp, height = 54.dp)
                                .pointerInput(comp.id, currentTool, snapToGrid) {
                                    if (currentTool == DesignerTool.MOVE || currentTool == DesignerTool.SELECT) {
                                        detectDragGestures(
                                            onDragStart = {
                                                selectedCompId = comp.id
                                                selectedWireId = null
                                            },
                                            onDrag = { change, dragAmount ->
                                                change.consume()
                                                val gridStep = if (snapToGrid) 16f else 1f
                                                // Convert drag pixels directly to dp using density for smooth 1:1 movement
                                                val newX = (comp.x + (dragAmount.x / density)).coerceIn(10f, 1000f)
                                                val newY = (comp.y + (dragAmount.y / density)).coerceIn(10f, 1000f)
                                                val snappedX = if (snapToGrid) ((newX / gridStep).roundToInt() * gridStep) else newX
                                                val snappedY = if (snapToGrid) ((newY / gridStep).roundToInt() * gridStep) else newY
                                                onUpdatePosition(comp.id, snappedX, snappedY)
                                            },
                                            onDragEnd = {
                                                recordHistory(components, wires)
                                            }
                                        )
                                    }
                                }
                                .clickable {
                                    selectedCompId = comp.id
                                    selectedWireId = null
                                }
                        ) {
                            // Component Body Surface
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.28f) else Color(0xFF131D2D),
                                border = androidx.compose.foundation.BorderStroke(
                                    if (isSelected) 1.8.dp else 1.dp,
                                    if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFF283B50)
                                ),
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(4.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(2.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    val icon = ComponentLibrary.getComponent(comp.type)?.icon ?: Icons.Default.Memory
                                    val iconTint = when {
                                        comp.type.startsWith("LED") -> LedRed
                                        comp.type.contains("RESISTOR") || comp.type == "POTENTIOMETER" -> CircuitGold
                                        comp.type == "GROUND" || comp.type == "GND" -> Color.White
                                        comp.type == "BATTERY" || comp.type == "DC_SOURCE" || comp.type == "VCC" -> Color(0xFF00E5FF)
                                        comp.type.startsWith("TRANSISTOR") -> Color(0xFFB388FF)
                                        else -> MaterialTheme.colorScheme.primary
                                    }

                                    Icon(
                                        imageVector = icon,
                                        contentDescription = comp.label,
                                        tint = iconTint,
                                        modifier = Modifier.size(18.dp)
                                    )

                                    Text(
                                        text = comp.label,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        maxLines = 1
                                    )

                                    Text(
                                        text = comp.value,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 8.sp,
                                            color = Color.LightGray,
                                            fontFamily = FontFamily.Monospace
                                        ),
                                        maxLines = 1
                                    )
                                }
                            }

                            // Component Pin Terminals (Interactive Click-to-Connect)
                            compPins.forEach { pin ->
                                val isPendingPin = pendingWirePin?.first == comp.id && pendingWirePin?.second == pin.id

                                // Calculate normalized pin offset
                                val pinAlignment = when {
                                    pin.relX < -0.3f && pin.relY < -0.3f -> Alignment.TopStart
                                    pin.relX > 0.3f && pin.relY < -0.3f -> Alignment.TopEnd
                                    pin.relX < -0.3f && pin.relY > 0.3f -> Alignment.BottomStart
                                    pin.relX > 0.3f && pin.relY > 0.3f -> Alignment.BottomEnd
                                    pin.relX < -0.3f -> Alignment.CenterStart
                                    pin.relX > 0.3f -> Alignment.CenterEnd
                                    pin.relY < -0.3f -> Alignment.TopCenter
                                    pin.relY > 0.3f -> Alignment.BottomCenter
                                    else -> Alignment.Center
                                }

                                Box(
                                    modifier = Modifier
                                        .align(pinAlignment)
                                        .size(18.dp)
                                        .clip(CircleShape)
                                        .background(if (isPendingPin) Color(0xFFFF9100) else Color(0xFF1E293B))
                                        .border(
                                            1.5.dp,
                                            if (isPendingPin) Color.White else CircuitTrace,
                                            CircleShape
                                        )
                                        .clickable {
                                            if (pendingWirePin == null) {
                                                // Start wire from this pin
                                                pendingWirePin = Pair(comp.id, pin.id)
                                                currentTool = DesignerTool.WIRE
                                            } else {
                                                // Complete wire if from different component
                                                val (fromCompId, fromPinId) = pendingWirePin!!
                                                if (fromCompId != comp.id) {
                                                    onAddWire(fromCompId, fromPinId, comp.id, pin.id)
                                                    recordHistory(components, wires)
                                                }
                                                pendingWirePin = null
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = pin.label,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 7.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isPendingPin) Color.Black else Color.White
                                        )
                                    )
                                }
                            }
                        }
                    }

                    // Empty state watermark
                    if (components.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.TouchApp,
                                    contentDescription = null,
                                    tint = Color.Gray.copy(alpha = 0.6f),
                                    modifier = Modifier.size(42.dp)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Workspace is Empty",
                                    style = MaterialTheme.typography.titleMedium.copy(color = Color.LightGray, fontWeight = FontWeight.Bold)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Click '+ Add Component' above to open the library and place parts",
                                    style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Button(
                                    onClick = { showLibrarySheet = true },
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Open Component Library")
                                }
                            }
                        }
                    }
                }

                // Floating Exit Full Screen Button (Always accessible when full screen)
                if (isFullScreen) {
                    FloatingActionButton(
                        onClick = { isFullScreen = false },
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                            .size(42.dp)
                            .testTag("exit_fullscreen_floating_button")
                    ) {
                        Icon(Icons.Default.FullscreenExit, contentDescription = "Exit Full Screen", modifier = Modifier.size(22.dp))
                    }
                }
            }
        }

        // Searchable Component Library Bottom Sheet
        if (showLibrarySheet) {
            ComponentLibrarySheet(
                onSelectComponent = { libComp ->
                    val nextIndex = components.count { it.type == libComp.type } + 1
                    val label = "${libComp.prefix}$nextIndex"
                    onAddComponent(libComp.type, label, libComp.defaultValue)
                    recordHistory(components, wires)
                    validationFeedback = null
                },
                onDismiss = { showLibrarySheet = false }
            )
        }

        // Component Properties Bottom Sheet
        if (showPropertiesSheet && selectedComponent != null) {
            ComponentPropertiesSheet(
                component = selectedComponent,
                onUpdateValue = { newVal ->
                    onUpdateComponentValue(selectedComponent.id, newVal)
                    recordHistory(components, wires)
                },
                onRotate = {
                    onRotateComponent(selectedComponent.id)
                    recordHistory(components, wires)
                },
                onDuplicate = {
                    onDuplicateComponent(selectedComponent.id)
                    recordHistory(components, wires)
                    showPropertiesSheet = false
                },
                onDelete = {
                    onRemoveComponent(selectedComponent.id)
                    selectedCompId = null
                    recordHistory(components, wires)
                    showPropertiesSheet = false
                },
                onDismiss = { showPropertiesSheet = false }
            )
        }
    }
}
