/*
 * Copyright (C) 2015 Matthias Gabriel
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.gabm.fancyplaces.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.View;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.Overlay;

/**
 * Created by gabm on 25/05/15.
 */
public class OsmMarker extends Overlay implements View.OnClickListener {

    public static final float ANCHOR_CENTER = 0.5F;
    public static final float ANCHOR_LEFT = 0.0F;
    public static final float ANCHOR_TOP = 0.0F;
    public static final float ANCHOR_RIGHT = 1.0F;
    public static final float ANCHOR_BOTTOM = 1.0F;
    protected float AnchorU;
    protected float AnchorV;
    protected float IWAnchorU;
    protected float IWAnchorV;
    protected float Alpha;
    protected Drawable Icon = null;
    protected OsmMarkerInfoWindow InfoWindow = null;
    protected int Id = -1;
    protected IMarkerSelected markerSelectedListener = null;
    private GeoPoint Position = null;

    public OsmMarker(Context ctx, MapView mapView) {
        super(ctx);

        this.Alpha = 1.0F;
        this.Position = new GeoPoint(0.0D, 0.0D);
        this.AnchorU = 0.5F;
        this.AnchorV = 0.5F;
        this.IWAnchorU = ANCHOR_CENTER;
        this.IWAnchorV = ANCHOR_TOP;

        this.InfoWindow = new OsmMarkerInfoWindow(mapView, this);
        InfoWindow.close();
    }

    public void setId(int id) {
        Id = id;
    }

    public void setMarkerSelectedListener(IMarkerSelected listener) {
        markerSelectedListener = listener;
    }

    public void setTitle(String title) {
        InfoWindow.setTitle(title);
    }

    public boolean isHit(MotionEvent event, MapView mapView) {
        Projection pj = mapView.getProjection();

        Point PositionPixels = new Point();
        pj.toPixels(this.Position, PositionPixels);
        Rect screenRect = pj.getIntrinsicScreenRect();
        int x = -PositionPixels.x + screenRect.left + (int) event.getX();
        int y = -PositionPixels.y + screenRect.top + (int) event.getY();
        boolean hit = this.Icon.getBounds().contains(x, y);
        return hit;
    }

    public void setInfoWindowVisible(boolean infoWindowVisible) {
        if (this.InfoWindow != null) {
            if (infoWindowVisible) {
                int markerWidth1 = this.Icon.getIntrinsicWidth();
                int markerHeight1 = this.Icon.getIntrinsicHeight();
                int offsetX = (int) (this.IWAnchorU * (float) markerWidth1) - (int) (this.AnchorU * (float) markerWidth1);
                int offsetY = (int) (this.IWAnchorV * (float) markerHeight1) - (int) (this.AnchorV * (float) markerHeight1);
                this.InfoWindow.open(this, this.Position, offsetX, offsetY);
            } else {
                InfoWindow.close();
            }
        }
    }

    public void toogleInfoWindow() {
        if (this.InfoWindow != null) {

            if (InfoWindow.isOpen())
                setInfoWindowVisible(false);
            else
                setInfoWindowVisible(true);
        }
    }

    public void setIcon(Drawable drawable) {
        Icon = drawable;
    }

    public void setAnchor(float anchorU, float anchorV) {
        this.AnchorU = anchorU;
        this.AnchorV = anchorV;
    }

    @Override
    protected void draw(Canvas canvas, MapView mapView, boolean shadow) {
        if (this.Icon != null) {
            Projection pj = mapView.getProjection();
            Point PositionPixels = new Point();
            pj.toPixels(this.Position, PositionPixels);
            int width = this.Icon.getIntrinsicWidth();
            int height = this.Icon.getIntrinsicHeight();
            Rect rect = new Rect(0, 0, width, height);
            rect.offset(-((int) (this.AnchorU * (float) width)), -((int) (this.AnchorV * (float) height)));
            this.Icon.setBounds(rect);
            this.Icon.setAlpha((int) (this.Alpha * 255.0F));
            drawAt(canvas, this.Icon, PositionPixels.x, PositionPixels.y, false, 0);
        }
    }


    @Override
    public boolean onSingleTapConfirmed(MotionEvent event, MapView mapView) {
        if (isHit(event, mapView)) {
            toogleInfoWindow();
            mapView.getController().animateTo(Position);
            return true;
        }
        return false;
    }

    @Override
    public void onClick(View view) {
        if (markerSelectedListener != null)
            markerSelectedListener.onMarkerSelected(Id);
    }

    public void setPosition(GeoPoint pt) {
        Position = pt.clone();
    }


    public interface IMarkerSelected {
        void onMarkerSelected(int id);
    }

}
