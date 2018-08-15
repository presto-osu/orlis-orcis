package org.toulibre.cdl.loaders;

import org.toulibre.cdl.db.DatabaseManager;
import org.toulibre.cdl.model.Day;
import org.toulibre.cdl.model.Track;

import android.content.Context;
import android.database.Cursor;

public class TrackScheduleLoader extends SimpleCursorLoader {

	private final Day day;
	private final Track track;

	public TrackScheduleLoader(Context context, Day day, Track track) {
		super(context);
		this.day = day;
		this.track = track;
	}

	@Override
	protected Cursor getCursor() {
		return DatabaseManager.getInstance().getEvents(day, track);
	}
}
