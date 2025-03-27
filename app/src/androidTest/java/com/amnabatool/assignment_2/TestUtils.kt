package com.amnabatool.assignment_1
import android.view.View
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf

object TestUtils {
    fun selectBottomNavItem(itemId: Int): ViewAction {
        return object : ViewAction {
            override fun getDescription(): String = "Select bottom navigation item"

            override fun getConstraints(): Matcher<View> =
                allOf(androidx.test.espresso.matcher.ViewMatchers.isDisplayed())

            override fun perform(uiController: UiController?, view: View?) {
                if (view is BottomNavigationView) {
                    view.selectedItemId = itemId
                }
            }
        }
    }
}
