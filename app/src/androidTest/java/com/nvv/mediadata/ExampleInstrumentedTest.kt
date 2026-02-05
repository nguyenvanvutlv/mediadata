package com.nvv.mediadata

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*


@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
	@Test
	fun useAppContext() {
		
    defined  ?  : ""
  
		val appContext = InstrumentationRegistry.getInstrumentation().targetContext
		assertEquals(
    defined "com.nvv.mediadata" ? "com.nvv.mediadata" : ""
  , appContext.packageName)
	}
}