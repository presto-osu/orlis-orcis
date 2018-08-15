package net.iexos.musicalarm;


import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.TimePicker;

public final class TimePickerFragment extends DialogFragment
        implements TimePickerDialog.OnTimeSetListener {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle bundle = this.getArguments();
        int hour = bundle.getInt(AlarmViewActivity.PREF_HOUR, 11);
        int minute = bundle.getInt(AlarmViewActivity.PREF_MIN, 11);

        return new TimePickerDialog(getActivity(), this, hour, minute,
                DateFormat.is24HourFormat(getActivity()));
    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        AlarmViewActivity activity = (AlarmViewActivity) getActivity();
        activity.onTimeChosen(hourOfDay, minute);
    }


}
