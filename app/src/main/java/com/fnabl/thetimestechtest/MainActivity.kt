package com.fnabl.thetimestechtest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.fnabl.thetimestechtest.ui.theme.TheTimesTechTestTheme
import com.fnabl.thetimestechtest.users.UsersScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TheTimesTechTestTheme {
                UsersScreen()
            }
        }
    }
}
