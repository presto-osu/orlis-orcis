package de.phoenixstudios.pc_dimmer;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class Scenes extends Fragment implements View.OnClickListener{
    private CallbackToMain mCallbackToMain;

    public Scenes() {
        // Required empty public constructor
    }

    public static Scenes newInstance() {
        return new Scenes();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_scene, container, false);
    }

    public void onViewCreated(View v, Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        // Fill ListView with Scenes
        mCallbackToMain.ScenesCallbackToMain(R.layout.fragment_scene);
    }

    @Override
    public void onClick(View v) {
        mCallbackToMain.ScenesCallbackToMain(v.getId());
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof CallbackToMain) {
            mCallbackToMain = (CallbackToMain) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbackToMain = null;
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
    public interface CallbackToMain {
        void ScenesCallbackToMain(int Cmd); // call self-defined function in main-program
    }
}
