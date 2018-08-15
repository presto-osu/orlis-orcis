package org.ligi.scr.model.decorated;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;
import org.ligi.scr.model.Event;

public class EventDecorator {
    final private Event event;

    public EventDecorator(Event event) {
        this.event = event;
    }

    public Duration getDuration() {
        final PeriodFormatter hoursMinutes = getPeriodFormatter();
        return hoursMinutes.parsePeriod(event.duration).toStandardDuration();
    }

    private PeriodFormatter getPeriodFormatter() {
        return new PeriodFormatterBuilder()
                    .appendHours()
                    .appendSeparator(":")
                    .appendMinutes()
                    .toFormatter();
    }


    public void setEnd(DateTime end) {
        final Period period = new Period(new Interval(getStart(), end));
        event.duration=String.format("%02d:%02d", period.getHours(), period.getMinutes());
    }

    public DateTime getEnd() {
        return new DateTime(event.date).plus(getDuration().toDuration());
    }

    public DateTime getStart() {
        return DateTime.parse(event.date);
    }
}
