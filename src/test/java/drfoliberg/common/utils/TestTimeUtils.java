package drfoliberg.common.utils;

import static org.junit.Assert.*;
import org.junit.Test;

public class TestTimeUtils {

	String format;
	long ms;

	@Test
	public void testMsFromString() {
		format = "00:04:24.53";
		ms = TimeUtils.getMsFromString(format);
		assertEquals(264530, ms);

		format = "00:04:24.531";
		ms = TimeUtils.getMsFromString(format);
		assertEquals(264531, ms);
		
		format = "02:04:24.531";
		ms = TimeUtils.getMsFromString(format);
		assertEquals(7464531, ms);
	}

	@Test
	public void testStringFromMs() {
		ms = 264530;
		format = TimeUtils.getStringFromMs(ms);
		assertEquals("00:04:24.53", format);
		
		ms = 264531;
		format = TimeUtils.getStringFromMs(ms);
		assertEquals("00:04:24.531", format);
		
		ms = 7464531;
		format = TimeUtils.getStringFromMs(ms);
		assertEquals("02:04:24.531", format);
	}

}
