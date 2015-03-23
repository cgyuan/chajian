package com.ldw.skinmain;

import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import dalvik.system.DexClassLoader;

public class MainActivity extends Activity implements OnItemClickListener {

	private List<ResolveInfo> mRlist;
	private ListView mListView;
	private PackageManager mPm;

	private RelativeLayout mMainLayout;
	private Button mButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		searchSkin();
		initView();
		initListView();
	}

	private void searchSkin() {
		Intent intent = new Intent("com.ldw.skin", null);
		mPm = getPackageManager();

		mRlist = mPm.queryIntentActivities(intent, 0);
	}

	private void initView() {
		mMainLayout = (RelativeLayout) findViewById(R.id.mainlayout);
		mButton = (Button) findViewById(R.id.button);
	}

	private void initListView() {
		mListView = (ListView) findViewById(R.id.listview);
		mListView.setAdapter(new MyAdapter());
		mListView.setOnItemClickListener(this);
	}

	private class MyAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return mRlist.size();
		}

		@Override
		public ResolveInfo getItem(int position) {
			return mRlist.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			ViewHolder holder;
			ResolveInfo info = getItem(position);
			if (convertView == null) {
				convertView = getLayoutInflater().inflate(R.layout.item, null);
				holder = new ViewHolder();
				holder.iv = (ImageView) convertView.findViewById(R.id.item_iv);
				holder.tv = (TextView) convertView.findViewById(R.id.item_tv);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			IPlugin plugin = getPlugin(info);
			holder.iv
					.setBackgroundDrawable(plugin.getDrawable("icon", info.activityInfo.packageName));
			holder.tv.setText(plugin.getString(info.activityInfo.packageName));

			return convertView;
		}

		class ViewHolder {
			TextView tv;
			ImageView iv;
		}
	}

	@SuppressLint("NewApi")
	private IPlugin getPlugin(ResolveInfo rinfo) {
		ActivityInfo ainfo = rinfo.activityInfo;

		String packageName = ainfo.packageName;
		// 目标类所在apk或jar包文件的路径
		String dexPath = ainfo.applicationInfo.sourceDir;
		// 由于dex文件存在apk中，因此在装载目标类之前需要先从apk中解压出dex文件，这个参数就是解压后存放的路径
		String dexOutputDir = getApplicationInfo().dataDir;
		// 指目标类中所使用的C/C++库存放的路径
		String libPath = ainfo.applicationInfo.nativeLibraryDir;

		DexClassLoader cl = new DexClassLoader(dexPath, dexOutputDir, libPath,
				this.getClass().getClassLoader());
		try {
			Class<?> clazz = cl.loadClass(packageName + ".SelectSkin");
			// 获取真正的PluginClass对象
			IPlugin plugin = (IPlugin) clazz.newInstance();
			plugin.setContext(this);
			return plugin;
		} catch (Exception e) {
			Log.i("Host", "error", e);
		}
		return null;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onItemClick(AdapterView<?> parent, View arg1, int position,
			long arg3) {
		ResolveInfo info = (ResolveInfo) parent.getItemAtPosition(position);
		IPlugin plugin = getPlugin(info);
		mMainLayout.setBackgroundDrawable(plugin.getDrawable("bg",
				info.activityInfo.packageName));
		mButton.setBackgroundDrawable(plugin.getDrawable("button_selector",
				info.activityInfo.packageName));
	}
}
