package im.r_c.android.jigsaw.fragment;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import im.r_c.android.jigsaw.R;

/**
 * Jigsaw
 * Created by richard on 16/5/15.
 */
public class WinFragment extends Fragment {
    public static WinFragment newInstance(Bitmap bitmap) {
        Bundle args = new Bundle();
        args.putParcelable("image", bitmap);
        WinFragment fragment = new WinFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public WinFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_win, container, false);
        ImageView iv = (ImageView) view.findViewById(R.id.iv_image);
        iv.setImageBitmap((Bitmap) getArguments().getParcelable("image"));
        return view;
    }
}
