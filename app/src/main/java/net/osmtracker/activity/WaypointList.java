package net.osmtracker.activity;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ListView;

import net.osmtracker.R;
import net.osmtracker.db.DataHelper;
import net.osmtracker.db.TrackContentProvider;
import net.osmtracker.db.WaypointListAdapter;
import net.osmtracker.listener.EditWaypointOnCancelListener;

/**
 * Activity that lists the previous waypoints tracked by the user.
 * 
 * @author Nicolas Guillaumin
 * 
 */
public class WaypointList extends ListActivity {

	@Override
	protected void onResume() {
		Long trackId = getIntent().getExtras().getLong(TrackContentProvider.Schema.COL_TRACK_ID);
		
		Cursor cursor = getContentResolver().query(TrackContentProvider.waypointsUri(trackId),
				null, null, null, TrackContentProvider.Schema.COL_TIMESTAMP + " desc");
		startManagingCursor(cursor);
		setListAdapter(new WaypointListAdapter(WaypointList.this, cursor));

		super.onResume();
	}

	@Override
	protected void onPause() {
		CursorAdapter adapter = (CursorAdapter) getListAdapter();
		if (adapter != null) {
			// Properly close the adapter cursor
			Cursor cursor = adapter.getCursor();
			stopManagingCursor(cursor);
			cursor.close();
			setListAdapter(null);
		}

		super.onPause();
	}


	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		final Cursor cursor1 = ((CursorAdapter)getListAdapter()).getCursor();

		final DataHelper dataHelper = new DataHelper(l.getContext());

		LayoutInflater inflater = this.getLayoutInflater();

		final View edit_waypoint_dialog = inflater.inflate(R.layout.edit_waypoint_dialog,null);
		final EditText edit_waypoint_et_name = edit_waypoint_dialog.findViewById(R.id.edit_waypoint_et_name);

		String old_text = cursor1.getString(cursor1.getColumnIndex("name"));

		edit_waypoint_et_name.setText(old_text);
		edit_waypoint_et_name.setSelection(old_text.length());

		final long trackId = cursor1.getLong(cursor1.getColumnIndex("track_id"));
		final String uuid = cursor1.getString(cursor1.getColumnIndex("uuid"));
		final String name = edit_waypoint_et_name.getText().toString();
		final String link = cursor1.getString(cursor1.getColumnIndex("link"));

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		//builder.setMessage(l.getContext().getResources().getString(R.string.edit_waypoint_tv_name));
		builder.setCancelable(true);

		builder.setPositiveButton(l.getContext().getResources().getString(R.string.edit_waypoint_bt_ok),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						String new_text = edit_waypoint_et_name.getText().toString();
						dataHelper.updateWayPoint(trackId,uuid,new_text,link);
					}
				}
		);

		builder.setNegativeButton(l.getContext().getResources().getString(R.string.edit_waypoint_bt_delete),
				new EditWaypointOnCancelListener(cursor1) {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dataHelper.deleteWayPoint(uuid);
						cursor1.requery();
					}
				}
		);

		AlertDialog alert = builder.create();
		alert.setView(edit_waypoint_dialog);
		alert.show();

		super.onListItemClick(l, v, position, id);
	}

}
