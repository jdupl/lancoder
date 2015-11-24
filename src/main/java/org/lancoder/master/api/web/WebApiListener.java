package org.lancoder.master.api.web;

import org.lancoder.common.network.messages.web.ApiJobRequest;
import org.lancoder.common.network.messages.web.ApiResponse;

public interface WebApiListener {

	void disconnectNode(String unid);

	boolean addJob(ApiJobRequest req);

	ApiResponse apiDeleteJob(String id);

}
