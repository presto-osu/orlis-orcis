package at.bitfire.cadroid;

import android.app.Fragment;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class IntroFragment extends Fragment {
	public static final String TAG = "cadroid.IntroFragment";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.frag_intro, container, false);
		
		TextView textIntro = (TextView)v.findViewById(R.id.intro_text);
		textIntro.setText(Html.fromHtml(getString(R.string.intro_text)));
		textIntro.setMovementMethod(LinkMovementMethod.getInstance());
		
		setHasOptionsMenu(true);
		return v;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		((MainActivity)getActivity()).onShowFragment(TAG);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.simple_next, menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.next:
			((MainActivity)getActivity()).showFragment(FetchFragment.TAG, true);
			break;
		default:
			return super.onOptionsItemSelected(item);
		}
		return true;
	}

}
