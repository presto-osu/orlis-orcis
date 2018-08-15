package org.ligi.scr;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.ligi.axt.AXT;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;

import info.metadude.java.library.halfnarp.model.TalkIds;

public class PersistentTalkIds extends TalkIds {

    private final Context context;

    public PersistentTalkIds(Context context) {
        this.context = context;
    }

    public void save() {

        final Gson gson = new Gson();

        final File dataFile = getDataFile();

        try {
            if (dataFile.exists()) {
                dataFile.delete();
            }

            dataFile.createNewFile();

        } catch (IOException e) {
            e.printStackTrace();
        }


        AXT.at(dataFile).writeString(gson.toJson(App.talkIds.getTalkIds()));
    }

    public void load() {
        if (getDataFile().exists()) {
            try {
                HashSet<Integer> hashSet = new Gson().fromJson(AXT.at(getDataFile()).readToString(), new TypeToken<HashSet<Integer>>() {}.getType());
                App.talkIds.add(hashSet);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private File getDataFile() {
        return new File(context.getFilesDir(), "talkids");
    }
}
