package de.karbach.tac.ui.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import de.karbach.tac.R;
import de.karbach.tac.core.BoardData;
import de.karbach.tac.core.Move;
import de.karbach.tac.ui.ColorToBallImage;
import de.karbach.tac.ui.ExportMovesTask;

/**
 * Shows a list of images, which were exported from the game.
 *
 */
public class ExportedImagesFragment extends ListFragment{

    /**
     * The data model, which has to be shown
     */
    private List<String> files;

    /**
     * Adapter for showing moves
     */
    private class ExportImageAdapter extends ArrayAdapter<String> {

        public ExportImageAdapter(List<String> files) {
            super(getActivity(), 0, files);


        }

        /**
         * Get a thumbnail for a picture.
         * @param imagepath path to the image, for which thumbnail is required
         * @param thumbnailSize size in pixels of the target thumbnail
         * @return the generated thumbnail image
         */
        Bitmap getPreview(String imagepath, int thumbnailSize) {
            File image = new File(imagepath);

            BitmapFactory.Options bounds = new BitmapFactory.Options();
            bounds.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(image.getPath(), bounds);
            if ((bounds.outWidth == -1) || (bounds.outHeight == -1))
                return null;

            int originalSize = (bounds.outHeight > bounds.outWidth) ? bounds.outHeight
                    : bounds.outWidth;

            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inSampleSize = originalSize / thumbnailSize;
            return BitmapFactory.decodeFile(image.getPath(), opts);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater().inflate(R.layout.image_item, parent, false);
            }

            String filepath = getItem(position);

            ImageView thumbnailView = (ImageView) convertView.findViewById(R.id.image_preview);

            Bitmap resized = getPreview(filepath, 100);
            thumbnailView.setImageBitmap(resized);

            TextView dateText = (TextView) convertView.findViewById(R.id.text_date);
            TextView partText = (TextView) convertView.findViewById(R.id.text_part);
            TextView gameIDText = (TextView) convertView.findViewById(R.id.text_id);

            dateText.setText(ExportMovesTask.getDateFromFilename(filepath, true));
            partText.setText( String.format( getString(R.string.exportparttemplate),
                    ExportMovesTask.getPartIDFromFilename(filepath),
                    ExportMovesTask.getPartcountForFilename(filepath, getActivity()) ) );
            gameIDText.setText(ExportMovesTask.getGameIDFromFilename(filepath));

            return convertView;
        }
    }

    public ExportedImagesFragment(){
        setRetainInstance(true);
        files = new ArrayList<String>();
    }

    /**
     * Set the paths to the files to display in this fragment
     * @param files string list, where each entry is the absolute path to the file containing the image
     */
    public void setFiles(List<String> files){
        this.files = files;
    }

    @Override
    public void onCreate(Bundle savedInstance){
        super.onCreate(savedInstance);

        setHasOptionsMenu(true);
        setListAdapter(new ExportImageAdapter(files));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View result = super.onCreateView(inflater, container, savedInstanceState);
        if(result == null){
            return null;
        }
        ListView listview = (ListView) result.findViewById(android.R.id.list);
        registerForContextMenu(listview);

        return result;
    }

    /**
     * Delete an image at a certain position in the list of image files.
     * @param position the position for the file to delete
     */
    protected void deleteImage(int position){
       if(position < 0 || position >= files.size()){
           return;
       }

        File file = new File(files.get(position));
        boolean result = file.delete();
        if(result) {
            files.remove(position);

            ExportImageAdapter adapter = (ExportImageAdapter) getListAdapter();
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        String file = files.get(position);
        File f = new File(file);
        Intent intent = new Intent();
        intent.setAction(android.content.Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(f), "image/png");
        getActivity().startActivity(intent);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        getActivity().getMenuInflater().inflate(R.menu.menu_exportedimages, menu);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.actionmenu_exportedimages, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.menu_exportedimages_delete_all){

            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which){
                        case DialogInterface.BUTTON_POSITIVE:
                            while(files.size() > 0){
                                int sizeBefore = files.size();
                                deleteImage(0);
                                int sizeAfter = files.size();
                                if(sizeBefore == sizeAfter){
                                    break;
                                }
                            }
                            break;

                        case DialogInterface.BUTTON_NEGATIVE:
                            //No button clicked
                            break;
                    }
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(getString(R.string.reallydelete)).setPositiveButton(getString(R.string.yes), dialogClickListener)
                    .setNegativeButton(getString(R.string.no), dialogClickListener).show();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        int position = info.position;

        if(item.getItemId() == R.id.menu_exportedimages_delete){
            deleteImage(position);
        }

        return super.onContextItemSelected(item);
    }
}
