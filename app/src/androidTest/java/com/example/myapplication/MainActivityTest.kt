package com.example.myapplication

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @Test
    fun loginButtonDisplayed() {

        ActivityScenario.launch(MainActivity::class.java)

        onView(withId(R.id.btnLogin))
            .check(matches(isDisplayed()))
    }

    @Test
    fun emailCanBeTyped() {

        ActivityScenario.launch(MainActivity::class.java)

        onView(withId(R.id.etEmail))
            .perform(typeText("admin@test.com"), closeSoftKeyboard())

        onView(withId(R.id.etEmail))
            .check(matches(withText("admin@test.com")))
    }

    @Test
    fun passwordCanBeTyped() {

        ActivityScenario.launch(MainActivity::class.java)

        onView(withId(R.id.etPassword))
            .perform(typeText("123456"), closeSoftKeyboard())

        onView(withId(R.id.etPassword))
            .check(matches(withText("123456")))
    }

    @Test
    fun clickLoginButton() {

        ActivityScenario.launch(MainActivity::class.java)

        onView(withId(R.id.etEmail))
            .perform(replaceText("admin@test.com"))

        onView(withId(R.id.etPassword))
            .perform(replaceText("123456"))

        onView(withId(R.id.btnLogin))
            .perform(click())
    }
}