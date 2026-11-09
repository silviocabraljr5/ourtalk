package br.com.ourtalk.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme

class MainActivity: ComponentActivity() {
 override fun onCreate(savedInstanceState:Bundle?) {
  super.onCreate(savedInstanceState)
  enableEdgeToEdge(statusBarStyle=SystemBarStyle.dark(0xFF0B1216.toInt()),navigationBarStyle=SystemBarStyle.dark(0xFF0B1216.toInt()))
  setContent {MaterialTheme(colorScheme=darkColorScheme(primary=TalkLime,onPrimary=TalkDark,secondary=TalkLime,onSecondary=TalkDark,secondaryContainer=androidx.compose.ui.graphics.Color(0xFF294019),onSecondaryContainer=TalkLime,background=TalkDark,surface=TalkDark)){OurTalkApp()}}
 }
}
