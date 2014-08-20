package drfoliberg.common.utils;

import static org.junit.Assert.*;
import org.junit.Test;

public class TestTimeUtils {

	String format;
	long ms;

	@Test
	public void testMsFromString() {
		format = "00:00:00.000";
		ms = TimeUtils.getMsFromString(format);
		assertEquals(0, ms);

		format = "00:04:24.530";
		ms = TimeUtils.getMsFromString(format);
		assertEquals(264530, ms);

		format = "00:04:24.053";
		ms = TimeUtils.getMsFromString(format);
		assertEquals(264053, ms);

		format = "00:04:24.531";
		ms = TimeUtils.getMsFromString(format);
		assertEquals(264531, ms);

		format = "02:04:24.531";
		ms = TimeUtils.getMsFromString(format);
		assertEquals(7464531, ms);
	}

	@Test
	public void testStringFromMs() {
		ms = 0;
		format = TimeUtils.getStringFromMs(ms);
		assertEquals("00:00:00.000", format);

		ms = 264530;
		format = TimeUtils.getStringFromMs(ms);
		assertEquals("00:04:24.530", format);

		ms = 264053;
		format = TimeUtils.getStringFromMs(ms);
		assertEquals("00:04:24.053", format);

		ms = 264531;
		format = TimeUtils.getStringFromMs(ms);
		assertEquals("00:04:24.531", format);

		ms = 7464531;
		format = TimeUtils.getStringFromMs(ms);
		assertEquals("02:04:24.531", format);
	}

}
