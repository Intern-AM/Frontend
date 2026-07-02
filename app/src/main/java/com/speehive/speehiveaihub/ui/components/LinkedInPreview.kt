package com.speehive.speehiveaihub.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.automirrored.outlined.Comment
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.speehive.speehiveaihub.ui.theme.PulseBlue
import com.speehive.speehiveaihub.ui.theme.TextMuted
import com.speehive.speehiveaihub.ui.theme.TextSecondary
import com.speehive.speehiveaihub.ui.theme.PureBlack
import com.speehive.speehiveaihub.ui.theme.CardBorder
import com.speehive.speehiveaihub.ui.theme.CardSurface

@Composable
fun LinkedInPostCard(
    content: String,
    hashtags: List<String>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(PureBlack, RoundedCornerShape(16.dp))
            .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(PulseBlue),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "PA", style = MaterialTheme.typography.bodyLarge, color = PureBlack)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Pulse AI", style = MaterialTheme.typography.titleLarge)
                Text(text = "AI Campaign Intelligence • 1st", style = MaterialTheme.typography.labelSmall)
                Text(text = "just now • \uD83C\uDF10", style = MaterialTheme.typography.labelSmall)
            }
            Icon(Icons.Default.MoreHoriz, contentDescription = null, tint = TextMuted)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Post text
        Text(
            text = content,
            style = MaterialTheme.typography.bodyMedium,
            lineHeight = 20.sp
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Image Placeholder
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(CardSurface, RoundedCornerShape(8.dp))
                .border(1.dp, CardBorder, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.AutoMirrored.Outlined.Send, contentDescription = null, tint = TextMuted)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "AI GENERATED IMAGE", style = MaterialTheme.typography.labelSmall, color = TextMuted)
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Hashtags
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            hashtags.forEach { tag ->
                Text(text = "#$tag", style = MaterialTheme.typography.labelMedium, color = PulseBlue)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Footer Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            SocialAction(Icons.Outlined.ThumbUp, "Like")
            SocialAction(Icons.AutoMirrored.Outlined.Comment, "Comment")
            SocialAction(Icons.Outlined.Repeat, "Repost")
            SocialAction(Icons.AutoMirrored.Outlined.Send, "Send")
        }
    }
}

@Composable
fun SocialAction(icon: ImageVector, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = TextMuted)
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = TextMuted)
    }
}
