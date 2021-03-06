package upb.com.smarttooth;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import adrian.upb.smarttooth.R;
import upb.com.smarttooth.Renderers.Patient;
import upb.com.smarttooth.Renderers.Renderer;
import upb.com.smarttooth.Renderers.ToothSettings;

public class MainActivity extends Activity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    public static Activity instance;
    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;
    public static String[] pagini = new String []{"Setări waveform", "Pacient", "Selecție pacient",
        "Access la date", "Opțiuni"};
    public static int[] views = new int[]{R.layout.fragment_tooth_settings,R.layout.fragment_pacient,
            R.layout.fragment_select_pacient,R.layout.fragment_data_access, R.layout.fragment_options};
    public static int[] menus = new int[]{R.menu.tooth_settings,0,0,0,0};
    public static Renderer[] renderers = new Renderer[]{new ToothSettings(), new Patient()};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.instance = this;
        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
        new Tooth(this);
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, PlaceholderFragment.newInstance(position))
                .commit();
    }

    public void onSectionAttached(int number) {
        mTitle = pagini[number];
    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            int sn =  getArguments().getInt(ARG_SECTION_NUMBER);
            final View rootView = inflater.inflate(views[sn], container, false);
            if(sn < renderers.length){
                renderers[sn].render(rootView);
            }
            setHasOptionsMenu(true);
            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MainActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            super.onCreateOptionsMenu(menu, inflater);
            int sn =  getArguments().getInt(ARG_SECTION_NUMBER);
            if(menus[sn] != 0) {
                inflater.inflate(menus[sn], menu);
            }
             Log.e("test", "meniul se apeleaza");
        }
        @Override
        public boolean onOptionsItemSelected (MenuItem item){
            int sn =  getArguments().getInt(ARG_SECTION_NUMBER);
            if(menus[sn] != 0) {
                renderers[sn].onOptionsItemSelected(item);
            }
            Log.e("test", "meniul se apeleaza");
            return true;
        }
    }

}
