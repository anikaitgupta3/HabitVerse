package com.anikaitgupta.habitverse.ui

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.anikaitgupta.habitverse.R
import com.anikaitgupta.habitverse.di.AuthModule
import com.anikaitgupta.habitverse.domain.AuthRepository
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
@UninstallModules(AuthModule::class)
class MainActivityTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @BindValue
    val authRepository: AuthRepository = mockk(relaxed = true)

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @After
    fun tearDown() {
    }

    @Test
    fun activity_launchesSuccessfully() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                assertNotNull(activity)
            }
        }
    }

    @Test
    fun onCreate_whenUserNotLoggedIn_setsStartDestinationToAuthGraph() {
        every { authRepository.checkLoggedIn() } returns false

        ActivityScenario.launch(MainActivity::class.java).use {
            onView(withId(R.id.etEmail)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun onCreate_whenUserIsLoggedIn_setsStartDestinationToMainGraph() {
        every { authRepository.checkLoggedIn() } returns true

        ActivityScenario.launch(MainActivity::class.java).use {
            onView(withId(R.id.fab)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun onCreate_withSavedInstanceState_doesNotReinflateNavGraph() {
        every { authRepository.checkLoggedIn() } returns true
        
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            // First launch calls checkLoggedIn
            verify(exactly = 1) { authRepository.checkLoggedIn() }
            
            // Clear mock to count only the next calls
            io.mockk.clearMocks(authRepository)
            
            scenario.recreate()
            
            // Verify it was not called again during recreation
            verify(exactly = 0) { authRepository.checkLoggedIn() }
        }
    }
}
