/*******************************************************************************
 * Copyright 2012-2013 Trento RISE
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either   express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package eu.trentorise.smartcampus.dt.custom.map;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.SparseArray;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;
import com.google.android.maps.Projection;

import eu.trentorise.smartcampus.dt.R;
import eu.trentorise.smartcampus.dt.R.color;
import eu.trentorise.smartcampus.dt.custom.CategoryHelper;
import eu.trentorise.smartcampus.dt.model.BaseDTObject;
import eu.trentorise.smartcampus.dt.model.StoryObject;

public class DTStoryItemizedOverlay extends ItemizedOverlay<OverlayItem> {

    private static int densityX = 10;
    private static int densityY = 10;

	private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
	private ArrayList<BaseDTObject> mObjects = new ArrayList<BaseDTObject>();
	private Set<OverlayItem> mGeneric = new HashSet<OverlayItem>();

	private SparseArray<int[]> item2group = new SparseArray<int[]>();
	
	private BasicObjectMapItemTapListener listener = null;

	private Drawable groupMarker = null;
	
	private Context mContext = null;
	
	private MapView mMapView = null;
	private StoryObject myStory = null;
	private OverlayItem selectedOverlay = null;
	private Integer currentStep = 0;

	List<List<List<OverlayItem>>> grid = new ArrayList<List<List<OverlayItem>>>(densityX);
	private Canvas canvas = null;
	private Path path = new Path();
	private Projection projection;  
	private Drawable drawableForDimension;
	
	public DTStoryItemizedOverlay(Context mContext, MapView mapView, StoryObject story) {
		super(boundCenterBottom(mContext.getResources().getDrawable(R.drawable.step)));
		this.mContext = mContext;
		this.mMapView = mapView;
		this.myStory = story;
	    this.projection = mapView.getProjection();
		this.drawableForDimension = mContext.getResources().getDrawable(R.drawable.selected_step);

		populate();
	}


	public void populateAll() {
		populate();
	}
	
	public void setMapItemTapListener(BasicObjectMapItemTapListener listener) {
		this.listener = listener;
	}



	public void addOverlay(BaseDTObject o, String string) {
		if ((o!=null)&&(o.getLocation() != null)) {
			GeoPoint point = new GeoPoint((int)(o.getLocation()[0]*1E6),(int)(o.getLocation()[1]*1E6));
			OverlayItem overlayitem = new OverlayItem(point, o.getTitle(), o.getDescription());
			overlayitem.setMarker(writeOnDrawable(R.drawable.step, ""+string));
			mOverlays.add(overlayitem);
			mObjects.add(o);
//			populate();
		}
	}

	@Override
	protected OverlayItem createItem(int i) {
		return mOverlays.get(i);
	}

	@Override
	public int size() {
		return mOverlays.size();
	}

	public void addGenericOverlay(OverlayItem overlay) {
		mOverlays.add(overlay);
		mObjects.add(null);
		mGeneric.add(overlay);
		populate();
	}

	public void clearMarkers() {
		mOverlays.clear();
		mObjects.clear();
		populate();
		
	}

	public boolean changeElementsonMap(int index, StoryObject story){
		myStory = story;
		if ((listener != null)&&(index!=-1)) {
			if (/*mObjects.size() > index && mObjects.get(index) != null &&*/ (myStory.getSteps().get(index).assignedPoi()!=null)){
				//if (item2group.get(index) != null) {
				//	int[] coords = item2group.get(index);
					try {

							{
								int k=0;
								for (int i=0; i<story.getSteps().size();i++)
									{
									if (story.getSteps().get(i).assignedPoi()!=null) //se ho il poi non nullo allora lo disegno sulla mappa
									{
										//se e' quello corrente lo faccio red, altrimenti blu
										if (i==index)
										//if (mObjects.get(index).getId().compareTo(story.getSteps().get(i).assignedPoi().getId())==0)
									{
										currentStep=k;
										mOverlays.get(k).setMarker(writeOnDrawable(R.drawable.selected_step, ""+(i+1)));
										selectedOverlay = mOverlays.get(k);
									}
									else 
										mOverlays.get(k).setMarker(writeOnDrawable(R.drawable.step, ""+(i+1)));
										k++;

									}
									}

							}
					} catch (Exception e) {
						return super.onTap(index);
					} 
				//}
				//return true;
			} 
		}
		if ((index==-1)||myStory.getSteps().get(index).assignedPoi()==null)
		{
			int k=0;
			selectedOverlay=null;
			for (int i=0; i<story.getSteps().size();i++)
				if (story.getSteps().get(i).assignedPoi()!=null)
					{
						mOverlays.get(k).setMarker(writeOnDrawable(R.drawable.step, ""+(i+1)));
						k++;
					}
		}
		return super.onTap(index);
	}
	
	@Override
	protected boolean onTap(int index) {
		//lancio non con index del layer ma con index del poi
		for (int i=0; i<myStory.getSteps().size();i++)
			if (myStory.getSteps().get(i).assignedPoi()!=null)
				if (myStory.getSteps().get(i).assignedPoi().getId().compareTo(mObjects.get(index).getId())==0)
						{
						boolean returnValue = changeElementsonMap(i,myStory);
						listener.onBasicObjectTap(mObjects.get(index));
						return returnValue;

						}
		return true;
	}


	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		this.canvas = canvas;
	    path.rewind();
	    boolean first = true;

		// binning:
        
        //item2group.clear();
        
        // 2D array with some configurable, fixed density
        grid.clear(); 

        for(int i = 0; i<densityX; i++){
            ArrayList<List<OverlayItem>> column = new ArrayList<List<OverlayItem>>(densityY);
            for(int j = 0; j < densityY; j++){
                column.add(new ArrayList<OverlayItem>());
            }
            grid.add(column);
        }

        int idx = 0;
        for (OverlayItem m : mOverlays) {
        	//draw the lines
	        Point p1 = new Point();
	        projection.toPixels(m.getPoint(), p1);
        	 if(first){
                 path.moveTo(p1.x, p1.y-drawableForDimension.getIntrinsicHeight()/2);
                 first = false;
        	 }else
                 path.lineTo(p1.x, p1.y-drawableForDimension.getIntrinsicHeight()/2);
        	if (!mGeneric.contains(m)) {
                int binX;
                int binY;

                Projection proj = mapView.getProjection();
                Point p = proj.toPixels(m.getPoint(), null);

                if (isWithin(p, mapView)) {
                    double fractionX = ((double)p.x / (double)mapView.getWidth());
                    binX = (int) (Math.floor(densityX * fractionX));
                    double fractionY = ((double)p.y / (double)mapView.getHeight());
                    binY = (int) (Math
                            .floor(densityX * fractionY));
                    item2group.put(idx, new int[]{binX,binY});
                    grid.get(binX).get(binY).add(m); // just push the reference
                }
        	}
            idx++;
        }
        
        Paint   mPaint = new Paint();
        mPaint.setDither(true);
        mPaint.setColor(new Color().parseColor(mContext.getString(R.color.dtappcolor)));
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(6);
        canvas.drawPath(path, mPaint);
        // drawing:

        for (int i = 0; i < densityX; i++) {
            for (int j = 0; j < densityY; j++) {
                List<OverlayItem> markerList = grid.get(i).get(j);
                if (markerList.size() > 1) {
                    //drawGroup(canvas, mapView, markerList);
                    drawSingle(canvas, mapView, markerList);
                } else {
                    // draw single marker
                    drawSingle(canvas, mapView, markerList);
                }
            }
        }
        
        for (OverlayItem m : mGeneric) {
        	drawSingleItem(canvas, mapView, m);
        }
        


	}



    private void drawSingle(Canvas canvas, MapView mapView, List<OverlayItem> markerList) {
        for (OverlayItem item : markerList) {
            drawSingleItem(canvas, mapView, item);
        }
    }


	protected Point drawSingleItem(Canvas canvas, MapView mapView,
			OverlayItem item) 
		{
		
		//disegna il singolo in base a quello attualmente selezionato
		GeoPoint point = item.getPoint();
		Point ptScreenCoord = new Point();
		mapView.getProjection().toPixels(point, ptScreenCoord);
		//check se e' quello selezionato
		if (selectedOverlay!=null)
			if ((item.getPoint().getLatitudeE6()==selectedOverlay.getPoint().getLatitudeE6())&&((item.getPoint().getLongitudeE6()==selectedOverlay.getPoint().getLongitudeE6())))
		{
			drawAt(canvas, mOverlays.get(currentStep).getMarker(0),ptScreenCoord.x, ptScreenCoord.y, true);
			drawAt(canvas, mOverlays.get(currentStep).getMarker(0),ptScreenCoord.x, ptScreenCoord.y, false);
		}
		else{
			drawAt(canvas, item.getMarker(0), ptScreenCoord.x, ptScreenCoord.y, true);
			drawAt(canvas, item.getMarker(0), ptScreenCoord.x, ptScreenCoord.y, false);
		}
		else{
			drawAt(canvas, item.getMarker(0), ptScreenCoord.x, ptScreenCoord.y, true);
			drawAt(canvas, item.getMarker(0), ptScreenCoord.x, ptScreenCoord.y, false);
		}

		return ptScreenCoord;
	}
	public static boolean isWithin(Point p, MapView mapView) {
        return (p.x > 0 & p.x < mapView.getWidth() & p.y > 0 & p.y < mapView
                .getHeight());
    }
	
	public Drawable writeOnDrawable(int drawableId, String text){


        Bitmap bm = BitmapFactory.decodeResource(mContext.getResources(), drawableId).copy(Bitmap.Config.ARGB_8888, true);

        Paint paint = new Paint(); 
        paint.setStyle(Style.FILL);  
        paint.setColor(Color.WHITE); 
        paint.setTextSize(20); 

        Canvas canvas = new Canvas(bm);
        canvas.drawText(text,bm.getWidth()/2-5, bm.getHeight()/2+5, paint);
        Drawable d = new BitmapDrawable(mContext.getResources(),bm);
        d.setBounds(-drawableForDimension.getIntrinsicWidth()/2, -drawableForDimension.getIntrinsicHeight(), drawableForDimension.getIntrinsicWidth() /2, 0);
        return d;
    }
	
	public void fithMaptOnTheStory (){
		MapManager.fitMapWithOverlays(mOverlays, mMapView);
		} 
	
}	
