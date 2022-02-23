package com.fimbleenterprises.medimileage.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fimbleenterprises.medimileage.MyApp;
import com.fimbleenterprises.medimileage.adapters.AccountInventoryRecyclerAdapter;
import com.fimbleenterprises.medimileage.dialogs.MonthYearPickerDialog;
import com.fimbleenterprises.medimileage.objects_and_containers.BasicEntity;
import com.fimbleenterprises.medimileage.adapters.BasicObjectRecyclerAdapter;
import com.fimbleenterprises.medimileage.objects_and_containers.BasicObjects;
import com.fimbleenterprises.medimileage.dialogs.ContactActions;
import com.fimbleenterprises.medimileage.Crm;
import com.fimbleenterprises.medimileage.objects_and_containers.CrmEntities;
import com.fimbleenterprises.medimileage.objects_and_containers.ExcelSpreadsheet;
import com.fimbleenterprises.medimileage.Helpers;
import com.fimbleenterprises.medimileage.objects_and_containers.MediUser;
import com.fimbleenterprises.medimileage.objects_and_containers.MileBuddyMetrics;
import com.fimbleenterprises.medimileage.MyPreferencesHelper;
import com.fimbleenterprises.medimileage.activities.ui.CustomViews.MyUnderlineEditText;
import com.fimbleenterprises.medimileage.MyViewPager;
import com.fimbleenterprises.medimileage.adapters.OrderLineRecyclerAdapter;
import com.fimbleenterprises.medimileage.CrmQueries;
import com.fimbleenterprises.medimileage.R;
import com.fimbleenterprises.medimileage.objects_and_containers.Requests;
import com.fimbleenterprises.medimileage.objects_and_containers.Territories.Territory;
import com.fimbleenterprises.medimileage.activities.fullscreen_pickers.FullscreenActivityChooseAccount;
import com.fimbleenterprises.medimileage.activities.fullscreen_pickers.FullscreenActivityChooseTerritory;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnLoadMoreListener;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerTitleStrip;
import cz.msebera.android.httpclient.Header;
import jxl.format.Colour;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;

import static com.fimbleenterprises.medimileage.activities.BasicEntityActivity.GSON_STRING;
import static com.fimbleenterprises.medimileage.CrmQueries.*;

/*import com.anychart.APIlib;
import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Cartesian;*/

public class Activity_AccountData extends AppCompatActivity {

    public static final String MENU_SELECTION = "MENU_SELECTION";
    public static final String FAB_CLICKED = "FAB_CLICKED";
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
    public static MyPreferencesHelper options;

    // Default to current month and year
    public static int curMonth = 0;
    public static int curYear = 0;

    public static final String GO_TO_ACCOUNT = "GO_TO_ACCOUNT";
    public static final String GO_TO_ACCOUNT_OBJECT = "GO_TO_ACCOUNT_OBJECT";
    public static final String INITIAL_PAGE = "INITIAL_PAGE";

    public static final int PRODUCTFAMILY_MENU_ROOT = 2;
    public static final int PRODUCTSTATUS_MENU_ROOT = 3;
    public static final int CALL_PHONE_REQ = 123;

    public static Activity_CompanyWideData.ServiceAgreementFilter serviceAgreementFilter
            = Activity_CompanyWideData.ServiceAgreementFilter.CURRENT;
    public static Opportunities.DealStatus dealStatus = Opportunities.DealStatus.ANY;
    public static int case_filter = Tickets.ANY;
    public static int case_state = Tickets.BOTH;
    public static CrmQueries.CustomerInventory.ProductStatus productStatus = CustomerInventory.ProductStatus.IN_STOCK;
    public static CrmQueries.CustomerInventory.ProductType productFamily = CustomerInventory.ProductType.PROBES;


    // public static BroadcastReceiver inventoryMenuItemSelectedReceiver;
    // public static BroadcastReceiver salesMenuItemSelectedReceiver;

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

    BroadcastReceiver fabClickReceiver;
    IntentFilter fabClickIntentFilter = new IntentFilter(FAB_CLICKED);

    public static Menu optionsMenu;
    public int curPageIndex = 0;

    public static boolean menuOpen = false;
    // Receivers for date range changes at the activity level
    public static IntentFilter intentFilterParentActivity = new IntentFilter(MENU_ACTION);

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        context = this;
        activity = this;
        options = new MyPreferencesHelper(this);

        // Log a metric
        // MileBuddyMetrics.updateMetric(this, MileBuddyMetrics.MetricName.LAST_ACCESSED_TERRITORY_DATA, DateTime.now());



        territory = new Territory();
        territory.territoryid = MediUser.getMe().territoryid;
        territory.territoryName = MediUser.getMe().territoryname;

        setContentView(R.layout.activity_account_stuff);

        sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager = (MyViewPager) findViewById(R.id.main_pager_yo_sales_perf);
        mViewPager.onRealPageChangedListener = new MyViewPager.OnRealPageChangedListener() {
            @Override
            public void onPageActuallyFuckingChanged(int pageIndex) {
                options.setLastAccountPage(pageIndex);
            }
        };
        mPagerStrip = (PagerTitleStrip) findViewById(R.id.pager_title_strip_sales_perf);
        mViewPager.setAdapter(sectionsPagerAdapter);
        mViewPager.setOffscreenPageLimit(0);
        mViewPager.setCurrentItem(options.getLastAccountPage());
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

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        intentFilterParentActivity.addAction(BasicEntityActivity.ENTITY_UPDATED);

        if (getIntent() != null && getIntent().getAction() != null) {
            if (getIntent().getAction().equals(GO_TO_ACCOUNT)) {
                if (getIntent().getParcelableExtra(GO_TO_ACCOUNT_OBJECT) != null) {
                    curAccount = getIntent().getParcelableExtra(GO_TO_ACCOUNT_OBJECT);
                    territoryPageOrigin = true;
                }
                if (getIntent().hasExtra(INITIAL_PAGE)) {
                    mViewPager.setCurrentItem(getIntent().getIntExtra(INITIAL_PAGE, options.getLastAccountPage()));
                }
            }
        }

        try {
            MileBuddyMetrics.updateMetric(context, MileBuddyMetrics.MetricName.LAST_ACCESSED_ACCOUNT_DATA, DateTime.now());
        } catch (Exception e) {
            e.printStackTrace();
        }

        fabClickReceiver = new BroadcastReceiver() {
            @SuppressLint("RestrictedApi") // Apparently a bug that causes method: .openOptionsMenu() to raise a lint warning (https://stackoverflow.com/a/44926919/2097893).
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent != null && intent.getAction() != null && intent.getAction().equals(FAB_CLICKED)) {
                    Objects.requireNonNull(getSupportActionBar()).openOptionsMenu();
                }
            }
        };

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == FullscreenActivityChooseAccount.ACCOUNT_CHOSEN_RESULT) {
            curAccount = data.getParcelableExtra(FullscreenActivityChooseAccount.ACCOUNT_RESULT);
            cachedAccounts = options.getCachedAccounts().list;

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

        if (resultCode == BasicEntityActivity.RESULT_CODE_ENTITY_DELETED) {

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
                setTitle("Choose an account");
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
    protected void onStop() {
        super.onStop();
        unregisterReceiver(fabClickReceiver);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (options == null) { options = new MyPreferencesHelper(context); }

        if (curAccount != null) {
            setTitle(curAccount.accountName);
        } else {
            setTitle("Choose an account");
        }
        MyApp.setIsVisible(true, this);

        registerReceiver(fabClickReceiver, fabClickIntentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        MyApp.setIsVisible(false, this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        curAccount = null;

    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.account_menu, menu);
        super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onPreparePanel(int featureId, @Nullable View view, @NonNull Menu menu) {

        // Show/Hide menu options
        menu.findItem(R.id.action_case_create_new).setVisible(false);
        // menu.findItem(R.id.action_case_create_new).setVisible(mViewPager.currentPosition == SectionsPagerAdapter.TICKETS_PAGE);
        menu.findItem(R.id.action_back).setVisible(mViewPager.currentPosition == SectionsPagerAdapter.CONTACTS_PAGE);
        menu.findItem(R.id.action_last_three_months).setVisible(mViewPager.currentPosition == SectionsPagerAdapter.SALES_LINE_PAGE);
        menu.findItem(R.id.action_this_month).setVisible(mViewPager.currentPosition == SectionsPagerAdapter.SALES_LINE_PAGE);
        menu.findItem(R.id.action_last_month).setVisible(mViewPager.currentPosition == SectionsPagerAdapter.SALES_LINE_PAGE);
        menu.findItem(R.id.action_choose_month).setVisible(mViewPager.currentPosition == SectionsPagerAdapter.SALES_LINE_PAGE);
        menu.findItem(R.id.action_producttype).setVisible(mViewPager.currentPosition == SectionsPagerAdapter.INVENTORY_PAGE);
        menu.findItem(R.id.action_productstatus).setVisible(mViewPager.currentPosition == SectionsPagerAdapter.INVENTORY_PAGE);
        menu.findItem(R.id.action_export_to_excel).setVisible(mViewPager.currentPosition == SectionsPagerAdapter.INVENTORY_PAGE);
        menu.findItem(R.id.action_case_state).setVisible(mViewPager.currentPosition == SectionsPagerAdapter.TICKETS_PAGE);
        menu.findItem(R.id.action_case_status).setVisible(mViewPager.currentPosition == SectionsPagerAdapter.TICKETS_PAGE);
        menu.findItem(R.id.action_opportunity_status).setVisible(mViewPager.currentPosition == SectionsPagerAdapter.OPPORTUNITIES_PAGE);

        // Check/Uncheck date ranges
        menu.findItem(R.id.action_last_three_months).setChecked(curMonth == 0 && curYear == 0);
        menu.findItem(R.id.action_this_month).setChecked(curMonth == DateTime.now().getMonthOfYear() && curYear == DateTime.now().getYear());
        menu.findItem(R.id.action_last_month).setChecked(curMonth == DateTime.now().minusMonths(1).getMonthOfYear() && curYear == DateTime.now().minusMonths(1).getYear());

        // Check/Uncheck product status/family
        menu.findItem(R.id.action_probes).setChecked(productFamily == CustomerInventory.ProductType.PROBES);
        menu.findItem(R.id.action_flowmeters).setChecked(productFamily == CustomerInventory.ProductType.FLOWMETERS);
        menu.findItem(R.id.action_cables).setChecked(productFamily == CustomerInventory.ProductType.CABLES);
        menu.findItem(R.id.action_licensing).setChecked(productFamily == CustomerInventory.ProductType.CARDS);
        menu.findItem(R.id.action_instock).setChecked(productStatus == CustomerInventory.ProductStatus.IN_STOCK);
        menu.findItem(R.id.action_returned).setChecked(productStatus == CustomerInventory.ProductStatus.RETURNED);
        menu.findItem(R.id.action_expired).setChecked(productStatus == CustomerInventory.ProductStatus.EXPIRED);
        menu.findItem(R.id.action_lost).setChecked(productStatus == CustomerInventory.ProductStatus.LOST);
        menu.findItem(R.id.action_any).setChecked(productStatus == CustomerInventory.ProductStatus.ANY);
        
        // Check/Uncheck deal status
        menu.findItem(R.id.action_opp_any).setChecked(dealStatus == CrmQueries.Opportunities.DealStatus.ANY);
        menu.findItem(R.id.action_opp_stalled).setChecked(dealStatus == CrmQueries.Opportunities.DealStatus.STALLED);
        menu.findItem(R.id.action_opp_qualifying).setChecked(dealStatus == CrmQueries.Opportunities.DealStatus.QUALIFYING);
        menu.findItem(R.id.action_opp_dead).setChecked(dealStatus == CrmQueries.Opportunities.DealStatus.DEAD);
        menu.findItem(R.id.action_opp_evaluating).setChecked(dealStatus == CrmQueries.Opportunities.DealStatus.EVALUATING);
        menu.findItem(R.id.action_opp_pending).setChecked(dealStatus == CrmQueries.Opportunities.DealStatus.PENDING);
        menu.findItem(R.id.action_opp_closed).setChecked(dealStatus == CrmQueries.Opportunities.DealStatus.CLOSED);
        menu.findItem(R.id.action_opp_canceled).setChecked(dealStatus == CrmQueries.Opportunities.DealStatus.CANCELED);
        menu.findItem(R.id.action_opp_discovery).setChecked(dealStatus == CrmQueries.Opportunities.DealStatus.DISCOVERY);
        menu.findItem(R.id.action_opp_won).setChecked(dealStatus == CrmQueries.Opportunities.DealStatus.WON);

        // Set case state values
        menu.findItem(R.id.action_case_state_open).setChecked(case_state == Tickets.OPEN);
        menu.findItem(R.id.action_case_state_closed).setChecked(case_state == Tickets.CLOSED);

        // Set case status values
        menu.findItem(R.id.action_case_status_any).setChecked(case_filter == Tickets.ANY);
        menu.findItem(R.id.action_case_status_inprogress).setChecked(case_filter == Tickets.IN_PROGRESS);
        menu.findItem(R.id.action_case_status_on_hold).setChecked(case_filter == Tickets.ON_HOLD);
        menu.findItem(R.id.action_case_status_to_be_inspected).setChecked(case_filter == Tickets.TO_BE_INSPECTED);
        menu.findItem(R.id.action_case_status_waiting_for_product).setChecked(case_filter == Tickets.WAITING_FOR_PRODUCT);
        menu.findItem(R.id.action_case_status_waiting_on_customer).setChecked(case_filter == Tickets.WAITING_ON_CUSTOMER);
        menu.findItem(R.id.action_change_case_status_waiting_on_rep).setChecked(case_filter == Tickets.WAITING_ON_REP);
        menu.findItem(R.id.action_case_status_to_be_billed).setChecked(case_filter == Tickets.TO_BE_BILLED);
        menu.findItem(R.id.action_case_status_problem_solved).setChecked(case_filter == Tickets.PROBLEM_SOLVED);
        
        return super.onPreparePanel(featureId, view, menu);

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {

            case 16908332: // back arrow (top, left)
                onBackPressed();
                break;

            case R.id.action_back:
                onBackPressed();
                break;

            case R.id.action_last_three_months:
                // If curMonth and curYear == 0 getSalesLines() will query the last three months.
                curMonth = 0;
                curYear = 0;
                sendMenuItemSelectedBroadcast();
                break;

            case R.id.action_this_month:
                curMonth = DateTime.now().getMonthOfYear();
                curYear = DateTime.now().getYear();
                sendMenuItemSelectedBroadcast();
                break;

            case R.id.action_last_month:
                curMonth = DateTime.now().minusMonths(1).getMonthOfYear();
                curYear = DateTime.now().minusMonths(1).getYear();
                sendMenuItemSelectedBroadcast();
                break;

            case R.id.action_choose_month:
                final MonthYearPickerDialog mpd = new MonthYearPickerDialog();
                mpd.setListener(new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        curMonth = month;
                        curYear = year;
                        sendMenuItemSelectedBroadcast();
                        mpd.dismiss();
                    }
                });
                mpd.show(getSupportFragmentManager(), "MonthYearPickerDialog");
                break;

            case R.id.action_export_to_excel:
                Intent intentExportInventory = new Intent(MENU_ACTION);
                intentExportInventory.putExtra(EXPORT_INVENTORY, EXPORT_INVENTORY);
                intentExportInventory.putExtra(EXPORT_PAGE_INDEX, mViewPager.currentPosition);
                sendBroadcast(intentExportInventory);
                break;

            // Set the product family
            case R.id.action_probes:
                productFamily = CustomerInventory.ProductType.PROBES;
                sendMenuItemSelectedBroadcast();
                break;
            case R.id.action_flowmeters:
                productFamily = CustomerInventory.ProductType.FLOWMETERS;
                sendMenuItemSelectedBroadcast();
                break;
            case R.id.action_cables:
                productFamily = CustomerInventory.ProductType.CABLES;
                sendMenuItemSelectedBroadcast();
                break;
            case R.id.action_licensing:
                productFamily = CustomerInventory.ProductType.CARDS;
                sendMenuItemSelectedBroadcast();
                break;

            // Set the product status
            case R.id.action_instock:
                productStatus = CustomerInventory.ProductStatus.IN_STOCK;
                sendMenuItemSelectedBroadcast();
                break;
            case R.id.action_returned:
                productStatus = CustomerInventory.ProductStatus.RETURNED;
                sendMenuItemSelectedBroadcast();
                break;
            case R.id.action_expired:
                productStatus = CustomerInventory.ProductStatus.EXPIRED;
                sendMenuItemSelectedBroadcast();
                break;
            case R.id.action_lost:
                productStatus = CustomerInventory.ProductStatus.LOST;
                sendMenuItemSelectedBroadcast();
                break;
            case R.id.action_any:
                productStatus = CustomerInventory.ProductStatus.ANY;
                sendMenuItemSelectedBroadcast();
                break;

            case R.id.action_opp_any:
                dealStatus = Opportunities.DealStatus.ANY;
                sendMenuItemSelectedBroadcast();
                break;
            case R.id.action_opp_canceled:
                dealStatus = Opportunities.DealStatus.CANCELED;
                sendMenuItemSelectedBroadcast();
                break;
            case R.id.action_opp_closed:
                dealStatus = Opportunities.DealStatus.CLOSED;
                sendMenuItemSelectedBroadcast();
                break;
            case R.id.action_opp_dead:
                dealStatus = Opportunities.DealStatus.DEAD;
                sendMenuItemSelectedBroadcast();
                break;
            case R.id.action_opp_discovery:
                dealStatus = Opportunities.DealStatus.DISCOVERY;
                sendMenuItemSelectedBroadcast();
                break;
            case R.id.action_opp_evaluating:
                dealStatus = Opportunities.DealStatus.EVALUATING;
                sendMenuItemSelectedBroadcast();
                break;
            case R.id.action_opp_pending:
                dealStatus = Opportunities.DealStatus.PENDING;
                sendMenuItemSelectedBroadcast();
                break;
            case R.id.action_opp_qualifying:
                dealStatus = Opportunities.DealStatus.QUALIFYING;
                sendMenuItemSelectedBroadcast();
                break;
            case R.id.action_opp_stalled:
                dealStatus = Opportunities.DealStatus.STALLED;
                sendMenuItemSelectedBroadcast();
                break;
            case R.id.action_opp_won:
                dealStatus = Opportunities.DealStatus.WON;
                sendMenuItemSelectedBroadcast();
                break;

            case R.id.action_case_state_open:
                if (case_state == Tickets.OPEN) {
                    case_state = Tickets.BOTH;
                } else {
                    case_state = Tickets.OPEN;
                }
                sendMenuItemSelectedBroadcast();
                break;

            case R.id.action_case_state_closed:
                if (case_state == Tickets.CLOSED) {
                    case_state = Tickets.BOTH;
                } else {
                    case_state = Tickets.CLOSED;
                }
                sendMenuItemSelectedBroadcast();
                break;

                // Cases
            case R.id.action_case_create_new:
                CrmEntities.Tickets.Ticket ticket = new CrmEntities.Tickets.Ticket() ;


                Intent intent = new Intent(context, BasicEntityActivity.class);
                intent.putExtra(BasicEntityActivity.ACTIVITY_TITLE, "Create New Ticket");
                intent.putExtra(BasicEntityActivity.ENTITYID, ticket.entityid);
                intent.putExtra(BasicEntityActivity.ENTITY_LOGICAL_NAME, "incident");
                intent.putExtra(BasicEntityActivity.LOAD_NOTES, false);
                intent.putExtra(BasicEntityActivity.CREATE_NEW, true);
                intent.putExtra(GSON_STRING, ticket.toBasicEntity().toGson());
                startActivityForResult(intent, BasicEntityActivity.REQUEST_BASIC);
                break;
            case R.id.action_case_status_inprogress:
                case_filter = Tickets.IN_PROGRESS;
                sendMenuItemSelectedBroadcast();
                break;
            case R.id.action_case_status_any:
                case_filter = Tickets.ANY;
                sendMenuItemSelectedBroadcast();
                break;
            case R.id.action_case_status_on_hold:
                case_filter = Tickets.ON_HOLD;
                sendMenuItemSelectedBroadcast();
                break;
            case R.id.action_case_status_to_be_inspected:
                case_filter = Tickets.TO_BE_INSPECTED;
                sendMenuItemSelectedBroadcast();
                break;
            case R.id.action_case_status_waiting_for_product:
                case_filter = Tickets.WAITING_FOR_PRODUCT;
                sendMenuItemSelectedBroadcast();
                break;
            case R.id.action_case_status_waiting_on_customer:
                case_filter = Tickets.WAITING_ON_CUSTOMER;
                sendMenuItemSelectedBroadcast();
                break;
            case R.id.action_change_case_status_waiting_on_rep:
                case_filter = Tickets.WAITING_ON_REP;
                sendMenuItemSelectedBroadcast();
                break;
            case R.id.action_case_status_to_be_billed:
                case_filter = Tickets.TO_BE_BILLED;
                sendMenuItemSelectedBroadcast();
                break;
            case R.id.action_case_status_problem_solved:
                case_filter = Tickets.PROBLEM_SOLVED;
                sendMenuItemSelectedBroadcast();
                break;
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
            String title = "";
            try {
                switch (position) {
                    case INVENTORY_PAGE:
                        title = Frag_AccountInventory.pageTitle;
                        return title;
                    case SALES_LINE_PAGE:
                        title = Frag_SalesLines.pageTitle;
                        if (curMonth == DateTime.now().getMonthOfYear() && curYear == DateTime.now().getYear()) {
                            title += " - This month";
                        } else if (curMonth == DateTime.now().minusMonths(1).getMonthOfYear() && curYear == DateTime.now().minusMonths(1).getYear()) {
                            title += " - Last month";
                        } else if (curMonth == 0 && curYear == 0) {
                            title += " - Last 3 months";
                        } else {
                            title += " - " + Helpers.DatesAndTimes.getMonthName(curMonth) + " " + curYear;
                        }
                        return title;
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

    public static class Frag_Contacts extends Fragment {
        public static final String ARG_SECTION_NUMBER = "section_number";
        public View root;
        public RecyclerView recyclerView;
        RefreshLayout refreshLayout;
        BasicObjectRecyclerAdapter adapter;
        ArrayList<BasicObjects.BasicObject> objects = new ArrayList<>();
        Button btnChooseAccount;
        public static String pageTitle = "Contacts";
        TextView txtNoContacts;
        BroadcastReceiver parentActivityReceiver;
        CrmEntities.Accounts.Account lastAccount;
        String attemptedPhonenumber;
        FloatingActionButton fab;


        @Nullable
        @Override
        public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable ViewGroup container,
                                 @Nullable Bundle savedInstanceState) {
            root = inflater.inflate(R.layout.frag_contacts, container, false);
            fab = root.findViewById(R.id.floatingActionButton);
            fab.setVisibility(View.GONE);

            txtNoContacts = root.findViewById(R.id.txtNoContacts);
            refreshLayout = root.findViewById(R.id.refreshLayout);
            refreshLayout.setEnableLoadMore(false);
            options = new MyPreferencesHelper(context);
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
            parentActivityReceiver = new BroadcastReceiver() {
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
                        }

                        // If the current account is null then clear the list.  This is to enable
                        // on back pressed behavior that doesn't automatically finish the activity.
                        if (curAccount == null) {
                            objects.clear();
                            populateList();
                        }

                        btnChooseAccount.setVisibility(curAccount == null ? View.VISIBLE : View.GONE);

                        getAccountContacts(true);

                        mViewPager.getAdapter().notifyDataSetChanged();
                    } else if (intent != null && intent.getAction().equals(BasicEntityActivity.ENTITY_UPDATED)) {
                        Log.i(TAG, "onReceive Broadcast received from parent activity suggesting a record was updated!");
                        getAccountContacts(true);
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

            getActivity().unregisterReceiver(parentActivityReceiver);
            Log.i(TAG, "Unregistered the account contacts receiver");
        }

        @Override
        public void onResume() {

            // Register the options menu selected receiver
            getActivity().registerReceiver(parentActivityReceiver, intentFilterParentActivity);

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

            String query = CrmQueries.Contacts.getContacts(curAccount.entityid);

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
                            BasicObjects.BasicObject object = new BasicObjects.BasicObject(contact.getFullname(), contact.jobtitle, contact);
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
                            BasicObjects.BasicObject clickedObject = objects.get(position);
                            CrmEntities.Contacts.Contact clickedContact = (CrmEntities.Contacts.Contact)
                                    clickedObject.object;
                            Log.i(TAG, "onItemClick Clicked item at position " + position
                                    + "(" + clickedObject.topText + ")");

                            ContactActions contactActions = new ContactActions(getActivity(), clickedContact);
                            contactActions.dismissOnSelection = true;
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

    public static class Frag_SalesLines extends Fragment {
        public static final String ARG_SECTION_NUMBER = "section_number";
        public View root;
        public RecyclerView recyclerView;
        RefreshLayout refreshLayout;
        OrderLineRecyclerAdapter adapter;
        ArrayList<CrmEntities.OrderProducts.OrderProduct> allOrders = new ArrayList<>();
        Button btnChooseAccount;
        BroadcastReceiver menuReceiver;
        public static String pageTitle = "Sales";
        TextView txtNoSales;
        CrmEntities.Accounts.Account lastAccount;
        FloatingActionButton fab;

        @Nullable
        @Override
        public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable ViewGroup container,
                                 @Nullable Bundle savedInstanceState) {
            root = inflater.inflate(R.layout.frag_saleslines, container, false);
            fab = root.findViewById(R.id.floatingActionButton);
            fab.setVisibility(View.GONE); // Don't need to show as there is no filtering for this frag (yet).  This layout is shared by the territory activity though so it cannot be removed.
            txtNoSales = root.findViewById(R.id.txtNoContacts);
            refreshLayout = root.findViewById(R.id.refreshLayout);
            options = new MyPreferencesHelper(context);
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
                    startActivityForResult(intentChangeAccount, FullscreenActivityChooseAccount.REQUESTCODE);
                }
            });

            fab = root.findViewById(R.id.floatingActionButton);
            fab.setVisibility(View.VISIBLE);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(FAB_CLICKED);
                    getActivity().sendBroadcast(intent);
                }
            });

            if (lastAccount != curAccount) {
                getAccountSales(true);
                lastAccount = curAccount;
            }

            recyclerView = root.findViewById(R.id.orderLinesRecyclerview);
            super.onCreateView(inflater, container, savedInstanceState);

            // Broadcast received regarding an options menu selection
            menuReceiver = new BroadcastReceiver() {
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
                                    Helpers.Files.shareFileProperly(context, spreadsheet.file);
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

            getActivity().unregisterReceiver(menuReceiver);
            Log.i(TAG, "Unregistered the account contacts receiver");
        }

        @Override
        public void onResume() {

            // Register the options menu selected receiver
            getActivity().registerReceiver(menuReceiver, intentFilterParentActivity);

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

            String query;
            if (curMonth == 0 || curYear == 0) {
                query = CrmQueries.OrderLines.getOrderLinesByAccount(curAccount.entityid, Operators.DateOperator.LAST_X_MONTHS, 3);
            } else {
                query = CrmQueries.OrderLines.getOrderLinesByAccount(curAccount.entityid, curMonth, curYear);
            }

            txtNoSales.setVisibility(View.GONE);

            if (curAccount == null) {
                Toast.makeText(context, "Please select an account", Toast.LENGTH_SHORT).show();
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

        protected void populateList() {

            final ArrayList<CrmEntities.OrderProducts.OrderProduct> orderList = new ArrayList<>();

            boolean addedTodayHeader = false;
            boolean addedYesterdayHeader = false;
            boolean addedThisWeekHeader = false;
            boolean addedThisMonthHeader = false;
            boolean addedLastMonthHeader = false;
            boolean addedOlderHeader = false;

            float todaySubtotal = 0, yesterdaySubtotal = 0, thisWeekSubtotal = 0, thisMonthSubtotal = 0, lastMonthSubtotal = 0, olderSubtotal = 0;

            Log.i(TAG, "populateTripList: Preparing the dividers and orders...");
            for (int i = 0; i < (allOrders.size()); i++) {
                CrmEntities.OrderProducts.OrderProduct curProduct = allOrders.get(i);

                // Trip was today
                if (curProduct.orderDate.getDayOfMonth() == DateTime.now().getDayOfMonth() &&
                        curProduct.orderDate.getMonthOfYear() == DateTime.now().getMonthOfYear() &&
                        curProduct.orderDate.getYear() == DateTime.now().getYear()) {
                    if (addedTodayHeader == false) {
                        CrmEntities.OrderProducts.OrderProduct headerObj = new CrmEntities.OrderProducts.OrderProduct();
                        headerObj.isSeparator = true;
                        headerObj.setTitle("Today");
                        orderList.add(headerObj);
                        addedTodayHeader = true;
                        Log.d(TAG + "getAllFullTrips", "Added a header object to the array that will eventually be a header childView in the list view named, 'Today' - This will not be added again!");
                    }
                    todaySubtotal += curProduct.extendedAmt;

                    // Order was yesterday
                } else if (curProduct.orderDate.getDayOfMonth() == DateTime.now().minusDays(1).getDayOfMonth() &&
                        curProduct.orderDate.getMonthOfYear() == DateTime.now().minusDays(1).getMonthOfYear() &&
                        curProduct.orderDate.getYear() == DateTime.now().minusDays(1).getYear()) {
                    if (addedYesterdayHeader == false) {
                        CrmEntities.OrderProducts.OrderProduct headerObj = new CrmEntities.OrderProducts.OrderProduct();
                        headerObj.isSeparator = true;
                        headerObj.setTitle("Yesterday");
                        orderList.add(headerObj);
                        addedYesterdayHeader = true;
                        Log.d(TAG + "getAllFullTrips", "Added a header object to the array that will eventually be a header childView in the list view named, 'Yesterday' - This will not be added again!");
                    }
                    yesterdaySubtotal += curProduct.extendedAmt;

                    // Order was this week
                } else if (curProduct.orderDate.getWeekOfWeekyear() == DateTime.now().getWeekOfWeekyear() &&
                        curProduct.orderDate.getMonthOfYear() == DateTime.now().getMonthOfYear() &&
                        curProduct.orderDate.getYear() == DateTime.now().getYear()) {
                    if (addedThisWeekHeader == false) {
                        CrmEntities.OrderProducts.OrderProduct headerObj = new CrmEntities.OrderProducts.OrderProduct();
                        headerObj.isSeparator = true;
                        headerObj.setTitle("This week");
                        orderList.add(headerObj);
                        addedThisWeekHeader = true;
                        Log.d(TAG + "getAllFullTrips", "Added a header object to the array that will eventually be a header childView in the list view named, 'This week' - This will not be added again!");
                    }
                    thisWeekSubtotal += curProduct.extendedAmt;

                    // Order was this month
                } else if (curProduct.orderDate.getMonthOfYear() == DateTime.now().getMonthOfYear() &&
                        curProduct.orderDate.getYear() == DateTime.now().getYear()) {
                    if (addedThisMonthHeader == false) {
                        CrmEntities.OrderProducts.OrderProduct headerObj = new CrmEntities.OrderProducts.OrderProduct();
                        headerObj.isSeparator = true;
                        headerObj.setTitle("This month");
                        orderList.add(headerObj);
                        addedThisMonthHeader = true;
                        Log.d(TAG + "getAllFullTrips", "Added a header object to the array that will eventually be a header childView in the list view named, 'This month' - This will not be added again!");
                    }
                    thisMonthSubtotal += curProduct.extendedAmt;
                // Order was last month
                } else if (curProduct.orderDate.getMonthOfYear() == DateTime.now().minusMonths(1).getMonthOfYear() &&
                            curProduct.orderDate.getYear() == DateTime.now().minusMonths(1).getYear()) {
                    if (addedLastMonthHeader == false) {
                        CrmEntities.OrderProducts.OrderProduct headerObj = new CrmEntities.OrderProducts.OrderProduct();
                        headerObj.isSeparator = true;
                        headerObj.setTitle("Last month");
                        orderList.add(headerObj);
                        addedLastMonthHeader = true;
                        Log.d(TAG + "getAllFullTrips", "Added a header object to the array that will eventually be a header childView in the list view named, 'Last month' - This will not be added again!");
                    }
                    lastMonthSubtotal += curProduct.extendedAmt;
                } else {
                    if (addedOlderHeader == false) {
                        CrmEntities.OrderProducts.OrderProduct headerObj = new CrmEntities.OrderProducts.OrderProduct();
                        headerObj.isSeparator = true;
                        headerObj.setTitle("Older than 2 months");
                        orderList.add(headerObj);
                        addedOlderHeader = true;
                        Log.d(TAG + "getAllFullTrips", "Added a header object to the array that will eventually be a header childView in the list view named, 'Total' - This will not be added again!");
                    }
                    olderSubtotal += curProduct.extendedAmt;
                }

                CrmEntities.OrderProducts.OrderProduct orderProduct = allOrders.get(i);
                orderList.add(orderProduct);
            }

            // Sum today, yesterday, this week, etc.
            // This feels... right but I can't actually logic out if it is or is even necessary.
            thisMonthSubtotal = thisMonthSubtotal + thisWeekSubtotal + yesterdaySubtotal + todaySubtotal;
            thisWeekSubtotal = thisWeekSubtotal + yesterdaySubtotal + todaySubtotal;

            // Append the subtotals to the headers (note that the partNumber property is used for the header's text - confusing I know)
            for (CrmEntities.OrderProducts.OrderProduct product : orderList) {
                if (todaySubtotal > 0) {
                    if (product.isSeparator && product.partNumber.equals("Today")) {
                        product.partNumber = product.partNumber + " (" + Helpers.Numbers.convertToCurrency(todaySubtotal) + ")";
                    }
                }
                if (yesterdaySubtotal > 0) {
                    if (product.isSeparator && product.partNumber.equals("Yesterday")) {
                        product.partNumber = product.partNumber + " (" + Helpers.Numbers.convertToCurrency(yesterdaySubtotal) + ")";
                    }
                }
                if (thisWeekSubtotal > 0) {
                    if (product.isSeparator && product.partNumber.equals("This week")) {
                        product.partNumber = product.partNumber + " (" + Helpers.Numbers.convertToCurrency(thisWeekSubtotal) + ")";
                    }
                }
                if (thisMonthSubtotal > 0) {
                    if (product.isSeparator && product.partNumber.equals("This month")) {
                        product.partNumber = product.partNumber + " (" + Helpers.Numbers.convertToCurrency(thisMonthSubtotal) + ")";
                    }
                }
                if (lastMonthSubtotal > 0) {
                    if (product.isSeparator && product.partNumber.equals("Last month")) {
                        product.partNumber = product.partNumber + " (" + Helpers.Numbers.convertToCurrency(lastMonthSubtotal) + ")";
                    }
                }
                if (olderSubtotal > 0) {
                    if (product.isSeparator && product.partNumber.equals("Older than 2 months")) {
                        product.partNumber = product.partNumber + " (" + Helpers.Numbers.convertToCurrency(olderSubtotal) + ")";
                    }
                }
            }

            Log.i(TAG, "populateTripList Finished preparing the dividers and trips.");

            if (!getActivity().isFinishing()) {
                adapter = new OrderLineRecyclerAdapter(getContext(), orderList);
                adapter.disableLinkButtons();
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

    public static class Frag_AccountInventory extends Fragment {
        public static final String ARG_SECTION_NUMBER = "section_number";
        public View root;
        public RecyclerView recyclerView;
        RefreshLayout refreshLayout;
        AccountInventoryRecyclerAdapter adapter;
        ArrayList<CrmEntities.AccountProducts.AccountProduct> custInventory = new ArrayList<>();
        Button btnChooseAccount;
        /*ProductType productType = ProductType.PROBES; // Set a default so our first createview can show data
        ProductStatus productStatus = ProductStatus.IN_STOCK;*/
        public static String pageTitle = "Inventory";
        TextView txtNoInventory;
        BroadcastReceiver parentActivityMenuReceiver;
        CrmEntities.Accounts.Account lastAccount;
        FloatingActionButton fab;
        RelativeLayout layoutFilter;
        EditText txtFilter;

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

            recyclerView = root.findViewById(R.id.orderLinesRecyclerview);

            fab = root.findViewById(R.id.floatingActionButton);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (curAccount == null) {
                        Toast.makeText(getContext(), "Please choose an account first.", Toast.LENGTH_SHORT).show();
                    } else {
                        Intent intent = new Intent(FAB_CLICKED);
                        getActivity().sendBroadcast(intent);
                    }
                }
            });

            layoutFilter = root.findViewById(R.id.layoutFilter);
            txtFilter = root.findViewById(R.id.edittextFilter);
            txtFilter.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    // NPE if we don't have a populated adapter!
                    if (adapter == null || adapter.mData == null) {
                        Log.w(TAG, "onTextChanged: No data in adapter - cannot filter!");
                        return;
                    }

                    String filter;

                    // Clear the adapter's data
                    adapter.mData.clear();

                    // Filter text present
                    if (charSequence != null && charSequence.length() > 0) {
                        filter = charSequence.toString().toLowerCase();

                        for (CrmEntities.AccountProducts.AccountProduct product : custInventory) {
                            try { // Lazy try/catch
                                if (product.serialnumber.toLowerCase().contains(filter)) {
                                    adapter.mData.add(product);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    } else { // No filter text so add everything
                        for (CrmEntities.AccountProducts.AccountProduct product : custInventory) {
                            adapter.mData.add(product);
                        }
                    }
                }

                @Override
                public void afterTextChanged(Editable editable) {
                    // Notify the list that the data has changed
                    if (adapter != null && adapter.mData != null) {
                        adapter.notifyDataSetChanged();
                    }
                }
            });

            txtNoInventory = root.findViewById(R.id.txtNoInventory);
            refreshLayout = root.findViewById(R.id.refreshLayout);
            options = new MyPreferencesHelper(context);
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
                    startActivityForResult(intentChangeAccount, FullscreenActivityChooseAccount.REQUESTCODE);
                }
            });

            if (lastAccount != curAccount) {
                getAccountInventory(true);
                lastAccount = curAccount;
            }

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
                                    Helpers.Files.shareFileProperly(context, spreadsheet.file);
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

                        btnChooseAccount.setVisibility(curAccount == null ? View.VISIBLE : View.GONE);

                        getAccountInventory(true);
                        mViewPager.getAdapter().notifyDataSetChanged();
                    }
                }
            };

            // Get inventory using whatever menu selections are currently set
            getAccountInventory((lastAccount != curAccount));

            super.onCreateView(inflater, container, savedInstanceState);
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
            getActivity().registerReceiver(parentActivityMenuReceiver, intentFilterParentActivity);
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

            try {
                layoutFilter.setEnabled(false);
                txtFilter.setEnabled(false);
                txtFilter.setText("");
            } catch (Exception e) {
                e.printStackTrace();
            }

            populateList();

            adapter.mData.clear();
            adapter.notifyDataSetChanged();

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

            switch (productFamily) {
                case PROBES:
                    query = Accounts.getAccountInventory(curAccount
                            .entityid,CrmEntities.AccountProducts.ITEM_GROUP_PROBES, statusCode);
                    break;
                case FLOWMETERS:
                    query = Accounts.getAccountInventory(curAccount
                            .entityid,CrmEntities.AccountProducts.ITEM_GROUP_FLOWMETERS, statusCode);
                    break;
                case CABLES:
                    query = Accounts.getAccountInventory(curAccount
                            .entityid,CrmEntities.AccountProducts.ITEM_GROUP_LICENSES, statusCode);
                    break;
                case CARDS:
                    query = Accounts.getAccountInventory(curAccount
                            .entityid,CrmEntities.AccountProducts.ITEM_GROUP_CABLES, statusCode);
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

            switch (productFamily) {
                case PROBES:
                    txtAppend = "probes - ";
                    break;
                case FLOWMETERS:
                    txtAppend = "flowmeters - ";
                    break;
                case CABLES:
                    txtAppend = "cables - ";
                    break;
                case CARDS:
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
                        String query = CrmQueries.CustomerInventory.getCustomerInventoryDetails(productList.get(position).customerinventoryid);
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

            try {
                layoutFilter.setEnabled(true);
                txtFilter.setEnabled(true);
                txtFilter.setText("");
            } catch (Exception e) {
                e.printStackTrace();
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

    public static class Frag_Opportunities extends Fragment {
        public static final String ARG_SECTION_NUMBER = "section_number";
        public View root;
        public RecyclerView recyclerView;
        RefreshLayout refreshLayout;
        BasicObjectRecyclerAdapter adapter;
        ArrayList<BasicObjects.BasicObject> objects = new ArrayList<>();
        TextView txtNoOpportunities;
        Button btnChooseAccount;
        public static String pageTitle = "Opportunities";
        BroadcastReceiver parentActivityMenuReceiver;
        CrmEntities.Accounts.Account lastAccount;
        FloatingActionButton fab;

        @Nullable
        @Override
        public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable ViewGroup container,
                                 @Nullable Bundle savedInstanceState) {
            root = inflater.inflate(R.layout.frag_opportunities, container, false);
            fab = root.findViewById(R.id.floatingActionButton);
            fab.setVisibility(View.VISIBLE);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(FAB_CLICKED);
                    getActivity().sendBroadcast(intent);
                }
            });
            refreshLayout = root.findViewById(R.id.refreshLayout);
            refreshLayout.setEnableLoadMore(false);
            options = new MyPreferencesHelper(context);
            txtNoOpportunities = root.findViewById(R.id.txtNoOpportunities);
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
            getActivity().unregisterReceiver(parentActivityMenuReceiver);

        }

        @Override
        public void onStart() {
            super.onStart();

            // Register the options menu selected receiver
            getActivity().registerReceiver(parentActivityMenuReceiver, intentFilterParentActivity);

        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
        }

        @Override
        public void onResume() {


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
        public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
            super.onActivityResult(requestCode, resultCode, data);

            if (resultCode == BasicEntityActivity.RESULT_CODE_ENTITY_UPDATED) {
                if (data != null && data.getStringExtra(GSON_STRING) != null) {
                    Log.i(TAG, "onActivityResult Basic entity was updated!");
                    BasicEntity updatedEntity = new BasicEntity(data.getStringExtra(GSON_STRING));
                    Intent updatedIntent = new Intent(BasicEntityActivity.ENTITY_UPDATED);
                    updatedIntent.putExtra(GSON_STRING, updatedEntity.toGson());

                    getAccountOpportunities(true);

                }
            }
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
                Toast.makeText(context, "Please select an account", Toast.LENGTH_SHORT).show();
                return;
            }

            getPagerTitle();

            if (objects != null && objects.size() > 0 && forceRefresh != true) {
                populateList();
                return;
            }

            String query = CrmQueries.Opportunities.getOpportunitiesByAccount(curAccount.entityid, dealStatus);
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
                            BasicObjects.BasicObject object = new BasicObjects.BasicObject(opportunity.accountname, opportunity.statuscodeFormatted, opportunity);
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
                        intent.putExtra(BasicEntityActivity.ENTITYID, selectedOpportunity.entityid);
                        intent.putExtra(BasicEntityActivity.ENTITY_LOGICAL_NAME, "opportunity");
                        intent.putExtra(GSON_STRING, selectedOpportunity.toBasicEntity().toGson());
                        startActivityForResult(intent, BasicEntityActivity.REQUEST_BASIC);

                        try {
                            MileBuddyMetrics.updateMetric(context, MileBuddyMetrics.MetricName.LAST_OPENED_OPPORTUNITY, DateTime.now());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                refreshLayout.finishRefresh();
            } else {
                Log.w(TAG, "populateList: CAN'T POPULATE AS THE ACTIVITY IS FINISHING!!!");
            }

            txtNoOpportunities.setVisibility( (objects == null || objects.size() == 0) ? View.VISIBLE : View.GONE );
        }

        void showOpportunityOptions(final CrmEntities.Opportunities.Opportunity clickedContact) {

        }

        public void insertContact(CrmEntities.Contacts.Contact contact) {
            Intent intent = new Intent(Intent.ACTION_INSERT);
            intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
            intent.putExtra(ContactsContract.Intents.Insert.NAME, contact.getFullname());
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
        ArrayList<BasicObjects.BasicObject> objects = new ArrayList<>();
        Button btnChooseAccount;
        public static String pageTitle = "Tickets";
        BroadcastReceiver parentActivityMenuReceiver;
        CrmEntities.Accounts.Account lastAccount;
        TextView txtNoTickets;
        FloatingActionButton fab;

        @Nullable
        @Override
        public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable ViewGroup container,
                                 @Nullable Bundle savedInstanceState) {
            root = inflater.inflate(R.layout.frag_cases, container, false);
            fab = root.findViewById(R.id.floatingActionButton);
            fab.setVisibility(View.VISIBLE);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(FAB_CLICKED);
                    getActivity().sendBroadcast(intent);
                }
            });
            refreshLayout = root.findViewById(R.id.refreshLayout);
            refreshLayout.setEnableLoadMore(false);
            options = new MyPreferencesHelper(context);
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
            getActivity().registerReceiver(parentActivityMenuReceiver, intentFilterParentActivity);

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
                Toast.makeText(context, "Please select an account", Toast.LENGTH_SHORT).show();
                return;
            }

            getPagerTitle();

            if (objects != null && objects.size() > 0 && forceRefresh != true) {
                populateList();
                return;
            }
            refreshLayout.autoRefreshAnimationOnly();

            String query = CrmQueries.Tickets.getAccountIncidents(curAccount.entityid, case_filter, case_state);
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
                        objects = tickets.toBasicObjects();
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

            /*for (int i = 0; i < (objects.size()); i++) {
                adapter = new BasicObjectRecyclerAdapter(context, objects);
            }*/

            adapter = new BasicObjectRecyclerAdapter(context, objects);

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
                        intent.putExtra(BasicEntityActivity.ENTITYID, selectedTicket.entityid);
                        intent.putExtra(BasicEntityActivity.ENTITY_LOGICAL_NAME, "incident");
                        intent.putExtra(GSON_STRING, selectedTicket.toBasicEntity().toGson());
                        startActivityForResult(intent, BasicEntityActivity.REQUEST_BASIC);

                        try {
                            MileBuddyMetrics.updateMetric(context, MileBuddyMetrics.MetricName.LAST_OPENED_TICKET, DateTime.now());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
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
