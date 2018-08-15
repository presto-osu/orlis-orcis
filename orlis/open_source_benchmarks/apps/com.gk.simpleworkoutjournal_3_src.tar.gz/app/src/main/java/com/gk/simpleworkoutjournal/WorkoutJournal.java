package com.gk.simpleworkoutjournal;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.gk.datacontrol.DBClass;
import com.gk.datacontrol.ExerciseDataCursorLoader;
import com.gk.datacontrol.SetDataCursorLoader;

import static com.gk.simpleworkoutjournal.WorkoutDataAdapter.*;

public class WorkoutJournal extends Activity implements  OnItemClickListener, OnTouchListener, LoaderManager.LoaderCallbacks<Cursor> {
    public enum TriggerEvent { NONE, INIT, ADD, DELETE, NOTEADD, TRIG_BY_EX, EDIT }

    private static final String APP_NAME = "SWJournal";
    private static final boolean DEBUG_FLAG = false;

    public static final int EXERCISES = 0;
    public static final int SETS = 1;

    Subject currSubj;
    TriggerEvent setsUpTrigger;
    TriggerEvent exUpTrigger;

    LinearLayout notesLayout;

    AutoCompleteTextView exerciseTextView;
    EditText repsEdit, weightEdit;

    ListView exercisesLv, setsLv;
    TextView exerciseNoteTv, setNoteTv;
    WorkoutDataAdapter exerciseLogAdapter, setsLogAdapter;

    SetDataCursorLoader setsListDataLoader;
    ExerciseDataCursorLoader exListDataLoader;

    ImageButton switchBtn;
    boolean inContextMode;

    boolean addSetIfSameDate;
    String prevExLogId;

    WJContext exercisesContextualMode;
    WJContext setsContextualMode;

    boolean notesShowed = false;
    DBClass dbmediator;

    //WorkoutTimer workoutTimer;

    int initExPos;
    int initSetPos;

    protected void onSaveInstanceState(Bundle outState) {
        if (outState == null) throw  new AssertionError("outState is null when onSaveInstanceState of workoutjournal ");
        super.onSaveInstanceState(outState);

        outState.putInt("exPos", exerciseLogAdapter.getIdxOfCurrent());
        outState.putInt("setPos", setsLogAdapter.getIdxOfCurrent());
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_journal);

        // fetch all required UI items
        notesLayout = (LinearLayout) findViewById(R.id.notesLayout);

        setsLv = (ListView) findViewById(R.id.setsLv);
        setNoteTv = (TextView) findViewById(R.id.setNoteTv);

        exercisesLv = (ListView) findViewById(R.id.exercisesLv);
        exerciseNoteTv = (TextView) findViewById(R.id.exerciseNoteTv);

        exerciseTextView = (AutoCompleteTextView) findViewById(R.id.addExerciseACTV);

        repsEdit = (EditText) findViewById(R.id.editReps);
        weightEdit = (EditText) findViewById(R.id.editWeight);

        switchBtn = (ImageButton) findViewById(R.id.CancelBtn);

        // set notes touch listeners for exercise and set
        exerciseNoteTv.setOnTouchListener(this);

        // set click / touch listeners
        setsLv.setOnItemClickListener(this);
        exercisesLv.setOnItemClickListener(this);

        setsLv.setOnTouchListener(this);
        exercisesLv.setOnTouchListener(this);

        dbmediator = new DBClass(this);

        Cursor exCursor = dbmediator.fetchExerciseHistory();
        exerciseLogAdapter = new WorkoutDataAdapter(this, exCursor, WorkoutDataAdapter.Subject.EXERCISES);
        exerciseTextView.setAdapter( new ExerciseEntrylistAdapter( this, dbmediator ));
        setsLogAdapter = null;
        //fill the text view now
        exercisesLv.setAdapter(exerciseLogAdapter);

        currSubj = Subject.EXERCISES;

        //TODO: show appropriate sets
        exercisesContextualMode = new WJContext(this, Subject.EXERCISES);
        setsContextualMode = new WJContext(this, Subject.SETS);

        exercisesLv.setMultiChoiceModeListener( exercisesContextualMode );
        setsLv.setMultiChoiceModeListener( setsContextualMode );
        inContextMode = false;

        addSetIfSameDate = false;
        prevExLogId = "";

        if ( savedInstanceState != null ) {
            initExPos= savedInstanceState.getInt("exPos");
            initSetPos = savedInstanceState.getInt("setPos");
        } else {
            initExPos = -1;
            initSetPos = -1;
        }

        initiateListUpdate( Subject.ALL, TriggerEvent.INIT );

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if ( DEBUG_FLAG ) Log.v(APP_NAME, "WorkoutJournal :: onCreateOptionsMenu()");
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.workout_options_menu, menu);

        //workoutTimer = new WorkoutTimer( this, menu.findItem( R.id.action_timer) );
        //workoutTimer.enable();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if ( DEBUG_FLAG ) Log.v(APP_NAME, "WorkoutJournal :: onOptionsItemSelected " + item.getItemId());
        switch (item.getItemId()) {
            case R.id.action_moreinfo_icon:
                if (notesShowed) {
                    notesLayout.setVisibility(View.GONE);
                    notesShowed = false;
                } else {
                    notesLayout.setVisibility(View.VISIBLE);
                    notesShowed = true;
                }
                break;

            case R.id.action_timer:
                //workoutTimer.nextStep();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        //workoutTimer.stop( true );

        setsListDataLoader.reset();
        exListDataLoader.reset();

        dbmediator.close();

        super.onDestroy();

    }

    /*
     * Contains logic for initiating updates of lists
     */
    public void initiateListUpdate( Subject trigSubj, TriggerEvent trigEvent ) {
        if ( DEBUG_FLAG ) Log.v(APP_NAME, "WorkoutJournal :: initiateListUpdate subj: " + trigSubj.toString() + " caused by: " + trigEvent.toString());
        int subject = -1;

        switch ( trigSubj ) {
            case EXERCISES:
                exUpTrigger = trigEvent;
                subject = EXERCISES;
                break;

            case SETS:
                setsUpTrigger = trigEvent;
                subject = SETS;
                break;

            case ALL:
                exUpTrigger = trigEvent;
                setsUpTrigger = trigEvent;
                break;
        }

        if ( trigEvent == TriggerEvent.NOTEADD && trigSubj != Subject.EXERCISES && trigSubj != Subject.SETS ) {
            if ( DEBUG_FLAG ) Log.v( APP_NAME, "WorkoutJournal :: initiateListUpdate : incoming parameters are messed" );
            return;
        }

        switch ( trigEvent ) {
            case INIT:
               // getLoaderManager().initLoader( EXERCISES, null, this);
               // getLoaderManager().initLoader( SETS, null, this);

                getLoaderManager().restartLoader( EXERCISES, null, this );
                getLoaderManager().restartLoader( SETS, null, this );
                break;

            case TRIG_BY_EX:
                //no need to do anything with exercises, but should renew sets
                if ( trigSubj == Subject.SETS ) {
                    getLoaderManager().getLoader( SETS ).forceLoad();
                }
                break;

            case EDIT:
            case DELETE: // set update may be not required if deleted ex is not current
            case NOTEADD:
            case ADD: // ex added - should renew both since focus changed. set added - only set lv to update
                 getLoaderManager().getLoader( subject ).forceLoad();
                break;

            default:
                Log.e( APP_NAME, "WorkoutJournal :: initiateListUpdate : unexpected trigger event: "+trigEvent.toString() );
            }
    }

    public void onBackButtonPressed(View v) {

        if ( DEBUG_FLAG ) Log.v(APP_NAME, "WorkoutJournal :: onBackButtonPressed()");
        if (exerciseTextView.getVisibility() == View.GONE) {
            showEditsForSubject( Subject.EXERCISES );

        } else if (exerciseLogAdapter.getIdxOfCurrent() != -1) {
            showEditsForSubject( Subject.SETS );
        }
    }

    /*
     * In exercise edit mode: check if exercise text field is not empty
     * and log exrcise. I exercise not exist in db -
     * add exercise to db
     * scroll listview  to last item
     * ans set is as checked
     */
    public void onAddButtonPressed(View v) {
        //we are trying  both to log exercise to log DB and to add it into exercise DB and list view

        if (exerciseTextView.getVisibility() == View.VISIBLE) {

            String incomingName = exerciseTextView.getText().toString();
            incomingName = incomingName.trim();

            if ( DEBUG_FLAG ) Log.v(APP_NAME, "WorkoutJournal :: onAddButtonPressed(). Exercise in edit text: \"" + incomingName + "\" Amount of exercises before btn pressed: "+exerciseLogAdapter.getCount() );

            if (incomingName.length() == 0) {
                Toast.makeText(this, R.string.empty_not_allowed, Toast.LENGTH_SHORT).show();
                return;
            }

            dbmediator.addExercise(incomingName); // may fail since exercise is in db - it's ok
            dbmediator.logExercise(incomingName);

            exerciseTextView.setText("");

            //populate list view with renewed data
            exerciseLogAdapter.setIdxOfCurrent(exerciseLogAdapter.getCount()); //no need to decrement since item is not renewed in the list yet: count will be larger
            setsListDataLoader.renewTargetEx( incomingName );
            initiateListUpdate( Subject.EXERCISES, TriggerEvent.ADD );

            //we are trying to add reps and weight
        } else {

            if ( DEBUG_FLAG ) Log.v(APP_NAME, "WorkoutJournal :: onAddButtonPressed() Reps: " + repsEdit.getText() + " Weight: " + weightEdit.getText() + " Curr ex idx: " + exerciseLogAdapter.getIdxOfCurrent());
            String repString = repsEdit.getText().toString();
            String weiString = weightEdit.getText().toString();

            setsLogAdapter.setIdxOfCurrent(setsLogAdapter.getCount());  //no need to decrement since item is not renewed in the list yet: count will be larger

            if (repString.trim().length() == 0 || weiString.trim().length() == 0) {
                Toast.makeText(this, R.string.empty_not_allowed, Toast.LENGTH_SHORT).show(); // TODO: make a string resources for this toast
                return;
            }

            if ( DEBUG_FLAG ) Log.v(APP_NAME, "WorkoutJournal :: onAddButtonPressed(). current exercise: "+ exerciseLogAdapter.getIdxOfCurrent());

            String exerciseName = exerciseLogAdapter.getNameForCurrent();
            String exerciseLogId = exerciseLogAdapter.getIdForCurrent();

            if ( !prevExLogId.equals( exerciseLogId ) )
            {
                addSetIfSameDate = false;
            }
            prevExLogId = exerciseLogId;

            weiString = ( weiString.equals(",") ) ? "0" : weiString;
            if ( DBClass.EX_IN_PAST == dbmediator.insertSet( exerciseName, exerciseLogId, repString, weiString, addSetIfSameDate) )
            {
                addSetIfSameDate = true;
                Toast.makeText(this, R.string.press_again_to_add, Toast.LENGTH_SHORT).show();
            }
            else
            {
                addSetIfSameDate = false;
            }

            //refresh cursor
            initiateListUpdate(Subject.SETS, TriggerEvent.ADD);

        }

        //set note (possible only for exercise!)
    }

    /*
     *
     *
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if ( DEBUG_FLAG ) Log.v(APP_NAME, "WorkoutJournal :: onItemClick :: position: " + position + " id: "+id + " view ID: "+view.getId() );

        switch (view.getId()) {
            case R.id.exercise_entry_container:
                currSubj = Subject.EXERCISES;

                repsEdit.setText("");
                weightEdit.setText("");

                // obtain sets for this exercise
                // fetch new sets only if exercise entry changed

                if (exerciseLogAdapter.getIdxOfCurrent() == position) {
                    if ( DEBUG_FLAG ) Log.v(APP_NAME, "WorkoutJournal :: onItemClick :: same item clicked, nothing to do.");
                } else {
                    exerciseLogAdapter.setIdxOfCurrent(position);

                    showEditsForSubject( Subject.SETS );
                    moveToSelected( Subject.EXERCISES, false );

                    //empty hint box for set since we have chosen other exercise
                    setNoteTv.setHint(getString(R.string.workout_set_no_note_hint));
                    setNoteTv.setText("");

                    //need to update sets according to new item
                    setsListDataLoader.renewTargetEx( (Cursor) exerciseLogAdapter.getItem( exerciseLogAdapter.getIdxOfCurrent() ) );
                    initiateListUpdate( Subject.SETS, TriggerEvent.TRIG_BY_EX);

                    exerciseLogAdapter.notifyDataSetChanged();
                }
                break;

            case R.id.set_entry_container:
                currSubj = Subject.SETS;
                exerciseTextView.setText("");

                setsLogAdapter.setIdxOfCurrent(position);

                //same code for exs and sets. update note
                String noteSet =  setsLogAdapter.getNoteForCurrent();
                //if ( DEBUG_FLAG ) Log.v(APP_NAME, "WorkoutJournal :: onItemClick :: note : " + noteSet);
                if ( noteSet == null || noteSet.isEmpty() ) {
                    setNoteTv.setHint( getString(R.string.workout_set_no_note_hint) );
                    setNoteTv.setText( "" );
                } else {
                    setNoteTv.setText(noteSet);
                }

                // show required exercise for selected date
                //PROBLEM possibly set if is not set at that moment

                setsLogAdapter.notifyDataSetChanged();

                if ( syncListPositions( Subject.SETS ) ) {
                    exerciseLogAdapter.notifyDataSetChanged();
                    exercisesLv.setSelection( exerciseLogAdapter.getIdxOfCurrent() );

                }

                break;
        }
    }

    /*
	 * Case if reps list view is touched - change edit panel for reps.
	 * otherwise do nothing (we will return to exercise mode of panel only when appropriate button is pressed
	 *
	 */
    @Override
    public boolean onTouch(View view, MotionEvent event) {
        //switch lower pane edits depending on touch target ( exercise list view or sets list view)
        if (event.getAction() == MotionEvent.ACTION_UP) {

            switch (view.getId()) {
                case R.id.setsLv:
                    if (exerciseLogAdapter.getIdxOfCurrent() != -1) {
                        showEditsForSubject( Subject.SETS );
                    }
                    break;

                case R.id.exercisesLv:

                    break;
            }
        }

        return false;
    }

    /*
     * return false if new position is not set, true if set
     */
    public boolean syncListPositions( Subject baseSubject ) {
        if ( DEBUG_FLAG ) Log.v(APP_NAME, "WorkoutJournal :: syncListPositions :: started. base subject: " + baseSubject.toString() );

        WorkoutDataAdapter baseAdapter, targetAdapter;
        Integer sourceKeyFieldIdx, targetKeyFieldIdx;

        if ( baseSubject  == Subject.EXERCISES ) {
            baseAdapter = exerciseLogAdapter;
            targetAdapter = setsLogAdapter;

            targetKeyFieldIdx = setsLogAdapter.getCursor().getColumnIndex( DBClass.KEY_EX_LOG_ID );
            sourceKeyFieldIdx = exerciseLogAdapter.getCursor().getColumnIndex( DBClass.KEY_ID );

        } else if (baseSubject == Subject.SETS ) {
            baseAdapter = setsLogAdapter;
            targetAdapter = exerciseLogAdapter;

            targetKeyFieldIdx = exerciseLogAdapter.getCursor().getColumnIndex( DBClass.KEY_ID );
            sourceKeyFieldIdx = setsLogAdapter.getCursor().getColumnIndex( DBClass.KEY_EX_LOG_ID );

        } else {
            Log.e(APP_NAME, "WorkoutJournal :: syncListPositions :: unexpected subject: " + baseSubject.toString() );
            return false;
        }

        if ( baseAdapter.getIdxOfCurrent() == -1  ) {
            Log.d(APP_NAME, "WorkoutJournal :: syncListPositions :: doing nothing since current for one of the adapters is not set" );
            return false;
        }

       /* if ( DEBUG_FLAG ) Log.v(APP_NAME, "*** exercise cursor with current "+exerciseLogAdapter.getIdxOfCurrent() +" actual pos: "+exerciseLogAdapter.getCursor().getPosition());
        DatabaseUtils.dumpCursor(exerciseLogAdapter.getCursor());
        if ( DEBUG_FLAG ) Log.v(APP_NAME, "*** sets cursor with current "+setsLogAdapter.getIdxOfCurrent() +" actual pos: "+setsLogAdapter.getCursor().getPosition());
        DatabaseUtils.dumpCursor(setsLogAdapter.getCursor());
*/
        baseAdapter.getCursor().moveToPosition( baseAdapter.getIdxOfCurrent() );
        long sourceId = baseAdapter.getCursor().getLong( sourceKeyFieldIdx );

        int initialTargetPos = targetAdapter.getIdxOfCurrent();
        int targetPos = -1;

        Cursor trgtCs = targetAdapter.getCursor();
        trgtCs.moveToFirst();

        for ( ; !trgtCs.isAfterLast() ; trgtCs.moveToNext() ) {

            if ( DEBUG_FLAG ) Log.v(APP_NAME, "WorkoutJournal :: syncListPositions :: sourceId: "+sourceId+" targetId:"+trgtCs.getInt( targetKeyFieldIdx ) );

            //may be several appropriate items in a sequence
            if ( trgtCs.getInt( targetKeyFieldIdx ) == sourceId ){
                targetPos = trgtCs.getPosition();
                if ( DEBUG_FLAG ) Log.v(APP_NAME, "WorkoutJournal :: syncListPositions :: intermediate position of target: "+targetPos );
                continue;
            }

            //no need to iterate further since we already got target pos
            if ( targetPos != -1) {
                break;
            }
        }

        //move to new pos if found target or just restore initial
        if (targetPos != -1) {
            targetAdapter.setIdxOfCurrent( targetPos );

            Subject targetSubject = (baseSubject == Subject.EXERCISES) ? Subject.SETS : baseSubject;
            moveToSelected( targetSubject, true );

            if ( DEBUG_FLAG ) Log.v(APP_NAME, "WorkoutJournal :: syncListPositions :: resulting target found. SourceId: "+sourceId+" Target moved from "+initialTargetPos+ " to "+targetPos );
            return true;
        } else {
            trgtCs.moveToPosition( initialTargetPos );

            if ( DEBUG_FLAG ) Log.v(APP_NAME, "WorkoutJournal :: syncListPositions :: target not found ( sourceId: "+sourceId+")" );
            return false;
        }

    }

    /*
    * will change current adapter to appropriate one
     */
    public void onNotesTap(View v) {
        if ( DEBUG_FLAG ) Log.v(APP_NAME, "WorkoutJournal :: onNotesTap");
        String headText;

        String targetId;
        WorkoutDataAdapter subjAdapter;

        switch (v.getId()) {
            case R.id.exerciseNoteTv:
                subjAdapter = exerciseLogAdapter;

                targetId = exerciseLogAdapter.getNameForCurrent();
                headText = targetId;
                break;
            case R.id.setNoteTv:
                subjAdapter = setsLogAdapter;

                targetId =  setsLogAdapter.getIdForCurrent();
                headText = exerciseLogAdapter.getNameForCurrent() + "  " + setsLogAdapter.getNameForCurrent();
                break;
            default:
                return;
        }

        if ( subjAdapter.getCount() == 0 || subjAdapter.getIdxOfCurrent() == -1 ) {
            if ( DEBUG_FLAG ) Log.v(APP_NAME, "WorkoutJournal :: onNotesTap : doing nothing since no source for note exist");
            return;
        }

        String note =  subjAdapter.getNoteForCurrent();
        if ( DEBUG_FLAG ) Log.v(APP_NAME, "WorkoutJournal :: onNotesTap : subj: "+subjAdapter.getSubject().toString()+" current: "+subjAdapter.getIdxOfCurrent()+ " note: " + note);

        Intent dialogIntent = new Intent(this, NotesDialog.class);
        dialogIntent.putExtra("targetId", targetId);
        dialogIntent.putExtra("headText", headText);
        dialogIntent.putExtra("note", note);
        // id is exercise name or sets id

        startActivityForResult(dialogIntent, (subjAdapter.getSubject() == Subject.EXERCISES) ? 1 : 0);
    }

    /*
     * note add window closed
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if ( DEBUG_FLAG ) Log.v(APP_NAME, "WorkoutJournal :: onActivityResult");

        if (resultCode == RESULT_OK) {
            //currCursor.moveToPosition(currAdapter.getIdxOfCurrent());
            String note = data.getStringExtra("note");
            String targetId = data.getStringExtra("targetId");

            if ( DEBUG_FLAG ) Log.v(APP_NAME, "WorkoutJournal :: onActivityResult once got item");

            switch (requestCode) {

                case 1: //exercise
                    if ( DEBUG_FLAG ) Log.v(APP_NAME, "WorkoutJournal :: onActivityResult : gonna insert note \"" + note + "\" for ex  \"" + exerciseLogAdapter.getNameForCurrent()+"\"" );
                    dbmediator.insertExerciseNote( targetId, note);
                    exerciseNoteTv.setText(note);

                    initiateListUpdate( Subject.EXERCISES, TriggerEvent.NOTEADD );
                    break;

                case 0: //set

                    dbmediator.insertSetNote( targetId, note);
                    setNoteTv.setText(note);

                    initiateListUpdate(Subject.SETS, TriggerEvent.NOTEADD);
                    break;
            }

        }
    }

    public void onContextButtonPressed(View contextualActionButton) {
        //TODO pay attention to curr handling when no more sets/exercises left! will it work?
        if ( DEBUG_FLAG ) Log.v(APP_NAME, "WorkoutJournal :: onContextButtonPressed :: active subject: " + currSubj.toString() );

        WJContext cmcb;

        switch ( currSubj ) {
            case EXERCISES:
                cmcb = exercisesContextualMode;
                break;

            case SETS:
                cmcb = setsContextualMode;
                break;

            default:
                Log.e(APP_NAME, "WorkoutJournal :: onContextButtonPressed. Weird state met");
                return;
        }

        switch (contextualActionButton.getId()) {
            case R.id.ctx_copySelectedBtn:
                cmcb.copyButtonPressed();
                break;

            case R.id.ctx_deleteLogEntriesBtn:
                cmcb.onDeleteLogEntriesPressed();
                break;

            case R.id.ctx_cancelBtn:
                cmcb.onRestoreContextLookBtnPressed();
                break;

            case R.id.ctx_addEditedBtn:
                cmcb.onAddEditedBtnPressed();
                break;

            default:
                Log.e(APP_NAME, "WorkoutJournal :: onContextButtonPressed. handler for passed view is missing");
        }
    }

    /*
    @return 3 if no items left to operate on
            2 if idx of checked must be decremented.
            1
            0 if no extra actions required
     */
    /*public int adjustAfterExDeleted( int idxOfDeleted ) {
        if ( DEBUG_FLAG ) Log.v(APP_NAME, "WorkoutJournal :: adjustAfterExDeleted");
        int retCode = 0;

        // remember - these are values before adjustment - DB is already changed!
        int sumOfElements = exercisesLv.getCount();

        //if it was the only left element - need to report to handle checked items.
        if ( sumOfElements <= 1 ) {
            if ( DEBUG_FLAG ) Log.v(APP_NAME, "WorkoutJournal :: adjustAfterExDeleted detected deletion of very last item. Sum of items before deletion: "+sumOfElements+" deleted item idx: "+idxOfDeleted);
            exerciseLogAdapter.setIdxOfCurrent(-1);
            retCode = 3;
        }
        //if deleted item was last in a row - need to decrement current
        else if ( idxOfDeleted == sumOfElements-1 ) {
            if ( DEBUG_FLAG ) Log.v(APP_NAME, "WorkoutJournal :: adjustAfterExDeleted detected deletion of last in a row item.");

            exerciseLogAdapter.setIdxOfCurrent(idxOfDeleted - 1);
            retCode = 2;
        }
        else if ( idxOfDeleted == 0 ) {
            if ( DEBUG_FLAG ) Log.v(APP_NAME, "WorkoutJournal :: adjustAfterExDeleted detected deletion of first in a row item.");

            //selected is still the same. However, it's position may be changed now if one of prev entries was deleted or selected was last item in the list
            if ( idxOfDeleted < exerciseLogAdapter.getIdxOfCurrent() || exerciseLogAdapter.getIdxOfCurrent() == sumOfElements-1 ) {
                exerciseLogAdapter.setIdxOfCurrent(exerciseLogAdapter.getIdxOfCurrent() - 1);
            }

            retCode = 1;
        }

        //show renewed data for exercises
        initiateListUpdate( Subject.EXERCISES, TriggerEvent.DELETE );

        //make sure exercise edit is active
        showEditsForSubject( Subject.EXERCISES );

        // obtain sets for new exercise
        // fetch new sets only if exercise entry changed

        //empty hint box for set since we have chosen other exercise
        setNoteTv.setHint(getString(R.string.workout_set_no_note_hint));
        setNoteTv.setText("");

        //no sets if no exercises
        if ( retCode == 3 )
        {
            setsLv.setAdapter(null);
            return retCode;
        }

        //update sets list view accordingly
        initiateListUpdate( Subject.SETS, TriggerEvent.DELETE );

        if (setsLv.getCount() != 0) {
            syncListPositions( Subject.EXERCISES );
        }

        return retCode;
    }*/

    public void showEditsForSubject( Subject subj ) {

        switch ( subj ) {
            case EXERCISES:
                exerciseTextView.clearAnimation();
                exerciseTextView.setVisibility(View.VISIBLE);
                repsEdit.setVisibility(View.GONE);
                weightEdit.setVisibility(View.GONE);
                switchBtn.setImageResource(R.drawable.ic_custom_circledforward);
                break;

            case SETS:
                exerciseTextView.setVisibility(View.GONE);
                repsEdit.setVisibility(View.VISIBLE);
                weightEdit.setVisibility(View.VISIBLE);
                switchBtn.setImageResource(R.drawable.ic_custom_circledback);
                break;

            default:
                Log.e(APP_NAME, "showEditsForSubject :: met unexpected subject : "+subj );
        }

    }

    /*
     * Handler for cases when we dont need to update list, but only change selected to one was previosly set.
     */
    public void moveToSelected( Subject subj, boolean needToScroll ) {
        if ( DEBUG_FLAG ) Log.v(APP_NAME, "WorkoutJournal :: moveToSelected :: subject : "+subj.toString()+" need to scroll: "+needToScroll );

        WorkoutDataAdapter targetAdapter;
        ListView targetLv;

        switch ( subj ) {
            case EXERCISES:
                targetAdapter = exerciseLogAdapter;
                targetLv = exercisesLv;

                //empty sets note since new exercise was selected
                //but what if new setes adapter have note? TODO
                setNoteTv.setText("");
                setNoteTv.setHint( R.string.workout_set_no_note_hint );
                break;

            case SETS:
                targetAdapter = setsLogAdapter;
                targetLv = setsLv;
                break;

            default:
                Log.e(APP_NAME,"WorkoutJournal :: moveToSelected :: unknown target subject :: subj: "+subj);
                return;
        }

        updateNoteView( targetAdapter );

        if ( needToScroll ) {
            targetLv.setSelection( targetAdapter.getIdxOfCurrent() );
        }
    }

    /*
     * Will update note text view based on the info in cursor of adapter. Appropriate position must be set prior.
     */
    public void updateNoteView( WorkoutDataAdapter sourceAdapter ) {
        if ( DEBUG_FLAG ) Log.v(APP_NAME, "WorkoutJournal :: updateNoteView for "+sourceAdapter.getSubject().toString() );

        if ( sourceAdapter.getIdxOfCurrent() == -1 ) {
            return;
        }

        String newNoteHint;
        TextView targetTV;

        switch ( sourceAdapter.getSubject() ) {
            case EXERCISES:
                newNoteHint = getString(R.string.workout_exercise_newnote_hint);
                targetTV = exerciseNoteTv;
                break;

            case SETS:
                newNoteHint = getString(R.string.workout_set_newnote_hint);
                targetTV = setNoteTv;
                break;

            default:
                Log.e(APP_NAME, "WorkoutJournal :: updateNoteView :: unexpected subject of adapter" );
                return;
        }

        String noteString = sourceAdapter.getNoteForCurrent();
        if ( noteString == null || noteString.isEmpty()) {
            targetTV.setHint( newNoteHint );
            targetTV.setText("");
        } else {
            targetTV.setText( noteString );
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if ( DEBUG_FLAG ) Log.v(APP_NAME, "onCreateLoader :: Id " + id );

        if ( id == EXERCISES ) {
            return exListDataLoader = new ExerciseDataCursorLoader(this, dbmediator );
        } else {
            return setsListDataLoader = new SetDataCursorLoader(this, dbmediator, exerciseLogAdapter.getCursor() );
        }

    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if ( DEBUG_FLAG ) Log.v(APP_NAME, "WorkoutJournal :: onLoadFinished : id " + loader.getId() + " trigger: "+  ( ( loader.getId() == 0 ) ? exUpTrigger.toString() : setsUpTrigger.toString()) );

        if ( ( loader.getId() == EXERCISES && exUpTrigger == TriggerEvent.NONE ) || ( loader.getId() == SETS &&  setsUpTrigger == TriggerEvent.NONE ) ) {
            Log.e(APP_NAME, "WorkoutJournal :: onLoadFinished : cannot update since trigger event is not set for loader "+ loader.getId() );
            return;
        }

        int current = -1;
        switch ( loader.getId() ) {
            case EXERCISES:

                if ( exUpTrigger == TriggerEvent.INIT ) {

                    //on orientation change position my be not last before
                    if ( initExPos != -1) {
                        current = initExPos;
                        initExPos = -1;
                    } else {
                        current = exerciseLogAdapter.getCount() - 1;
                    }

                } else {
                    current = exerciseLogAdapter.getIdxOfCurrent();
                }

                exerciseLogAdapter.swapCursor( data );
                exerciseLogAdapter.setIdxOfCurrent( current ); //this will move new cursor to initial position

                // empty notes box for sets since we might lost focus from sets.
                if ( exUpTrigger != TriggerEvent.NOTEADD && exUpTrigger != TriggerEvent.EDIT ) {
                    setNoteTv.setText("");
                    setNoteTv.setHint(R.string.workout_set_no_note_hint);
                }

                // if add button clicked
                if ( exUpTrigger == TriggerEvent.ADD || exUpTrigger == TriggerEvent.EDIT || exUpTrigger == TriggerEvent.INIT ) {
                    moveToSelected(Subject.EXERCISES, true);
                }

                if ( exUpTrigger != TriggerEvent.NOTEADD && exUpTrigger != TriggerEvent.EDIT ) {
                    //set list behavior for add is the same as for ex click
                    setsUpTrigger = TriggerEvent.TRIG_BY_EX;
                    setsListDataLoader.renewTargetEx((Cursor) exerciseLogAdapter.getItem(exerciseLogAdapter.getIdxOfCurrent()));
                    getLoaderManager().getLoader(SETS).forceLoad();
                }

                if (exerciseLogAdapter.getCount() != 0) {
                    updateNoteView( exerciseLogAdapter );
                } else {
                    exerciseNoteTv.setText("");
                    exerciseNoteTv.setHint( R.string.workout_exercise_no_note_hint );
                }

                //if no exercises left - enforce ex adding toolbar
                if ( exerciseLogAdapter.getCount() == 0 ) {
                    showEditsForSubject( Subject.EXERCISES );
                }

                /*
                if ( exerciseLogAdapter.getIdxOfCurrent() != -1 ) {
                    setsListDataLoader.renewTargetEx((Cursor) exerciseLogAdapter.getItem(exerciseLogAdapter.getIdxOfCurrent()));
                }*/

                exUpTrigger = TriggerEvent.NONE;
                break;

            case SETS:

                //setsLv.setAdapter( setsLogAdapter = new WorkoutDataAdapter(this, data, WorkoutDataAdapter.Subject.SETS) );
                if ( setsLogAdapter == null ) {
                    setsLv.setAdapter( setsLogAdapter = new WorkoutDataAdapter(this, data, WorkoutDataAdapter.Subject.SETS) );
                } else {
                    setsLogAdapter.swapCursor( data );
                }

                if ( setsLogAdapter != null && ( setsUpTrigger == TriggerEvent.DELETE || setsUpTrigger == TriggerEvent.NOTEADD || setsUpTrigger == TriggerEvent.EDIT  ) )  {
                    current = setsLogAdapter.getIdxOfCurrent();
                } else if ( setsUpTrigger == TriggerEvent.INIT || setsUpTrigger == TriggerEvent.ADD ) {
                    current = setsLogAdapter.getCount() - 1;
                }

                setsLogAdapter.setIdxOfCurrent(current);

                if (  ( setsUpTrigger == TriggerEvent.TRIG_BY_EX || setsUpTrigger == TriggerEvent.DELETE ) && setsLv.getCount() != 0) {

                    //on orientation change position is known
                    if ( initSetPos != -1 ) {
                        setsLogAdapter.setIdxOfCurrent( initSetPos );
                        initSetPos = -1;
                    } else {
                        syncListPositions( Subject.EXERCISES );
                    }

                }

                if ( setsUpTrigger == TriggerEvent.ADD  || setsUpTrigger == TriggerEvent.INIT ) {
                    //move and update note
                    moveToSelected(Subject.SETS, true);
                } else {

                    //only update note if not an add event
                    if (setsLogAdapter.getCount() != 0) {
                        updateNoteView( setsLogAdapter );
                    }

                }

                setsUpTrigger = TriggerEvent.NONE;
                break;
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

}
