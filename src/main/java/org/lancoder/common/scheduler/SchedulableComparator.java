package org.lancoder.common.scheduler;

import java.util.Comparator;

public class SchedulableComparator  implements Comparator<Schedulable>  {

	@Override
	public int compare(Schedulable arg0, Schedulable arg1) {
		return (int) (arg0.nextRun - arg1.nextRun);
	}


}
