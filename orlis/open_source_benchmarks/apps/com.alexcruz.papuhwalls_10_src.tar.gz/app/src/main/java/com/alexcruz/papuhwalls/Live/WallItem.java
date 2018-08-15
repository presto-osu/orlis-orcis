package com.alexcruz.papuhwalls.Live;

/**
 * Created by Daniel Huber on 25.12.2015.
 */
public class WallItem {

    private boolean isChecked = true;
    private String path;

    public WallItem(String path){
        this.path = path;
    }

    public String getPath(){
        return this.path;
    }

    public boolean isChecked(){
        return this.isChecked;
    }

    public void setChecked(boolean checked){
        this.isChecked = checked;
    }
}
