package org.lancoder.common;

import java.util.ArrayList;
import java.util.HashMap;

import org.lancoder.common.exceptions.MissingThirdPartyException;
import org.lancoder.common.scheduler.Schedulable;
import org.lancoder.common.scheduler.Scheduler;
import org.lancoder.common.third_parties.FFmpeg;
import org.lancoder.common.third_parties.FFprobe;
import org.lancoder.common.third_parties.ThirdParty;

public abstract class Container extends RunnableServiceAdapter implements ServiceManager {

	private final HashMap<Class<? extends ThirdParty>, ThirdParty> thirdParties = new HashMap<>();

	protected final ArrayList<Service> services = new ArrayList<>();
	protected final ThreadGroup serviceThreads = new ThreadGroup("services");
	protected FilePathManager filePathManager;

	protected Scheduler scheduler;

	protected void bootstrap() {
		registerThirdParties();
		checkThirdParties();
		registerServices();
	}

	protected FFmpeg getFFmpeg() {
		return (FFmpeg) getThirdParty(FFmpeg.class);
	}

	protected FFprobe getFFprobe() {
		return (FFprobe) getThirdParty(FFprobe.class);
	}

	private ThirdParty getThirdParty(Class<? extends ThirdParty> clazz) {
		return thirdParties.get(clazz);
	}

	protected abstract void registerThirdParties();

	protected void registerThirdParty(ThirdParty thirdParty) {
		thirdParties.put(thirdParty.getClass(), thirdParty);
	}

	protected void checkThirdParties() {
		for (ThirdParty thirdParty : thirdParties.values()) {
			if (!thirdParty.isInstalled()) {
				throw new MissingThirdPartyException(thirdParty);
			}
		}
	}

	protected void registerServices() {
		this.scheduler = new Scheduler();
		services.add(scheduler);
	}

	@Override
	public void startServices() {
		for (Service s : services) {
			if (s instanceof Runnable) {
				Thread t = new Thread(this.serviceThreads, (Runnable) s, s.getClass().getSimpleName());
				if (s instanceof Scheduler) {
					scheduler.setThread(t);
				}
				t.start();
			}
			if (s instanceof Schedulable) {
				scheduler.addSchedulable((Schedulable) s);
			}
		}
	}

	@Override
	public void stopServices() {
		for (Service s : services) {
			s.stop();
		}
		services.clear();
		this.serviceThreads.interrupt();
	}

	@Override
	public final void stop() {
		super.stop();
		stopServices();
	}

	public abstract void shutdown();

}
