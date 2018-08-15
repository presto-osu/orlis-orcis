package de.karbach.tac.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Debug;
import android.os.Environment;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.karbach.tac.R;
import de.karbach.tac.core.BoardData;
import de.karbach.tac.core.BoardViewData;
import de.karbach.tac.core.Card;
import de.karbach.tac.core.CardStack;

/**
 * Run export of all moves in background task.
 * Generates a list of filenames, where the exported bitmaps as files are stored.
 */
public class ExportMovesTask extends AsyncTask<Void,Integer,List<String>> {

    private BoardData cdata;
    private BoardViewData cviewdata;
    private Context context;
    private TaskFinishedCallback callback;
    private ProgressBar progressbar;

    private Bitmap backsideBMP;

    private static int lastProgress = 0;
    private static ExportMovesTask instance;

    /**
     *
     * @return the singleton instance of this task or null if none was created yet
     */
    public static ExportMovesTask getInstance(){
        return instance;
    }

    /**
     * Either update the existing task or create a new one.
     * @param cdata copied board data used during drawing
     * @param cviewdata copied view data used during drawing
     * @param context the context to access files with and start intents
     * @param callback the calling instance, which is informed about the finish of the task
     * @param progressbar the view to update with progress information
     * @return instance of the task
     */
    public static ExportMovesTask createInstance(BoardData cdata, BoardViewData cviewdata, Context context, TaskFinishedCallback callback, ProgressBar progressbar){
        ExportMovesTask result = getInstance();
        if(result != null){
            result.progressbar = progressbar;
            if(result.progressbar != null) {
                result.progressbar.setProgress(lastProgress);
            }
            result.callback = callback;
            return result;
        }
        instance = new ExportMovesTask(cdata,cviewdata,context,callback,progressbar);
        return instance;
    }

    /**
     * Interface to a callback instance (e.g. BoardControl) to inform it as
     * soon as the task is completed.
     */
    public static interface TaskFinishedCallback{
        /**
         * This function is called from the onPostExecute method to tell the caller,
         * that the export task is finished.
         */
        public void taskIsFinished();
    }

    /**
     * Init the task.
     * @param cdata copied board data used during drawing
     * @param cviewdata copied view data used during drawing
     * @param context the context to access files with and start intents
     * @param callback the calling instance, which is informed about the finish of the task
     * @param progressbar the view to update with progress information
     */
    private ExportMovesTask(BoardData cdata, BoardViewData cviewdata, Context context, TaskFinishedCallback callback, ProgressBar progressbar){
        this.cdata = cdata;
        this.cviewdata = cviewdata;
        this.context = context;
        this.callback = callback;
        this.progressbar = progressbar;

        backsideBMP = BitmapFactory.decodeResource(context.getResources(), de.karbach.tac.R.drawable.backside);
    }

    /**
     * Sets the text size for a Paint object so a given string of text will be a
     * given width.
     *
     * @param paint
     *            the Paint to set the text size for
     * @param desiredWidth
     *            the desired width
     * @param text
     *            the text that should be that width
     */
    private static void setTextSizeForWidth(Paint paint, float desiredWidth,
                                            String text) {

        // Pick a reasonably large value for the test. Larger values produce
        // more accurate results, but may cause problems with hardware
        // acceleration. But there are workarounds for that, too; refer to
        // http://stackoverflow.com/questions/6253528/font-size-too-large-to-fit-in-cache
        final float testTextSize = 48f;

        // Get the bounds of the text, using our testTextSize.
        paint.setTextSize(testTextSize);
        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);

        // Calculate the desired size as a proportion of our testTextSize.
        float desiredTextSize = testTextSize * desiredWidth / bounds.width();

        // Set the paint for that size.
        paint.setTextSize(desiredTextSize);
    }

    /**
     * Draw the bar summarizing the current move
     * @param xstart left of move bar
     * @param ystart top of movebar
     * @param moveBarHeight height of the bar
     * @param textPadding padding for texts and ball images
     * @param paint paint object for draw calls
     * @param pos id of move, 1 stands for first card played
     * @param movePos absolute position of moves in the entire export
     * @param moveTextWidth width of text requested for the move id
     * @param cardWidth width of card rendering
     * @param cardHeight height of card
     * @param drawboard the board needed to get ball images
     * @param canvas the canvas to draw on
     */
    protected void drawMoveBar(int xstart, int ystart, int moveBarHeight, int textPadding, Paint paint, int pos, int movePos, int moveTextWidth, int cardWidth, int cardHeight, BoardWithCards drawboard, Canvas canvas){
        int moveystart = ystart - moveBarHeight + textPadding;
        //Draw move information
        if(movePos > 0) {
            setTextSizeForWidth(paint, moveTextWidth, "999");
            CardStack stack = cdata.getPlayedCards();
            int moveId = stack.getTotalSize()-stack.getSize()+movePos;
            //Draw a bar holding all information about the current move
            canvas.drawText(String.valueOf(moveId), xstart + textPadding, moveystart+moveBarHeight/2, paint);

            int cardstart = xstart+moveTextWidth + 3 * textPadding;
            List<Card> cards = cdata.getPlayedCards().getCards();
            if (cards != null && cards.size() > 0) {
                Card currentCard = cards.get(cards.size() - 1);
                boolean drawText = false;
                if(currentCard.getDrawableId() == R.drawable.backside){
                    drawText = true;
                }
                currentCard.draw(context, canvas, cardWidth, cardHeight, cardstart, moveystart, 0, drawText);
                //Draw ball bitmaps:
                int[] ballIds = currentCard.getInvolvedBallIDs();
                if(ballIds == null){
                    ballIds = new int[]{currentCard.getPlayedById()*4};
                }
                Bitmap bm = null;
                int ball1X = xstart+moveTextWidth*3;
                int ball2X = xstart+moveTextWidth*4;
                int ballWidth = moveTextWidth-2*textPadding;
                int ballTop = moveystart;
                if(moveBarHeight-2*textPadding < ballWidth){
                    ballWidth = moveTextWidth-2*textPadding;
                }
                if (ballIds.length >= 1) {
                    bm = drawboard.getBallbitmapForId(ballIds[0]);
                }
                if (bm == null) {
                    bm = drawboard.getBallbitmapForId(R.drawable.grey);
                }

                if (ballIds.length >= 2) {
                    Bitmap bm2 = drawboard.getBallbitmapForId(ballIds[1]);
                    Rect src = new Rect(0,0, bm2.getWidth()-1, bm2.getHeight()-1);
                    Rect dst = new Rect(ball2X, ballTop, ball2X+ballWidth-1, ballTop+ballWidth-1);
                    //noinspection ConstantConditions
                    if (bm2 != null) {//This might be null, but is not detected by inspector, if getBallbitmapForId returns null
                        canvas.drawBitmap(bm2, src, dst, paint);
                    }
                }
                if (bm != null) {
                    Rect src = new Rect(0,0, bm.getWidth()-1, bm.getHeight()-1);
                    Rect dst = new Rect(ball1X, ballTop, ball1X+ballWidth-1, ballTop+ballWidth-1);
                    canvas.drawBitmap(bm, src, dst, paint);
                }
            }
        }//End of draw move information
    }

    /**
     * Parameters for exported image
     */
    private static final int imagesPerRow = 4;
    private static int moveBarHeight = 80;
    private static int boardWidth = 300;
    private static int textPadding = boardWidth/100;
    private static int boardHeight = 300;

    /**
     * Get the size in bytes for a bitmap, which would be generated by calling
     * generateBitmap.
     * @param beginStep first step to export
     * @param lastStep last step to export
     * @return size of bitmap in bytes
     */
    protected long getExpectedSizeOfBitmap(int beginStep, int lastStep){
        int steps = lastStep-beginStep+1;

        int rows = steps/imagesPerRow;
        if(steps%imagesPerRow != 0){
            rows++;
        }

        long expectedSize = imagesPerRow*boardWidth*rows*(boardHeight+moveBarHeight)*4;//Size of the generated bitmap in bytes
        return expectedSize;
    }

    /**
     * Draw one set of exported bitmaps.
     * @param beginStep first step to export
     * @param lastStep last step to export
     * @param drawboard the board used for draw calls
     * @param filename the filename to store the image at
     * @param publishProgress if true, publish the progress according to the total number of images to draw for an entire export
     * @return the name of the image file
     */
    protected String generateBitmap(int beginStep, int lastStep, BoardWithCards drawboard, String filename, boolean publishProgress){
        int steps = lastStep-beginStep+1;

        int rows = steps/imagesPerRow;
        if(steps%imagesPerRow != 0){
            rows++;
        }
        int resultBitmapWidth = imagesPerRow*boardWidth;
        if(steps < imagesPerRow){
            resultBitmapWidth = steps*boardWidth;
        }
        Bitmap result = Bitmap.createBitmap( resultBitmapWidth, rows*(boardHeight+moveBarHeight), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(result);
        Paint paint = new Paint();
        paint.setColor(Color.LTGRAY);
        int moveTextWidth = boardWidth/5;

        int cardHeight = moveBarHeight-2*textPadding;
        int cardWidth = (int)Math.round( (double)backsideBMP.getWidth() / (double)backsideBMP.getHeight() * cardHeight );

        int pos = 0;//Relative position in this bitmap
        int movePos = beginStep;//Absolute position in the entire export
        boolean moreToPaint = true;

        while(moreToPaint && movePos<=lastStep){

            int xpos = pos%imagesPerRow;
            int ypos = pos/imagesPerRow;

            int xstart = boardWidth*xpos;
            int ystart = (boardHeight+moveBarHeight)*ypos+moveBarHeight;

            Bitmap bitmap = drawboard.generateBitmapFromView(boardWidth, boardHeight);

            canvas.drawBitmap(bitmap, xstart, ystart, paint);
            bitmap.recycle();
            drawMoveBar(xstart, ystart, moveBarHeight, textPadding, paint, pos, movePos, moveTextWidth,
                    cardWidth, cardHeight, drawboard, canvas);

            if(cdata.canGoForward()) {
                cdata.goForward();
                moreToPaint = true;
            }
            else{
                moreToPaint = false;
            }
            if(publishProgress){
                publishProgress(movePos*100/cdata.getCurrentHistorySize());
            }
            pos++;
            movePos++;

            if(isCancelled()){
                break;
            }
        }

        this.saveBitmapToFile(result, filename);
        result.recycle();

        return filename;
    }

    /**
     *
     * @return maximum number of bytes available for bitmap generation
     */
    protected static long getAvailableMemoryForBitmapCreation(){
        return Runtime.getRuntime().maxMemory()-Runtime.getRuntime().totalMemory();
    }

    /**
     * Get the biggest number of moves, which can be placed into a single bitmap in the export.
     * This needs to be adjusted to the remaining memory size.
     *
     * @return the number of moves exported in one bitmap
     */
    private int getMovesPerPartOptimizedForMemory(){
        int movesPerPart = 32;

        long remainingMemory = getAvailableMemoryForBitmapCreation();
        long expectedImageSize = getExpectedSizeOfBitmap(0,movesPerPart-1);

        long allowedMemoryUsage = remainingMemory/2;//To definitely avoid out of memory exceptions, cut the remaining heap size by 2

        while(movesPerPart > 1 && expectedImageSize > allowedMemoryUsage){
            movesPerPart = movesPerPart/2;
            expectedImageSize = getExpectedSizeOfBitmap(0,movesPerPart-1);
        }

        return movesPerPart;
    }

    @Override
    protected List<String> doInBackground(Void... params) {
        BoardWithCards drawboard = new BoardWithCards(context);

        publishProgress(0);

        while(cdata.canGoBack()){
            cdata.goBack();
        }

        drawboard.setData(cdata, cviewdata);
        drawboard.setCardStack(cdata.getPlayedCards());
        drawboard.setAnimateCards(false);

        int steps = cdata.getCurrentHistorySize();

        int exportID = getNextExportID(context);
        int movesPerPart = getMovesPerPartOptimizedForMemory();

        if(movesPerPart == 0){
            return new ArrayList<String>();
        }

        int parts = steps/movesPerPart;
        if(steps%movesPerPart != 0){
            parts++;
        }

        List<String> result = new ArrayList<String>();

        File picDir = getPictureDirectory(context);

        for(int i=0; i<parts; i++){
            int beginStep = i*movesPerPart;
            int lastStep = (i+1)*movesPerPart-1;
            if(lastStep >= steps){
                lastStep = steps-1;
            }
            String filename = getFilenameFor(getTodayFormatted(), exportID, i+1);
            generateBitmap(beginStep, lastStep, drawboard, filename, true);

            File file = new File(picDir, filename);

            result.add(file.getAbsolutePath());
        }

        publishProgress(100);

        return result;
    }

    /**
     * Prefix for image files, which are generated here
     */
    public static final String filePrefix = "MoTAC_export";

    /**
     *
     * @param context context to access files/directories
     * @return the directory, where pictures are stored
     */
    public static File getPictureDirectory(Context context){
        return context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
    }

    /**
     *
     * @param absolutePath if true, absolute file paths are returned, otherwise only the file names
     *
     * @return list of exported images, which were already stored by this task before
     */
    public static List<String> getStoredImages(Context context, boolean absolutePath){
        File directory = getPictureDirectory(context);
        if(!directory.exists() || !directory.isDirectory() ){
            return null;
        }

        List<String> result = new ArrayList<String>();
        File[] files = directory.listFiles();
        for(File f: files){
            if(! f.isFile()){
                continue;
            }
            String name = f.getName();
            if(name.startsWith(filePrefix)){
                if(! absolutePath){
                    result.add(name);
                }
                else{
                    result.add(f.getAbsolutePath());
                }
            }
        }

        return result;
    }

    /**
     *
     * @return formatted date string for today's date
     */
    public static String getTodayFormatted(){
        return new SimpleDateFormat("yyyy_MM_dd", Locale.US).format(new Date());
    }

    /**
     * Generate the bitmap filename for an export id and the part position of the bitmap.
     * One export can generate multiple parts.
     *
     * @param id the id of the export on this day
     * @param part the part of the export
     * @return the filename of the bitmap to use
     */
    public static String getFilenameFor(String date, int id, int part){
        return filePrefix+"_"+date+"_"+id+"_"+part+".png";
    }

    /**
     * Get the number of parts for an export, of which the given file is part of.
     *
     * @param filename the full absolute path to the image
     * @param context context to traverse picture directory
     * @return the number of parts found for that game id at that date
     */
    public static String getPartcountForFilename(String filename, Context context){
        String date = getDateFromFilename(filename, false);
        String gameid = getGameIDFromFilename(filename);

        File directory = getPictureDirectory(context);
        if(!directory.exists() || !directory.isDirectory() ){
            return "";
        }

        File[] files = directory.listFiles();
        int count = 0;

        String prefix = filePrefix+"_"+date+"_"+gameid+"_";
        for(File f: files){
            if(f.getName().startsWith(prefix)){
                count++;
            }
        }

        return String.valueOf(count);
    }

    /**
     * Get the date part from an image filename
     * @param filename the full absolute path to the image
     * @param germanFormat use the German format instead of file format
     * @return the data part nicely formatted
     */
    public static String getDateFromFilename(String filename, boolean germanFormat){
        File f = new File(filename);
        String name = f.getName();

        String[] parts = name.split("_");

        if(parts.length >= 5 ){
            String year = parts[2];
            String month = parts[3];
            String day = parts[4];

            if(germanFormat) {
                return day + "." + month + "." + year;
            }
            else{
                return year+"_"+month+"_"+day;
            }
        }
        else{
            return "";
        }
    }

    /**
     * Get the game id from an image filename
     * @param filename the full absolute path to the image
     * @return the game id
     */
    public static String getGameIDFromFilename(String filename){
        File f = new File(filename);
        String name = f.getName();

        String[] parts = name.split("_");

        if(parts.length >= 6 ){
           return parts[5];
        }
        else{
            return "";
        }
    }

    /**
     * Get the part of the export from an image filename
     * @param filename the full absolute path to the image
     * @return the part of export
     */
    public static String getPartIDFromFilename(String filename){
        File f = new File(filename);
        String name = f.getName();

        String[] parts = name.split("_");

        if(parts.length >= 7 ){
            String[] partAndEnding = parts[6].split("\\.");
            if(partAndEnding.length >= 1) {
                return partAndEnding[0];
            }
            else{
                return "";
            }
        }
        else{
            return "";
        }
    }

    /**
     * Get the next export ID to use
     * @param context for accessing files
     * @return the id for the next export to use
     */
    public static int getNextExportID(Context context){
        List<String> images = getStoredImages(context, false);
        String prefixToday = filePrefix+"_"+getTodayFormatted();
        int maxid = 0;
        if(images != null) {
            for (String filename : images) {
                if (filename.startsWith(prefixToday)) {
                    String[] parts = filename.split("_");
                    if (parts.length < 6) {
                        continue;
                    }
                    String id = parts[5];
                    int idvalue = Integer.valueOf(id);
                    if (idvalue > maxid) {
                        maxid = idvalue;
                    }
                }
            }
        }

        return maxid+1;
    }

    /**
     * Store a bitmap to file.
     *
     * @param bitmap the bitmap to store
     * @param filename the filename (only filename no folder) to store the bitmap at
     * @return true on success, false, if an error occurred
     */
    protected boolean saveBitmapToFile(Bitmap bitmap, String filename){
        boolean mExternalStorageWriteable;
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            mExternalStorageWriteable = true;
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            // We can only read the media
            mExternalStorageWriteable = false;
        } else {
            // Something else is wrong. It may be one of many other states, but all we need
            //  to know is we can neither read nor write
            mExternalStorageWriteable = false;
        }

        if(! mExternalStorageWriteable){
            Toast.makeText(context, "Sorry, could not store the image.", Toast.LENGTH_LONG).show();
            return false;
        }

        File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), filename);
        filename = file.getAbsolutePath();

        FileOutputStream out = null;
        try {
            out = new FileOutputStream(filename);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Sorry, could not store the image.", Toast.LENGTH_LONG).show();
            return false;
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return true;
    }

    @Override
    protected void onPostExecute(List<String> files){
        instance = null;

        ArrayList<String> arg = new ArrayList<String>(files);

        Intent intent = new Intent(context, ExportedImagesActivity.class);
        intent.putStringArrayListExtra(ExportedImagesActivity.FILE_LIST, arg);
        context.startActivity(intent);

        if(callback != null) {
            callback.taskIsFinished();
        }
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        if(values == null || values.length == 0 || progressbar == null){
            return;
        }
        int value = values[0];
        lastProgress = value;
        progressbar.setProgress(value);
    }
}
