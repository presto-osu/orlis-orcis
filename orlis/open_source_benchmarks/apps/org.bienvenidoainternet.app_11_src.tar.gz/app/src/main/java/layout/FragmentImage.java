package layout;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import org.bienvenidoainternet.app.R;
import org.bienvenidoainternet.app.ViewerActivity;
import org.bienvenidoainternet.app.structure.BoardItemFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

/**
 *   BaiApp - Bienvenido a internet Android Application
 *   Copyright (C) 2016 Renard1911(https://github.com/Renard1911)
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FragmentImage.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FragmentImage#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentImage extends Fragment {
    private static final String ARG_BOARD_ITEM = "param1";

    public BoardItemFile boardItemFile;

    private OnFragmentInteractionListener mListener;
    private SubsamplingScaleImageView imageView;
    private GifImageView gifView;
    private static final String ARG_FILE_URL = "fileURL";
    private ProgressBar downloadBar;

    public FragmentImage() {
        // Required empty public constructor
    }

    public static FragmentImage newInstance(BoardItemFile boardItemFile) {
        FragmentImage fragment = new FragmentImage();
        Bundle args = new Bundle();
        args.putParcelable(ARG_BOARD_ITEM, boardItemFile);
//        args.putString(ARG_FILE_URL, file);
        fragment.setArguments(args);
        Log.v("FragmentImage", fragment.toString() + " new Fragment");
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
//            currentThread = getArguments().getParcelable(ARG_BOARD_ITEM);
            boardItemFile = getArguments().getParcelable(ARG_BOARD_ITEM);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_fragment_image, container, false);
        imageView = (SubsamplingScaleImageView) view.findViewById(R.id.imageView);
        gifView = (GifImageView) view.findViewById(R.id.gifView);
        imageView.setVisibility(View.GONE);
        gifView.setVisibility(View.GONE);
        RelativeLayout layoutOpenBrowser = (RelativeLayout) view.findViewById(R.id.layoutOpenBrowser);
        if (boardItemFile.file != null) {
            if (!boardItemFile.file.endsWith(".webm") && !boardItemFile.file.endsWith(".swf") && !boardItemFile.file.endsWith(".ogg") && !boardItemFile.file.endsWith(".opus")) {
                layoutOpenBrowser.setVisibility(View.GONE);
                downloadFile();
            }else{
                layoutOpenBrowser.setVisibility(View.VISIBLE);
            }
        }
        Button btnOpenBrowser = (Button) view.findViewById(R.id.btnLaunchBrowser);
        btnOpenBrowser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent in = new Intent(Intent.ACTION_VIEW, Uri.parse(boardItemFile.fileURL));
                in.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                v.getContext().startActivity(in);
            }
        });
        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void setDownloadBar(ProgressBar downloadBar) {
        this.downloadBar = downloadBar;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    private void downloadFile() {
        downloadBar = ((ViewerActivity)getActivity()).barDownload;
        downloadBar.setVisibility(View.VISIBLE);
        ContextWrapper cw = new ContextWrapper(getContext());
        File directory = cw.getDir("src", Context.MODE_PRIVATE);
        final File filePath = new File(directory, boardItemFile.boardDir + "_" + boardItemFile.file);
        if (filePath.exists()) {
            downloadBar.setVisibility(View.GONE);
            if (boardItemFile.file.endsWith(".gif")) {
                try {
                    GifDrawable gifFromFile = new GifDrawable(filePath);
                    gifView.setImageDrawable(gifFromFile);
                    gifView.setVisibility(View.VISIBLE);
                    imageView.setVisibility(View.GONE);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                imageView.setImage(ImageSource.uri(filePath.toURI().getPath()));
                imageView.setVisibility(View.VISIBLE);
                gifView.setVisibility(View.GONE);
            }
        }
        Ion.with(getContext())
                .load(boardItemFile.fileURL)
                .progressBar(downloadBar)
                .asInputStream()
                .setCallback(new FutureCallback<InputStream>() {
                    @Override
                    public void onCompleted(Exception e, InputStream result) {
                        downloadBar.setVisibility(View.GONE);
                        if (e != null) {
                            e.printStackTrace();
                        } else {
                            FileOutputStream fout;
                            try {
                                fout = new FileOutputStream(filePath);
                                final byte data[] = new byte[1024];
                                int count;
                                while ((count = result.read(data, 0, 1024)) != -1) {
                                    fout.write(data, 0, count);
                                }
                            } catch (Exception e1) {
                                e1.printStackTrace();
                            }
                            if (boardItemFile.file.endsWith(".gif")) {
                                try {
                                    GifDrawable gifFromFile = new GifDrawable(filePath);
                                    gifView.setImageDrawable(gifFromFile);
                                    gifView.setVisibility(View.VISIBLE);
                                    imageView.setVisibility(View.GONE);
                                } catch (Exception e2) {
                                    e2.printStackTrace();
                                }
                            } else {
                                imageView.setImage(ImageSource.uri(filePath.toURI().getPath()));
                                gifView.setVisibility(View.GONE);
                                imageView.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                });
    }
}
