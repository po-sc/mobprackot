package ru.mirea.prac5

import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import org.hamcrest.CoreMatchers.not
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppTestsUITest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    // Тест 1: Проверка отображения изображения после нажатия на кнопку "Download random photo"
    @Test
    fun loadRandomPhoto_imageViewIsNotEmpty() {
        // Нажимаем на кнопку загрузки фото
        onView(withId(R.id.buttonLoad)).perform(click())

        // Ждем некоторое время для загрузки изображения
        Thread.sleep(3000) // Для упрощения используем sleep, лучше использовать IdlingResource

        // Проверяем, что ImageView имеет drawable
        onView(withId(R.id.imageView)).check(matches(hasDrawable()))
    }

    // Тест 2: Проверка, что поле ввода работает
    @Test
    fun editTextLink_acceptsInput() {
        val testInput = "https://example.com/image.png"

        // Вводим текст в поле ввода
        onView(withId(R.id.editTextLink)).perform(typeText(testInput), closeSoftKeyboard())

        // Проверяем, что введенный текст отображается
        onView(withId(R.id.editTextLink)).check(matches(withText(testInput)))
    }

    // Вспомогательный метод для проверки, что ImageView имеет drawable
    private fun hasDrawable(): org.hamcrest.Matcher<android.view.View> {
        return object : org.hamcrest.TypeSafeMatcher<android.view.View>() {
            override fun describeTo(description: org.hamcrest.Description?) {
                description?.appendText("has drawable")
            }

            override fun matchesSafely(view: android.view.View?): Boolean {
                if (view !is android.widget.ImageView) return false
                return view.drawable != null
            }
        }
    }
}
