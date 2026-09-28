package com.example.ui.screens.tools

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Polyline
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DesignerComponentItem
import com.example.data.model.DesignerWireItem
import com.example.data.model.StudentProfile
import com.example.ui.ToolTab
import com.example.ui.TutorMessage
import com.example.ui.screens.aitutor.AiTutorScreen
import com.example.ui.screens.calculator.CalculatorsScreen
import com.example.ui.screens.designer.CircuitDesignerScreen

@Composable
fun ToolsScreen(
    student: StudentProfile,
    selectedTab: ToolTab,
    onTabSelected: (ToolTab) -> Unit,
    // AI Tutor props
    tutorMessages: List<TutorMessage>,
    isTutorThinking: Boolean,
    tutorMode: String,
    onSetTutorMode: (String) -> Unit,
    onSendTutorMessage: (String) -> Unit,
    // Designer props
    designerComps: List<DesignerComponentItem>,
    designerWires: List<DesignerWireItem>,
    onAddDesignerComponent: (String, String, String) -> Unit,
    onRemoveDesignerComponent: (String) -> Unit,
    onRotateDesignerComponent: (String) -> Unit,
    onUpdateDesignerPosition: (String, Float, Float) -> Unit,
    onClearDesigner: () -> Unit,
    onCheckCircuit: () -> String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Clean, compact secondary tool tabs
        Surface(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                ToolTabButton(
                    title = "AI Tutor",
                    icon = Icons.Default.Psychology,
                    isSelected = selectedTab == ToolTab.AI_TUTOR,
                    onClick = { onTabSelected(ToolTab.AI_TUTOR) },
                    modifier = Modifier.weight(1f)
                )
                ToolTabButton(
                    title = "Designer",
                    icon = Icons.Default.Polyline,
                    isSelected = selectedTab == ToolTab.DESIGNER,
                    onClick = { onTabSelected(ToolTab.DESIGNER) },
                    modifier = Modifier.weight(1f)
                )
                ToolTabButton(
                    title = "Calculator",
                    icon = Icons.Default.Calculate,
                    isSelected = selectedTab == ToolTab.CALCULATOR,
                    onClick = { onTabSelected(ToolTab.CALCULATOR) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when (selectedTab) {
                ToolTab.AI_TUTOR -> AiTutorScreen(
                    student = student,
                    messages = tutorMessages,
                    isThinking = isTutorThinking,
                    tutorMode = tutorMode,
                    onSetMode = onSetTutorMode,
                    onSendMessage = onSendTutorMessage
                )
                ToolTab.DESIGNER -> CircuitDesignerScreen(
                    components = designerComps,
                    wires = designerWires,
                    onAddComponent = onAddDesignerComponent,
                    onRemoveComponent = onRemoveDesignerComponent,
                    onRotateComponent = onRotateDesignerComponent,
                    onUpdatePosition = onUpdateDesignerPosition,
                    onClearWorkspace = onClearDesigner,
                    onCheckCircuit = onCheckCircuit
                )
                ToolTab.CALCULATOR -> CalculatorsScreen()
            }
        }
    }
}

@Composable
private fun ToolTabButton(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primary else androidx.compose.ui.graphics.Color.Transparent,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            )
        }
    }
}
