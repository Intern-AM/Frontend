package com.speehive.speehiveaihub

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.speehive.speehiveaihub.navigation.NavGraph
import com.speehive.speehiveaihub.ui.theme.SpeehiveAIHubTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            SpeehiveAIHubTheme {
                val navController = rememberNavController()
                NavGraph(navController = navController)
            }
        }
        }
    }
