package me.danlowe.meshcommunicator.test.base

import androidx.compose.ui.test.junit4.createComposeRule
import dagger.hilt.android.testing.HiltAndroidRule
import org.junit.Before
import org.junit.Rule

/**
 * Base class for instantiating Compose tests that use Dagger Hilt
 *
 * No need to use @Before, [setup] is called in a @Before by default
 */
open class BaseComposeHiltTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun internalInit() {
        hiltRule.inject()
        setup()
    }

    /**
     * Setup to be performed before each test
     */
    open fun setup() {

    }
}