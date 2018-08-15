package de.phoenixstudios.pc_dimmer;

public class GradientColor {
    private int startColor;
    private int endColor;

    public GradientColor(int start, int end){
        startColor = start;
        endColor = end;
    }

    public int getStartColor() {
        return startColor;
    }
    public void setStartColor(int startColor) {
        this.startColor = startColor;
    }
    public int getEndColor() {
        return endColor;
    }
    public void setEndColor(int endColor) {
        this.endColor = endColor;
    }
}