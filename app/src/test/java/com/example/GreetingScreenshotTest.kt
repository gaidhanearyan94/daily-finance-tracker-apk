package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun finflow_screenshot() {
    composeTestRule.setContent {
      MyApplicationTheme {
        com.example.ui.components.NeonVisaCard(
          card = com.example.data.CardAccountEntity(
            name = "Universal",
            cardNumberMasked = "*9423",
            expiry = "06/28",
            balance = 8523.20,
            creditLimit = 22000.00,
            creditUsed = 0.0,
            currency = "USD",
            cardType = "VISA",
            isPrimary = true
          ),
          secondaryCard = null,
          onAddCardClick = {}
        )
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/finflow_card.png")
  }
}
