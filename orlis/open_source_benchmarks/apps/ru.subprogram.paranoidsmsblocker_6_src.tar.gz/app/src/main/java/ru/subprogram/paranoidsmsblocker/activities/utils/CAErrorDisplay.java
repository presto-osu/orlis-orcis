package ru.subprogram.paranoidsmsblocker.activities.utils;

import android.content.Context;
import android.widget.Toast;
import ru.subprogram.paranoidsmsblocker.R;
import ru.subprogram.paranoidsmsblocker.exceptions.CAError;
import ru.subprogram.paranoidsmsblocker.exceptions.CAException;

public class CAErrorDisplay {

	public static void showError(Context context, Exception e) {
		showError(context, CAError.GENERAL_EXCEPTION_ERROR);
	}

	public static void showError(Context context, CAException e) {
		showError(context, e.getErrorCode());
	}

	public static void showError(Context context, int error_code) {
		if(error_code==CAError.NO_ERROR)
			return;

		showText(context, getErrorMessage(context, error_code));
	}

	public static void showText(Context context, int resId) {
		showText(context, context.getString(resId));
	}

	public static void showText(Context context, String text) {
		Toast.makeText(context,
			text,
			Toast.LENGTH_LONG).show();
	}

	private static String getErrorMessage(Context context, int error_code) {
		switch (error_code) {
			case CAError.DB_ENGINE_OPENORCREATE_FAILED_TO_CREATE_FOLDER:
				return context.getString(R.string.failed_to_init_db_error, error_code);
			case CAError.DB_ENGINE_OPENORCREATE_FAILED_TO_OPENORCREATE:
				return context.getString(R.string.failed_to_init_db_error, error_code);
			case CAError.DB_ENGINE_WRONG_DB_VERSION:
				return context.getString(R.string.db_corrupted_error);
			default:
				return context.getString(R.string.unknown_error, error_code);
		}
	}


}
