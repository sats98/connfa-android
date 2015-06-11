package com.ls.drupalconapp.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.ls.drupalconapp.R;
import com.ls.drupalconapp.model.DatabaseManager;
import com.ls.drupalconapp.model.data.InfoItem;
import com.ls.drupalconapp.ui.receiver.DataUpdateManager;

import java.util.ArrayList;
import java.util.List;

public class AboutActivity extends ActionBarActivity {

	private AboutListAdapter adapter;
	private List<InfoItem> infoItems;

	private DataUpdateManager dataUpdateManager = new DataUpdateManager(new DataUpdateManager.DataUpdatedListener() {
		@Override
		public void onDataUpdated(int[] requestIds) {
			Log.d("UPDATED", "AboutActivity");
			initViews();
		}
	});

	public static void startThisActivity(Activity activity) {
		Intent intent = new Intent(activity, AboutActivity.class);
		activity.startActivity(intent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ac_about);
		dataUpdateManager.register(this);

		initStatusBar();
		initToolbar();
		initViews();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		dataUpdateManager.unregister(this);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void initStatusBar() {
		int currentApiVersion = android.os.Build.VERSION.SDK_INT;
		if (currentApiVersion >= Build.VERSION_CODES.LOLLIPOP) {
			getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
			findViewById(R.id.viewStatusBarTrans).setVisibility(View.VISIBLE);
		}
	}

	private void initToolbar() {
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolBar);
		if (toolbar != null) {
			toolbar.setTitle("");
			setSupportActionBar(toolbar);
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	private void initViews() {
		DatabaseManager dbManager = DatabaseManager.instance();
		infoItems = dbManager.getInfo();

		ListView listMenu = (ListView) findViewById(R.id.listView);
		listMenu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
				onItemClicked(position);
			}
		});

		if (adapter == null) {
			adapter = new AboutListAdapter(infoItems);
			listMenu.setAdapter(adapter);
		} else {
			adapter.setData(infoItems);
			adapter.notifyDataSetChanged();
		}
	}

	private void onItemClicked(int position) {
		InfoItem item = infoItems.get(position);
		Intent intent = new Intent(this, AboutDetailsActivity.class);
		intent.putExtra(AboutDetailsActivity.EXTRA_DETAILS_TITLE, item.getTitle());
		intent.putExtra(AboutDetailsActivity.EXTRA_DETAILS_ID, item.getId());
		intent.putExtra(AboutDetailsActivity.EXTRA_DETAILS_CONTENT, item.getContent());
		startActivity(intent);
	}

	private class AboutListAdapter extends BaseAdapter {

		List<InfoItem> mItems = new ArrayList<InfoItem>();

		public AboutListAdapter(List<InfoItem> items) {
			mItems = items;
		}

		public void setData(List<InfoItem> items) {
			mItems = items;
		}

		@Override
		public int getCount() {
			return mItems.size();
		}

		@Override
		public Object getItem(int i) {
			return mItems.get(i);
		}

		@Override
		public long getItemId(int i) {
			return mItems.get(i).getId();
		}

		@Override
		public View getView(int i, View view, ViewGroup parent) {
			View resultView;

			if (view == null) {
				resultView = getLayoutInflater().inflate(R.layout.item_about, parent, false);
			} else {
				resultView = view;
			}

			InfoItem item = mItems.get(i);
			((TextView) resultView.findViewById(R.id.txtTitle)).setText(item.getTitle());

			return resultView;
		}
	}
}