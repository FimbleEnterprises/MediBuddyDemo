package com.fimbleenterprises.medimileage;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnLoadMoreListener;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;

import org.joda.time.DateTime;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerTitleStrip;
import cz.msebera.android.httpclient.Header;
import jxl.format.Colour;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;

import static com.fimbleenterprises.medimileage.Queries.*;

/*import com.anychart.APIlib;
import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Cartesian;*/

public class Activity_AccountData extends AppCompatActivity {

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

    public static final String GO_TO_ACCOUNT = "GO_TO_ACCOUNT";
    public static final String GO_TO_ACCOUNT_OBJECT = "GO_TO_ACCOUNT_OBJECT";

    public static final int PRODUCTFAMILY_MENU_ROOT = 2;
    public static final int PRODUCTSTATUS_MENU_ROOT = 3;
    public static final int CALL_PHONE_REQ = 123;

    // public static BroadcastReceiver inventoryMenuItemSelectedReceiver;
    // public static BroadcastReceiver salesMenuItemSelectedReceiver;

    // vars for the date ranges
    public static int monthNum;
    public static int yearNum;

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
    public static final String EXPORT_PAGE_INDEX = "EXPORT_PAGE_INDEX";
    public static boolean territoryPageOrigin = false;

    public static Menu optionsMenu;
    public int curPageIndex = 0;

    public static boolean menuOpen = false;
    // Receivers for date range changes at the activity level
    public static IntentFilter intentFilterMenuAction = new IntentFilter(MENU_ACTION);

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        context = this;
        activity = this;

        // Log a metric
        // MileBuddyMetrics.updateMetric(this, MileBuddyMetrics.MetricName.LAST_ACCESSED_TERRITORY_DATA, DateTime.now());

        if (getIntent() != null && getIntent().getAction() != null) {
            if (getIntent().getAction().equals(GO_TO_ACCOUNT)) {
                if (getIntent().getParcelableExtra(GO_TO_ACCOUNT_OBJECT) != null) {
                    curAccount = getIntent().getParcelableExtra(GO_TO_ACCOUNT_OBJECT);
                    territoryPageOrigin = true;
                }
            }
        }

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
                // intentChangeAccount.putExtra(FullscreenActivityChooseAccount.CACHED_ACCOUNTS, cachedAccounts);
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

            if (territoryPageOrigin) {
                finish();
                return true;
            }

            if (curAccount != null) {
                curAccount = null;
                sendBroadcast(new Intent(MENU_ACTION));
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

        optionsMenu = menu;
        super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onPreparePanel(int featureId, @Nullable View view, @NonNull Menu menu) {

        switch (mViewPager.currentPosition) {
            case SectionsPagerAdapter.INVENTORY_PAGE : // Account inventory
                // Main items
                menu.findItem(R.id.action_producttype).setVisible(true);
                menu.findItem(R.id.action_productstatus).setVisible(true);
                // Sub items
                menu.findItem(R.id.action_probes).setVisible(true);
                menu.findItem(R.id.action_flowmeters).setVisible(true);
                menu.findItem(R.id.action_cables).setVisible(true);
                menu.findItem(R.id.action_licensing).setVisible(true);
                menu.findItem(R.id.action_instock).setVisible(true);
                menu.findItem(R.id.action_returned).setVisible(true);
                menu.findItem(R.id.action_expired).setVisible(true);
                menu.findItem(R.id.action_lost).setVisible(true);
                menu.findItem(R.id.action_any).setVisible(true);
                // Excel
                menu.findItem(R.id.action_export_to_excel).setVisible(true);
                break;
            case SectionsPagerAdapter.SALES_LINE_PAGE : // Account sales lines
                // Main items
                menu.findItem(R.id.action_producttype).setVisible(false);
                menu.findItem(R.id.action_productstatus).setVisible(false);
                // Sub items
                menu.findItem(R.id.action_probes).setVisible(false);
                menu.findItem(R.id.action_flowmeters).setVisible(false);
                menu.findItem(R.id.action_cables).setVisible(false);
                menu.findItem(R.id.action_licensing).setVisible(false);
                menu.findItem(R.id.action_instock).setVisible(false);
                menu.findItem(R.id.action_returned).setVisible(false);
                menu.findItem(R.id.action_expired).setVisible(false);
                menu.findItem(R.id.action_lost).setVisible(false);
                menu.findItem(R.id.action_any).setVisible(false);
                // Excel
                menu.findItem(R.id.action_export_to_excel).setVisible(true);
                break;
            case SectionsPagerAdapter.CONTACTS_PAGE : // Contact lines
                // Main items
                menu.findItem(R.id.action_producttype).setVisible(false);
                menu.findItem(R.id.action_productstatus).setVisible(false);
                // Sub items
                menu.findItem(R.id.action_probes).setVisible(false);
                menu.findItem(R.id.action_flowmeters).setVisible(false);
                menu.findItem(R.id.action_cables).setVisible(false);
                menu.findItem(R.id.action_licensing).setVisible(false);
                menu.findItem(R.id.action_instock).setVisible(false);
                menu.findItem(R.id.action_returned).setVisible(false);
                menu.findItem(R.id.action_expired).setVisible(false);
                menu.findItem(R.id.action_lost).setVisible(false);
                menu.findItem(R.id.action_any).setVisible(false);
                // Excel
                menu.findItem(R.id.action_export_to_excel).setVisible(false);
            case SectionsPagerAdapter.OPPORTUNITIES_PAGE: // Opportunity lines
                // Main items
                menu.findItem(R.id.action_producttype).setVisible(false);
                menu.findItem(R.id.action_productstatus).setVisible(false);
                // Sub items
                menu.findItem(R.id.action_probes).setVisible(false);
                menu.findItem(R.id.action_flowmeters).setVisible(false);
                menu.findItem(R.id.action_cables).setVisible(false);
                menu.findItem(R.id.action_licensing).setVisible(false);
                menu.findItem(R.id.action_instock).setVisible(false);
                menu.findItem(R.id.action_returned).setVisible(false);
                menu.findItem(R.id.action_expired).setVisible(false);
                menu.findItem(R.id.action_lost).setVisible(false);
                menu.findItem(R.id.action_any).setVisible(false);
                // Excel
                menu.findItem(R.id.action_export_to_excel).setVisible(false);
                break;
            case SectionsPagerAdapter.TICKETS_PAGE: // Ticket lines
                // Main items
                menu.findItem(R.id.action_producttype).setVisible(false);
                menu.findItem(R.id.action_productstatus).setVisible(false);
                // Sub items
                menu.findItem(R.id.action_probes).setVisible(false);
                menu.findItem(R.id.action_flowmeters).setVisible(false);
                menu.findItem(R.id.action_cables).setVisible(false);
                menu.findItem(R.id.action_licensing).setVisible(false);
                menu.findItem(R.id.action_instock).setVisible(false);
                menu.findItem(R.id.action_returned).setVisible(false);
                menu.findItem(R.id.action_expired).setVisible(false);
                menu.findItem(R.id.action_lost).setVisible(false);
                menu.findItem(R.id.action_any).setVisible(false);
                // Excel
                menu.findItem(R.id.action_export_to_excel).setVisible(false);
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
                intentExportInventory.putExtra(EXPORT_PAGE_INDEX, curPageIndex);
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

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public static final int INVENTORY_PAGE = 2;
        public static final int SALES_LINE_PAGE = 1;
        public static final int CONTACTS_PAGE = 0;
        public static final int OPPORTUNITIES_PAGE = 3;
        public static final int TICKETS_PAGE = 4;

        public SectionsPagerAdapter(androidx.fragment.app.FragmentManager fm) {
            super(fm);
            sectionsPagerAdapter = this;
        }

        @Override
        public Fragment getItem(int position) {

            Log.d("getItem", "Creating Fragment in pager at index: " + position);
            Log.w(TAG, "getItem: PAGER POSITION: " + position);

            if (position == INVENTORY_PAGE) {
                Fragment fragment = new Frag_AccountInventory();
                Bundle args = new Bundle();
                args.putInt(Frag_SalesLines.ARG_SECTION_NUMBER, position + 1);
                fragment.setArguments(args);
                return fragment;
            }

            if (position == SALES_LINE_PAGE) {
                Fragment fragment = new Frag_SalesLines();
                Bundle args = new Bundle();
                args.putInt(Frag_SalesLines.ARG_SECTION_NUMBER, position + 1);
                fragment.setArguments(args);
                return fragment;
            }

            if (position == CONTACTS_PAGE) {
                Fragment fragment = new Frag_Contacts();
                Bundle args = new Bundle();
                args.putInt(Frag_Contacts.ARG_SECTION_NUMBER, position + 1);
                fragment.setArguments(args);
                return fragment;
            }

            if (position == OPPORTUNITIES_PAGE) {
                Fragment fragment = new Frag_Opportunities();
                Bundle args = new Bundle();
                args.putInt(Frag_Tickets.ARG_SECTION_NUMBER, position + 1);
                fragment.setArguments(args);
                return fragment;
            }

            if (position == TICKETS_PAGE) {
                Fragment fragment = new Frag_Tickets();
                Bundle args = new Bundle();
                args.putInt(Frag_Tickets.ARG_SECTION_NUMBER, position + 1);
                fragment.setArguments(args);
                return fragment;
            }

            return null;
        }

        @Override
        public int getCount() {
            return 5;
        }

        @Override
        public CharSequence getPageTitle(int position) {

            curPageIndex = position;

            try {
                switch (position) {
                    case INVENTORY_PAGE:
                        return Frag_AccountInventory.pageTitle;
                    case SALES_LINE_PAGE:
                        return Frag_SalesLines.pageTitle;
                    case CONTACTS_PAGE:
                        return Frag_Contacts.pageTitle;
                    case OPPORTUNITIES_PAGE:
                        return Frag_Opportunities.pageTitle;
                    case TICKETS_PAGE:
                        return Frag_Tickets.pageTitle;
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
        BroadcastReceiver parentActivityMenuReceiver;
        CrmEntities.Accounts.Account lastAccount;


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
            refreshLayout.setEnableLoadMore(false);
            refreshLayout.setOnRefreshListener(new OnRefreshListener() {
                @Override
                public void onRefresh(RefreshLayout refreshlayout) {
                    getAccountInventory(true);
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

            if (lastAccount != curAccount) {
                getAccountInventory(true);
                lastAccount = curAccount;
            }

            recyclerView = root.findViewById(R.id.orderLinesRecyclerview);
            super.onCreateView(inflater, container, savedInstanceState);

            // Broadcast received regarding an options menu selection
            parentActivityMenuReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    Log.i(TAG, "onReceive Local receiver received broadcast!");
                    // Validate intent shit
                    if (intent != null && intent.getAction().equals(MENU_ACTION)) {

                        // Check if this is regarding an inventory export to Excel
                        if (intent.getStringExtra(EXPORT_INVENTORY) != null) {

                            // Check which active page was stipulated by the caller so we know if our list is desired
                            if (intent.getIntExtra(EXPORT_PAGE_INDEX, -1) == SectionsPagerAdapter.INVENTORY_PAGE) {

                                // Ensure there is an account stipulated and inventory to export
                                if (curAccount != null && custInventory != null &&
                                        custInventory.size() > 0) {
                                    // Export and share
                                    ExcelSpreadsheet spreadsheet = exportToExcel(
                                            curAccount.accountName
                                                    + "_inventory_export.xls");
                                    Helpers.Files.shareFile(context, spreadsheet.file);
                                } else {
                                    Toast.makeText(context, "No inventory to export!", Toast.LENGTH_SHORT).show();
                                }

                            } // is inventory page
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

                        getAccountInventory(true);
                        mViewPager.getAdapter().notifyDataSetChanged();
                    }
                }
            };

            // Get inventory using whatever menu selections are currently set
            getAccountInventory((lastAccount != curAccount));

            return root;
        }

        @Override
        public void onStop() {
            super.onStop();
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();

            getActivity().unregisterReceiver(parentActivityMenuReceiver);
            Log.i(TAG, "Unregistered the account products receiver");
        }

        @Override
        public void onResume() {

            // Register the options menu selected receiver
            getActivity().registerReceiver(parentActivityMenuReceiver, intentFilterMenuAction);
            Log.i(TAG, "onResume Registered the account products receiver");

            // Hide/show the choose account button
            if (curAccount == null) {
                btnChooseAccount.setVisibility(View.VISIBLE);
            } else {
                btnChooseAccount.setVisibility(View.GONE);
            }

            super.onResume();
        }

        @Override
        public void onPause() {
            super.onPause();
        }

        protected void getAccountInventory(boolean forceRefresh) {
            String query = "";

            if (custInventory != null && custInventory.size() > 0 && forceRefresh != true) {
                populateList();
                return;
            }

            lastAccount = curAccount;

            txtNoInventory.setVisibility(View.GONE);

            if (curAccount == null) {
                // Toast.makeText(context, "Please select an account", Toast.LENGTH_SHORT).show();
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

            final ArrayList<CrmEntities.AccountProducts.AccountProduct> productList = new ArrayList<>();

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
                        ArrayList<Requests.Argument> args = new ArrayList<>();
                        String query = Queries.CustomerInventory.getCustomerInventoryDetails(productList.get(position).customerinventoryid);
                        args.add(new Requests.Argument("query", query));
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

    public static class Frag_SalesLines extends Fragment {
        public static final String ARG_SECTION_NUMBER = "section_number";
        public View root;
        public RecyclerView recyclerView;
        RefreshLayout refreshLayout;
        OrderLineRecyclerAdapter adapter;
        ArrayList<CrmEntities.OrderProducts.OrderProduct> allOrders = new ArrayList<>();
        Button btnChooseAccount;
        BroadcastReceiver parentActivityMenuReceiver;
        public static String pageTitle = "Sales";
        TextView txtNoSales;
        CrmEntities.Accounts.Account lastAccount;

        @Nullable
        @Override
        public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable ViewGroup container,
                                 @Nullable Bundle savedInstanceState) {
            root = inflater.inflate(R.layout.frag_saleslines, container, false);
            txtNoSales = root.findViewById(R.id.txtNoContacts);
            refreshLayout = root.findViewById(R.id.refreshLayout);
            options = new MySettingsHelper(context);
            RefreshLayout refreshLayout = root.findViewById(R.id.refreshLayout);
            refreshLayout.setEnableLoadMore(false);
            refreshLayout.setOnRefreshListener(new OnRefreshListener() {
                @Override
                public void onRefresh(RefreshLayout refreshlayout) {
                    getAccountSales(true);
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

            if (lastAccount != curAccount) {
                getAccountSales(true);
                lastAccount = curAccount;
            }

            recyclerView = root.findViewById(R.id.orderLinesRecyclerview);
            super.onCreateView(inflater, container, savedInstanceState);

            // Broadcast received regarding an options menu selection
            parentActivityMenuReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    Log.i(TAG, "onReceive Local receiver received broadcast!");
                    // Validate intent shit
                    if (intent != null && intent.getAction().equals(MENU_ACTION)) {

                        if (intent.getIntExtra(EXPORT_PAGE_INDEX, -1) == SectionsPagerAdapter.SALES_LINE_PAGE) {
                            // Check if this is regarding an inventory export to Excel
                            if (intent.getStringExtra(EXPORT_INVENTORY) != null) {
                                // Ensure there is an account stipulated and inventory to export
                                if (curAccount != null && allOrders != null &&
                                        allOrders.size() > 0) {
                                    // Export and share
                                    ExcelSpreadsheet spreadsheet = exportToExcel(
                                            curAccount.accountName
                                                    + "_sales_export.xls");
                                    Helpers.Files.shareFile(context, spreadsheet.file);
                                } else {
                                    Toast.makeText(context, "No inventory to export!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        } // is sales line page

                        // If the current account is null then clear the list.  This is to enable
                        // on back pressed behavior that doesn't automatically finish the activity.
                        if (curAccount == null) {
                            allOrders.clear();
                            populateList();
                        }

                        btnChooseAccount.setVisibility(curAccount == null ? View.VISIBLE : View.GONE);

                        getAccountSales(true);

                        mViewPager.getAdapter().notifyDataSetChanged();
                    }
                }
            };

            // Get inventory using whatever menu selections are currently set
            // getAccountInventory();

            getAccountSales(false);

            return root;
        }

        @Override
        public void onStop() {
            super.onStop();
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();

            getActivity().unregisterReceiver(parentActivityMenuReceiver);
            Log.i(TAG, "Unregistered the account contacts receiver");
        }

        @Override
        public void onResume() {

            // Register the options menu selected receiver
            getActivity().registerReceiver(parentActivityMenuReceiver, intentFilterMenuAction);

            // Hide/show the choose account button
            if (curAccount == null) {
                btnChooseAccount.setVisibility(View.VISIBLE);
            } else {
                btnChooseAccount.setVisibility(View.GONE);
            }

            Log.i(TAG, "onResume Registered the account sales receiver");
            super.onResume();
        }

        @Override
        public void onPause() {
            super.onPause();
        }

        protected void getAccountSales(boolean forceRefresh) {

            if (curAccount == null) {
                return;
            }

            if (allOrders != null && allOrders.size() > 0 && forceRefresh != true) {
                populateList();
                return;
            }

            lastAccount = curAccount;

            String query = Queries.OrderLines.getOrderLinesByAccount(curAccount.accountid, Operators.DateOperator.LAST_X_MONTHS, 3);

            txtNoSales.setVisibility(View.GONE);

            if (curAccount == null) {
                Toast.makeText(context, "Please select an account", Toast.LENGTH_SHORT).show();
                return;
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
                        allOrders = new CrmEntities.OrderProducts(response).list;
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

            pageTitle = "Sales (last 3 months)";
            mViewPager.getAdapter().notifyDataSetChanged();
            return pageTitle;
        }

        protected void populateList() {

            final ArrayList<CrmEntities.OrderProducts.OrderProduct> orderList = new ArrayList<>();

            boolean addedTodayHeader = false;
            boolean addedYesterdayHeader = false;
            boolean addedThisWeekHeader = false;
            boolean addedThisMonthHeader = false;
            boolean addedLastMonthHeader = false;
            boolean addedOlderHeader = false;

            // Now today's date attributes
            int todayDayOfYear = Helpers.DatesAndTimes.returnDayOfYear(DateTime.now());
            int todayWeekOfYear = Helpers.DatesAndTimes.returnWeekOfYear(DateTime.now());
            int todayMonthOfYear = Helpers.DatesAndTimes.returnMonthOfYear(DateTime.now());

            Log.i(TAG, "populateTripList: Preparing the dividers and trips...");
            for (int i = 0; i < (allOrders.size()); i++) {
                int orderDayOfYear = Helpers.DatesAndTimes.returnDayOfYear(allOrders.get(i).orderDate);
                int orderWeekOfYear = Helpers.DatesAndTimes.returnWeekOfYear(allOrders.get(i).orderDate);
                int orderMonthOfYear = Helpers.DatesAndTimes.returnMonthOfYear(allOrders.get(i).orderDate);

                // Trip was today
                if (orderDayOfYear == todayDayOfYear) {
                    if (addedTodayHeader == false) {
                        CrmEntities.OrderProducts.OrderProduct headerObj = new CrmEntities.OrderProducts.OrderProduct();
                        headerObj.isSeparator = true;
                        headerObj.setTitle("Today");
                        orderList.add(headerObj);
                        addedTodayHeader = true;
                        Log.d(TAG + "getAllFullTrips", "Added a header object to the array that will eventually be a header childView in the list view named, 'Today' - This will not be added again!");
                    }
                    // Trip was yesterday
                } else if (orderDayOfYear == (todayDayOfYear - 1)) {
                    if (addedYesterdayHeader == false) {
                        CrmEntities.OrderProducts.OrderProduct headerObj = new CrmEntities.OrderProducts.OrderProduct();
                        headerObj.isSeparator = true;
                        headerObj.setTitle("Yesterday");
                        orderList.add(headerObj);
                        addedYesterdayHeader = true;
                        Log.d(TAG + "getAllFullTrips", "Added a header object to the array that will eventually be a header childView in the list view named, 'Yesterday' - This will not be added again!");
                    }

                    // Trip was this week
                } else if (orderWeekOfYear == todayWeekOfYear) {
                    if (addedThisWeekHeader == false) {
                        CrmEntities.OrderProducts.OrderProduct headerObj = new CrmEntities.OrderProducts.OrderProduct();
                        headerObj.isSeparator = true;
                        headerObj.setTitle("This week");
                        orderList.add(headerObj);
                        addedThisWeekHeader = true;
                        Log.d(TAG + "getAllFullTrips", "Added a header object to the array that will eventually be a header childView in the list view named, 'This week' - This will not be added again!");
                    }

                    // Trip was this month
                } else if (orderMonthOfYear == todayMonthOfYear) {
                    if (addedThisMonthHeader == false) {
                        CrmEntities.OrderProducts.OrderProduct headerObj = new CrmEntities.OrderProducts.OrderProduct();
                        headerObj.isSeparator = true;
                        headerObj.setTitle("This month");
                        orderList.add(headerObj);
                        addedThisMonthHeader = true;
                        Log.d(TAG + "getAllFullTrips", "Added a header object to the array that will eventually be a header childView in the list view named, 'This month' - This will not be added again!");
                    }

                    // Trip was older than this month
                } else if (orderMonthOfYear < todayMonthOfYear) {
                    if (addedOlderHeader == false) {
                        CrmEntities.OrderProducts.OrderProduct headerObj = new CrmEntities.OrderProducts.OrderProduct();
                        headerObj.isSeparator = true;
                        headerObj.setTitle("Last month and older");
                        orderList.add(headerObj);
                        addedOlderHeader = true;
                        Log.d(TAG + "getAllFullTrips", "Added a header object to the array that will eventually be a header childView in the list view named, 'Older' - This will not be added again!");
                    }
                }

                CrmEntities.OrderProducts.OrderProduct orderProduct = allOrders.get(i);
                orderList.add(orderProduct);
            }

            Log.i(TAG, "populateTripList Finished preparing the dividers and trips.");

            if (!getActivity().isFinishing()) {
                adapter = new OrderLineRecyclerAdapter(getContext(), orderList);
                adapter.setClickListener(new OrderLineRecyclerAdapter.ItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        final MyProgressDialog progressDialog = new MyProgressDialog(context , "Getting order details...");
                        progressDialog.show();
                        ArrayList<Requests.Argument> args = new ArrayList<>();
                        String query = Queries.OrderLines.getOrderLines(orderList.get(position).salesorderid);
                        args.add(new Requests.Argument("query", query));
                        Requests.Request request = new Requests.Request(Requests.Request.Function.GET, args);
                        new Crm().makeCrmRequest(getContext(), request, new AsyncHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                                String response = new String(responseBody);
                                CrmEntities.OrderProducts orderLines = new CrmEntities.OrderProducts(response);
                                progressDialog.dismiss();
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                                progressDialog.dismiss();
                            }
                        });

                    }
                });
                recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                recyclerView.setAdapter(adapter);

                refreshLayout.finishRefresh();
            } else {
                Log.w(TAG, "populateList: CAN'T POPULATE AS THE ACTIVITY IS FINISHING!!!");
            }

            txtNoSales.setVisibility( (allOrders == null || allOrders.size() == 0) ? View.VISIBLE : View.GONE);
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
                spreadsheet.createSheet("Sales Lines", SHEET1);

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

            spreadsheet.addCell(SHEET1, 0, 0, curAccount.accountName, headerFormat);

            // Header row
            spreadsheet.addCell(SHEET1, 0, 1, "Qty", headerFormat);
            spreadsheet.addCell(SHEET1, 1, 1, "Product", headerFormat);
            spreadsheet.addCell(SHEET1, 2, 1, "Account number", headerFormat);
            spreadsheet.addCell(SHEET1, 3, 1, "Account", headerFormat);
            spreadsheet.addCell(SHEET1, 4, 1, "Sales order", headerFormat);
            spreadsheet.addCell(SHEET1, 5, 1, "Order date", headerFormat);
            spreadsheet.addCell(SHEET1, 6, 1, "Revenue", headerFormat);

            for (int i = 2; i < allOrders.size() + 2; i++) {
                CrmEntities.OrderProducts.OrderProduct product = allOrders.get(i - 2);
                spreadsheet.addCell(SHEET1, 0, i, Integer.toString(product.qty));
                spreadsheet.addCell(SHEET1, 1, i, product.partNumber);
                spreadsheet.addCell(SHEET1, 2, i, product.accountnumber);
                spreadsheet.addCell(SHEET1, 3, i, product.customeridFormatted);
                spreadsheet.addCell(SHEET1, 4, i, product.salesorderidFormatted);
                spreadsheet.addCell(SHEET1, 5, i, product.orderdateFormatted);
                spreadsheet.addCell(SHEET1, 6, i, Double.toString(product.extendedAmt));
            }

            // Save the file
            spreadsheet.save();

            return spreadsheet;

        }

    }

    public static class Frag_Contacts extends Fragment {
        public static final String ARG_SECTION_NUMBER = "section_number";
        public View root;
        public RecyclerView recyclerView;
        RefreshLayout refreshLayout;
        BasicObjectRecyclerAdapter adapter;
        ArrayList<BasicObject> objects = new ArrayList<>();
        Button btnChooseAccount;
        public static String pageTitle = "Contacts";
        TextView txtNoContacts;
        BroadcastReceiver parentActivityMenuReceiver;
        CrmEntities.Accounts.Account lastAccount;
        String attemptedPhonenumber;


        @Nullable
        @Override
        public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable ViewGroup container,
                                 @Nullable Bundle savedInstanceState) {
            root = inflater.inflate(R.layout.frag_contacts, container, false);
            txtNoContacts = root.findViewById(R.id.txtNoContacts);
            refreshLayout = root.findViewById(R.id.refreshLayout);
            refreshLayout.setEnableLoadMore(false);
            options = new MySettingsHelper(context);
            RefreshLayout refreshLayout = root.findViewById(R.id.refreshLayout);
            refreshLayout.setOnRefreshListener(new OnRefreshListener() {
                @Override
                public void onRefresh(RefreshLayout refreshlayout) {
                    getAccountContacts(true);
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

            if (lastAccount != curAccount) {
                getAccountContacts(true);
                lastAccount = curAccount;
            }

            recyclerView = root.findViewById(R.id.orderLinesRecyclerview);
            super.onCreateView(inflater, container, savedInstanceState);

            // Broadcast received regarding an options menu selection
            parentActivityMenuReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    Log.i(TAG, "onReceive Local receiver received broadcast!");
                    // Validate intent shit
                    if (intent != null && intent.getAction().equals(MENU_ACTION)) {

                        if (intent.getIntExtra(EXPORT_PAGE_INDEX, -1) == SectionsPagerAdapter.CONTACTS_PAGE) {
                            // Check if this is regarding an inventory export to Excel
                            if (intent.getStringExtra(EXPORT_INVENTORY) != null) {
                                // Ensure there is an account stipulated and inventory to export
                                if (curAccount != null && objects != null &&
                                        objects.size() > 0) {
                                } else {
                                    Toast.makeText(context, "No sales to export!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        } // is sales line page

                        // If the current account is null then clear the list.  This is to enable
                        // on back pressed behavior that doesn't automatically finish the activity.
                        if (curAccount == null) {
                            objects.clear();
                            populateList();
                        }

                        btnChooseAccount.setVisibility(curAccount == null ? View.VISIBLE : View.GONE);

                        getAccountContacts(true);

                        mViewPager.getAdapter().notifyDataSetChanged();
                    }
                }
            };

            getAccountContacts(false);

            return root;
        }

        @Override
        public void onStop() {
            super.onStop();
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();

            getActivity().unregisterReceiver(parentActivityMenuReceiver);
            Log.i(TAG, "Unregistered the account contacts receiver");
        }

        @Override
        public void onResume() {

            // Register the options menu selected receiver
            getActivity().registerReceiver(parentActivityMenuReceiver, intentFilterMenuAction);

            // Hide/show the choose account button
            if (curAccount == null) {
                btnChooseAccount.setVisibility(View.VISIBLE);
            } else {
                btnChooseAccount.setVisibility(View.GONE);
            }

            Log.i(TAG, "onResume Registered the account contacts receiver");
            super.onResume();
        }

        @Override
        public void onPause() {
            super.onPause();
        }

        @Override
        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);

            try {
                if (requestCode == CALL_PHONE_REQ && Helpers.Permissions.isGranted(Helpers.Permissions.PermissionType.CALL_PHONE)) {
                    Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + attemptedPhonenumber));
                    startActivity(intent);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            Log.i(TAG, "onRequestPermissionsResult ");

        }

        protected void getAccountContacts(boolean forceRefresh) {

            if (curAccount == null) {
                return;
            }

            String query = Queries.Contacts.getContacts(curAccount.accountid);

            txtNoContacts.setVisibility(View.GONE);

            getPagerTitle();

            if (objects != null && objects.size() > 0 && forceRefresh != true) {
                populateList();
                return;
            }

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
                        CrmEntities.Contacts contacts = new CrmEntities.Contacts(response);
                        objects.clear();
                        for (CrmEntities.Contacts.Contact contact : contacts.list) {
                            BasicObject object = new BasicObject(contact.fullname, contact.jobtitle, contact);
                            objects.add(object);
                        }
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

        protected void populateList() {

            for (int i = 0; i < (objects.size()); i++) {
                adapter = new BasicObjectRecyclerAdapter(context, objects);
            }

            // Check if adapter is null and leave if so.  If user hasn't selected an account this will be the case
            if (adapter == null) {
                return;
            }

            Log.i(TAG, "populateTripList Finished preparing the dividers and trips.");

            if (!getActivity().isFinishing()) {
                recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                recyclerView.setAdapter(adapter);
                adapter.setClickListener(new BasicObjectRecyclerAdapter.ItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        try {
                            BasicObject clickedObject = objects.get(position);
                            CrmEntities.Contacts.Contact clickedContact = (CrmEntities.Contacts.Contact)
                                    clickedObject.object;
                            Log.i(TAG, "onItemClick Clicked item at position " + position
                                    + "(" + clickedObject.title + ")");

                            ContactActions contactActions = new ContactActions(getActivity(), clickedContact);
                            contactActions.showContactOptions();
                        } catch (Exception e) { e.printStackTrace(); }
                    }
                });
                refreshLayout.finishRefresh();
            } else {
                Log.w(TAG, "populateList: CAN'T POPULATE AS THE ACTIVITY IS FINISHING!!!");
            }
            txtNoContacts.setVisibility( (objects == null || objects.size() == 0) ? View.VISIBLE : View.GONE);
        }

        String getPagerTitle() {
            pageTitle = "Contacts";
            mViewPager.getAdapter().notifyDataSetChanged();
            return pageTitle;
        }

    }

    public static class Frag_Opportunities extends Fragment {
        public static final String ARG_SECTION_NUMBER = "section_number";
        public View root;
        public RecyclerView recyclerView;
        RefreshLayout refreshLayout;
        BasicObjectRecyclerAdapter adapter;
        ArrayList<BasicObject> objects = new ArrayList<>();
        Button btnChooseAccount;
        public static String pageTitle = "Opportunities";
        BroadcastReceiver parentActivityMenuReceiver;
        CrmEntities.Accounts.Account lastAccount;


        @Nullable
        @Override
        public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable ViewGroup container,
                                 @Nullable Bundle savedInstanceState) {
            root = inflater.inflate(R.layout.frag_opportunities, container, false);
            refreshLayout = root.findViewById(R.id.refreshLayout);
            refreshLayout.setEnableLoadMore(false);
            options = new MySettingsHelper(context);
            RefreshLayout refreshLayout = root.findViewById(R.id.refreshLayout);
            refreshLayout.setOnRefreshListener(new OnRefreshListener() {
                @Override
                public void onRefresh(RefreshLayout refreshlayout) {
                    getAccountOpportunities(true);
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

            if (lastAccount != curAccount) {
                getAccountOpportunities(true);
                lastAccount = curAccount;
            }

            recyclerView = root.findViewById(R.id.opportunitiesRecyclerview);
            super.onCreateView(inflater, container, savedInstanceState);

            // Broadcast received regarding an options menu selection
            parentActivityMenuReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    Log.i(TAG, "onReceive Local receiver received broadcast!");
                    // Validate intent shit
                    if (intent != null && intent.getAction().equals(MENU_ACTION)) {

                        if (intent.getIntExtra(EXPORT_PAGE_INDEX, -1) == SectionsPagerAdapter.CONTACTS_PAGE) {
                            // Check if this is regarding an inventory export to Excel
                            if (intent.getStringExtra(EXPORT_INVENTORY) != null) {
                                // Ensure there is an account stipulated and inventory to export
                                if (curAccount != null && objects != null &&
                                        objects.size() > 0) {
                                } else {
                                    Toast.makeText(context, "No sales to export!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        } // is sales line page

                        // If the current account is null then clear the list.  This is to enable
                        // on back pressed behavior that doesn't automatically finish the activity.
                        if (curAccount == null) {
                            objects.clear();
                            populateList();
                        }

                        btnChooseAccount.setVisibility(curAccount == null ? View.VISIBLE : View.GONE);

                        getAccountOpportunities(true);

                        mViewPager.getAdapter().notifyDataSetChanged();
                    }
                }
            };

            getAccountOpportunities(false);

            return root;
        }

        @Override
        public void onStop() {
            super.onStop();
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();

            getActivity().unregisterReceiver(parentActivityMenuReceiver);
            Log.i(TAG, "Unregistered the account contacts receiver");
        }

        @Override
        public void onResume() {

            // Register the options menu selected receiver
            getActivity().registerReceiver(parentActivityMenuReceiver, intentFilterMenuAction);

            // Hide/show the choose account button
            if (curAccount == null) {
                btnChooseAccount.setVisibility(View.VISIBLE);
            } else {
                btnChooseAccount.setVisibility(View.GONE);
            }

            Log.i(TAG, "onResume Registered the account contacts receiver");
            super.onResume();
        }

        @Override
        public void onPause() {
            super.onPause();
        }

        @Override
        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);

            try {
                if (requestCode == CALL_PHONE_REQ && Helpers.Permissions.isGranted(Helpers.Permissions.PermissionType.CALL_PHONE)) {

                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            Log.i(TAG, "onRequestPermissionsResult ");

        }

        protected void getAccountOpportunities(boolean forceRefresh) {

            if (curAccount == null) {
                return;
            }

            String query = Queries.Opportunities.getOpportunitiesByAccount(curAccount.accountid);

            if (curAccount == null) {
                Toast.makeText(context, "Please select an account", Toast.LENGTH_SHORT).show();
                return;
            }

            getPagerTitle();

            if (objects != null && objects.size() > 0 && forceRefresh != true) {
                populateList();
                return;
            }

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
                        CrmEntities.Opportunities opportunities = new CrmEntities.Opportunities(response);
                        objects.clear();
                        for (CrmEntities.Opportunities.Opportunity opportunity : opportunities.list) {
                            BasicObject object = new BasicObject(opportunity.accountname, opportunity.dealStatus, opportunity);
                            objects.add(object);
                        }
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

        protected void populateList() {

            for (int i = 0; i < (objects.size()); i++) {
                adapter = new BasicObjectRecyclerAdapter(context, objects);
            }

            // Check if adapter is null and leave if so.  If user hasn't selected an account this will be the case
            if (adapter == null) {
                return;
            }

            if (!getActivity().isFinishing()) {
                recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                recyclerView.setAdapter(adapter);
                adapter.setClickListener(new BasicObjectRecyclerAdapter.ItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        CrmEntities.Opportunities.Opportunity selectedOpportunity =
                                (CrmEntities.Opportunities.Opportunity) objects.get(position).object;
                        Intent intent = new Intent(context, BasicEntityActivity.class);
                        intent.putExtra(BasicEntityActivity.ACTIVITY_TITLE, "Opportunity Details");
                        intent.putExtra(BasicEntityActivity.ENTITYID, selectedOpportunity.opportunityid);
                        intent.putExtra(BasicEntityActivity.ENTITY_LOGICAL_NAME, "opportunity");
                        intent.putExtra(BasicEntityActivity.GSON_STRING, selectedOpportunity.toBasicEntity().toGson());
                        startActivityForResult(intent, BasicEntityActivity.REQUEST_BASIC);
                    }
                });
                refreshLayout.finishRefresh();
            } else {
                Log.w(TAG, "populateList: CAN'T POPULATE AS THE ACTIVITY IS FINISHING!!!");
            }
        }

        void showOpportunityOptions(final CrmEntities.Opportunities.Opportunity clickedContact) {

        }

        public void insertContact(CrmEntities.Contacts.Contact contact) {
            Intent intent = new Intent(Intent.ACTION_INSERT);
            intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
            intent.putExtra(ContactsContract.Intents.Insert.NAME, contact.fullname);
            intent.putExtra(ContactsContract.Intents.Insert.EMAIL, contact.email);
            intent.putExtra(ContactsContract.Intents.Insert.PHONE, contact.mobile);
            intent.putExtra(ContactsContract.Intents.Insert.SECONDARY_PHONE, contact.address1Phone);
            intent.putExtra(ContactsContract.Intents.Insert.COMPANY, contact.accountFormatted);
            intent.putExtra(ContactsContract.Intents.Insert.NOTES, "Added from MileBuddy");
            intent.putExtra(ContactsContract.Intents.Insert.JOB_TITLE, contact.jobtitle);
            if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                startActivity(intent);
            }
        }

        String getPagerTitle() {
            pageTitle = "Opportunities";
            mViewPager.getAdapter().notifyDataSetChanged();
            return pageTitle;
        }

    }

    public static class Frag_Tickets extends Fragment {
        public static final String ARG_SECTION_NUMBER = "section_number";
        public View root;
        public RecyclerView recyclerView;
        RefreshLayout refreshLayout;
        BasicObjectRecyclerAdapter adapter;
        ArrayList<BasicObject> objects = new ArrayList<>();
        Button btnChooseAccount;
        public static String pageTitle = "Tickets";
        BroadcastReceiver parentActivityMenuReceiver;
        CrmEntities.Accounts.Account lastAccount;
        TextView txtNoTickets;

        @Nullable
        @Override
        public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable ViewGroup container,
                                 @Nullable Bundle savedInstanceState) {
            root = inflater.inflate(R.layout.frag_cases, container, false);
            refreshLayout = root.findViewById(R.id.refreshLayout);
            refreshLayout.setEnableLoadMore(false);
            options = new MySettingsHelper(context);
            RefreshLayout refreshLayout = root.findViewById(R.id.refreshLayout);
            refreshLayout.setOnRefreshListener(new OnRefreshListener() {
                @Override
                public void onRefresh(RefreshLayout refreshlayout) {
                    getAccountTickets(true);
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

            if (lastAccount != curAccount) {
                getAccountTickets(true);
                lastAccount = curAccount;
            }

            recyclerView = root.findViewById(R.id.casesRecyclerview);
            super.onCreateView(inflater, container, savedInstanceState);

            // Broadcast received regarding an options menu selection
            parentActivityMenuReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    Log.i(TAG, "onReceive Local receiver received broadcast!");
                    // Validate intent shit
                    if (intent != null && intent.getAction().equals(MENU_ACTION)) {

                        if (intent.getIntExtra(EXPORT_PAGE_INDEX, -1) == SectionsPagerAdapter.CONTACTS_PAGE) {
                            // Check if this is regarding an inventory export to Excel
                            if (intent.getStringExtra(EXPORT_INVENTORY) != null) {
                                // Ensure there is an account stipulated and inventory to export
                                if (curAccount != null && objects != null &&
                                        objects.size() > 0) {
                                } else {
                                    Toast.makeText(context, "No sales to export!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        } // is sales line page

                        // If the current account is null then clear the list.  This is to enable
                        // on back pressed behavior that doesn't automatically finish the activity.
                        if (curAccount == null) {
                            objects.clear();
                            populateList();
                        }

                        btnChooseAccount.setVisibility(curAccount == null ? View.VISIBLE : View.GONE);

                        getAccountTickets(true);

                        mViewPager.getAdapter().notifyDataSetChanged();
                    }
                }
            };

            getAccountTickets(false);

            txtNoTickets = root.findViewById(R.id.txtNoTickets);

            return root;
        }

        @Override
        public void onStop() {
            super.onStop();
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();

            getActivity().unregisterReceiver(parentActivityMenuReceiver);
            Log.i(TAG, "Unregistered the account contacts receiver");
        }

        @Override
        public void onResume() {

            // Register the options menu selected receiver
            getActivity().registerReceiver(parentActivityMenuReceiver, intentFilterMenuAction);

            // Hide/show the choose account button
            if (curAccount == null) {
                btnChooseAccount.setVisibility(View.VISIBLE);
            } else {
                btnChooseAccount.setVisibility(View.GONE);
            }

            Log.i(TAG, "onResume Registered the account contacts receiver");
            super.onResume();
        }

        @Override
        public void onPause() {
            super.onPause();
        }

        @Override
        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);

            try {
                if (requestCode == CALL_PHONE_REQ && Helpers.Permissions.isGranted(Helpers.Permissions.PermissionType.CALL_PHONE)) {

                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            Log.i(TAG, "onRequestPermissionsResult ");

        }

        protected void getAccountTickets(boolean forceRefresh) {

            if (curAccount == null) {
                return;
            }

            String query = Queries.Tickets.getAccountTickets(curAccount.accountid);

            if (curAccount == null) {
                Toast.makeText(context, "Please select an account", Toast.LENGTH_SHORT).show();
                return;
            }

            getPagerTitle();

            if (objects != null && objects.size() > 0 && forceRefresh != true) {
                populateList();
                return;
            }

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
                        CrmEntities.Tickets tickets = new CrmEntities.Tickets(response);
                        objects.clear();
                        for (CrmEntities.Tickets.Ticket ticket : tickets.list) {
                            BasicObject object = new BasicObject(ticket.ticketnumber, ticket.title, ticket);
                            object.middleText = ticket.description;
                            object.topRightText = ticket.statusFormatted;
                            object.bottomRightText = Helpers.DatesAndTimes.getPrettyDateAndTime(ticket.modifiedon);
                            objects.add(object);
                        }
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

        protected void populateList() {

            for (int i = 0; i < (objects.size()); i++) {
                adapter = new BasicObjectRecyclerAdapter(context, objects);
            }

            // Check if adapter is null and leave if so.  If user hasn't selected an account this will be the case
            if (adapter == null) {
                return;
            }

            if (!getActivity().isFinishing()) {
                recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                recyclerView.setAdapter(adapter);
                adapter.setClickListener(new BasicObjectRecyclerAdapter.ItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        CrmEntities.Tickets.Ticket selectedTicket =
                                (CrmEntities.Tickets.Ticket) objects.get(position).object;
                        Intent intent = new Intent(context, BasicEntityActivity.class);
                        intent.putExtra(BasicEntityActivity.ACTIVITY_TITLE, "Ticket Details");
                        intent.putExtra(BasicEntityActivity.ENTITYID, selectedTicket.ticketid);
                        intent.putExtra(BasicEntityActivity.ENTITY_LOGICAL_NAME, "incident");
                        intent.putExtra(BasicEntityActivity.GSON_STRING, selectedTicket.toBasicEntity().toGson());
                        startActivityForResult(intent, BasicEntityActivity.REQUEST_BASIC);
                    }
                });
                refreshLayout.finishRefresh();
            } else {
                Log.w(TAG, "populateList: CAN'T POPULATE AS THE ACTIVITY IS FINISHING!!!");
            }

            txtNoTickets.setVisibility(objects.size() > 0 ? View.GONE : View.VISIBLE);

        }

        String getPagerTitle() {
            pageTitle = "Tickets";
            mViewPager.getAdapter().notifyDataSetChanged();
            return pageTitle;
        }

    }

}
