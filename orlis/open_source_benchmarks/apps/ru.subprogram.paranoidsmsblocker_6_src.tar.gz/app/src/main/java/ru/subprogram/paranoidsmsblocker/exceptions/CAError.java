package ru.subprogram.paranoidsmsblocker.exceptions;

public class CAError {
	public static final int NO_ERROR = 0;
	public static final int GENERAL_EXCEPTION_ERROR = 1;

	public static final int DB_ENGINE_GENERAL_ERROR = 1000;
	public static final int DB_ENGINE_OPENORCREATE_NO_FILENAME = DB_ENGINE_GENERAL_ERROR + 1;
	public static final int DB_ENGINE_OPENORCREATE_FAILED_TO_CREATE_FOLDER = DB_ENGINE_GENERAL_ERROR + 2;
	public static final int DB_ENGINE_OPENORCREATE_FAILED_TO_OPENORCREATE = DB_ENGINE_GENERAL_ERROR + 3;
	public static final int DB_ENGINE_WRONG_DB_VERSION = DB_ENGINE_GENERAL_ERROR + 4;
	public static final int DB_ENGINE_SQL_ERROR = DB_ENGINE_GENERAL_ERROR + 5;
}
