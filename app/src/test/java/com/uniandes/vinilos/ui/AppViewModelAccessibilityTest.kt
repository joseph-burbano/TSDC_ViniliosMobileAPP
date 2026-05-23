package com.uniandes.vinilos.ui

import com.uniandes.vinilos.AppViewModel
import com.uniandes.vinilos.model.ColorBlindMode
import com.uniandes.vinilos.testing.FakePreferencesRepository
import com.uniandes.vinilos.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AppViewModelAccessibilityTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var fakeRepo: FakePreferencesRepository
    private lateinit var viewModel: AppViewModel

    @Before
    fun setup() {
        fakeRepo = FakePreferencesRepository()
        viewModel = AppViewModel(fakeRepo)
    }

    @Test
    fun `colorBlindMode inicia en NONE`() = runTest {
        assertEquals(ColorBlindMode.NONE, viewModel.colorBlindMode.value)
    }

    @Test
    fun `setColorBlindMode persiste DEUTERANOPIA`() = runTest {
        viewModel.setColorBlindMode(ColorBlindMode.DEUTERANOPIA)
        advanceUntilIdle()
        assertEquals(ColorBlindMode.DEUTERANOPIA, viewModel.colorBlindMode.value)
    }

    @Test
    fun `setColorBlindMode persiste PROTANOPIA`() = runTest {
        viewModel.setColorBlindMode(ColorBlindMode.PROTANOPIA)
        advanceUntilIdle()
        assertEquals(ColorBlindMode.PROTANOPIA, viewModel.colorBlindMode.value)
    }

    @Test
    fun `setColorBlindMode persiste TRITANOPIA`() = runTest {
        viewModel.setColorBlindMode(ColorBlindMode.TRITANOPIA)
        advanceUntilIdle()
        assertEquals(ColorBlindMode.TRITANOPIA, viewModel.colorBlindMode.value)
    }

    @Test
    fun `setColorBlindMode puede regresar a NONE`() = runTest {
        viewModel.setColorBlindMode(ColorBlindMode.DEUTERANOPIA)
        advanceUntilIdle()
        viewModel.setColorBlindMode(ColorBlindMode.NONE)
        advanceUntilIdle()
        assertEquals(ColorBlindMode.NONE, viewModel.colorBlindMode.value)
    }
}
