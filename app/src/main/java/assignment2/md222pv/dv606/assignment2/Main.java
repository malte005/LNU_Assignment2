package assignment2.md222pv.dv606.assignment2;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Calendars;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.NavigationView.OnNavigationItemSelectedListener;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import assignment2.md222pv.dv606.assignment2.alarm.Alarm;
import assignment2.md222pv.dv606.assignment2.country.AddCountry;
import assignment2.md222pv.dv606.assignment2.country.CalendarProviderClient;
import assignment2.md222pv.dv606.assignment2.country.CalendarUtils;
import assignment2.md222pv.dv606.assignment2.country.Country;
import assignment2.md222pv.dv606.assignment2.country.CountryAdapter;
import assignment2.md222pv.dv606.assignment2.country.DividerItemDecoration;
import assignment2.md222pv.dv606.assignment2.country.SettingsActivity;
import assignment2.md222pv.dv606.assignment2.mp3.MP3Player;

public class Main extends AppCompatActivity
        implements OnNavigationItemSelectedListener, CalendarProviderClient {

    private List<Country> countryList = new ArrayList<>();
    private RecyclerView recyclerView;
    private CountryAdapter cAdapter;
    RecyclerView.LayoutManager mLayoutManager;

    private CursorLoader cursorLoader;
    public CalendarUtils calendarUtils;

    private final int CONTEXT_EDIT_ENTRY = 0;
    private final int CONTEXT_DELETE_ENTRY = 1;
    private final int NEW_EVENT = 123;
    private final int EDIT_EVENT = 124;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getBaseContext(), AddCountry.class);
                startActivityForResult(intent, 1);
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        cAdapter = new CountryAdapter(countryList);
        mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(cAdapter);

        changeUI();

        calendarUtils = new CalendarUtils();

        cursorLoader = (CursorLoader) getLoaderManager().initLoader(LOADER_MANAGER_ID, null, this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        changeUI();
    }

    private void changeUI() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String sorting = null;
        String sort = prefs.getString("sort_list", "-1");
        String color = prefs.getString("color_list", "-1");

        switch (color) {
            case "-1":
                recyclerView.setBackgroundColor(ContextCompat.getColor(getBaseContext(), R.color.colorWhite));
                break;
            case "0":
                recyclerView.setBackgroundColor(ContextCompat.getColor(getBaseContext(), R.color.colorGrey));
                break;
            case "1":
                recyclerView.setBackgroundColor(ContextCompat.getColor(getBaseContext(), R.color.colorGreen));
                break;
            default:
                recyclerView.setBackgroundColor(ContextCompat.getColor(getBaseContext(), R.color.colorWhite));
                break;
        }

        Bundle bundle = new Bundle();
        switch (sort) {
            case "-1":
                sorting = CalendarContract.Events.TITLE + " ASC";
                break;
            case "0":
                sorting = CalendarContract.Events.TITLE + " DESC";
                break;
            case "1":
                sorting = CalendarContract.Events.DTSTART + " ASC";
                break;
            case "2":
                sorting = CalendarContract.Events.DTSTART + " DESC";
                break;
            default:
                sorting = CalendarContract.Events.TITLE + " ASC";
                break;
        }
        bundle.putString("sortOrder", sorting);
        getLoaderManager().restartLoader(LOADER_MANAGER_ID, bundle, this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_CANCELED || data.getStringExtra("country").isEmpty() || data.getStringExtra("year").isEmpty()) {
                Snackbar.make(findViewById(R.id.fab), "Nothing stored", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            } else if (resultCode == Activity.RESULT_OK) {
                String country = data.getStringExtra("country");
                String strYear = data.getStringExtra("year");
                int year = Integer.parseInt(strYear);

                switch (requestCode) {
                    case NEW_EVENT:
                        addNewEvent(year, country);
                        break;
                    case EDIT_EVENT:
                        //edit event with ID eventID
                        int eventID = data.getIntExtra("eventID", -1);
                        ContentValues values = new ContentValues();
                        values.put(CalendarContract.Events.TITLE, country);
                        values.put(CalendarContract.Events.DTSTART, calendarUtils.getEventStart(year));
                        Uri updateUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventID);
                        getContentResolver().update(updateUri, values, null, null);
                        break;
                }

           /*     Country temp = new Country(country, year);
                countryList.add(temp);
                cAdapter.notifyDataSetChanged(); */

                Snackbar.make(findViewById(R.id.fab), country + " stored", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case CONTEXT_DELETE_ENTRY:
                deleteEvent((int) info.id);
                Toast.makeText(Main.this, "DELETED", Toast.LENGTH_SHORT).show();
                return true;
            case CONTEXT_EDIT_ENTRY:
                //get year and country
                String year = (String) ((TextView) ((LinearLayout) info.targetView).getChildAt(0)).getText();
                String country = (String) ((TextView) ((LinearLayout) info.targetView).getChildAt(1)).getText();

                //send content of event
                Intent intent = new Intent(this, AddCountry.class);
                intent.putExtra("country", country);
                intent.putExtra("year", year);
                intent.putExtra("eventID", (int) info.id);
                this.startActivityForResult(intent, EDIT_EVENT);
                Toast.makeText(Main.this, "DELETE: " + year, Toast.LENGTH_SHORT).show();
                return false;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_option, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Bundle bundle = new Bundle();
        switch (item.getItemId()) {
            case R.id.action_prefs:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_country) {

        } else if (id == R.id.nav_alarm) {
            Intent intent = new Intent(this, Alarm.class);
            startActivity(intent);
        } else if (id == R.id.nav_mp3) {
            Intent intent = new Intent(this, MP3Player.class);
            startActivity(intent);
        } else if (id == R.id.nav_prefs) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public long getMyCountriesCalendarId() {
        long id;
        Cursor cursor = this.getContentResolver()
                .query(
                        CALENDARS_LIST_URI,
                        CALENDARS_LIST_PROJECTION,
                        CALENDARS_LIST_SELECTION,
                        CALENDARS_LIST_SELECTION_ARGS,
                        null);

        boolean hasCalendar = cursor.moveToFirst();
        if (hasCalendar) {
            id = cursor.getLong(PROJ_CALENDARS_LIST_ID_INDEX);
        } else {
            ContentResolver contentResolver = this.getContentResolver();
            Uri uri = asSyncAdapter(CALENDARS_LIST_URI, CALENDAR_TITLE, CalendarContract.ACCOUNT_TYPE_LOCAL);

            ContentValues contentValues = new ContentValues();
            contentValues.put(Calendars.ACCOUNT_NAME, ACCOUNT_TITLE);
            contentValues.put(Calendars.ACCOUNT_TYPE, CalendarContract.ACCOUNT_TYPE_LOCAL);
            contentValues.put(Calendars.NAME, CALENDAR_TITLE);
            contentValues.put(Calendars.CALENDAR_DISPLAY_NAME, CALENDAR_TITLE);
            contentValues.put(Calendars.CALENDAR_ACCESS_LEVEL, Calendars.CAL_ACCESS_OWNER);
            contentValues.put(Calendars.OWNER_ACCOUNT, ACCOUNT_TITLE);
            contentValues.put(Calendars.VISIBLE, 1);
            contentValues.put(Calendars.SYNC_EVENTS, 1);

            Uri uriLocation = contentResolver.insert(uri, contentValues);
            id = ContentUris.parseId(uriLocation);
        }
        return id;
    }

    public static Uri asSyncAdapter(Uri uri, String account, String accountType) {
        return uri.buildUpon()
                .appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true")
                .appendQueryParameter(Calendars.ACCOUNT_NAME, account)
                .appendQueryParameter(Calendars.ACCOUNT_TYPE, accountType).build();
    }

    @Override
    public void addNewEvent(int year, String country) {
        ContentValues values = new ContentValues();
        values.put(CalendarContract.Events.DTSTART, CalendarUtils.getEventStart(year));
        values.put(CalendarContract.Events.DTEND, CalendarUtils.getEventEnd(year));
        values.put(CalendarContract.Events.TITLE, country);
        values.put(CalendarContract.Events.CALENDAR_ID, getMyCountriesCalendarId());
        values.put(CalendarContract.Events.EVENT_TIMEZONE, CalendarUtils.getTimeZoneId());
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        getContentResolver().insert(CalendarContract.Events.CONTENT_URI, values);

        //restart the Loader Manager from your activity to get the updated data
        getLoaderManager().restartLoader(LOADER_MANAGER_ID, null, this);
    }

    @Override
    public void updateEvent(int eventId, int year, String country) {
        ContentValues values = new ContentValues();
        values.put(CalendarContract.Events.TITLE, country);
        values.put(CalendarContract.Events.DTSTART, CalendarUtils.getEventStart(year));
        Uri updateUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId);
        int rows = getContentResolver().update(updateUri, values, null, null);
        System.out.println("Row updated: " + rows);

        //restart the Loader Manager from your activity to get the updated data
        getLoaderManager().restartLoader(LOADER_MANAGER_ID, null, this);
    }

    @Override
    public void deleteEvent(int eventId) {
        Uri deleteUri;
        deleteUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId);
        int rows = getContentResolver().delete(deleteUri, null, null);
        System.out.println("Row deleted: " + rows);

        //restart the Loader Manager from your activity to get the updated data
        getLoaderManager().restartLoader(LOADER_MANAGER_ID, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (args != null) {
            String sortOrder = args.getString("sortOrder");
            System.out.println("SORT ORDER " + sortOrder);
            return new CursorLoader(this, EVENTS_LIST_URI, EVENTS_LIST_PROJECTION, CalendarContract.Events.CALENDAR_ID + "=" + getMyCountriesCalendarId(), null, sortOrder);
        } else {
            return new CursorLoader(this, EVENTS_LIST_URI, EVENTS_LIST_PROJECTION, CalendarContract.Events.CALENDAR_ID + "=" + getMyCountriesCalendarId(), null, null);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null) {

        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
