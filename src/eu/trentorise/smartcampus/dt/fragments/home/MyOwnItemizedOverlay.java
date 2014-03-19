package eu.trentorise.smartcampus.dt.fragments.home;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import com.actionbarsherlock.app.SherlockFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import eu.trentorise.smartcampus.dt.R;
import eu.trentorise.smartcampus.dt.custom.map.MapManager.ClusteringHelper;
import eu.trentorise.smartcampus.dt.fragments.events.EventsListingFragment;
import eu.trentorise.smartcampus.dt.fragments.pois.PoisListingFragment;
import eu.trentorise.smartcampus.dt.fragments.search.SearchFragment;
import eu.trentorise.smartcampus.dt.model.LocalEventObject;
import eu.trentorise.smartcampus.osm.android.util.GeoPoint;
import eu.trentorise.smartcampus.osm.android.views.overlay.ItemizedIconOverlay;
import eu.trentorise.smartcampus.osm.android.views.overlay.OverlayItem;
import eu.trentorise.smartcampus.osm.android.views.overlay.ItemizedIconOverlay.ActiveItem;
import eu.trentorise.smartcampus.territoryservice.model.BaseDTObject;
import eu.trentorise.smartcampus.territoryservice.model.POIObject;
import android.app.AlertDialog;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.MotionEvent;
import android.widget.Toast;

public class MyOwnItemizedOverlay extends ItemizedIconOverlay<OverlayItem> {
	protected Context mContext;
	protected static Collection<BaseDTObject> dirtyObjects = null;
	public static final int MAX_VISIBLE_DISTANCE = 15;
	private static final int MAX_ZOOM = 18;

	public MyOwnItemizedOverlay(final Context context,
			final List<OverlayItem> aList, final FragmentManager fragman) {
		super(context, aList, new OnItemGestureListener<OverlayItem>() {
			@SuppressWarnings("unused")
			@Override
			public boolean onItemSingleTapUp(final int index,
					final OverlayItem item) {

				List<OverlayItem> list = ClusteringHelper.getFromGridId(item
						.getTitle());
				System.err.println(list);
				
				List<BaseDTObject> objectDT = new ArrayList<BaseDTObject>();

//				// Estraggo i BaseDTObject dall'overlayItem
				for (int i = 0; i < list.size(); i++) {
					OverlayObject overlay = (OverlayObject) list.get(i);
					BaseDTObject o = overlay.getData();
					objectDT.add(o);
				}
//				for (int i = 0; i < list.size(); i++) {
//					OverlayObject overlay = (OverlayObject) list.get(i);
//					
//					for (int j = 0; j < HomeFragment.objects.size(); j++) {
//						if(overlay.getTitle()==HomeFragment.objects.get(j).getTitle())
//							objectDT.add(HomeFragment.objects.get(j).getData());
//					}
//				}

			
				// controllo se all'interno della lista c'è un elemento o più di
				// uno
				if (objectDT == null || objectDT.isEmpty())
					return true;

				if (objectDT.size() == 1) {
					onBaseDTObjectTap(objectDT.get(0), fragman);
				} else if (HomeFragment.mapView.canZoomIn() == false) {
					onBaseDTObjectsTap(objectDT, fragman);
				} else {
					fitMapWithOverlays(objectDT);
//					 Toast.makeText(
//					 context,
//					 "no zooooom level "
//					 + HomeFragment.mapView.canZoomIn(), 10).show();
					 
					 //HomeFragment.mapController.zoomIn();
					// double[] cordinate = objectDT.get(0).getLocation();
					// GeoPoint point = new GeoPoint(cordinate[0],
					// cordinate[1]);
					// HomeFragment.mapController.setCenter(point);
					//
					// HomeFragment.mapController.zoomIn();

				}

				// Bundle args = new Bundle();
				// OverlayObject object = (OverlayObject) list.get(0);
				//
				// BaseDTObject DTObject = object.getData();
				//
				// // DTObject.setTitle(item.getTitle());
				// // DTObject.setDescription(item.getSnippet());
				// // double[] point = new double[2];
				// // point[0] = item.getPoint().getLatitudeE6();
				// // point[1] = item.getPoint().getLongitudeE6();
				// // DTObject.setLocation(point);
				// //
				// // test title poi da rimuovere
				//
				// Toast.makeText(
				// context,
				// "Titolo: " + object.getTitle() + "Descrizione: "
				// + object.getSnippet() + "Coordinate"
				// + item.getPoint(), 10).show();
				// //
				// args.putSerializable(InfoDialog.PARAM, DTObject);
				// InfoDialog dtoTap = new InfoDialog();
				// dtoTap.setArguments(args);
				// dtoTap.show(fragman, "me");
				// //
				// //
				return true;
			}

			@Override
			public boolean onItemLongPress(final int index,
					final OverlayItem item) {
				Toast.makeText(context, "long press", 10).show();
				return true;
			}

			// @Override
			// public boolean onItemSingleTapUp(int index, OverlayObject item) {
			// // TODO Auto-generated method stub
			// return false;
			// }

			// @Override
			// public boolean onItemLongPress(int index, OverlayObject item) {
			// // TODO Auto-generated method stub
			// return false;
			// }
		});
		// TODO Auto-generated constructor stub
		mContext = context;
	}

	private static void onBaseDTObjectTap(BaseDTObject o,
			FragmentManager fragman) {
		dirtyObjects = new ArrayList<BaseDTObject>(1);
		dirtyObjects.add(o);

		Bundle args = new Bundle();
		args.putSerializable(InfoDialog.PARAM, o);
		InfoDialog dtoTap = new InfoDialog();
		dtoTap.setArguments(args);
		dtoTap.show(fragman, "me");
	}

	private static void onBaseDTObjectsTap(List<BaseDTObject> list,
			FragmentManager fragman) {
		if (list == null || list.size() == 0)
			return;
		if (list.size() == 1) {
			onBaseDTObjectTap(list.get(0), fragman);
			return;
		}

		dirtyObjects = new HashSet<BaseDTObject>(list);

		FragmentTransaction fragmentTransaction = fragman.beginTransaction();
		SherlockFragment fragment = null;
		Bundle args = new Bundle();
		if (list.get(0) instanceof LocalEventObject) {
			fragment = new EventsListingFragment();
			args.putSerializable(SearchFragment.ARG_LIST, new ArrayList(list));
		} else if (list.get(0) instanceof POIObject) {
			fragment = new PoisListingFragment();
			args.putSerializable(SearchFragment.ARG_LIST, new ArrayList(list));
		}
		if (fragment != null) {
			fragment.setArguments(args);
			fragmentTransaction
					.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
			// fragmentTransaction.detach(this); R.id.fragment_container
			fragmentTransaction.replace(android.R.id.content, fragment, "me");
			fragmentTransaction.addToBackStack(fragment.getTag());
			fragmentTransaction.commit();
		}
	}

	private static void fitMapWithOverlays(
			Collection<? extends BaseDTObject> objects) {
		double[] ll = null, rr = null;
		if (objects != null) {
			for (BaseDTObject o : objects) {
				double[] location = o.getLocation();
				if (ll == null) {
					ll = location.clone();
					rr = ll.clone();
				} else {
					ll[0] = Math.min(ll[0], location[0]);
					ll[1] = Math.max(ll[1], location[1]);

					rr[0] = Math.max(rr[0], location[0]);
					rr[1] = Math.min(rr[1], location[1]);
				}
			}
		}
		fit(ll, rr, objects != null && objects.size() > 1);
	}

	private static void fit(double[] ll, double[] rr, boolean zoomIn) {
		if (ll != null && rr != null) {
			float[] dist = new float[3];
			Location.distanceBetween(ll[0], ll[1], rr[0], rr[1], dist);
			if (dist[0] > MAX_VISIBLE_DISTANCE) {
				LatLngBounds bounds = LatLngBounds.builder()
						.include(new LatLng(rr[0], rr[1]))
						.include(new LatLng(ll[0], ll[1])).build();
				
				Double lat = (double) ((ll[0]+rr[0])/2);
				Double lon = (double) ((ll[1]+rr[1])/2 );
				
				GeoPoint point = new GeoPoint(lat,lon);
				
				HomeFragment.mapController.animateTo(point);
				HomeFragment.mapController.zoomIn();
				
				// map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds,64));
			} else {
				GeoPoint point = new GeoPoint(ll[0], ll[1]);
				HomeFragment.mapController.setZoom(MAX_ZOOM);
				HomeFragment.mapController.setCenter(point);

				// map.animateCamera(CameraUpdateFactory.newLatLngZoom(new
				// LatLng(
				// ll[0], ll[1]), MAX_ZOOM));
			}
		}
	}

}