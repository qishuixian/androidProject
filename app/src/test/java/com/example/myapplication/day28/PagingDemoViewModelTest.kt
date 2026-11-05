package com.example.myapplication.day28

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PagingDemoViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadNextPage_appendsFiveItems() = runTest {
        val model = PagingDemoViewModel()

        model.loadNextPage()
        advanceUntilIdle()

        assertEquals(5, model.state.value.items.size)
        assertEquals(1, model.state.value.page)
        assertEquals(true, model.state.value.hasNext)
    }

    @Test
    fun fourthPage_marksEnd() = runTest {
        val model = PagingDemoViewModel()

        repeat(4) {
            model.loadNextPage()
            advanceUntilIdle()
        }

        assertEquals(20, model.state.value.items.size)
        assertEquals(false, model.state.value.hasNext)
    }
}
