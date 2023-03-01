package com.voiceapp.coroutines

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.junit.Assert

/**
 * Enable testing on a [Flow] object.
 *
 * @param T The type of data the [Flow] emits.
 * @param scope The [CoroutineScope] to execute the [Flow] under.
 * @return A [FlowTestObserver] for the given [Flow].
 */
fun <T> Flow<T>.test(scope: CoroutineScope): FlowTestObserver<T> = FlowTestObserver(scope, this)

/**
 * This is used to aid in the testing of [Flow] objects.
 *
 * Obtained from https://proandroiddev.com/from-rxjava-to-kotlin-flow-testing-42f1641d8433
 *
 * @param T The type of data the [Flow] emits.
 * @param scope The [CoroutineScope] to execute the [Flow] under.
 * @param flow The [Flow] that is being tested.
 */
class FlowTestObserver<T>(
    private val scope: CoroutineScope,
    flow: Flow<T>? = null) {

    private val values = mutableListOf<T>()

    private var job: Job? = null

    init {
        flow?.let {
            observeFlow(it)
        }
    }

    /**
     * Allows for the late observation of a [Flow] when the [Flow] instance was not available at the
     * time of instantiating this class.
     *
     * If a [Flow] is already being observed when this method is called, an [IllegalStateException]
     * will be thrown.
     *
     * @param flow The [Flow] to observe.
     */
    fun observe(flow: Flow<T>) {
        if (job == null) {
            observeFlow(flow)
        } else {
            throw IllegalStateException("Already observing a Flow.")
        }
    }

    /**
     * Assert that a [Flow] has never emitted any values.
     */
    fun assertNoValues(): FlowTestObserver<T> {
        Assert.assertTrue(values.isEmpty())

        return this
    }

    /**
     * Assert that a [Flow] has emitted these values.
     */
    fun assertValues(vararg values: T): FlowTestObserver<T> {
        Assert.assertEquals(values.toList(), this.values)

        return this
    }

    /**
     * Finish the flow.
     */
    fun finish() {
        job?.cancel()
    }

    /**
     * Observe a given [Flow].
     *
     * @param flow The [Flow] to observe.
     */
    private fun observeFlow(flow: Flow<T>) {
        job = scope.launch {
            flow.collect {
                values.add(it)
            }
        }
    }
}