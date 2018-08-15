package de.phoenixstudios.pc_dimmer;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class Setup extends Fragment implements View.OnClickListener{
    private CallbackToMain mCallbackToMain;

    public Setup() {
        // Required empty public constructor
    }

    public static Setup newInstance() {
        return new Setup();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_setup, container, false);

        view.findViewById(R.id.connectBtn).setOnClickListener(this);
        view.findViewById(R.id.disconnectBtn).setOnClickListener(this);
        view.findViewById(R.id.syncBtn).setOnClickListener(this);
        view.findViewById(R.id.loadpresetbtn).setOnClickListener(this);
        view.findViewById(R.id.savepresetbtn).setOnClickListener(this);
        view.findViewById(R.id.deletepresetbtn).setOnClickListener(this);
        view.findViewById(R.id.savesettingsbtn).setOnClickListener(this);
        view.findViewById(R.id.fadetimeEdit).setOnClickListener(this);

        return view;
    }

    public void onViewCreated(View v, Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        mCallbackToMain.SetupCallbackToMain(R.layout.fragment_setup);
    }

    @Override
    public void onClick(View v) {
        mCallbackToMain.SetupCallbackToMain(v.getId());
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

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        Main.fragment_setup_isvisible = isVisibleToUser;
    }

    public interface CallbackToMain {
        void SetupCallbackToMain(int Cmd); // call self-defined function in main-program
    }
}
