package de.karbach.tac.ui.fragments;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;

import de.karbach.tac.R;

/**
 * Created by Carsten on 12.11.2015.
 */
public class ImageViewFragment extends Fragment {

    /**
     * Argument name for the image path to display
     */
    public static final String IMAGEPATH = "de.karbach.tac.ui.fragments.ImageViewFragment.IMAGEPATH";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle bundle = getArguments();
        String imagepath = bundle.getString(IMAGEPATH);

        View result = inflater.inflate(R.layout.boardimage_viewer, container, false);

        //Show the image
        ImageView image = (ImageView) result.findViewById(R.id.board_image);

        if(imagepath == null){
            image.setImageResource(R.drawable.backside);
        }
        else {
            File imgFile = new File(imagepath);
            if (imgFile.exists()) {
                Bitmap boardBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                image.setImageBitmap(boardBitmap);
            } else {
                image.setBackgroundResource(R.drawable.board);
            }
        }

        return result;
    }

}
