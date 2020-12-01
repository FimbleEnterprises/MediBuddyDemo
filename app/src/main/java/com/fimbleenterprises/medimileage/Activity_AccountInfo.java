package com.fimbleenterprises.medimileage;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.graphics.fonts.SystemFonts;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.scwang.smart.refresh.footer.ClassicsFooter;
import com.scwang.smart.refresh.header.MaterialHeader;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnLoadMoreListener;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;

import org.joda.time.DateTime;

import java.io.File;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerTitleStrip;
import cz.msebera.android.httpclient.Header;
import jxl.format.Colour;
import jxl.write.WritableCell;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;

import static com.fimbleenterprises.medimileage.Queries.*;

/*import com.anychart.APIlib;
import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Cartesian;*/

public class Activity_AccountInfo extends AppCompatActivity {

    private static CrmEntities.Accounts.Account curAccount;
    private static ArrayList<CrmEntities.Accounts.Account> cachedAccounts;
    private static ArrayList<Territory> cachedTerritories;

    public static Activity activity;
    public static EditText title;
    public static MyUnderlineEditText date;
    public static Context context;
    public static MyViewPager mViewPager;
    public static PagerTitleStrip mPagerStrip;
    public static SectionsPagerAdapter sectionsPagerAdapter;
    public static androidx.fragment.app.FragmentManager fragMgr;
    public static MySettingsHelper options;

    public static final int PRODUCTFAMILY_MENU_ROOT = 3;
    public static final int PRODUCTSTATUS_MENU_ROOT = 4;

    // Receivers for date range changes at the activity level
    public static IntentFilter intentFilterMenuAction;
    public static BroadcastReceiver menuItemSelectedReceiver;

    public static IntentFilter intentFilterMonthYear;
    public static BroadcastReceiver goalsReceiverMtd;
    public static BroadcastReceiver goalsReceiverYtd;
    public static BroadcastReceiver salesLinesReceiver;
    public static BroadcastReceiver casesReceiver;
    public static BroadcastReceiver opportunitiesReceiver;

    public static final int REQUEST_CODE_CHANGE_ACCOUNT = 1;
    SearchView searchView;

    // vars for the date ranges
    public static int monthNum;
    public static int yearNum;

    // var for region
    public static boolean isEastRegion = true;

    // var for territoryid
    public static Territory territory;

    // The popup dialog for goals represented by the chart.
    public static Dialog chartPopupDialog;

    public final static String TAG = "TerritoryData";
    public static final String DATE_CHANGED = "DATE_CHANGED";
    public static final String MONTH = "MONTH";
    public static final String YEAR = "YEAR";
    public static final String MENU_ACTION = "MENU_ACTION";
    public static final String EXPORT_INVENTORY = "EXPORT_INVENTORY";

    public static Menu optionsMenu;
    public int curPageIndex = 0;

    public static boolean menuOpen = false;

    enum MenuAction {
        CHANGE_TERRITORY,
        CHANGE_ACCOUNT,
        SHOW_PROBES,
        SHOW_CABLES,
        SHOW_FLOWMETERS,
        SHOW_LICENSING,
        SHOW_ALL,
        SHOW_PROBES_IN_STOCK,
        SHOW_PROBES_RETURNED,
        SHOW_PROBES_EXPIRED,
        SHOW_FLOWMETERS_IN_STOCK,
        SHOW_FLOWMETERS_RETURNED,
        SHOW_CABLES_IN_STOCK,
        SHOW_CABLES_RETURNED,
        SHOW_LICENSES_IN_STOCK
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        context = this;
        activity = this;
        intentFilterMonthYear = new IntentFilter(DATE_CHANGED);
        intentFilterMenuAction = new IntentFilter(MENU_ACTION);

        // Log a metric
        // MileBuddyMetrics.updateMetric(this, MileBuddyMetrics.MetricName.LAST_ACCESSED_TERRITORY_DATA, DateTime.now());

        territory = new Territory();
        territory.territoryid = MediUser.getMe().territoryid;
        territory.territoryName = MediUser.getMe().territoryname;
        monthNum = DateTime.now().getMonthOfYear();
        yearNum = DateTime.now().getYear();

        setContentView(R.layout.activity_account_stuff);

        sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager = (MyViewPager) findViewById(R.id.main_pager_yo_sales_perf);
        mViewPager.onRealPageChangedListener = new MyViewPager.OnRealPageChangedListener() {
            @Override
            public void onPageActuallyFuckingChanged(int pageIndex) {

            }
        };
        mPagerStrip = (PagerTitleStrip) findViewById(R.id.pager_title_strip_sales_perf);
        mViewPager.setAdapter(sectionsPagerAdapter);
        mViewPager.setOffscreenPageLimit(0);
        mViewPager.setCurrentItem(0);
        mViewPager.setPageCount(6);
        mViewPager.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                destroyChartDialogIfVisible();
            }
        });

        // Set the viewpager font
        Typeface fontTypeFace = getResources().getFont(R.font.casual);
        for (int i = 0; i < mViewPager.getChildCount(); ++i) {
            View nextChild = mViewPager.getChildAt(i);
            for (int j = 0; j < ((PagerTitleStrip) nextChild).getChildCount(); j++) {
                View subChild = ((PagerTitleStrip) nextChild).getChildAt(j);
                if (subChild instanceof TextView) {
                    TextView textViewToConvert = (TextView) subChild;
                    textViewToConvert.setTypeface(fontTypeFace, Typeface.BOLD);
                }
            }
        }

        fragMgr = getSupportFragmentManager();

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == FullscreenActivityChooseAccount.ACCOUNT_CHOSEN_RESULT) {
            curAccount = data.getParcelableExtra(FullscreenActivityChooseAccount.ACCOUNT_RESULT);
            // Save the account list if it was returned
            cachedAccounts = data.getParcelableArrayListExtra(FullscreenActivityChooseAccount.CACHED_ACCOUNTS);

            if (curAccount != null) {
                Intent intentChosenAccount = new Intent(MENU_ACTION);
                intentChosenAccount.putExtra(FullscreenActivityChooseAccount.ACCOUNT_RESULT, curAccount);
                sendBroadcast(intentChosenAccount);
            }
            return;
        }

        if (resultCode == FullscreenActivityChooseTerritory.TERRITORY_CHOSEN_RESULT) {
            try {
                territory = data.getParcelableExtra(FullscreenActivityChooseTerritory.TERRITORY_RESULT);
                cachedTerritories = data.getParcelableArrayListExtra(FullscreenActivityChooseTerritory.CACHED_TERRITORIES);
                sectionsPagerAdapter.notifyDataSetChanged();

                // Show the change account dialog so that we have an account to show in this new territory
                Intent intentChangeAccount = new Intent(context, FullscreenActivityChooseAccount.class);
                intentChangeAccount.putExtra(FullscreenActivityChooseAccount.CURRENT_TERRITORY, territory);
                intentChangeAccount.putExtra(FullscreenActivityChooseAccount.CURRENT_ACCOUNT, curAccount);
                intentChangeAccount.putExtra(FullscreenActivityChooseAccount.CACHED_ACCOUNTS, cachedAccounts);
                startActivityForResult(intentChangeAccount, FullscreenActivityChooseAccount.REQUESTCODE);
                // Default to all items as the checked menu item
                optionsMenu.getItem(PRODUCTFAMILY_MENU_ROOT).setChecked(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // *****************************************************************************************
        // **   Reminder: returning, "true" indicates to the OS that the event has been handled   **
        // **   and to not let it percolate to the next receiver.                                 **
        // *****************************************************************************************

        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            Log.i(TAG, "onKeyDown back pressed!");

            if (!searchView.isIconified()) {
                searchView.onActionViewCollapsed();
                return true;
            } else if (curAccount != null) {
                curAccount = null;
                sendMenuItemSelectedBroadcast();
                return true;
            }

        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        menuOpen = true;
        return super.onMenuOpened(featureId, menu);
    }

    @Override
    public void onPanelClosed(int featureId, @NonNull Menu menu) {
        super.onPanelClosed(featureId, menu);
        menuOpen = false;
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (options == null) { options = new MySettingsHelper(context); }

        if (curAccount != null) {
            setTitle(curAccount.accountName);
        } else {
            setTitle("Choose an account");
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        curAccount = null;

    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.account_stuff, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo( searchManager.getSearchableInfo(new
                ComponentName(this, SearchResultsActivity.class)));

        if (searchView != null) {
            searchView.setInputType(InputType.TYPE_CLASS_NUMBER);
        }

        optionsMenu = menu;
        super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onPreparePanel(int featureId, @Nullable View view, @NonNull Menu menu) {

        switch (mViewPager.currentPosition) {
            case 0 : // Account inventory
                menu.findItem(R.id.action_choose_territory).setVisible(true);
                menu.findItem(R.id.action_choose_account).setVisible(true);
                menu.findItem(R.id.action_probes).setVisible(true);
                menu.findItem(R.id.action_flowmeters).setVisible(true);
                menu.findItem(R.id.action_cables).setVisible(true);
                menu.findItem(R.id.action_licensing).setVisible(true);
                break;
        }

        return super.onPreparePanel(featureId, view, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        // Change account or territory selected
        switch (item.getItemId()) {

            case 16908332 :
                onBackPressed();
                return true;

            case R.id.action_choose_territory :
                // change territory
                Intent intentChangeTerritory = new Intent(context, FullscreenActivityChooseTerritory.class);
                intentChangeTerritory.putExtra(FullscreenActivityChooseAccount.CURRENT_TERRITORY, territory);
                intentChangeTerritory.putExtra(FullscreenActivityChooseTerritory.CACHED_TERRITORIES, cachedTerritories);
                startActivityForResult(intentChangeTerritory, FullscreenActivityChooseTerritory.REQUESTCODE);
                // Default to all items as the checked menu item
                optionsMenu.getItem(PRODUCTFAMILY_MENU_ROOT).setChecked(true);
                return true;

            case R.id.action_choose_account :
                // change account
                Intent intentChangeAccount = new Intent(context, FullscreenActivityChooseAccount.class);
                intentChangeAccount.putExtra(FullscreenActivityChooseAccount.CURRENT_TERRITORY, territory);
                intentChangeAccount.putExtra(FullscreenActivityChooseAccount.CURRENT_ACCOUNT, curAccount);
                intentChangeAccount.putExtra(FullscreenActivityChooseAccount.CACHED_ACCOUNTS, cachedAccounts);
                startActivityForResult(intentChangeAccount, FullscreenActivityChooseAccount.REQUESTCODE);
                // Default to all items as the checked menu item
                optionsMenu.getItem(PRODUCTFAMILY_MENU_ROOT).setChecked(true);
                return true;

            case R.id.action_export_to_excel :
                Intent intentExportInventory = new Intent(MENU_ACTION);
                intentExportInventory.putExtra(EXPORT_INVENTORY, EXPORT_INVENTORY);
                sendBroadcast(intentExportInventory);
                return true;
        }

        // We will need these to properly check/uncheck items in the menu
        boolean isProductType = false, isProductStatus = false;

        // Set the product family item
        switch (item.getItemId()) {
            case R.id.action_probes :
            case R.id.action_flowmeters :
            case R.id.action_cables :
            case R.id.action_licensing :
                isProductType = true;
                break;
        }

        // Set the product status item
        switch (item.getItemId()) {
            case R.id.action_instock :
            case R.id.action_returned :
            case R.id.action_expired :
            case R.id.action_lost :
            case R.id.action_any :
                isProductStatus = true;
                break;
        }

        if (isProductType) {
            // Uncheck all the product types
            for (int i = 0; i < optionsMenu.getItem(PRODUCTFAMILY_MENU_ROOT).getSubMenu().size(); i++) {
                optionsMenu.getItem(PRODUCTFAMILY_MENU_ROOT).getSubMenu().getItem(i).setChecked(false);
            }
            // Check the selected product type
            optionsMenu.getItem(PRODUCTFAMILY_MENU_ROOT).getSubMenu().findItem(item.getItemId()).setChecked(true);
            sendMenuItemSelectedBroadcast();
        }

        if (isProductStatus) {
            // Uncheck all the product status selections
            for (int i = 0; i < optionsMenu.getItem(PRODUCTSTATUS_MENU_ROOT).getSubMenu().size(); i++) {
                optionsMenu.getItem(PRODUCTSTATUS_MENU_ROOT).getSubMenu().getItem(i).setChecked(false);
            }
            // Check the selected product status
            optionsMenu.getItem(PRODUCTSTATUS_MENU_ROOT).getSubMenu().findItem(item.getItemId()).setChecked(true);
            sendMenuItemSelectedBroadcast();
        }

        return true;
    }

    public static void destroyChartDialogIfVisible() {
        if (chartPopupDialog != null && chartPopupDialog.isShowing()) {
            chartPopupDialog.dismiss();
        }
    }

    public static void sendMenuItemSelectedBroadcast() {

        Intent menuAction = new Intent(MENU_ACTION);
        activity.sendBroadcast(menuAction);
    }

    public static String getRegionid() {
        if (isEastRegion) {
            return EAST_REGIONID;
        } else {
            return WEST_REGIONID;
        }
    }

    public static String getRegionName() {
        if (isEastRegion) {
            return "East Region";
        } else {
            return "West Region";
        }
    }

    @SuppressLint("NewApi")
    private void showMonthYearPicker() {
        final MonthYearPickerDialog mpd = new MonthYearPickerDialog();
        mpd.setListener(new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                Intent dateChanged = new Intent(DATE_CHANGED);
                dateChanged.putExtra(MONTH, month);
                dateChanged.putExtra(YEAR, year);
                monthNum = month;
                yearNum = year;
                sendBroadcast(dateChanged);
                mpd.dismiss();
            }
        });
        mpd.show(getSupportFragmentManager(), "MonthYearPickerDialog");
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(androidx.fragment.app.FragmentManager fm) {
            super(fm);
            sectionsPagerAdapter = this;
        }

        @Override
        public Fragment getItem(int position) {

            Log.d("getItem", "Creating Fragment in pager at index: " + position);
            Log.w(TAG, "getItem: PAGER POSITION: " + position);

            if (position == 0) {
                Fragment fragment = new Frag_AccountInventory();
                Bundle args = new Bundle();
                args.putInt(Frag_AccountInventory.ARG_SECTION_NUMBER, position + 1);
                fragment.setArguments(args);
                return fragment;
            }

            if (position == 1) {
                Fragment fragment = new Frag_Goals_MTD();
                Bundle args = new Bundle();
                args.putInt(Frag_Goals_MTD.ARG_SECTION_NUMBER, position + 1);
                fragment.setArguments(args);
                return fragment;
            }

            if (position == 2) {
                Fragment fragment = new Frag_Goals_YTD();
                Bundle args = new Bundle();
                args.putInt(Frag_Goals_YTD.ARG_SECTION_NUMBER, position + 1);
                fragment.setArguments(args);
                return fragment;
            }

            if (position == 3) {
                Fragment fragment = new Frag_Opportunities();
                Bundle args = new Bundle();
                args.putInt(Frag_Opportunities.ARG_SECTION_NUMBER, position + 1);
                fragment.setArguments(args);
                return fragment;
            }

            if (position == 4) {
                Fragment fragment = new Frag_Cases();
                Bundle args = new Bundle();
                args.putInt(Frag_Opportunities.ARG_SECTION_NUMBER, position + 1);
                fragment.setArguments(args);
                return fragment;
            }
            return null;
        }

        @Override
        public int getCount() {
            return 1;
        }

        @Override
        public CharSequence getPageTitle(int position) {

            curPageIndex = position;

            try {
                switch (position) {
                    case 0:
                        return Frag_AccountInventory.pageTitle;
                    case 1:
                        return "MTD Goals by Region";
                    case 2:
                        return "YTD Goals by Region";
                    case 3:
                        return "Opportunities";
                    case 4:
                        return "Cases";
                }
            } catch (Exception e) {
                e.printStackTrace();
                return "";
            }
            return null;
        }
    }

// ********************************** FRAGS *****************************************

    public static class Frag_AccountInventory extends Fragment {
        public static final String ARG_SECTION_NUMBER = "section_number";
        public View root;
        public RecyclerView recyclerView;
        RefreshLayout refreshLayout;
        AccountInventoryRecyclerAdapter adapter;
        ArrayList<CrmEntities.AccountProducts.AccountProduct> custInventory = new ArrayList<>();
        Button btnChooseAccount;
        ProductType productType = ProductType.PROBES; // Set a default so our first createview can show data
        ProductStatus productStatus = ProductStatus.IN_STOCK;
        public static String pageTitle = "Inventory";
        TextView txtNoInventory;


        enum ProductType {
            ALL, PROBES, CABLES, FLOWMETERS, LICENSES
        }

        enum ProductStatus {
            IN_STOCK, RETURNED, EXPIRED, ANY, LOST
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable ViewGroup container,
                                 @Nullable Bundle savedInstanceState) {
            root = inflater.inflate(R.layout.frag_account_inventory, container, false);
            txtNoInventory = root.findViewById(R.id.txtNoInventory);
            refreshLayout = root.findViewById(R.id.refreshLayout);
            options = new MySettingsHelper(context);
            RefreshLayout refreshLayout = root.findViewById(R.id.refreshLayout);
            refreshLayout.setRefreshHeader(new MaterialHeader(getContext()));
            refreshLayout.setOnRefreshListener(new OnRefreshListener() {
                @Override
                public void onRefresh(RefreshLayout refreshlayout) {
                    getAccountInventory();
                }
            });
            refreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
                @Override
                public void onLoadMore(RefreshLayout refreshlayout) {
                    refreshlayout.finishLoadMore(500/*,false*/);
                }
            });

            btnChooseAccount = root.findViewById(R.id.btnChooseAct);
            btnChooseAccount.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intentChangeAccount = new Intent(context, FullscreenActivityChooseAccount.class);
                    intentChangeAccount.putExtra(FullscreenActivityChooseAccount.CURRENT_TERRITORY, territory);
                    intentChangeAccount.putExtra(FullscreenActivityChooseAccount.CURRENT_ACCOUNT, curAccount);
                    intentChangeAccount.putExtra(FullscreenActivityChooseAccount.CACHED_ACCOUNTS, cachedAccounts);
                    startActivityForResult(intentChangeAccount, FullscreenActivityChooseAccount.REQUESTCODE);
                }
            });

            // Check if there is an account stipulated in preferences
            if (curAccount == null) {
                Log.w(TAG, "onCreateView: No account stipulated!");
                // do something!
            } else {
                Log.i(TAG, "onCreateView Account is stipulated (" + curAccount.accountnumber + ")");
            }

            recyclerView = root.findViewById(R.id.orderLinesRecyclerview);
            super.onCreateView(inflater, container, savedInstanceState);

            // Broadcast received regarding an options menu selection
            menuItemSelectedReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    Log.i(TAG, "onReceive Local receiver received broadcast!");
                    // Validate intent shit
                    if (intent != null && intent.getAction().equals(MENU_ACTION)) {

                        // Check if this is regarding an inventory export to Excel
                        if (intent.getStringExtra(EXPORT_INVENTORY) != null) {
                            // Ensure there is an account stipulated and inventory to export
                            if (curAccount != null && custInventory != null &&
                                    custInventory.size() > 0) {
                                // Export and share
                                ExcelSpreadsheet spreadsheet = exportToExcel(
                                        curAccount.accountnumber
                                                + "_inventory_export.xls");
                                Helpers.Files.shareFile(context, spreadsheet.file);
                            } else {
                                Toast.makeText(context, "No inventory to export!", Toast.LENGTH_SHORT).show();
                            }
                        }

                        // If the current account is null then clear the list.  This is to enable
                        // on back pressed behavior that doesn't automatically finish the activity.
                        if (curAccount == null) {
                            custInventory.clear();
                            populateList();
                        }

                        // *******************************************************************
                        // *                EVALUATE MENU SELECTIONS                         *
                        // *******************************************************************
                        // *                     PRODUCT TYPE                                *
                        // *******************************************************************
                        if (optionsMenu.getItem(PRODUCTFAMILY_MENU_ROOT).getSubMenu().getItem(0).isChecked()) {
                            productType = ProductType.PROBES;
                        }
                        if (optionsMenu.getItem(PRODUCTFAMILY_MENU_ROOT).getSubMenu().getItem(1).isChecked()) {
                            productType = ProductType.FLOWMETERS;
                        }
                        if (optionsMenu.getItem(PRODUCTFAMILY_MENU_ROOT).getSubMenu().getItem(2).isChecked()) {
                            productType = ProductType.CABLES;
                        }
                        if (optionsMenu.getItem(PRODUCTFAMILY_MENU_ROOT).getSubMenu().getItem(3).isChecked()) {
                            productType = ProductType.LICENSES;
                        }
                        // *******************************************************************
                        // *                     PRODUCT STATUS                              *
                        // *******************************************************************
                        if (optionsMenu.getItem(PRODUCTSTATUS_MENU_ROOT).getSubMenu().getItem(0).isChecked()) {
                            productStatus = ProductStatus.IN_STOCK;
                        }
                        if (optionsMenu.getItem(PRODUCTSTATUS_MENU_ROOT).getSubMenu().getItem(1).isChecked()) {
                            productStatus = ProductStatus.RETURNED;
                        }
                        if (optionsMenu.getItem(PRODUCTSTATUS_MENU_ROOT).getSubMenu().getItem(2).isChecked()) {
                            productStatus = ProductStatus.EXPIRED;
                        }
                        if (optionsMenu.getItem(PRODUCTSTATUS_MENU_ROOT).getSubMenu().getItem(3).isChecked()) {
                            productStatus = ProductStatus.LOST;
                        }
                        if (optionsMenu.getItem(PRODUCTSTATUS_MENU_ROOT).getSubMenu().getItem(4).isChecked()) {
                            productStatus = ProductStatus.ANY;
                        }
                        // *******************************************************************
                        // *                END EVALUATE MENU SELECTIONS                     *
                        // *******************************************************************


                        btnChooseAccount.setVisibility(curAccount == null ? View.VISIBLE : View.GONE);

                        getAccountInventory();
                        mViewPager.getAdapter().notifyDataSetChanged();
                    }
                }
            };

            // Get inventory using whatever menu selections are currently set
            getAccountInventory();

            return root;
        }



        @Override
        public void onStop() {
            super.onStop();

        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
            getActivity().unregisterReceiver(menuItemSelectedReceiver);
            Log.i(TAG, "onPause Unregistered the sales lines receiver");
        }

        @Override
        public void onResume() {

            // Register the options menu selected receiver
            getActivity().registerReceiver(menuItemSelectedReceiver, intentFilterMenuAction);

            // Hide/show the choose account button
            if (curAccount == null) {
                btnChooseAccount.setVisibility(View.VISIBLE);
            } else {
                btnChooseAccount.setVisibility(View.GONE);
            }

            Log.i(TAG, "onResume Registered the options menu receiver");
            super.onResume();
        }

        @Override
        public void onPause() {

            super.onPause();
        }

        protected void getAccountInventory() {
            String query = "";

            txtNoInventory.setVisibility(View.GONE);

            if (curAccount == null) {
                Toast.makeText(context, "Please select an account", Toast.LENGTH_SHORT).show();
                return;
            }

            CustomerInventoryStatusCode statusCode = CustomerInventoryStatusCode.ONSITE;
            switch (productStatus) {
                case ANY:
                    statusCode = CustomerInventoryStatusCode.ANY;
                    break;
                case RETURNED:
                    statusCode = CustomerInventoryStatusCode.RETURNED;
                    break;
                case EXPIRED:
                    statusCode = CustomerInventoryStatusCode.EXPIRED;
                    break;
                case LOST:
                    statusCode = CustomerInventoryStatusCode.LOST;
                    break;
                case IN_STOCK:
                    statusCode = CustomerInventoryStatusCode.ONSITE;
                    break;
            }

            switch (productType) {
                case PROBES:
                    query = Accounts.getAccountInventory(curAccount
                            .accountid,CrmEntities.AccountProducts.ITEM_GROUP_PROBES, statusCode);
                    break;
                case FLOWMETERS:
                    query = Accounts.getAccountInventory(curAccount
                            .accountid,CrmEntities.AccountProducts.ITEM_GROUP_FLOWMETERS, statusCode);
                    break;
                case CABLES:
                    query = Accounts.getAccountInventory(curAccount
                            .accountid,CrmEntities.AccountProducts.ITEM_GROUP_LICENSES, statusCode);
                    break;
                case LICENSES:
                    query = Accounts.getAccountInventory(curAccount
                            .accountid,CrmEntities.AccountProducts.ITEM_GROUP_CABLES, statusCode);
                    break;
            }

            getPagerTitle();

            refreshLayout.autoRefreshAnimationOnly();
            ArrayList<Requests.Argument> args = new ArrayList<>();
            Requests.Argument argument = new Requests.Argument("query", query);
            args.add(argument);
            Requests.Request request = new Requests.Request(Requests.Request.Function.GET, args);
            Crm crm = new Crm();
            crm.makeCrmRequest(getContext(), request, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    try {
                        String response = new String(responseBody);
                        Log.i(TAG, "onSuccess " + response);
                        custInventory = new CrmEntities.AccountProducts(response).list;
                        populateList();
                        refreshLayout.finishRefresh();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    Log.w(TAG, "onFailure: " + error.getLocalizedMessage());

                    refreshLayout.finishRefresh();
                }
            });
        }

        String getPagerTitle() {
            String txtAppend = "";

            switch (productType) {
                case PROBES:
                    txtAppend = "probes - ";
                    break;
                case FLOWMETERS:
                    txtAppend = "flowmeters - ";
                    break;
                case CABLES:
                    txtAppend = "cables - ";
                    break;
                case LICENSES:
                    txtAppend = "licenses - ";
                    break;
            }

            switch (productStatus) {
                case ANY:
                    txtAppend += "all";
                    break;
                case RETURNED:
                    txtAppend += "returned";
                    break;
                case EXPIRED:
                    txtAppend += "expired";
                    break;
                case LOST:
                    txtAppend += "lost";
                    break;
                case IN_STOCK:
                    txtAppend += "in stock/at location";
                    break;
            }

            pageTitle = "Inventory (" + txtAppend + ")";
            mViewPager.getAdapter().notifyDataSetChanged();
            return pageTitle;
        }

        protected void populateList() {

            btnChooseAccount.setVisibility(curAccount == null ? View.VISIBLE : View.GONE);

            ArrayList<CrmEntities.AccountProducts.AccountProduct> productList = new ArrayList<>();

            Log.i(TAG, "populateTripList: Preparing the dividers and trips...");
            for (int i = 0; i < (custInventory.size()); i++) {
                // Cur product
                CrmEntities.AccountProducts.AccountProduct accountProduct = custInventory.get(i);
                productList.add(accountProduct);
            }

            Log.i(TAG, "populateTripList Finished preparing the dividers and trips.");

            if (!getActivity().isFinishing()) {
                adapter = new AccountInventoryRecyclerAdapter(getContext(), productList);
                adapter.setClickListener(new AccountInventoryRecyclerAdapter.ItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        Toast.makeText(context, custInventory.get(position).productDescription, Toast.LENGTH_SHORT).show();
                    }
                });
                recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                recyclerView.setAdapter(adapter);

                refreshLayout.finishRefresh();
            } else {
                Log.w(TAG, "populateList: CAN'T POPULATE AS THE ACTIVITY IS FINISHING!!!");
            }

            getPagerTitle();

            if (custInventory == null || custInventory.size() == 0) {
                txtNoInventory.setVisibility(View.VISIBLE);
            } else {
                txtNoInventory.setVisibility(View.GONE);
            }

        }

        ExcelSpreadsheet exportToExcel(String filename) {

            ExcelSpreadsheet spreadsheet = null;

            final int SHEET1 = 0;

            // Create a new spreadsheet
            try {
                spreadsheet = new ExcelSpreadsheet(filename);
            } catch (Exception e) {
                // Toast.makeText(this, "Failed to create spreadsheet!\n" + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
                return null;
            }

            // Add the sheets that we will populate
            try {

                // All raw trips last 2 months
                spreadsheet.createSheet("Inventory", SHEET1);

            } catch (Exception e) {
                e.printStackTrace();
            }

            // Format header and content values
            WritableFont headerFont = new WritableFont(WritableFont.TAHOMA, 10, WritableFont.BOLD);
            try {
                headerFont.setColour(Colour.BLACK);
            } catch (Exception e) {
                e.printStackTrace();
            }
            WritableCellFormat headerFormat = new WritableCellFormat(headerFont);

            WritableFont contentFont = new WritableFont(WritableFont.TAHOMA, 10, WritableFont.NO_BOLD);
            try {
                contentFont.setColour(Colour.BLACK);
            } catch (Exception e) {
                e.printStackTrace();
            }
            WritableCellFormat contentFormat = new WritableCellFormat(contentFont);

            WritableFont capitalFont = new WritableFont(WritableFont.TAHOMA, 10, WritableFont.NO_BOLD);
            try {
                capitalFont.setColour(Colour.DARK_RED);
            } catch (Exception e) {
                e.printStackTrace();
            }
            WritableCellFormat capitalFormat = new WritableCellFormat(capitalFont);

            spreadsheet.addCell(SHEET1, 0, 0, curAccount.accountName, headerFormat);

            // Header row
            spreadsheet.addCell(SHEET1, 0, 1, "Product");
            spreadsheet.addCell(SHEET1, 1, 1, "Serial number");
            spreadsheet.addCell(SHEET1, 2, 1, "Status");
            spreadsheet.addCell(SHEET1, 3, 1, "Date modified");
            spreadsheet.addCell(SHEET1, 4, 1, "Is capital");
            spreadsheet.addCell(SHEET1, 5, 1, "Revision");

            for (int i = 2; i < custInventory.size() + 2; i++) {
                CrmEntities.AccountProducts.AccountProduct product = custInventory.get(i - 2);
                spreadsheet.addCell(SHEET1, 0, i, product.partNumber);
                spreadsheet.addCell(SHEET1, 1, i, product.serialnumber);
                spreadsheet.addCell(SHEET1, 2, i, product.statusFormatted);
                spreadsheet.addCell(SHEET1, 3, i, product.modifiedOnFormatted);
                spreadsheet.addCell(SHEET1, 4, i, product.isCapitalFormatted, product.isCapital ? capitalFormat : contentFormat);
                spreadsheet.addCell(SHEET1, 5, i, product.revision);
            }

            // Save the file
            spreadsheet.save();

            return spreadsheet;

        }

    }

    public static class Frag_Goals_MTD extends Fragment {
        private View rootView;
        public static final String ARG_SECTION_NUMBER = "section_number";
        // ProgressBar pbLoading;
        /*Cartesian bar;
        AnyChartView anyChartView;*/
        RefreshLayout mtdRefreshLayout;
        HorizontalBarChart chartMtd;
        TextView txtChartTitle;

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                                 @Nullable Bundle savedInstanceState) {
            rootView = inflater.inflate(R.layout.frag_sales_mtd, container, false);

            txtChartTitle = rootView.findViewById(R.id.txtChartTitle);
            chartMtd = rootView.findViewById(R.id.chartMtd);

            goalsReceiverMtd = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    Log.i(TAG, "onReceive Received month and year broadcast! (goals MTD frag)");
                    monthNum = intent.getIntExtra(MONTH, DateTime.now().getMonthOfYear());
                    yearNum = intent.getIntExtra(YEAR, DateTime.now().getYear());
                    getMtdGoalsByRegion();
                }
            };

            mtdRefreshLayout = (RefreshLayout) rootView.findViewById(R.id.mtdRefreshLayout);
            mtdRefreshLayout.setRefreshHeader(new MaterialHeader(getContext()));
            mtdRefreshLayout.setRefreshFooter(new ClassicsFooter(getContext()));
            mtdRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
                @Override
                public void onRefresh(RefreshLayout refreshlayout) {
                    getMtdGoalsByRegion();
                }
            });
            mtdRefreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
                @Override
                public void onLoadMore(RefreshLayout refreshlayout) {
                    refreshlayout.finishLoadMore(1/*,false*/);
                }
            });

            if (monthNum == 0) {
                monthNum = DateTime.now().getMonthOfYear();
            }

            if (yearNum == 0) {
                yearNum = DateTime.now().getYear();
            }

            getMtdGoalsByRegion();
            return rootView;
        }

        @Override
        public void onResume() {
            getActivity().registerReceiver(goalsReceiverMtd, intentFilterMonthYear);
            Log.i(TAG, "onResume Registered the goals receiver");
            super.onResume();
        }

        @Override
        public void onPause() {
            getActivity().unregisterReceiver(goalsReceiverMtd);
            Log.i(TAG, "onPause Unregistered the goals receiver");
            super.onPause();
        }

        void getMtdGoalsByRegion() {
            // pbLoading.setVisibility(View.VISIBLE);
            // anyChartView.setVisibility(View.GONE);
            mtdRefreshLayout.autoRefreshAnimationOnly();

            String query = Goals.getMtdGoalsByRegion(getRegionid(), monthNum, yearNum);
            ArrayList<Requests.Argument> args = new ArrayList<>();
            Requests.Argument argument = new Requests.Argument("query", query);
            args.add(argument);
            Requests.Request request = new Requests.Request(Requests.Request.Function.GET, args);
            Crm crm = new Crm();
            crm.makeCrmRequest(getContext(), request, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    String response = new String(responseBody);
                    CrmEntities.Goals goals = new CrmEntities.Goals(response);
                    Log.i(TAG, "onSuccess " + response);
                    populateChartMtd(goals);
                    mtdRefreshLayout.finishRefresh();
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    Log.w(TAG, "onFailure: " + error.getLocalizedMessage());
                    // pbLoading.setVisibility(View.GONE);
                    mtdRefreshLayout.finishRefresh();
                }
            });
        }

        void populateChartMtd(final CrmEntities.Goals goals) {

            String title = "MTD Goals " + getRegionName() + " (month: " + monthNum + ", year: " + yearNum + ")";
            
            txtChartTitle.setText(title);

            // Start building the containers to hold the goal data
            ArrayList<IBarDataSet> sets = new ArrayList<>();
            ArrayList<BarEntry> entries = new ArrayList<>();

            // Create and populate a container to hold the bar entries, labels and colors
            final ArrayList<String> xAxisLabel = new ArrayList<>();
            for(int i = 0; i < goals.size(); i++) {
                CrmEntities.Goal goal = goals.list.get(i);
                BarEntry entry = new BarEntry(i, goal.pct);
                entries.add(entry);
                xAxisLabel.add(goal.ownername + " (" + goal.territoryname + ")");
            }

            // Build a BarDataSet container and fill it with our data containers
            BarDataSet ds = new BarDataSet(entries, title);
            ds.setColors(ColorTemplate.MATERIAL_COLORS);
            sets.add(ds);
            BarData d = new BarData(sets);

            // Apply the chart data to the chart
            chartMtd.setData(d);

            // Hide the legend
            chartMtd.getLegend().setEnabled(false);

            // Aesthetics
            chartMtd.setDrawValueAboveBar(true);
            chartMtd.animateXY(2000, 2000);

            // Show each label for each entry
            ValueFormatter xAxisFormatter = new ValueFormatter() {
                @Override
                public String getBarLabel(BarEntry barEntry) {
                    return super.getBarLabel(barEntry);
                }
            };

            // Format the entries and labels
            XAxis xAxis = chartMtd.getXAxis();
            xAxis.setPosition(XAxis.XAxisPosition.TOP_INSIDE); // Where to put the labels
            xAxis.setDrawGridLines(false);
            xAxis.setGranularity(1f); // intervals
            xAxis.setLabelCount(xAxisLabel.size());
            xAxis.setValueFormatter(xAxisFormatter);
            xAxis.setGranularity(1f); // minimum axis-step (interval) is 1
            ValueFormatter formatter = new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    return xAxisLabel.get((int) value);
                }
            };
            xAxis.setValueFormatter(formatter);

            // Refresh the chart
            chartMtd.invalidate();

            // Make an onclick listener for chart values
            chartMtd.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
                @Override
                public void onValueSelected(Entry e, Highlight h) {
                    Log.i(TAG, "onValueSelected " + e.getData());

                    // Get the goal represented by the selected entry
                    CrmEntities.Goal selectedGoal = goals.list.get((int) e.getX());

                    // Show the goal summary for the selected entry
                    chartPopupDialog = new Dialog(context);
                    final Context c = context;
                    chartPopupDialog.setContentView(R.layout.generic_app_dialog);
                    chartPopupDialog.setTitle(selectedGoal.ownername);
                    chartPopupDialog.setCancelable(true);
                    final TextView txtMainText = chartPopupDialog.findViewById(R.id.txtMainText);
                    Button btnOkay = chartPopupDialog.findViewById(R.id.btnOkay);
                    btnOkay.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            chartPopupDialog.dismiss();
                        }
                    });
                    txtMainText.setText(
                            selectedGoal.ownername + "\n" +
                            "Month: " + selectedGoal.period + ", Year: " + selectedGoal.year + "\n" +
                            "\n" +
                            "Target: " + selectedGoal.getPrettyTarget() + "\n" +
                            "Actual: " + selectedGoal.getPrettyActual() + "\n" +
                            "Percent: " + selectedGoal.getPrettyPct() + "\n"
                    );
                    chartPopupDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                        @Override
                        public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                            if (keyCode == KeyEvent.KEYCODE_BACK) {
                                dialog.dismiss();
                                return true;
                            } else {
                                return false;
                            }
                        }
                    });
                    chartPopupDialog.show();

                }

                @Override
                public void onNothingSelected() {

                }
            }); // end onChartClickListener

        }

    }

    public static class Frag_Goals_YTD extends Fragment {
        private View rootView;
        public static final String ARG_SECTION_NUMBER = "section_number";
        // ProgressBar pbLoading;
        /*Cartesian bar;
        AnyChartView anyChartView;*/
        RefreshLayout ytdRefreshLayout;
        TextView txtChartTitle;
        HorizontalBarChart chartYtd;

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                                 @Nullable Bundle savedInstanceState) {
            rootView = inflater.inflate(R.layout.frag_sales_ytd, container, false);
            txtChartTitle = rootView.findViewById(R.id.txtChartTitle);
            chartYtd = (HorizontalBarChart) rootView.findViewById(R.id.chartYtd);
            goalsReceiverYtd = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    Log.i(TAG, "onReceive Received month and year broadcast! (goals YTD frag)");
                    yearNum = intent.getIntExtra(YEAR, DateTime.now().getYear());
                    getYtdGoalsByRegion();
                }
            };

            txtChartTitle = rootView.findViewById(R.id.txtChartTitle);

            ytdRefreshLayout = (RefreshLayout) rootView.findViewById(R.id.ytdRefreshLayout);
            ytdRefreshLayout.setRefreshHeader(new MaterialHeader(getContext()));
            ytdRefreshLayout.setRefreshFooter(new ClassicsFooter(getContext()));
            ytdRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
                @Override
                public void onRefresh(RefreshLayout refreshlayout) {
                    getYtdGoalsByRegion();
                }
            });
            ytdRefreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
                @Override
                public void onLoadMore(RefreshLayout refreshlayout) {
                    refreshlayout.finishLoadMore(1/*,false*/);
                }
            });

            if (monthNum == 0) {
                monthNum = DateTime.now().getMonthOfYear();
            }

            if (yearNum == 0) {
                yearNum = DateTime.now().getYear();
            }

            getYtdGoalsByRegion();
            return rootView;
        }

        @Override
        public void onResume() {
            getActivity().registerReceiver(goalsReceiverYtd, intentFilterMonthYear);
            Log.i(TAG, "onResume Registered the goals receiver");
            super.onResume();
        }

        @Override
        public void onPause() {
            getActivity().unregisterReceiver(goalsReceiverYtd);
            Log.i(TAG, "onPause Unregistered the goals receiver");
            super.onPause();
        }

        void getYtdGoalsByRegion() {
            // pbLoading.setVisibility(View.VISIBLE);
            // anyChartView.setVisibility(View.GONE);
            ytdRefreshLayout.autoRefreshAnimationOnly();

            String query = Goals.getYtdGoalsByRegion(getRegionid(),  yearNum);
            ArrayList<Requests.Argument> args = new ArrayList<>();
            Requests.Argument argument = new Requests.Argument("query", query);
            args.add(argument);
            Requests.Request request = new Requests.Request(Requests.Request.Function.GET, args);
            Crm crm = new Crm();
            crm.makeCrmRequest(getContext(), request, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    String response = new String(responseBody);
                    CrmEntities.Goals goals = new CrmEntities.Goals(response);
                    Log.i(TAG, "onSuccess " + response);
                    populateChartYtd(goals);
                    ytdRefreshLayout.finishRefresh();
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    Log.w(TAG, "onFailure: " + error.getLocalizedMessage());
                    // pbLoading.setVisibility(View.GONE);
                    ytdRefreshLayout.finishRefresh();
                }
            });
        }

        void populateChartYtd(final CrmEntities.Goals goals) {

            String title = "YTD Goals " + getRegionName() + " (year: " + yearNum + ")";

            txtChartTitle.setText(title);

            // Start building the containers to hold the goal data
            ArrayList<IBarDataSet> sets = new ArrayList<>();
            ArrayList<BarEntry> entries = new ArrayList<>();

            // Create a container to hold the bar entry labels and populate them
            final ArrayList<String> xAxisLabel = new ArrayList<>();
            for(int i = 0; i < goals.size(); i++) {
                CrmEntities.Goal goal = goals.list.get(i);
                BarEntry entry = new BarEntry(i, goal.pct);
                entries.add(entry);
                xAxisLabel.add(goal.ownername + " (" + goal.territoryname + ")");
            }

            // Create and populate a container to hold the bar entries, labels and colors
            BarDataSet ds = new BarDataSet(entries, title);
            ds.setColors(ColorTemplate.MATERIAL_COLORS);
            sets.add(ds);
            BarData d = new BarData(sets);

            // Apply the chart data to the chart
            chartYtd.setData(d);

            // Hide the legend
            chartYtd.getLegend().setEnabled(false);

            // Aesthetics
            chartYtd.setDrawValueAboveBar(true);
            chartYtd.animateXY(2000, 2000);

            // Show each label for each entry
            ValueFormatter xAxisFormatter = new ValueFormatter() {
                @Override
                public String getBarLabel(BarEntry barEntry) {
                    return super.getBarLabel(barEntry);
                }
            };

            // Format the entries and labels
            XAxis xAxis = chartYtd.getXAxis();
            xAxis.setPosition(XAxis.XAxisPosition.TOP_INSIDE); // Where to put the labels
            xAxis.setDrawGridLines(false);
            xAxis.setGranularity(1f); // intervals
            xAxis.setLabelCount(xAxisLabel.size());
            xAxis.setValueFormatter(xAxisFormatter);
            xAxis.setGranularity(1f); // minimum axis-step (interval) is 1
            ValueFormatter formatter = new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    return xAxisLabel.get((int) value);
                }
            };
            xAxis.setValueFormatter(formatter);

            // Refresh the chart
            chartYtd.invalidate();

            // Make an onclick listener for chart values
            chartYtd.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
                @Override
                public void onValueSelected(Entry e, Highlight h) {
                    Log.i(TAG, "onValueSelected " + e.getData());

                    // Get the goal represented by the selected entry
                    CrmEntities.Goal selectedGoal = goals.list.get((int) e.getX());

                    // Show the goal summary for the selected entry
                    chartPopupDialog = new Dialog(context);
                    final Context c = context;
                    chartPopupDialog.setContentView(R.layout.generic_app_dialog);
                    chartPopupDialog.setTitle(selectedGoal.ownername);
                    chartPopupDialog.setCancelable(true);
                    final TextView txtMainText = chartPopupDialog.findViewById(R.id.txtMainText);
                    Button btnOkay = chartPopupDialog.findViewById(R.id.btnOkay);
                    btnOkay.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            chartPopupDialog.dismiss();
                        }
                    });
                    txtMainText.setText(
                            selectedGoal.ownername + "\n" +
                            "Month: " + selectedGoal.period + ", Year: " + selectedGoal.year + "\n" +
                            "\n" +
                            "Target: " + selectedGoal.getPrettyTarget() + "\n" +
                            "Actual: " + selectedGoal.getPrettyActual() + "\n" +
                            "Percent: " + selectedGoal.getPrettyPct() + "\n"
                    );
                    chartPopupDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                        @Override
                        public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                            if (keyCode == KeyEvent.KEYCODE_BACK) {
                                dialog.dismiss();
                                return true;
                            } else {
                                return false;
                            }
                        }
                    });
                    chartPopupDialog.show();

                }

                @Override
                public void onNothingSelected() {

                }
            }); // end onChartClickListener

        }

    }

    public static class Frag_Opportunities extends Fragment {
        public static final String ARG_SECTION_NUMBER = "section_number";
        public View rootView;
        public RecyclerView listview;
        RefreshLayout refreshLayout;

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }

        @Override
        public void onPause() {
            super.onPause();
            getActivity().unregisterReceiver(opportunitiesReceiver);
            Log.i(TAG, "onResume Unregistered opportunities receiver");
        }

        @Override
        public void onResume() {
            super.onResume();
            getActivity().registerReceiver(opportunitiesReceiver, intentFilterMonthYear);
            Log.i(TAG, "onResume Registered opportunities receiver");
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            rootView = inflater.inflate(R.layout.frag_saleslines, container, false);
            listview = rootView.findViewById(R.id.opportunitiesRecyclerview);
            refreshLayout = rootView.findViewById(R.id.refreshLayout);

            super.onCreateView(inflater, container, savedInstanceState);
            opportunitiesReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    Log.i(TAG, "onReceive Received month and year broadcast! (opportunities frag)");
                    monthNum = intent.getIntExtra(MONTH, DateTime.now().getMonthOfYear());
                    yearNum = intent.getIntExtra(YEAR, DateTime.now().getYear());
                }
            };


            return rootView;
        }

    }

    public static class Frag_Cases extends Fragment {
        public static final String ARG_SECTION_NUMBER = "section_number";
        public View rootView;
        public RecyclerView listview;
        RefreshLayout refreshLayout;

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            rootView = inflater.inflate(R.layout.frag_saleslines, container, false);
            listview = rootView.findViewById(R.id.casesRecyclerview);
            refreshLayout = rootView.findViewById(R.id.refreshLayout);
            super.onCreateView(inflater, container, savedInstanceState);

            casesReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    Log.i(TAG, "onReceive Received month and year broadcast! (cases frag)");
                    monthNum = intent.getIntExtra(MONTH, DateTime.now().getMonthOfYear());
                    yearNum = intent.getIntExtra(YEAR, DateTime.now().getYear());
                }
            };

            return rootView;
        }

        @Override
        public void onResume() {
            super.onResume();
            getActivity().registerReceiver(casesReceiver, intentFilterMonthYear);
            Log.i(TAG, "onResume Registered the cases receiver");
        }

        @Override
        public void onPause() {
            super.onPause();
            getActivity().unregisterReceiver(casesReceiver);
            Log.i(TAG, "onPause Unregistered the cases receiver");
        }
    }

}
