package org.lancoder.common.file_components.streams;

import java.io.Serializable;

import org.lancoder.common.codecs.base.AbstractCodec;
import org.lancoder.common.progress.Unit;

public abstract class BaseStream implements Serializable {

	private static final long serialVersionUID = 774310730253165761L;

	protected String relativeFile;
	protected int index;
	protected AbstractCodec codec;
	protected String title = "";
	protected String language = "und";
	protected boolean isDefault = false;
	protected long unitCount;
	protected Unit unit = Unit.SECONDS;

	public String getRelativeFile() {
		return relativeFile;
	}

	public int getIndex() {
		return index;
	}

	public AbstractCodec getCodec() {
		return codec;
	}

	public String getTitle() {
		return title;
	}

	public String getLanguage() {
		return language;
	}

	public boolean isDefault() {
		return isDefault;
	}

	public long getUnitCount() {
		return unitCount;
	}

	public Unit getUnit() {
		return unit;
	}

}
