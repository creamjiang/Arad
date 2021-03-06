/*
 * Copyright 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.beanu.arad.demo;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

/**
 * A fragment that renders Google+ search results for a given query, provided as
 * the {@link LoadMoreFragment#EXTRA_QUERY} extra in the fragment arguments. If
 * no search query is provided, the conference hashtag is used as the default
 * query.
 */
public class LoadMoreFragment extends ListFragment implements AbsListView.OnScrollListener,
		LoaderManager.LoaderCallbacks<List<String>> {

	private static final String TAG = LoadMoreFragment.class.getSimpleName();

	private static final String STATE_POSITION = "position";
	private static final String STATE_TOP = "top";

	private static final long MAX_RESULTS_PER_REQUEST = 20;
	private static final int STREAM_LOADER_ID = 0;

	private String mSearchString;

	private List<String> mStream = new ArrayList<String>();
	private StreamAdapter mStreamAdapter = new StreamAdapter();
	private int mListViewStatePosition;
	private int mListViewStateTop;

	// private ImageLoader mImageLoader;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// final Intent intent =
		// BaseActivity.fragmentArgumentsToIntent(getArguments());
		//
		// // mSearchString can be populated before onCreate() by called
		// refresh(String)
		// if (TextUtils.isEmpty(mSearchString)) {
		// mSearchString = intent.getStringExtra(EXTRA_QUERY);
		// }
		// if (TextUtils.isEmpty(mSearchString)) {
		// mSearchString = UIUtils.CONFERENCE_HASHTAG;
		// }
		//
		// if (!mSearchString.startsWith("#")) {
		// mSearchString = "#" + mSearchString;
		// }
		//
		// mImageLoader =
		// PlusStreamRowViewBinder.getPlusStreamImageLoader(getActivity(),
		// getResources());

		// setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			mListViewStatePosition = savedInstanceState.getInt(STATE_POSITION, -1);
			mListViewStateTop = savedInstanceState.getInt(STATE_TOP, 0);
		} else {
			mListViewStatePosition = -1;
			mListViewStateTop = 0;
		}
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setEmptyText("SSS");

		// In support library r8, calling initLoader for a fragment in a
		// FragmentPagerAdapter
		// in the fragment's onCreate may cause the same LoaderManager to be
		// dealt to multiple
		// fragments because their mIndex is -1 (haven't been added to the
		// activity yet). Thus,
		// we do this in onActivityCreated.
		getLoaderManager().initLoader(STREAM_LOADER_ID, null, this);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		final ListView listView = getListView();
		// if (!UIUtils.isTablet(getActivity())) {
		// view.setBackgroundColor(getResources().getColor(R.color.plus_stream_spacer_color));
		// }

		// if (getArguments() != null
		// && getArguments().getBoolean(EXTRA_ADD_VERTICAL_MARGINS, false)) {
		// int verticalMargin = getResources().getDimensionPixelSize(
		// R.dimen.plus_stream_padding_vertical);
		// if (verticalMargin > 0) {
		// listView.setClipToPadding(false);
		// listView.setPadding(0, verticalMargin, 0, verticalMargin);
		// }
		// }

		listView.setOnScrollListener(this);
		listView.setDrawSelectorOnTop(true);
		listView.setDivider(getResources().getDrawable(android.R.color.transparent));
		listView.setDividerHeight(1);

		// TypedValue v = new TypedValue();
		// getActivity().getTheme().resolveAttribute(R.attr.activatableItemBackground,
		// v, true);
		// listView.setSelector(v.resourceId);

		setListAdapter(mStreamAdapter);
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
	}

	@Override
	public void onDestroyOptionsMenu() {
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		if (isAdded()) {
			View v = getListView().getChildAt(0);
			int top = (v == null) ? 0 : v.getTop();
			outState.putInt(STATE_POSITION, getListView().getFirstVisiblePosition());
			outState.putInt(STATE_TOP, top);
		}
		super.onSaveInstanceState(outState);
	}

	public void refresh(String newQuery) {
		mSearchString = newQuery;
		refresh(true);
	}

	public void refresh() {
		refresh(false);
	}

	public void refresh(boolean forceRefresh) {
		if (isStreamLoading() && !forceRefresh) {
			return;
		}

		// clear current items
		mStream.clear();
		mStreamAdapter.notifyDataSetInvalidated();

		if (isAdded()) {
			Loader loader = getLoaderManager().getLoader(STREAM_LOADER_ID);
			((StreamLoader) loader).init(mSearchString);
		}

		loadMoreResults();
	}

	public void loadMoreResults() {
		if (isAdded()) {
			Loader loader = getLoaderManager().getLoader(STREAM_LOADER_ID);
			if (loader != null) {
				loader.forceLoad();
			}
		}
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
	}

	@Override
	public void onScrollStateChanged(AbsListView listView, int scrollState) {
		// Pause disk cache access to ensure smoother scrolling
		if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
			// mImageLoader.stopProcessingQueue();
		} else {
			// mImageLoader.startProcessingQueue();
		}
	}

	@Override
	public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		if (!isStreamLoading() && streamHasMoreResults() && visibleItemCount != 0
				&& firstVisibleItem + visibleItemCount >= totalItemCount - 1) {
			loadMoreResults();
		}
	}

	@Override
	public Loader<List<String>> onCreateLoader(int id, Bundle args) {
		return new StreamLoader(getActivity(), mSearchString);

	}

	@Override
	public void onLoadFinished(Loader<List<String>> listLoader, List<String> activities) {
		if (activities != null) {
			mStream = activities;
		}
		mStreamAdapter.notifyDataSetChanged();
		if (mListViewStatePosition != -1 && isAdded()) {
			getListView().setSelectionFromTop(mListViewStatePosition, mListViewStateTop);
			mListViewStatePosition = -1;
		}
	}

	@Override
	public void onLoaderReset(Loader<List<String>> listLoader) {
	}

	private boolean isStreamLoading() {
		if (isAdded()) {
			final Loader loader = getLoaderManager().getLoader(STREAM_LOADER_ID);
			if (loader != null) {
				return ((StreamLoader) loader).isLoading();
			}
		}
		return true;
	}

	private boolean streamHasMoreResults() {
		if (isAdded()) {
			final Loader loader = getLoaderManager().getLoader(STREAM_LOADER_ID);
			if (loader != null) {
				return ((StreamLoader) loader).hasMoreResults();
			}
		}
		return false;
	}

	private boolean streamHasError() {
		if (isAdded()) {
			final Loader loader = getLoaderManager().getLoader(STREAM_LOADER_ID);
			if (loader != null) {
				return ((StreamLoader) loader).hasError();
			}
		}
		return false;
	}

	private static class StreamLoader extends AsyncTaskLoader<List<String>> {
		List<String> mActivities;
		private String mSearchString;
		private String mNextPageToken;
		private boolean mIsLoading;
		private boolean mHasError;

		public StreamLoader(Context context, String searchString) {
			super(context);
			init(searchString);
		}

		private void init(String searchString) {
			mSearchString = searchString;
			mHasError = false;
			mNextPageToken = null;
			mIsLoading = true;
			mActivities = null;
		}

		@Override
		public List<String> loadInBackground() {
			mIsLoading = true;

			List<String> items = new ArrayList<String>();
			for (int i = 0; i < 20; i++)
				items.add("AAA" + i);

			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return items;
		}

		@Override
		public void deliverResult(List<String> items) {
			mIsLoading = false;
			if (items != null) {
				if (mActivities == null) {
					mActivities = items;
				} else {
					mActivities.addAll(items);
				}
			}
			if (isStarted()) {
				// Need to return new ArrayList for some reason or
				// onLoadFinished() is not called
				super.deliverResult(mActivities == null ? null : new ArrayList<String>(mActivities));
			}
		}

		@Override
		protected void onStartLoading() {
			if (mActivities != null) {
				// If we already have results and are starting up, deliver what
				// we already have.
				deliverResult(null);
			} else {
				forceLoad();
			}
		}

		@Override
		protected void onStopLoading() {
			mIsLoading = false;
			cancelLoad();
		}

		@Override
		protected void onReset() {
			super.onReset();
			onStopLoading();
			mActivities = null;
		}

		public boolean isLoading() {
			return mIsLoading;
		}

		public boolean hasMoreResults() {
			return true;
		}

		public boolean hasError() {
			return mHasError;
		}

		public void setSearchString(String searchString) {
			mSearchString = searchString;
		}

		public void refresh() {
			reset();
			startLoading();
		}
	}

	private class StreamAdapter extends BaseAdapter {
		private static final int VIEW_TYPE_ACTIVITY = 0;
		private static final int VIEW_TYPE_LOADING = 1;

		@Override
		public boolean areAllItemsEnabled() {
			return false;
		}

		@Override
		public boolean isEnabled(int position) {
			return getItemViewType(position) == VIEW_TYPE_ACTIVITY;
		}

		@Override
		public int getViewTypeCount() {
			return 2;
		}

		@Override
		public boolean hasStableIds() {
			return true;
		}

		@Override
		public int getCount() {
			return mStream.size() + (
			// show the status list row if...
					((isStreamLoading() && mStream.size() == 0) // ...this is
																// the first
																// load
							|| streamHasMoreResults() // ...or there's another
														// page
					|| streamHasError()) // ...or there's an error
					? 1
							: 0);
		}

		@Override
		public int getItemViewType(int position) {
			return (position >= mStream.size()) ? VIEW_TYPE_LOADING : VIEW_TYPE_ACTIVITY;
		}

		@Override
		public Object getItem(int position) {
			return (getItemViewType(position) == VIEW_TYPE_ACTIVITY) ? mStream.get(position) : null;
		}

		@Override
		public long getItemId(int position) {
			// TODO: better unique ID heuristic
			return (getItemViewType(position) == VIEW_TYPE_ACTIVITY) ? mStream.get(position).hashCode() : -1;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (getItemViewType(position) == VIEW_TYPE_LOADING) {
				if (convertView == null) {
					convertView = getLayoutInflater(null).inflate(android.R.layout.simple_list_item_1, parent, false);
				}

				if (streamHasError()) {
					// convertView.findViewById(android.R.id.progress).setVisibility(View.GONE);
					((TextView) convertView.findViewById(android.R.id.text1)).setText("error");
				} else {
					// convertView.findViewById(android.R.id.progress).setVisibility(View.VISIBLE);
					((TextView) convertView.findViewById(android.R.id.text1)).setText("loading");
				}

				return convertView;

			} else {
				String activity = (String) getItem(position);
				if (convertView == null) {
					convertView = getLayoutInflater(null).inflate(android.R.layout.simple_list_item_1, parent, false);
				}

				// PlusStreamRowViewBinder.bindActivityView(convertView,
				// activity, mImageLoader, false);
				((TextView) convertView.findViewById(android.R.id.text1)).setText(activity);

				return convertView;
			}
		}
	}
}
