package com.example.apex.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.apex.R
import com.example.apex.ui.navigation.CreateWorkoutRoute
import com.example.apex.ui.navigation.DetailsRoute
import com.example.apex.ui.navigation.HomeRoute
import com.example.apex.ui.navigation.WorkoutsRoute
import com.example.apex.ui.screens.CreateWorkoutScreen
import com.example.apex.ui.screens.HomeScreen
import com.example.apex.ui.screens.WorkoutDetailsScreen
import com.example.apex.ui.screens.WorkoutsScreen
import com.example.apex.viewmodel.WorkoutViewModel

@Composable
fun ApexApp() {
    val navController = rememberNavController()
    val workoutViewModel: WorkoutViewModel = viewModel()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = currentDestination?.hasRoute<HomeRoute>() == true,
                    onClick = {
                        navController.navigate(HomeRoute) {
                            popUpTo<HomeRoute> { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    icon = {
                        Icon(
                            Icons.Default.Home,
                            contentDescription = stringResource(R.string.nav_accueil)
                        )
                    },
                    label = { Text(stringResource(R.string.nav_accueil)) }
                )

                NavigationBarItem(
                    selected = currentDestination?.hasRoute<WorkoutsRoute>() == true,
                    onClick = {
                        navController.navigate(WorkoutsRoute) {
                            launchSingleTop = true
                        }
                    },
                    icon = {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = stringResource(R.string.nav_entrainements)
                        )
                    },
                    label = { Text(stringResource(R.string.nav_entrainements)) }
                )
            }
        }
    ) { innerPadding ->
        WorkoutsNavHost(
            navController = navController,
            workoutViewModel = workoutViewModel,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
private fun WorkoutsNavHost(
    navController: NavHostController,
    workoutViewModel: WorkoutViewModel,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = HomeRoute,
        modifier = modifier
    ) {
        composable<HomeRoute> {
            HomeScreen(
                viewModel = workoutViewModel,
                onAllWorkoutsClick = { navController.navigate(WorkoutsRoute) },
                onDetailClick = { idWorkout ->
                    navController.navigate(DetailsRoute(idWorkout))
                },
                onCreateClick = { navController.navigate(CreateWorkoutRoute) }
            )
        }

        composable<WorkoutsRoute> {
            WorkoutsScreen(
                viewModel = workoutViewModel,
                onDetailClick = { idWorkout ->
                    navController.navigate(DetailsRoute(idWorkout))
                },
                onCreateClick = { navController.navigate(CreateWorkoutRoute) }
            )
        }

        composable<CreateWorkoutRoute> {
            CreateWorkoutScreen(
                onBackClick = { navController.popBackStack() },
                onWorkoutCreated = {
                    workoutViewModel.chargerWorkouts()
                    navController.popBackStack()
                }
            )
        }

        composable<DetailsRoute> { backStackEntry ->
            val route: DetailsRoute = backStackEntry.toRoute()

            WorkoutDetailsScreen(
                idWorkout = route.idWorkout,
                onWorkoutChanged = {
                    workoutViewModel.chargerWorkouts()
                },
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
