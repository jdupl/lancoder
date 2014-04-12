package drfoliberg.common.network;

import java.io.Serializable;

public class Message implements Serializable {

	private static final long serialVersionUID = -483657531000641905L;

	protected ClusterProtocol code;
	protected String unid;

	/**
	 * Generic Message object. Uses code to send generic commands as closing
	 * connection, update required etc.
	 * 
	 * @param code
	 *            The Message code used in the ClusterProtocol
	 * @param unid
	 *            The worker id
	 */
	public Message(ClusterProtocol code, String unid) {
		this.code = code;
		this.unid = unid;
	}

	/**
	 * Generic message object without unid
	 * 
	 * @param code
	 */
	public Message(ClusterProtocol code) {
		this.code = code;
		this.unid = "";
	}

	public ClusterProtocol getCode() {
		return code;
	}

	public String getUnid() {
		return unid;
	}

	public void setUnid(String unid) {
		this.unid = unid;
	}
}
