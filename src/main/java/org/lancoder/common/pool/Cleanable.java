package org.lancoder.common.pool;

public interface Cleanable {

	public final static long CLEAN_DELAY_MSEC = 5 * 1000 * 60;

	public boolean clean();

	public boolean shouldClean();
}
