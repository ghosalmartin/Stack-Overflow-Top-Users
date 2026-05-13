package com.fnabl.thetimestechtest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.fnabl.thetimestechtest.ui.theme.TheTimesTechTestTheme
import com.fnabl.thetimestechtest.users.UserDetailScreen
import com.fnabl.thetimestechtest.users.UsersScreen
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.serialization.Serializable

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TheTimesTechTestTheme {
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = UserListDestination) {
                    composable<UserListDestination> {
                        UsersScreen(
                            onNavigateToUserDetail = { id ->
                                navController.navigate(UserDetailDestination(id))
                            },
                        )
                    }
                    composable<UserDetailDestination> {
                        UserDetailScreen()
                    }
                }
            }
        }
    }
}

@Serializable
object UserListDestination

@Serializable
data class UserDetailDestination(val id: Long)
