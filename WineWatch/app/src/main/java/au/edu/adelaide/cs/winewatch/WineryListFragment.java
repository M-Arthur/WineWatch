package au.edu.adelaide.cs.winewatch;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link WineryListFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link WineryListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WineryListFragment extends Fragment {
	/**
	 * The fragment argument representing the section number for this
	 * fragment.
	 */
	private static final String ARG_SECTION_NUMBER = "section_number";
	private static final String ARG_WINERY_NAME_LIST = "winery_name_list";

	private int mSectionNumber;
	private String mWineryNameList;
	private ListView mWineryListView;

	private OnFragmentInteractionListener mListener;

	/**
	 * Returns a new instance of this fragment for the given section
	 * number.
	 */
	public static WineryListFragment newInstance(int sectionNumber, ArrayList<String> wineryNameList) {

		WineryListFragment fragment = new WineryListFragment();
		Bundle args = new Bundle();
		args.putInt(ARG_SECTION_NUMBER, sectionNumber);
		args.putStringArrayList(ARG_WINERY_NAME_LIST, wineryNameList);
		fragment.setArguments(args);
		return fragment;
	}

	public WineryListFragment() {
		// Required empty public constructor
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			mSectionNumber = getArguments().getInt(ARG_SECTION_NUMBER);
			mWineryNameList = getArguments().getString(ARG_WINERY_NAME_LIST);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		mWineryListView = (ListView) inflater.inflate(R.layout.fragment_winery_list, container, false);
		if (!mWineryNameList.equals(null)) {

		}
		return mWineryListView;
	}

	public int showIndex() {
		return getArguments().getInt(ARG_SECTION_NUMBER, 1);
	}

	// TODO: Rename method, update argument and hook method into UI event
	public void onButtonPressed(Uri uri) {
		if (mListener != null) {
			mListener.onFragmentInteraction(uri);
		}
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mListener = (OnFragmentInteractionListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnFragmentInteractionListener");
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mListener = null;
	}

	/**
	 * This interface must be implemented by activities that contain this
	 * fragment to allow an interaction in this fragment to be communicated
	 * to the activity and potentially other fragments contained in that
	 * activity.
	 * <p/>
	 * See the Android Training lesson <a href=
	 * "http://developer.android.com/training/basics/fragments/communicating.html"
	 * >Communicating with Other Fragments</a> for more information.
	 */
	public interface OnFragmentInteractionListener {
		// TODO: Update argument type and name
		public void onFragmentInteraction(Uri uri);
	}

}
