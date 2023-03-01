package com.voiceapp.util

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import com.voiceapp.Const
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class PinHelperTest {

    companion object {

        private const val SHARED_PREFS = "myProperties"
    }

    private lateinit var mockContext: Context
    private lateinit var pinHelper: PinHelper

    @Before
    fun setUp() {
        mockContext = InstrumentationRegistry.getInstrumentation().context
        pinHelper = PinHelper(mockContext)
    }

    @After
    fun tearDown() {
        mockContext.deleteSharedPreferences(SHARED_PREFS)
    }

    @Test
    fun testLockedByDefault() {
        assertTrue(pinHelper.locked)
    }

    @Test
    fun testUnlockedOnCorrectPin() {
        pinHelper.savePin("123456")
        assertTrue(pinHelper.locked)
        pinHelper.testPin("123456")
        assertFalse(pinHelper.locked)
    }

    @Test
    fun testPinNullByDefault() {
        assertNull(pinHelper.pin)
    }

    @Test
    fun testPinSave() {
        pinHelper.savePin("123456")
        assertEquals("123456", pinHelper.pin)
    }

    @Test
    fun testLockout() {
        pinHelper.savePin("123456")

        for (x in 1..Const.MAX_PIN_ATTEMPTS + 1){
            assertFalse(pinHelper.testPin("111111"))
            assertEquals(x, pinHelper.attempts)
        }

        // Test actual pin fails due to lockout
        assertFalse(pinHelper.testPin("123456"))
    }

    @Test
    fun testLockTimeout() {
        pinHelper.savePin("123456")
        pinHelper.locked = false

        val time = System.currentTimeMillis()
        pinHelper.leaveTime = time
        pinHelper.resumedTime = time + Const.APP_LOCK_TIMEOUT - 1

        // Test unlocked
        assertFalse(pinHelper.locked)

        pinHelper.leaveTime = time
        pinHelper.resumedTime = time + Const.APP_LOCK_TIMEOUT + 1

        assertTrue(pinHelper.locked)
    }
}