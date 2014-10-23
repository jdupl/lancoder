package org.lancoder.common.exceptions;

import org.lancoder.common.third_parties.ThirdParty;

public class MissingThirdPartyException extends RuntimeException {

	private static final long serialVersionUID = -6124633310276703927L;

	public MissingThirdPartyException(ThirdParty party) {
		super(String.format("Can not run needed third party program: '%s'.%n"
				+ "Please install the correct program and check path configuration.", party.getPath()));
	}

}
