package fr.tvbarthel.apps.simpleweatherforcast.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.ArrayAdapter;

import fr.tvbarthel.apps.simpleweatherforcast.R;
import fr.tvbarthel.apps.simpleweatherforcast.utils.SharedPreferenceUtils;


public class TemperatureUnitPickerDialogFragment extends DialogFragment {

    private static final String BUNDLE_TEMPERATURE_UNIT_NAMES = "BundleTemperatureUnitNames";
    private static final String BUNDLE_TEMPERATURE_UNIT_SYMBOLS = "BundleTemperatureUnitSymbols";

    public static TemperatureUnitPickerDialogFragment newInstance(String[] temperatureUnitNames, String[] temperatureUnitSymbols) {
        final TemperatureUnitPickerDialogFragment instance = new TemperatureUnitPickerDialogFragment();
        final Bundle arguments = new Bundle();
        arguments.putStringArray(BUNDLE_TEMPERATURE_UNIT_NAMES, temperatureUnitNames);
        arguments.putStringArray(BUNDLE_TEMPERATURE_UNIT_SYMBOLS, temperatureUnitSymbols);
        instance.setArguments(arguments);
        return instance;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Bundle arguments = getArguments();

        final String[] temperatureUnitNames = arguments.getStringArray(BUNDLE_TEMPERATURE_UNIT_NAMES);
        final String[] temperatureUnitSymbols = arguments.getStringArray(BUNDLE_TEMPERATURE_UNIT_SYMBOLS);

        final ArrayAdapter<String> temperatureUnitNameAdapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_expandable_list_item_1, temperatureUnitNames);

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.dialog_temperature_unit_picker_title);
        builder.setAdapter(temperatureUnitNameAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferenceUtils.storeTemperatureUnitSymbol(getActivity(), temperatureUnitSymbols[which]);
            }
        });
        builder.setCancelable(true);
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.setInverseBackgroundForced(true);
        return builder.create();
    }
}
