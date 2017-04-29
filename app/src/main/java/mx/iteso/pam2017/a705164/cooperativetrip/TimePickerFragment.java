package mx.iteso.pam2017.a705164.cooperativetrip;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.TimePicker;

import java.util.Calendar;

/**
 * Created by paco on 24/04/2017.
 */

public class TimePickerFragment extends DialogFragment /*implements TimePickerDialog.OnTimeSetListener */{
    private  TimePickerDialog.OnTimeSetListener listener;

    public TimePickerFragment() {}


    public void setTimeListener(TimePickerDialog.OnTimeSetListener listener) {
        this.listener = listener;
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current time as the default values for the picker
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        // Create a new instance of TimePickerDialog and return it
        if (listener != null)
            return new TimePickerDialog(getActivity(), listener, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        return null;
    }


}
