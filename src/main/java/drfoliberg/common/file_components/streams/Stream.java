package drfoliberg.common.file_components.streams;

import drfoliberg.common.codecs.Codec;

public abstract class Stream {
	protected int index;
	protected Codec codec;
	protected boolean isDefault;
	protected String title;
	protected String language;
}
