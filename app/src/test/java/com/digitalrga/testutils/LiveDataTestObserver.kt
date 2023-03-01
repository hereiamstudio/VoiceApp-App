package com.voiceapp.testutils

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import org.junit.Assert

/**
 * This class is used to collect the emissions of a [LiveData] object and store them for later
 * value assertion during unit testing.
 *
 * @param T The type of data the [Observer] will receive.
 */
class LiveDataTestObserver<T> : Observer<T> {

    /**
     * This is the [List] of observed values.
     */
    val observedValues: List<T> get() = values

    private val values = mutableListOf<T>()

    override fun onChanged(t: T) {
        values.add(t)
    }

    /**
     * Assert that a [LiveData] has emitted these values.
     *
     * @param values The values to compare with the collected values. This must match the collected
     * values exactly, otherwise the assertion will fail.
     */
    fun assertValues(vararg values: T) {
        Assert.assertEquals(values.toList(), this.values)
    }

    /**
     * Assert this [Observer] has not collected any values.
     */
    fun assertEmpty() {
        Assert.assertTrue(values.isEmpty())
    }
}