package koeln.mop.elpeefpe;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import koeln.mop.elpeefpe.databinding.CharacterEditBinding;

/**
 * Created by Andreas Streichardt on 22.06.2016.
 */
public class CharacterEditFragment extends Fragment {
    private CharacterForm characterForm;
    private CharacterEditBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        CharacterEditBinding binding = CharacterEditBinding.inflate(inflater, container, false);
        binding.setCharacter(characterForm);
        return binding.getRoot();
    }

    public void setCharacterForm(CharacterForm characterForm) {
        this.characterForm = characterForm;
    }
}
