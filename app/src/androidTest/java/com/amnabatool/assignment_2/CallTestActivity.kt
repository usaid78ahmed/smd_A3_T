package com.amnabatool.assignment_2

import android.Manifest
import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CallActivityTest {

    private lateinit var activityScenario: ActivityScenario<CallActivity>

    // Grant audio recording permission for the test
    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.RECORD_AUDIO
    )

    @Before
    fun setUp() {
        // Create an intent to launch the CallActivity
        val intent = Intent(ApplicationProvider.getApplicationContext(), CallActivity::class.java)

        // Launch the activity under test
        activityScenario = ActivityScenario.launch(intent)

        // Give some time for Agora to initialize
        Thread.sleep(2000)
    }

    @After
    fun tearDown() {
        activityScenario.close()
    }

    @Test
    fun testActivityLaunches() {
        // Verify that the activity launches without crashing
        activityScenario.onActivity { activity ->
            assert(activity.isFinishing.not())
        }
    }

    @Test
    fun testEndCallButtonIsDisplayedAndClickable() {
        // Check if end call button is displayed
        onView(withId(R.id.endCall)).check(matches(isDisplayed()))

        // Click on end call button
        onView(withId(R.id.endCall)).perform(click())

        // Activity should finish after clicking end call button
        Thread.sleep(1000) // Give time for the activity to finish
        assert(activityScenario.state.isAtLeast(ActivityScenario.State.DESTROYED))
    }

    @Test
    fun testAgoraEngineInitialization() {
        activityScenario.onActivity { activity ->
            // Access the private rtcEngine field for testing
            val rtcEngineField = CallActivity::class.java.getDeclaredField("rtcEngine")
            rtcEngineField.isAccessible = true
            val rtcEngine = rtcEngineField.get(activity)

            // Assert that rtcEngine is not null, indicating successful initialization
            assert(rtcEngine != null)
        }
    }

    @Test
    fun testJoinChannelFlow() {
        // Wait for some time to allow joining the channel
        Thread.sleep(3000)

        // Check that the activity is still running
        assert(activityScenario.state.isAtLeast(ActivityScenario.State.CREATED))
    }

    @Test
    fun testRtcEngineEventHandler() {
        activityScenario.onActivity { activity ->
            // Access the handler field
            val handlerField = CallActivity::class.java.getDeclaredField("rtcEngineEventHandler")
            handlerField.isAccessible = true
            val handler = handlerField.get(activity)

            // Ensure the handler exists
            assert(handler != null)

            // In a real test with mocking, we would trigger and verify the callbacks
            // For now, we just verify that the handler is initialized
        }
    }

    @Test
    fun testLeaveChannelMethodCalled() {
        // This is a bit tricky to test directly, but we can verify that clicking the end call
        // button finishes the activity

        // Click end call button
        onView(withId(R.id.endCall)).perform(click())

        // Verify activity is finishing
        Thread.sleep(1000)
        assert(activityScenario.state.isAtLeast(ActivityScenario.State.DESTROYED))
    }
}