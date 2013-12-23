package drfoliberg.network;


public class ClusterProtocol {

	public static final int BYE = 1;
	public static final int STATUS_REQUEST = 2;
	public static final int STATUS_REPORT = 3;

	public static final int CONNECT_ME = 100;
	public static final int DISCONNECT_ME = 101;

	public static final int TASK_REQUEST = 200;
	public static final int TASK_REPORT = 201;
	public static final int TASK_REFUSED = 202;
	public static final int TASK_ACCEPTED = 203;
	// private static final int BAD_MASTER = 100;
	public static final int BAD_REQUEST = -1;

}
