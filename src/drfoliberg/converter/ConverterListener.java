package drfoliberg.converter;

import drfoliberg.common.task.audio.AudioEncodingTask;

public interface ConverterListener {

	public void convertionFinished(AudioEncodingTask t);

	public void convertionStarted(AudioEncodingTask t);

	public void convertionFailed(AudioEncodingTask t);

}
