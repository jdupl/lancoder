package drfoliberg.common.job;

public class JobFactory {

//	// 2 pass with only bitrate control
//	public static Job new2PassJob() {
//		return new Job(null, null, JobType.BITRATE_2_PASS_JOB, 600000);
//	}
//
//	// Constant quality mode (also known as constant ratefactor). Bitrate
//	// corresponds approximately to that of constant quantizer, but gives better
//	// quality overall at little speed cost. The best one-pass option in x264.
//	public static Job newCrfJob() {
//		return new Job(null, null, JobType.CRF_JOB, 600000);
//	}
//
//	// Constant quantizer mode. Not exactly constant completely--B-frames and
//	// I-frames have different quantizers from P-frames. Generally should not be
//	// used, since CRF gives better quality at the same bitrate.
//	public static Job newCqpJob() {
//		return new Job(null, null, JobType.CQP_JOB, 600000);
//	}
}
