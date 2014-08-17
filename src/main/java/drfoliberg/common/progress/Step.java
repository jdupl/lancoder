package drfoliberg.common.progress;

import java.io.Serializable;

public class Step implements Serializable {

	private static final long serialVersionUID = -7941828789521798615L;

	protected long timeStarted;
	protected long timeElapsed;
	protected long timeEstimated;

	protected long unitsCompleted;
	protected long unitsTotal;
	protected double speed;

	public Step(long units) {
		this.unitsTotal = 0;
	}

	public void update(long units) {

	}

	public void start() {

	}

	public void reset() {

	}

	public void complete() {

	}
}
