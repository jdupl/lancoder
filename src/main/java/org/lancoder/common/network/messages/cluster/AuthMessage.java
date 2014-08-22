package org.lancoder.common.network.messages.cluster;

import org.lancoder.common.network.messages.ClusterProtocol;

/**
 * Extends Message to require unid. Master server directly knows which node sent the message. Master server can verify
 * packets are not spoofed with SHA1 key.
 * 
 * All exchanges should be protected by SSH/SSL (issue #5).
 */
public class AuthMessage extends Message {

	private static final long serialVersionUID = 3349893444450151769L;
	protected String unid;

	@Deprecated
	public AuthMessage(String path, String unid) {
		super(path);
		this.unid = unid;
	}
	
	public AuthMessage(ClusterProtocol code, String unid) {
		super(code);
		this.unid = unid;
	}

	public String getUnid() {
		return unid;
	}

	public void setUnid(String unid) {
		this.unid = unid;
	}
}
