package drfoliberg.common.network;

public class UNID extends Message {

	private static final long serialVersionUID = -70750737172920823L;
	private String nuid;

	public UNID(String nuid) {
		super(nuid);
		this.code = ClusterProtocol.NEW_UNID;
		this.nuid = nuid;
	}

	public String getUnid() {
		return nuid;
	}

}
