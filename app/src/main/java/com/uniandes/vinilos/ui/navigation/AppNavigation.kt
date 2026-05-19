package com.uniandes.vinilos.ui.navigation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.uniandes.vinilos.AppViewModel
import com.uniandes.vinilos.model.UserRole
import com.uniandes.vinilos.ui.albums.AddTrackScreen
import com.uniandes.vinilos.ui.albums.AlbumDetailScreen
import com.uniandes.vinilos.ui.albums.AlbumListScreen
import com.uniandes.vinilos.ui.albums.AlbumViewModel
import com.uniandes.vinilos.ui.albums.CreateAlbumScreen
import com.uniandes.vinilos.ui.albums.CreateAlbumViewModel
import com.uniandes.vinilos.ui.albums.CreateTrackViewModel
import com.uniandes.vinilos.ui.artists.ArtistDetailScreen
import com.uniandes.vinilos.ui.artists.ArtistListScreen
import com.uniandes.vinilos.ui.artists.ArtistViewModel
import com.uniandes.vinilos.ui.collectors.CollectorDetailScreen
import com.uniandes.vinilos.ui.collectors.CollectorListScreen
import com.uniandes.vinilos.ui.collectors.CollectorViewModel
import com.uniandes.vinilos.ui.collectors.FavoritePerformersScreen
import com.uniandes.vinilos.ui.collectors.FavoritePerformersViewModel
import com.uniandes.vinilos.ui.home.HomeScreen
import com.uniandes.vinilos.ui.prizes.PrizeAssociateScreen
import com.uniandes.vinilos.ui.prizes.PrizeViewModel
import com.uniandes.vinilos.ui.role.RoleSelectionScreen
import com.uniandes.vinilos.ui.components.AppSettingsDrawer
import kotlinx.coroutines.launch
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.Column

sealed class Screen(val route: String) {
    object RoleSelection : Screen("role_selection")
    object Home : Screen("home")
    object AlbumList : Screen("album_list")
    object AlbumDetail : Screen("album_detail/{albumId}") {
        fun createRoute(albumId: Int) = "album_detail/$albumId"
    }
    object AlbumCreate : Screen("album_create")
    object AlbumAddTrack : Screen("album_add_track/{albumId}") {
        fun createRoute(albumId: Int) = "album_add_track/$albumId"
    }
    object ArtistList : Screen("artist_list")
    object ArtistDetail : Screen("artist_detail/{artistId}") {
        fun createRoute(artistId: Int) = "artist_detail/$artistId"
    }
    object CollectorList : Screen("collector_list")
    object CollectorDetail : Screen("collector_detail/{collectorId}") {
        fun createRoute(collectorId: Int) = "collector_detail/$collectorId"
    }
    object FavoritePerformers : Screen("favorite_performers/{collectorId}") {
        fun createRoute(collectorId: Int) = "favorite_performers/$collectorId"
    }
    object PrizeAssociate : Screen("prize_associate/{artistId}") {
        fun createRoute(artistId: Int) = "prize_associate/$artistId"
    }
}

enum class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val accessibilityId: String
) {
    VINYL("home", "Vinilos", Icons.Filled.Home, "nav_vinilos"),
    ALBUMES("album_list", "Álbumes", Icons.Filled.MusicNote, "nav_albumes"),
    ARTISTS("artist_list", "Artistas", Icons.Filled.Person, "nav_artistas"),
    PEOPLE("collector_list", "colecc.", Icons.Filled.AccountCircle, "nav_coleccionistas")
}

@Composable
fun AppNavigation(
    appViewModel: AppViewModel,
    userRole: UserRole?
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val isDarkTheme by appViewModel.isDarkTheme.collectAsStateWithLifecycle()
    val colorBlindMode by appViewModel.colorBlindMode.collectAsStateWithLifecycle()
    
    val onMenuClick: () -> Unit = { scope.launch { drawerState.open() } }

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val context = LocalContext.current
    val albumViewModel: AlbumViewModel = viewModel(factory = AlbumViewModel.factory(context))
    val artistViewModel: ArtistViewModel = viewModel(factory = ArtistViewModel.factory(context))
    val collectorViewModel: CollectorViewModel = viewModel(factory = CollectorViewModel.factory(context))
    val prizeViewModel: PrizeViewModel = viewModel(factory = PrizeViewModel.factory(context))
    val createAlbumViewModel: CreateAlbumViewModel = viewModel(factory = CreateAlbumViewModel.factory(context))
    val createTrackViewModel: CreateTrackViewModel = viewModel(factory = CreateTrackViewModel.factory(context))
    val favoritePerformersViewModel: FavoritePerformersViewModel = viewModel(factory = FavoritePerformersViewModel.factory(context))

    val isDetailScreen = currentRoute?.startsWith("album_detail") == true ||
            currentRoute?.startsWith("artist_detail") == true ||
            currentRoute?.startsWith("collector_detail") == true ||
            currentRoute?.startsWith("favorite_performers") == true ||
            currentRoute?.startsWith("prize_associate") == true

    val isCreateScreen = currentRoute == Screen.AlbumCreate.route ||
            currentRoute?.startsWith("album_add_track") == true

    var isBarVisible by remember { mutableStateOf(true) }

    val systemNavInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val barHeightPx = with(LocalDensity.current) { (80.dp + systemNavInset).toPx() }
    val offsetY = remember { Animatable(0f) }

    LaunchedEffect(currentRoute) {
        if (!isDetailScreen && !isCreateScreen) isBarVisible = true
        if (isCreateScreen) isBarVisible = false
    }

    LaunchedEffect(isBarVisible) {
        offsetY.animateTo(
            targetValue = if (isBarVisible) 0f else barHeightPx,
            animationSpec = tween(durationMillis = 250)
        )
    }

    val nestedScrollConnection = remember(isDetailScreen) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (isDetailScreen) {
                    if (available.y < -5) isBarVisible = false
                    else if (available.y > 5) isBarVisible = true
                }
                return Offset.Zero
            }
        }
    }

    val userRole by appViewModel.userRole.collectAsStateWithLifecycle()
    val isReady by appViewModel.isReady.collectAsStateWithLifecycle()

    if (!isReady) return

    val startDestination = if (userRole == null)
        Screen.RoleSelection.route
    else
        Screen.Home.route

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = userRole != null,
        drawerContent = {
            AppSettingsDrawer(
                userRole = userRole,
                isDarkTheme = isDarkTheme,
                colorBlindMode = colorBlindMode,
                onToggleTheme = { appViewModel.toggleDarkTheme() },
                onToggleColorBlind = {
                    val next = if (colorBlindMode == com.uniandes.vinilos.model.ColorBlindMode.NONE)
                        com.uniandes.vinilos.model.ColorBlindMode.DEUTERANOPIA
                    else
                        com.uniandes.vinilos.model.ColorBlindMode.NONE
                    appViewModel.setColorBlindMode(next)
                },
                onBecomeCollector = { appViewModel.setUserRole(UserRole.COLLECTOR) },
                onLeaveCollector = { appViewModel.setUserRole(UserRole.VISITOR) },
                onCloseDrawer = { scope.launch { drawerState.close() } }
            )
        }
    ) {
        Scaffold(
            bottomBar = {
                Column(
                    modifier = Modifier.graphicsLayer {
                        translationY = offsetY.value
                        alpha = if (offsetY.value > barHeightPx * 0.5f) 0f else 1f
                    }
                ) {
                    HorizontalDivider(
                        thickness = 1.5.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                    )
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.background,
                        tonalElevation = 0.dp,
                    ) {
                        BottomNavItem.entries.forEach { item ->
                            NavigationBarItem(
                                icon = { Icon(imageVector = item.icon, contentDescription = item.label) },
                                label = { Text(item.label) },
                                selected = currentRoute == item.route,
                                modifier = Modifier.semantics {
                                    contentDescription = item.accessibilityId
                                },
                                onClick = {
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = false
                                        }
                                        launchSingleTop = true
                                        restoreState = false
                                    }
                                }
                            )
                        }
                    }
                }
            },
            modifier = Modifier.nestedScroll(nestedScrollConnection),
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = startDestination,
                modifier = if (offsetY.value > 0f)
                    Modifier.padding(top = innerPadding.calculateTopPadding())
                else
                    Modifier.padding(innerPadding)
            ) {
                composable(Screen.RoleSelection.route) {
                    RoleSelectionScreen(
                        onRoleSelected = { role ->
                            appViewModel.setUserRole(role)
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.RoleSelection.route) { inclusive = true }
                            }
                        }
                    )
                }
                composable(Screen.Home.route) {
                    HomeScreen(
                        albumViewModel = albumViewModel,
                        artistViewModel = artistViewModel,
                        collectorViewModel = collectorViewModel,
                        onMenuClick = onMenuClick,
                        userRole = userRole, 
                        onAlbumClick = { navController.navigate(Screen.AlbumDetail.createRoute(it)) },
                        onArtistClick = { navController.navigate(Screen.ArtistDetail.createRoute(it)) },
                        onCollectorClick = { navController.navigate(Screen.CollectorDetail.createRoute(it)) }
                    )
                }
                composable(Screen.AlbumList.route) {
                    AlbumListScreen(
                        viewModel = albumViewModel,
                        onAlbumClick = { navController.navigate(Screen.AlbumDetail.createRoute(it)) },
                        onMenuClick = onMenuClick,
                        userRole = userRole,
                        onCreateAlbum = {
                            navController.navigate(Screen.AlbumCreate.route)
                        }
                    )
                }
                composable(Screen.AlbumCreate.route) {
                    CreateAlbumScreen(
                        viewModel = createAlbumViewModel,
                        onSuccess = {
                            albumViewModel.refresh()
                            navController.popBackStack()
                        },
                        onDiscard = { navController.popBackStack() }
                    )
                }
                composable(
                    route = Screen.AlbumDetail.route,
                    arguments = listOf(navArgument("albumId") { type = NavType.IntType })
                ) { backStackEntry ->
                    val albumId = backStackEntry.arguments?.getInt("albumId") ?: return@composable
                    AlbumDetailScreen(
                        albumId = albumId,
                        viewModel = albumViewModel,
                        onBack = { navController.navigateUp() },
                        onMenuClick = onMenuClick,
                        onAddTrack = {
                            navController.navigate(Screen.AlbumAddTrack.createRoute(it))
                        },
                        userRole = userRole
                    )
                }
                composable(
                    route = Screen.AlbumAddTrack.route,
                    arguments = listOf(navArgument("albumId") { type = NavType.IntType })
                ) { backStackEntry ->
                    val albumId = backStackEntry.arguments?.getInt("albumId") ?: return@composable
                    val album = albumViewModel.findById(albumId)
                    AddTrackScreen(
                        albumId = albumId,
                        albumName = album?.name ?: "este álbum",
                        viewModel = createTrackViewModel,
                        onSuccess = {
                            albumViewModel.refresh()
                            navController.popBackStack()
                        },
                        onDiscard = { navController.popBackStack() }
                    )
                }
                composable(Screen.ArtistList.route) {
                    ArtistListScreen(
                        viewModel = artistViewModel,
                        onArtistClick = { navController.navigate(Screen.ArtistDetail.createRoute(it)) },
                        onMenuClick = onMenuClick,
                        userRole = userRole
                    )
                }
                composable(
                    route = Screen.ArtistDetail.route,
                    arguments = listOf(navArgument("artistId") { type = NavType.IntType })
                ) { backStackEntry ->
                    val artistId = backStackEntry.arguments?.getInt("artistId") ?: return@composable
                    ArtistDetailScreen(
                        artistId = artistId,
                        viewModel = artistViewModel,
                        onBack = { navController.navigateUp() },
                        onMenuClick = onMenuClick,
                        onAssociatePrize = { id ->
                            navController.navigate(Screen.PrizeAssociate.createRoute(id))
                        },
                        userRole = userRole
                    )
                }
                composable(
                    route = Screen.PrizeAssociate.route,
                    arguments = listOf(navArgument("artistId") { type = NavType.IntType })
                ) { backStackEntry ->
                    val artistId = backStackEntry.arguments?.getInt("artistId") ?: return@composable
                    PrizeAssociateScreen(
                        artist = artistViewModel.findById(artistId),
                        viewModel = prizeViewModel,
                        onBack = { navController.navigateUp() },
                        onMenuClick = onMenuClick,
                        onAssociated = { navController.navigateUp() },
                        userRole = userRole
                    )
                }
                composable(Screen.CollectorList.route) {
                    CollectorListScreen(
                        viewModel = collectorViewModel,
                        onCollectorClick = { navController.navigate(Screen.CollectorDetail.createRoute(it)) },
                        onMenuClick = onMenuClick,
                        userRole = userRole
                    )
                }
                composable(
                    route = Screen.CollectorDetail.route,
                    arguments = listOf(navArgument("collectorId") { type = NavType.IntType })
                ) { backStackEntry ->
                    val collectorId = backStackEntry.arguments?.getInt("collectorId") ?: return@composable

                    // Cuando el usuario vuelve desde FavoritePerformersScreen, el
                    // LaunchedEffect(collectorId) de la pantalla de detalle NO re-dispara
                    // porque la clave no cambió. Este observer detecta que la entrada
                    // vuelve a RESUMED (navegación hacia atrás incluida) y fuerza una
                    // re-lectura desde Room, donde el repository ya dejó los favoritos
                    // actualizados.
                    DisposableEffect(backStackEntry) {
                        val observer = LifecycleEventObserver { _, event ->
                            if (event == Lifecycle.Event.ON_RESUME) {
                                collectorViewModel.refreshCollectorFromCache(collectorId)
                            }
                        }
                        backStackEntry.lifecycle.addObserver(observer)
                        onDispose { backStackEntry.lifecycle.removeObserver(observer) }
                    }

                    CollectorDetailScreen(
                        collectorId = collectorId,
                        viewModel = collectorViewModel,
                        onBack = { navController.navigateUp() },
                        onMenuClick = onMenuClick,
                        userRole = userRole,
                        onAddFavoriteArtist = {
                            navController.navigate(Screen.FavoritePerformers.createRoute(collectorId))
                        }
                    )
                }
                composable(
                    route = Screen.FavoritePerformers.route,
                    arguments = listOf(navArgument("collectorId") { type = NavType.IntType })
                ) { backStackEntry ->
                    val collectorId = backStackEntry.arguments?.getInt("collectorId") ?: return@composable
                    FavoritePerformersScreen(
                        collector = collectorViewModel.findById(collectorId),
                        viewModel = favoritePerformersViewModel,
                        onBack = { navController.navigateUp() },
                        onMenuClick = onMenuClick,
                        userRole = userRole
                    )
                }
            }
        }
    }
}
