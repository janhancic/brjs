package org.bladerunnerjs.api.spec.utility;

import static org.junit.Assert.*;

public class BRJSAssertions {
	public static void assertContains(String expectedSubstring, String actualString) {
		if(!actualString.contains(expectedSubstring)) {
			assertEquals("'" + actualString + "' was expected to contain '" + expectedSubstring + "'.", expectedSubstring, actualString);
		}
	}
	
	public static void assertDoesNotContain(String unexpectedSubstring, String actualString) {
		if(actualString.contains(unexpectedSubstring)) {
			assertEquals("'" + actualString + "' was not expected to contain '" + unexpectedSubstring + "'.", unexpectedSubstring, actualString);
		}
	}
}
