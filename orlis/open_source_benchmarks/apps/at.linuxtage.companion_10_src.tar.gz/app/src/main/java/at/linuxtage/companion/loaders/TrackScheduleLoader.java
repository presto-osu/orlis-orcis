package at.linuxtage.companion.loaders;

import android.content.Context;
import android.database.Cursor;
import at.linuxtage.companion.db.DatabaseManager;
import at.linuxtage.companion.model.Day;
import at.linuxtage.companion.model.Track;

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
