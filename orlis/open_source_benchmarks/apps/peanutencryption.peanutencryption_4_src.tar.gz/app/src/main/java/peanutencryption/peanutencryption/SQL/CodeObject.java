/*
 * @author Gabriel Oexle
 * 2015.
 */
package peanutencryption.peanutencryption.SQL;

import java.sql.Timestamp;
import java.util.List;

public class CodeObject {

    private String CodeName;
    private String Code;
    private Timestamp CreationTime;
    private long DataID;


    public CodeObject(String _CodeName, String _Code, Timestamp _CreationTime, long _DataID) {
        this.CodeName = _CodeName;
        this.Code = _Code;
        this.CreationTime = _CreationTime;
        this.DataID = _DataID;
    }

    @Override
    public String toString() {
        return this.CodeName;
    }


    public Timestamp getCreationTime() {
        return CreationTime;
    }

    public String getCodeName() {
        return CodeName;
    }

    public String getCode() {
        return Code;
    }

    public long getDataID() {
        return DataID;
    }

    public void setCode(String code) {
        this.Code = code;
    }


    public static List<CodeObject> createContactsList(SQLiteHelper sqLiteHelper) {
        return sqLiteHelper.getAllCodes();
    }
}
