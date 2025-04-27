package com.amnabatool.assignment_2

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.amnabatool.assignment_2.R
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NavigationTest {

    @get:Rule
    val loginActivityRule = ActivityTestRule(LoginActivity::class.java)

    @Before
    fun setUp() {
        Intents.init()
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    @Test
    fun testLoginButtonNavigation_toHomeActivity() {
        onView(withId(R.id.loginButton)).perform(click())
        intended(hasComponent(HomeActivity::class.java.name))
    }

    @Test
    fun testRegisterTextNavigation_fromLogin_toRegisterActivity() {
        onView(withId(R.id.registerText)).perform(click())
        intended(hasComponent(RegisterActivity::class.java.name))
    }

    @Test
    fun testRegisterButtonNavigation_toLoginActivity() {
        ActivityScenario.launch(RegisterActivity::class.java)
        onView(withId(R.id.registerButton)).perform(click())
        intended(hasComponent(LoginActivity::class.java.name))
    }

    @Test
    fun testLoginTextNavigation_fromRegister_toLoginActivity() {
        ActivityScenario.launch(RegisterActivity::class.java)
        onView(withId(R.id.loginText)).perform(click())
        intended(hasComponent(LoginActivity::class.java.name))
    }

    @Test
    fun testDMIconNavigation_inHomeActivity_toDMActivity() {
        ActivityScenario.launch(HomeActivity::class.java)
        onView(withId(R.id.dmicon)).perform(click())
        intended(hasComponent(DMActivity::class.java.name))
    }

    @Test
    fun testBottomNavigation_inHomeActivity() {
        ActivityScenario.launch(HomeActivity::class.java)

        onView(withId(R.id.bottomNavigationView))
            .perform(TestUtils.selectBottomNavItem(R.id.nav_search))
        intended(hasComponent(SearchActivity::class.java.name))

        ActivityScenario.launch(HomeActivity::class.java)
        onView(withId(R.id.bottomNavigationView))
            .perform(TestUtils.selectBottomNavItem(R.id.nav_profile))
        intended(hasComponent(ProfileActivity::class.java.name))

        ActivityScenario.launch(HomeActivity::class.java)
        onView(withId(R.id.bottomNavigationView))
            .perform(TestUtils.selectBottomNavItem(R.id.nav_contacts))
        intended(hasComponent(ContactsActivity::class.java.name))

        ActivityScenario.launch(HomeActivity::class.java)
        onView(withId(R.id.bottomNavigationView))
            .perform(TestUtils.selectBottomNavItem(R.id.nav_add))
        intended(hasComponent(NewPostActivity::class.java.name))
    }

    @Test
    fun testBackButtonNavigation_inChatActivity_toDMActivity() {
        ActivityScenario.launch(ChatActivity::class.java)
        onView(withId(R.id.backButton)).perform(click())
        intended(hasComponent(DMActivity::class.java.name))
    }

    @Test
    fun testBackButtonNavigation_inDMActivity_toHomeActivity() {
        ActivityScenario.launch(DMActivity::class.java)
        onView(withId(R.id.backButton)).perform(click())
        intended(hasComponent(HomeActivity::class.java.name))
    }

    @Test
    fun testDoneTextNavigation_inEditProfileActivity_toProfileActivity() {
        ActivityScenario.launch(EditProfileActivity::class.java)
        onView(withId(R.id.doneTextView)).perform(click())
        intended(hasComponent(ProfileActivity::class.java.name))
    }

    @Test
    fun testFollowersActivityNavigation() {
        ActivityScenario.launch(FollowersActivity::class.java)
        onView(withId(R.id.backButton)).perform(click())
        intended(hasComponent(ProfileActivity::class.java.name))

        ActivityScenario.launch(FollowersActivity::class.java)
        onView(withId(R.id.followingText)).perform(click())
        intended(hasComponent(FollowingActivity::class.java.name))
    }

    @Test
    fun testFollowingActivityNavigation() {
        ActivityScenario.launch(FollowingActivity::class.java)
        onView(withId(R.id.backButton)).perform(click())
        intended(hasComponent(ProfileActivity::class.java.name))

        ActivityScenario.launch(FollowingActivity::class.java)
        onView(withId(R.id.followersText)).perform(click())
        intended(hasComponent(FollowersActivity::class.java.name))
    }

    @Test
    fun testNewPostActivityNavigation() {
        ActivityScenario.launch(NewPostActivity::class.java)
        onView(withId(R.id.cameraIcon)).perform(click())
        intended(hasComponent(CaptureActivity::class.java.name))

        ActivityScenario.launch(NewPostActivity::class.java)
        onView(withId(R.id.nextText)).perform(click())
        intended(hasComponent(NewPostActivity2::class.java.name))
    }

    @Test
    fun testCaptureActivityNextNavigation() {
        ActivityScenario.launch(CaptureActivity::class.java)
        onView(withId(R.id.nextText)).perform(click())
        intended(hasComponent(NewPostActivity2::class.java.name))
    }

    @Test
    fun testProfileActivityNavigation() {
        ActivityScenario.launch(ProfileActivity::class.java)
        onView(withId(R.id.editProfileButton)).perform(click())
        intended(hasComponent(EditProfileActivity::class.java.name))

        ActivityScenario.launch(ProfileActivity::class.java)
        onView(withId(R.id.followersText)).perform(click())
        intended(hasComponent(FollowersActivity::class.java.name))

        ActivityScenario.launch(ProfileActivity::class.java)
        onView(withId(R.id.followingText)).perform(click())
        intended(hasComponent(FollowingActivity::class.java.name))
    }

    @Test
    fun testBottomNavigation_inSearchActivity() {
        ActivityScenario.launch(SearchActivity::class.java)

        onView(withId(R.id.bottomNavigationView))
            .perform(TestUtils.selectBottomNavItem(R.id.nav_home))
        intended(hasComponent(HomeActivity::class.java.name))
    }

    @Test
    fun testVideoCallActivityEndCallNavigation() {
        ActivityScenario.launch(VideoCallActivity::class.java)
        onView(withId(R.id.endCall)).perform(click())
    }

    @Test
    fun testCallActivityEndCallNavigation() {
        ActivityScenario.launch(CallActivity::class.java)
        onView(withId(R.id.endCall)).perform(click())
    }
}
