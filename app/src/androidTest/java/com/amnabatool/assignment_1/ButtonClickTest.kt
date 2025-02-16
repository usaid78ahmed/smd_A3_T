package com.amnabatool.assignment_1

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ButtonClickTest {

    private fun withItemCount(expectedCount: Int) = object : TypeSafeMatcher<View>() {
        override fun describeTo(description: Description?) {
            description?.appendText("RecyclerView with item count: $expectedCount")
        }

        override fun matchesSafely(item: View?): Boolean {
            if (item !is RecyclerView) return false
            return item.adapter?.itemCount == expectedCount
        }
    }

    @Test
    fun testSendButtonIsClickable() {
        ActivityScenario.launch(ChatActivity::class.java).use {
            onView(withId(R.id.sendButton)).check(matches(isClickable()))
        }
    }

    @Test
    fun testNewPostActivity2CloseButtonFinishesActivity() {
        val scenario = ActivityScenario.launch(NewPostActivity2::class.java)
        onView(withId(R.id.closeButton)).perform(click())
        scenario.onActivity { activity ->
            assertTrue("Activity should be finishing", activity.isFinishing)
        }
    }

    @Test
    fun testCallActivityEndCallButtonFinishesActivity() {
        val scenario = ActivityScenario.launch(CallActivity::class.java)
        onView(withId(R.id.endCall)).perform(click())
        scenario.onActivity { activity ->
            assertTrue("CallActivity should be finishing", activity.isFinishing)
        }
    }

    @Test
    fun testVideoCallActivityEndCallButtonFinishesActivity() {
        val scenario = ActivityScenario.launch(VideoCallActivity::class.java)
        onView(withId(R.id.endCall)).perform(click())
        scenario.onActivity { activity ->
            assertTrue("VideoCallActivity should be finishing", activity.isFinishing)
        }
    }

    @Test
    fun testEditProfileActivityDoneButtonClick() {
        val scenario = ActivityScenario.launch(EditProfileActivity::class.java)
        onView(withId(R.id.doneTextView)).perform(click())
        scenario.onActivity { activity ->
            assertTrue("EditProfileActivity should be finishing", activity.isFinishing)
        }
    }
}
