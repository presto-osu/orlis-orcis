package com.gk.reports;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.gk.datacontrol.DBClass;
import com.gk.simpleworkoutjournal.R;

public class ReportConfigurator extends Activity implements LoaderManager.LoaderCallbacks<Cursor> {
    final String APP_NAME = "SWJournal";
    private static final boolean DEBUG_FLAG = false;
    DBClass dbmediator;
    SimpleCursorAdapter exChooserAdapter;

    Spinner wPointChooser;
    Spinner rPointChooser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (DEBUG_FLAG) Log.v(APP_NAME, "onCreate :: creating ReportConfigurator");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.report_configurator);

        Spinner periodChooser = (Spinner) findViewById( R.id.periodChooser );
        ArrayAdapter<CharSequence> periodAdapter = ArrayAdapter.createFromResource( this, R.array.periods, R.layout.period_chooser_spinner);
        periodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        periodChooser.setAdapter(periodAdapter);

        wPointChooser = (Spinner) findViewById( R.id.weightPointChooser );
        ArrayAdapter<CharSequence> wPointAdapter= ArrayAdapter.createFromResource( this, R.array.points_w, R.layout.period_chooser_spinner);
        wPointAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        wPointChooser.setAdapter(wPointAdapter);

        wPointChooser.setEnabled(false);

        rPointChooser = (Spinner) findViewById( R.id.repsPointChooser );
        ArrayAdapter<CharSequence> rPointAdapter= ArrayAdapter.createFromResource( this, R.array.points_r, R.layout.period_chooser_spinner);
        rPointAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        rPointChooser.setAdapter(rPointAdapter);

        rPointChooser.setEnabled(false);

        Spinner exChooser = (Spinner) findViewById( R.id.exerciseChooser );

        String[] from = new String[] { DBClass.KEY_ID };
        int[] to = new int[] { R.id.periodChooserItem };

        dbmediator  = new DBClass(this);
        exChooserAdapter = new SimpleCursorAdapter(this, R.layout.exercise_chooser_spinner, null, from, to, 0);
        exChooserAdapter.setDropDownViewResource( R.layout.exercise_chooser_spinner);
        exChooser.setAdapter(exChooserAdapter);

        getLoaderManager().initLoader(0, null, this);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new MyCursorLoader(this, dbmediator);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        exChooserAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    public void onChbClick(View v) {
        if (DEBUG_FLAG) Log.v(APP_NAME, "onChbClick :: started");

        Spinner targetSpinner;
        TextView targetLabel;
        boolean checked = ((CheckBox) v).isChecked();

        switch ( v.getId() )
        {
            case R.id.show_weight_checkbox:
                targetSpinner = wPointChooser;
                targetLabel = (TextView) findViewById( R.id.weights_per_date_label);
                break;

            case R.id.show_rep_checkbox:
                targetSpinner = rPointChooser;
                targetLabel = (TextView) findViewById( R.id.reps_per_date_label);
                break;

            default:
                Log.e(APP_NAME, "onChbClick :: unknown source ID");
                return;
        }

        targetSpinner.setEnabled( checked );
        targetLabel.setEnabled( checked );

    }

    int getPeriodInMonths()
    {
        if (DEBUG_FLAG) Log.v(APP_NAME, "getPeriodInMonths :: started");
        int months;
        switch ( ( (Spinner) findViewById(R.id.periodChooser)).getSelectedItemPosition() )
        {
            case 0:
                months = 1;
                break;

            case 1:
                months = 3;
                break;

            case 2:
                months  = 6;
                break;

            case 3:
                months = 12;
                break;

            case 4:
                months = 24;
                break;

            default:
                Log.e(APP_NAME, "createReport :: unknown period");
                return -1;
        }
        return months;
    }

    int getWeightPointType()
    {
        if (DEBUG_FLAG) Log.v(APP_NAME, "getWeightPointType :: started");
        if ( ((CheckBox) findViewById(R.id.show_weight_checkbox) ).isChecked() )
        {
            return ((Spinner) findViewById(R.id.weightPointChooser)).getSelectedItemPosition();
        }
        else
        {
            return -1;
        }
    }

    int getRepsPointType()
    {
        if (DEBUG_FLAG) Log.v(APP_NAME, "getRepsPointType :: started");
        if ( ((CheckBox) findViewById(R.id.show_rep_checkbox) ).isChecked() )
        {
            return ((Spinner) findViewById(R.id.repsPointChooser)).getSelectedItemPosition();
        }
        else
        {
            return -1;
        }
    }

    void createReport()
    {
        if (DEBUG_FLAG) Log.v(APP_NAME, "createReport :: started");

        Cursor selectedExCs  = (Cursor) ((Spinner) findViewById(R.id.exerciseChooser)).getSelectedItem();

        String exName;
        if ( selectedExCs == null ) {
            Toast.makeText(this, R.string.no_ex_selected,Toast.LENGTH_SHORT).show();
            return;
        } else {
            exName = selectedExCs.getString(selectedExCs.getColumnIndex(DBClass.KEY_ID));
        }

        int months = getPeriodInMonths();
        int weightType = getWeightPointType();
        int repsType = getRepsPointType();

        if ( weightType < 0 && repsType < 0)
        {
            //error
            Log.e(APP_NAME, "createReport :: no reps and weight selected. Must select at least one.");
            Toast.makeText(this, R.string.need_include_reps_or_weights,Toast.LENGTH_SHORT).show();
            return;
        }

        Intent reportConfigurator = new Intent( this, ExerciseReportContainer.class );
        reportConfigurator.putExtra( "exName"    , exName     );
        reportConfigurator.putExtra( "months"    , months     );
        reportConfigurator.putExtra( "weightType", weightType );
        reportConfigurator.putExtra( "repsType"  , repsType   );
        startActivityForResult(reportConfigurator, 0);
    }

    public void onBtnClick( View view) {
        if (DEBUG_FLAG) Log.v(APP_NAME, "onBtnClick :: started");

        switch ( view.getId() )
        {
            case R.id.gen_rep_btn:
                createReport();
                break;

            case R.id.cancel_report:
                finish();
                break;

            default:
                Log.e(APP_NAME, "onBtnClick :: unknown source ID");
        }
    }

    static class MyCursorLoader extends CursorLoader {
        DBClass db;

        public MyCursorLoader(Context context, DBClass db) {
            super(context);
            this.db = db;
        }

        @Override
        public Cursor loadInBackground() {
            return db.fetchExerciseNames( "" );
        }
    }

}