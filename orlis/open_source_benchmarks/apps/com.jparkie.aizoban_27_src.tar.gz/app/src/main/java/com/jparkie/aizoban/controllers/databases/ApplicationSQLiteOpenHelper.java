package com.jparkie.aizoban.controllers.databases;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.jparkie.aizoban.AizobanApplication;
import com.jparkie.aizoban.models.Chapter;
import com.jparkie.aizoban.models.databases.FavouriteManga;
import com.jparkie.aizoban.models.databases.RecentChapter;
import com.jparkie.aizoban.models.downloads.DownloadChapter;
import com.jparkie.aizoban.models.downloads.DownloadManga;
import com.jparkie.aizoban.models.downloads.DownloadPage;

import nl.qbusict.cupboard.Cupboard;
import nl.qbusict.cupboard.CupboardBuilder;

public class ApplicationSQLiteOpenHelper extends SQLiteOpenHelper {
    private static ApplicationSQLiteOpenHelper sInstance;

    public ApplicationSQLiteOpenHelper(Context context) {
        super(context, ApplicationContract.DATABASE_NAME, null, ApplicationContract.DATABASE_VERSION);
    }

    public static synchronized ApplicationSQLiteOpenHelper getInstance() {
        if (sInstance == null) {
            sInstance = new ApplicationSQLiteOpenHelper(AizobanApplication.getInstance());
        }

        return sInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Cupboard applicationCupboard = constructCustomCupboard();
        applicationCupboard.withDatabase(db).createTables();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Cupboard applicationCupboard = constructCustomCupboard();
        applicationCupboard.withDatabase(db).upgradeTables();
    }

    private Cupboard constructCustomCupboard() {
        Cupboard customCupboard = new CupboardBuilder().build();
        customCupboard.register(Chapter.class);
        customCupboard.register(FavouriteManga.class);
        customCupboard.register(RecentChapter.class);
        customCupboard.register(DownloadManga.class);
        customCupboard.register(DownloadChapter.class);
        customCupboard.register(DownloadPage.class);

        return customCupboard;
    }
}
