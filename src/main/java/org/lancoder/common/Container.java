package org.lancoder.common;

import java.util.ArrayList;

import org.lancoder.common.exceptions.MissingThirdPartyException;
import org.lancoder.common.pool.Cleanable;
import org.lancoder.common.pool.PoolCleanerService;
import org.lancoder.common.third_parties.ThirdParty;

public abstract class Container extends RunnableService implements ServiceManager {

	protected final ArrayList<Service> services = new ArrayList<>();
	protected final ArrayList<ThirdParty> thirdParties = new ArrayList<>();
	protected final ThreadGroup serviceThreads = new ThreadGroup("services");
	protected PoolCleanerService poolCleaner;

	protected void bootstrap() {
		registerThirdParties();
		checkThirdParties();
		registerServices();
	}

	protected abstract void registerThirdParties();

	protected void checkThirdParties() {
		for (ThirdParty thirdParty : thirdParties) {
			if (!thirdParty.isInstalled()) {
				throw new MissingThirdPartyException(thirdParty);
			}
		}
	}

	protected void registerServices() {
		poolCleaner = new PoolCleanerService();
		services.add(poolCleaner);
	}

	@Override
	public void startServices() {
		for (Service s : services) {
			if (s instanceof RunnableService) {
				Thread t = new Thread(this.serviceThreads, (RunnableService) s);
				t.start();
			}
			if (s instanceof Cleanable) {
				poolCleaner.addCleanable((Cleanable) s);
			}
		}
	}

	@Override
	public void stopServices() {
		for (Service s : services) {
			s.stop();
		}
		this.serviceThreads.interrupt();
	}

	@Override
	public final void stop() {
		super.stop();
		stopServices();
		shutdown();
	}

	public abstract void shutdown();

	@Override
	public void serviceFailure(Exception e) {
		e.printStackTrace();
	}

}
