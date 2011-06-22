package com.af.mbtwhere;

import java.util.ArrayList;

import android.app.ListActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;

public class ServiceUpdate extends ListActivity {
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		ArrayList<String> updates = getIntent().getStringArrayListExtra("com.af.mbtwhere.ServiceUpdates");
		setListAdapter(new ArrayAdapter<String>(this, R.layout.list_updates, updates));
	}
}