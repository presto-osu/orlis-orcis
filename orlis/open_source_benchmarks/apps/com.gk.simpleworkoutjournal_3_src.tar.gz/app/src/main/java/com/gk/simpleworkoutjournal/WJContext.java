package com.gk.simpleworkoutjournal;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.gk.datacontrol.DBClass;

import java.util.HashSet;

public class WJContext implements AbsListView.MultiChoiceModeListener, DialogInterface.OnClickListener  {
    private static final String APP_NAME = "SWJournal";
    private static final boolean DEBUG_FLAG = false;
    WorkoutJournal activity;
    WorkoutDataAdapter.Subject contextSubj;

    LinearLayout actionModeZone;
    Button ctxDeleteLogBtn, ctxCopyBtn;

    ImageButton ctxCancelBtn;
    ImageButton ctxAddBtn;

    EditText ctxEditRepsField;
    EditText ctxEditWeightField;

    AutoCompleteTextView ctxEditExField;

    ActionMode thisActionMode;

    String idOfSelected;

    WJContext(WorkoutJournal activity, WorkoutDataAdapter.Subject subj)
    {
        super();
        this.activity = activity;
        this.contextSubj = subj;

        actionModeZone = (LinearLayout) activity.findViewById( R.id.actionModeZone);

        ctxCopyBtn = (Button)activity.findViewById(R.id.ctx_copySelectedBtn);
        ctxDeleteLogBtn = (Button)activity.findViewById(R.id.ctx_deleteLogEntriesBtn);

        ctxCancelBtn = (ImageButton)activity.findViewById(R.id.ctx_cancelBtn);
        ctxAddBtn = (ImageButton) activity.findViewById(R.id.ctx_addEditedBtn);

        ctxEditRepsField = (EditText)activity.findViewById(R.id.ctx_editReps);
        ctxEditWeightField = (EditText)activity.findViewById(R.id.ctx_editWeight);

        ctxEditExField = (AutoCompleteTextView) activity.findViewById(R.id.ctx_editExerciseACTV);

    }


    @Override
    public boolean onActionItemClicked(ActionMode actMode, MenuItem menuItem) {
       if ( DEBUG_FLAG ) Log.v(APP_NAME, "WJContext :: onActionItemClicked mode: " + actMode + " item: " + menuItem);

        WorkoutDataAdapter currAdapter;
        ListView currLv;

        switch ( contextSubj ) {
            case EXERCISES:
                currAdapter = activity.exerciseLogAdapter;
                currLv = activity.exercisesLv;
                break;

            case SETS:
                currAdapter = activity.setsLogAdapter;
                currLv = activity.setsLv;

                break;
            default:
                return false;
        }

        //get the only possible entry to work with
        if ( currAdapter.getcheckedAmount() != 1 ) {
            Log.e(APP_NAME, "WJContext :: onActionItemClicked: one checked expected, other amount is actually checked: "+currAdapter.getcheckedAmount());
            return false;
        }

        Integer sequenceNumber = (Integer)currAdapter.getListIdsOfCtxChecked().toArray()[0];
        Cursor entry = (Cursor)currLv.getItemAtPosition( sequenceNumber );

        //launch appropriate action for this entry
        switch( menuItem.getItemId() )
        {
            case R.id.context_action_rename_edit_single:
               if ( DEBUG_FLAG ) Log.v(APP_NAME, "WJContext :: onActionItemClicked case: edit/rename");

                ctxCopyBtn.setVisibility(View.GONE);
                ctxDeleteLogBtn.setVisibility(View.GONE);
                ctxCancelBtn.setVisibility(View.VISIBLE);
                ctxAddBtn.setVisibility(View.VISIBLE);

                if ( contextSubj == WorkoutDataAdapter.Subject.EXERCISES )   {
                    ctxEditExField.setVisibility(View.VISIBLE);
                    ctxEditRepsField.setVisibility(View.GONE);
                    ctxEditWeightField.setVisibility(View.GONE);

                    ctxEditExField.setText( entry.getString( entry.getColumnIndex( DBClass.KEY_EX_NAME ) ) );

                    idOfSelected = entry.getString( entry.getColumnIndex( DBClass.KEY_EX_NAME ));
                } else {
                    ctxEditExField.setVisibility(View.GONE);
                    ctxEditRepsField.setVisibility(View.VISIBLE);
                    ctxEditWeightField.setVisibility(View.VISIBLE);

                    ctxEditRepsField.setText( entry.getString(entry.getColumnIndex(DBClass.KEY_REPS)) );
                    ctxEditWeightField.setText( entry.getString( entry.getColumnIndex( DBClass.KEY_WEIGHT ) ) );

                    idOfSelected = entry.getString( entry.getColumnIndex( DBClass.KEY_ID ));
                }


                break;

            case R.id.context_action_delete_ex:

                ctxCopyBtn.setVisibility(View.VISIBLE);
                ctxDeleteLogBtn.setVisibility(View.VISIBLE);
                ctxCancelBtn.setVisibility(View.GONE);
                ctxAddBtn.setVisibility(View.GONE);

               if ( DEBUG_FLAG ) Log.v(APP_NAME, "WJContext :: onActionItemClicked case: delete ex");

                String exToDelete = entry.getString( entry.getColumnIndex("exercise_name") );

                //some dialog over here
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder( this.activity );
                alertBuilder.setPositiveButton( R.string.delete, this );
                alertBuilder.setNegativeButton( R.string.cancel, this );
                alertBuilder.setTitle(exToDelete);
                alertBuilder.setMessage( activity.getResources().getString( R.string.delete_everything_related_to_ex ));

                AlertDialog alert = alertBuilder.create();

                alert.show();
                //rest of work will be done by alert handler


                break;

            default:
                Log.e(APP_NAME, "WJContext :: onActionItemClicked unexpected case");
                break;
        }

        return true;
    }

    @Override
    public boolean onCreateActionMode(ActionMode actMode, Menu menu) {
       if ( DEBUG_FLAG ) Log.v(APP_NAME, "WJContext :: onCreateActionMode mode: "+actMode+" menu: "+menu);
        activity.currSubj = contextSubj;

        actionModeZone.setVisibility( View.VISIBLE );
        thisActionMode = actMode;
        MenuInflater inflater = actMode.getMenuInflater();
        inflater.inflate(R.menu.workout_context_menu, menu);

        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode actMode) {
       if ( DEBUG_FLAG ) Log.v(APP_NAME, "WJContext :: onDestroyActionMode mode: "+actMode);
        actionModeZone.setVisibility( View.GONE );

        if ( !activity.setsLv.isEnabled() ) activity.setsLv.setEnabled( true );
        if ( !activity.exercisesLv.isEnabled() ) activity.exercisesLv.setEnabled( true );

        switch ( contextSubj )
        {
            case EXERCISES:
                activity.exerciseLogAdapter.clearChecked();
                break;

            case SETS:
                activity.setsLogAdapter.clearChecked();
                break;
        }

        idOfSelected = "";

    }

    @Override
    public boolean onPrepareActionMode(ActionMode arg0, Menu arg1) {
       if ( DEBUG_FLAG ) Log.v(APP_NAME, "WJContext :: onPrepareActionMode mode: "+arg0+ " menu: "+arg1);
        // TODO Auto-generated method stub
        return true;
    }


    @Override
    public void onItemCheckedStateChanged(ActionMode actMode, int index, long arg2, boolean isChecked ) {
        //contextMode =  startActionMode( this ); //required to set title later //TODO: check if need reduce scope of context mode.
       if ( DEBUG_FLAG ) Log.v(APP_NAME, "WJContext :: onItemCheckedStateChanged subject: " + contextSubj + " int: " + index + " long " + arg2 + " bool: " + isChecked);

        //reset buttons and editTexts
        onRestoreContextLookBtnPressed();

        String actionBarText;
        WorkoutDataAdapter currAdapter;

        //if long click is the first click - we need to get currLv here. Potentially will need to define other current as well!
        switch ( this.contextSubj )
        {
            case EXERCISES:
                //if changed from other listview
                if ( activity.setsLv.isEnabled() ) activity.setsLv.setEnabled( false );
                actionBarText = activity.getString( R.string.exercises_chosen );

                currAdapter = activity.exerciseLogAdapter;

                break;
            case SETS:
                //if changed from other listview
                if ( activity.exercisesLv.isEnabled() ) activity.exercisesLv.setEnabled( false );
                actionBarText =  activity.getString( R.string.sets_chosen );

                currAdapter = activity.setsLogAdapter;

                break;
            default:
                return;
        }

        currAdapter.invertCtxChecked(index);
        currAdapter.notifyDataSetChanged();

        //if all items deselected
        int checkedAmount = currAdapter.getcheckedAmount();
        if ( checkedAmount == 0 ) {

            actMode.finish();

        } else if ( checkedAmount == 1 ) {

            actMode.getMenu().getItem( 0 ).setVisible( true ); //edit btn
            ctxCopyBtn.setVisibility( View.VISIBLE );

            if ( this.contextSubj == WorkoutDataAdapter.Subject.EXERCISES ) {
                actMode.getMenu().getItem(1).setVisible(true); // delete exercise btn
            } else {
                actMode.getMenu().getItem( 1 ).setVisible( false );
            }

        } else {
            ctxCopyBtn.setVisibility( View.GONE );
            actMode.getMenu().getItem( 0 ).setVisible( false );
            actMode.getMenu().getItem( 1 ).setVisible( false );
        }

        actionBarText += currAdapter.getcheckedAmount();
        actMode.setTitle( actionBarText );
    }

    public void copyButtonPressed(){
        if ( DEBUG_FLAG ) Log.v(APP_NAME, "WJContext :: copyButtonPressed : active context subject: "+contextSubj.toString() );

        if ( contextSubj  == WorkoutDataAdapter.Subject.EXERCISES ) {
            HashSet<Integer> ids = activity.exerciseLogAdapter.getListIdsOfCtxChecked();
            if ( ids.size() != 1 )
            {
                Log.e(APP_NAME, "WJContext :: copyButtonPressed : one selected expected for subject: "+contextSubj.toString() );
                return;
            }
            Cursor entry = (Cursor) activity.exercisesLv.getItemAtPosition( (Integer)ids.toArray()[0] );
            String ex = entry.getString(entry.getColumnIndex(DBClass.KEY_EX_NAME) );

            activity.exerciseTextView.setText( ex );
            activity.showEditsForSubject(WorkoutDataAdapter.Subject.EXERCISES);

        } else if ( contextSubj  == WorkoutDataAdapter.Subject.SETS ) {
            HashSet<Integer> ids = activity.setsLogAdapter.getListIdsOfCtxChecked();
            if ( ids.size() != 1 )
            {
                Log.e(APP_NAME, "WJContext :: copyButtonPressed : one selected expected for subject: "+contextSubj.toString() );
                return;
            }
            Cursor entry = (Cursor) activity.setsLv.getItemAtPosition( (Integer)ids.toArray()[0] );

            String w = entry.getString(entry.getColumnIndex(DBClass.KEY_WEIGHT));
            String r = entry.getString(entry.getColumnIndex(DBClass.KEY_REPS));

            activity.weightEdit.setText( w );
            activity.repsEdit.setText(r);

        }

        thisActionMode.finish();
    }

    public void onDeleteLogEntriesPressed() {
       if ( DEBUG_FLAG ) Log.v(APP_NAME, "WJContext :: onDeleteLogEntriesPressed : active context subject: "+contextSubj.toString() );

        if ( contextSubj  == WorkoutDataAdapter.Subject.EXERCISES ) {

            HashSet<Integer> ids = activity.exerciseLogAdapter.getListIdsOfCtxChecked();

            int affectedSetEntries = 0;
            int affectedExEntries = 0;
            Cursor entry;
            for (Integer id : ids) {
               if ( DEBUG_FLAG ) Log.v(APP_NAME, "WJContext :: onDeleteLogEntriesPressed : following checked ex ID of item in list view to delete: " + id);
                entry = (Cursor) activity.exercisesLv.getItemAtPosition(id);

                long exId = entry.getLong( entry.getColumnIndex( DBClass.KEY_ID ) );
                affectedSetEntries += activity.dbmediator.rmExLogEntry( exId, 1 );
                affectedExEntries++;
            }

            int newMaxIdx = activity.setsLogAdapter.getCount() - affectedSetEntries - 1;
            if (activity.setsLogAdapter.getIdxOfCurrent() > newMaxIdx)
                activity.setsLogAdapter.setIdxOfCurrent(newMaxIdx);

            newMaxIdx = activity.exerciseLogAdapter.getCount() - affectedExEntries - 1;
            if (activity.exerciseLogAdapter.getIdxOfCurrent() > newMaxIdx) {
                activity.exerciseLogAdapter.setIdxOfCurrent(newMaxIdx);
            }

            if (affectedExEntries > 0)
                activity.initiateListUpdate(WorkoutDataAdapter.Subject.EXERCISES, WorkoutJournal.TriggerEvent.DELETE);

        } else if ( contextSubj  == WorkoutDataAdapter.Subject.SETS ) {

            HashSet<Integer> setIds = activity.setsLogAdapter.getListIdsOfCtxChecked();

            int affectedSetEntries = 0;

            HashSet<Integer> exIds = new  HashSet<Integer>();
            Cursor entry;
            for (Integer id : setIds) {
               if ( DEBUG_FLAG ) Log.v(APP_NAME, "WJContext :: onDeleteLogEntriesPressed : following checked set ID of item in list view to delete: " + id);
                entry = (Cursor) activity.setsLv.getItemAtPosition(id);

                exIds.add( entry.getInt(entry.getColumnIndex(DBClass.KEY_EX_LOG_ID)) );

                affectedSetEntries += activity.dbmediator.rmSetLogEntry(entry);

            }

            //if ex log entry have no related sets - get rid of it as well
            int affectedExEntries = 0;
            for (Integer id : exIds) {

                if ( !activity.dbmediator.haveSetsWithExId( id ) ) {
                    affectedExEntries += activity.dbmediator.rmExLogEntry( id , 0 );
                }

            }

            //need to refresh ex list if some ex entry was removed
            //same code for ex and set
            if ( affectedExEntries != 0 ) {
                int newMaxIdx = activity.exerciseLogAdapter.getCount() - affectedExEntries - 1;
                if (activity.exerciseLogAdapter.getIdxOfCurrent() > newMaxIdx)
                    activity.exerciseLogAdapter.setIdxOfCurrent(newMaxIdx);

                activity.initiateListUpdate(WorkoutDataAdapter.Subject.EXERCISES, WorkoutJournal.TriggerEvent.DELETE);
            }

            if (affectedSetEntries != 0) {
                int newMaxIdx = activity.setsLogAdapter.getCount() - affectedSetEntries - 1;
                if (activity.setsLogAdapter.getIdxOfCurrent() > newMaxIdx)
                    activity.setsLogAdapter.setIdxOfCurrent(newMaxIdx);

                activity.initiateListUpdate(WorkoutDataAdapter.Subject.SETS, WorkoutJournal.TriggerEvent.DELETE);
            }

        }

        thisActionMode.finish();
    }

    public void onRestoreContextLookBtnPressed() {
       if ( DEBUG_FLAG ) Log.v(APP_NAME, "WJContext :: onRestoreContextLookBtnPressed");

        ctxCancelBtn.setVisibility(View.GONE);
        ctxAddBtn.setVisibility(View.GONE);
        ctxEditRepsField.setVisibility(View.GONE);
        ctxEditWeightField.setVisibility(View.GONE);
        ctxEditExField.setVisibility(View.GONE);

        ctxDeleteLogBtn.setVisibility(View.VISIBLE);
        ctxCopyBtn.setVisibility(View.VISIBLE);

        ctxEditWeightField.setText("");
        ctxEditRepsField.setText("");
        ctxEditExField.setText("");

    }

    public void onAddEditedBtnPressed() {
       if ( DEBUG_FLAG ) Log.v(APP_NAME, "WJContext :: onAddEditedBtnPressed \""+ctxEditExField.getText().toString()+"\"" );

        if ( idOfSelected.isEmpty() ) {
            Log.e(APP_NAME, "WJContext :: onAddEditedBtnPressed : ID of selected item is unknown");
            return;
        }

        String newName   = ctxEditExField.getText().toString().trim();
        String newReps   = ctxEditRepsField.getText().toString().trim();
        String newWeight = ctxEditWeightField.getText().toString().trim();

        boolean haveError = false;
        if ( (contextSubj == WorkoutDataAdapter.Subject.EXERCISES && newName.isEmpty() )  )
        {
            haveError = true;
        }
        else if ( contextSubj == WorkoutDataAdapter.Subject.SETS && ( newReps.isEmpty() || newWeight.isEmpty() ) )
        {
            haveError = true;
        }

        if ( haveError ) {
            Toast.makeText( activity, "Empty fields are not allowed", Toast.LENGTH_SHORT).show(); // TODO: make a string resources for this toast
        }

        if ( !haveError ) {

            switch ( contextSubj ) {
                case EXERCISES:

                    if ( !activity.dbmediator.updateExercise( idOfSelected, newName ) ) {
                        Log.w(APP_NAME, "Cannot rename: exercise with this name already exist (orig: "+idOfSelected+" new: " +newName+")");
                        haveError = true;
                        Toast.makeText( activity, "Cannot rename: exercise with this name already exist", Toast.LENGTH_SHORT).show(); // TODO: make a string resources for this toast
                    }
                    break;

                case SETS:
                    activity.dbmediator.updateSetLog( idOfSelected,  newReps,  newWeight.equals(".") ? "0" : newWeight);
                    break;
            }

            if ( !haveError ) {
                activity.initiateListUpdate( contextSubj, WorkoutJournal.TriggerEvent.EDIT ); //TODO: need other, separate event?
                activity.exerciseLogAdapter.notifyDataSetChanged();
                onRestoreContextLookBtnPressed();
            }
        }

        if( !haveError ) {
            idOfSelected = "";
            thisActionMode.finish();
        }
    }

    private void deleteSelectedExercise() {

        Integer idOfChecked = (Integer)activity.exerciseLogAdapter.getListIdsOfCtxChecked().toArray()[0];
        int deletedExLogs = activity.dbmediator.deleteEx( (Cursor)activity.exercisesLv.getItemAtPosition( idOfChecked ) );

        if ( deletedExLogs != 0 ) {
            int newMaxIdx = activity.exerciseLogAdapter.getCount() - deletedExLogs - 1;

            //handle idx of current
            if (activity.exerciseLogAdapter.getIdxOfCurrent() > newMaxIdx)
                activity.exerciseLogAdapter.setIdxOfCurrent(newMaxIdx);

            ///handle idx of checked
            if ( idOfChecked > newMaxIdx ) {
                activity.exerciseLogAdapter.invertCtxChecked( newMaxIdx ); //select
                activity.exerciseLogAdapter.invertCtxChecked( idOfChecked ); //deselect

            }

            //show renewed data for exercises
            activity.initiateListUpdate(WorkoutDataAdapter.Subject.EXERCISES, WorkoutJournal.TriggerEvent.DELETE);

            //make sure exercise edit is active
            actionModeZone.setVisibility( View.GONE );
            activity.showEditsForSubject(WorkoutDataAdapter.Subject.EXERCISES);

            //empty hint box for set since we have chosen other exercise
            activity.setNoteTv.setHint(activity.getString(R.string.workout_set_no_note_hint));
            activity.setNoteTv.setText("");

            //now take care of sets
            if ( newMaxIdx == -1 ) {
                activity.setsLv.setAdapter(null);
                return;
            }

            //update sets list view accordingly
            activity.initiateListUpdate( WorkoutDataAdapter.Subject.SETS, WorkoutJournal.TriggerEvent.DELETE );

        }

    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
       if ( DEBUG_FLAG ) Log.v(APP_NAME, "WJContext :: onClick of alert dialog pressed. dialog: "+dialog+ " which: "+which );

        switch ( which )  {
            case -1: // Delete button
                deleteSelectedExercise();
                break;

            case -2: // Cancel button
                break;
        }

    }
}





