package co.loubo.icicle;

import java.util.Date;

/**
 * Created by Owner on 4/26/2015.
 */
public class FreenetMessage {

    private Date date;
    private String message;
    private String sender;
    private String recipient;

    public FreenetMessage(Date date, String message, String sender, String recipient){
        this.date = date;
        this.message = message;
        this.sender = sender;
        this.recipient = recipient;
    }

    public Date getDate() {
        return date;
    }

    public String getMessage() {
        return message;
    }

    public String getSender() {
        return sender;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }
}
