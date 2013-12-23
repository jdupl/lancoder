package drfoliberg.task;

public class TaskFactory {

	//2 pass with only bitrate control
	public static Task new2PassTask() {
		return new Task();
	}

	// Constant quality mode (also known as constant ratefactor). Bitrate
	// corresponds approximately to that of constant quantizer, but gives better
	// quality overall at little speed cost. The best one-pass option in x264.
	public static Task newCrfTask() {
		return new Task();
	}

	// Constant quantizer mode. Not exactly constant completely--B-frames and
	// I-frames have different quantizers from P-frames. Generally should not be
	// used, since CRF gives better quality at the same bitrate.
	public static Task newCqpTask() {
		return new Task();
	}
}
