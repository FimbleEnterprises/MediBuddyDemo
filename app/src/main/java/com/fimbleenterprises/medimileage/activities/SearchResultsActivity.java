package com.fimbleenterprises.medimileage.activities;

import android.app.Activity;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

/*import com.anychart.APIlib;
import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Cartesian;*/
import com.fimbleenterprises.medimileage.MyApp;
import com.fimbleenterprises.medimileage.adapters.BasicObjectRecyclerAdapter;
import com.fimbleenterprises.medimileage.objects_and_containers.BasicObjects;
import com.fimbleenterprises.medimileage.dialogs.ContactActions;
import com.fimbleenterprises.medimileage.Crm;
import com.fimbleenterprises.medimileage.objects_and_containers.CrmEntities;
import com.fimbleenterprises.medimileage.objects_and_containers.CrmEntities.AccountProducts;
import com.fimbleenterprises.medimileage.DelayedWorker;
import com.fimbleenterprises.medimileage.Helpers;
import com.fimbleenterprises.medimileage.objects_and_containers.MediUser;
import com.fimbleenterprises.medimileage.objects_and_containers.MileBuddyMetrics;
import com.fimbleenterprises.medimileage.MyPreferencesHelper;
import com.fimbleenterprises.medimileage.MyViewPager;
import com.fimbleenterprises.medimileage.CrmQueries;
import com.fimbleenterprises.medimileage.R;
import com.fimbleenterprises.medimileage.objects_and_containers.Requests;
import com.fimbleenterprises.medimileage.sharepoint.SharePoint;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
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

public class SearchResultsActivity extends AppCompatActivity {

    private static final String TAG = "SearchResultsActivity";
    public Context context;
    MyPreferencesHelper prefs;
    public static final int CALL_PHONE_REQ = 123;
    public static final String SEARCH_INITIATED = "SEARCH_INITIATED";
    public static final String SEARCH_QUERY = "SEARCH_QUERY";
    public static IntentFilter searchFilter = new IntentFilter(SEARCH_INITIATED);
    public static Activity activity;
    public static MyViewPager mViewPager;
    public static PagerTitleStrip mPagerStrip;
    public static SectionsPagerAdapter sectionsPagerAdapter;
    public int curPageIndex = 0;
    public static String query = "query";
    public static androidx.fragment.app.FragmentManager fragMgr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        prefs = new MyPreferencesHelper(context);
        setContentView(R.layout.activity_search_results);
        this.context = this;

        sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager = (MyViewPager) findViewById(R.id.main_pager_yo_search_results);
        mViewPager.onRealPageChangedListener = new MyViewPager.OnRealPageChangedListener() {
            @Override
            public void onPageActuallyFuckingChanged(int pageIndex) {
                if (query != null) {
                    setTitle("Search: " + query.toUpperCase());
                } else {
                    setTitle("Search (" + sectionsPagerAdapter.getPageTitle(pageIndex) + ")");
                }

                // If the query is not a serial number then set the last used tab.  We want s/n
                // searches to always end up on the cust inv. tab but not necessarily set that tab
                // as the initial tab for the next search.
                if (query != null && !Helpers.Numbers.isNumeric(query)) {
                    prefs.setLastSearchTab(pageIndex);
                }

            }
        };
        mPagerStrip = findViewById(R.id.pager_title_strip_search_results);
        mViewPager.setAdapter(sectionsPagerAdapter);
        mViewPager.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) { }
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

        try {
            MileBuddyMetrics.updateMetric(context, MileBuddyMetrics.MetricName.LAST_ACCESSED_SEARCH, DateTime.now());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
        MyApp.setIsVisible(false, this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        MyApp.setIsVisible(true, this);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        handleIntent(getIntent());
        new DelayedWorker(1500, new DelayedWorker.DelayedJob() {
            @Override
            public void doWork() {
                handleIntent(getIntent());
            }

            @Override
            public void onComplete(Object object) {
                handleIntent(getIntent());
            }
        });

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        /*
        Intent resultIntent = new Intent(ENTITY_UPDATED);
        resultIntent.putExtra(GSON_STRING, basicEntity.toGson());
        setResult(RESULT_CODE_ENTITY_UPDATED, resultIntent);
         */
    }

    /**
     * This where the search query is retrieved and repackaged as a broadcast to any listening fragments
     * so that they may perform their respective searches.
     * @param intent This is the intent from the activity hosting the searchview sent by the OS.
     *               This intent contains the query as a string extra.
     */
    private void handleIntent(Intent intent) {

        if (!MediUser.isLoggedIn()) {
            finish();
            Toast.makeText(context, "You must login!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if the search query exists and extract it if so.
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            query = intent.getStringExtra(SearchManager.QUERY);
            Intent newIntent = new Intent(SEARCH_INITIATED);
            newIntent.putExtra(SEARCH_QUERY, query);
            sendBroadcast(newIntent);

            // If the query starts with 400 we assume that the user is looking up an account number.
            // If the query is numeric (and doesn't start with "400") we can pretty safely assume the
            // user is interested in customer inventory.  Otherwise we go to their last used search page.
            if (query.startsWith("400")) {
                mViewPager.setCurrentItem(SectionsPagerAdapter.ACCOUNTS, true);
            } else if (Helpers.Numbers.isNumeric(query)) {
                mViewPager.setCurrentItem(SectionsPagerAdapter.CUSTOMER_INVENTORY, true);
            } else {
                mViewPager.setCurrentItem(new MyPreferencesHelper().getLastSearchTab(), true);
            }

        }
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public static final int CUSTOMER_INVENTORY = 0;
        public static final int ACCOUNTS = 1;
        public static final int TICKETS = 2;
        public static final int OPPORTUNITIES = 3;
        public static final int CONTACTS = 4;
        public static final int SHAREPOINT = 5;

        public SectionsPagerAdapter(androidx.fragment.app.FragmentManager fm) {
            super(fm);
            sectionsPagerAdapter = this;
        }

        @Override
        public Fragment getItem(int position) {

            Log.d("getItem", "Creating Fragment in pager at index: " + position);

            if (position == CUSTOMER_INVENTORY) {
                Fragment fragment = new Frag_SearchCustomerInventory();
                Bundle args = new Bundle();
                args.putInt(Frag_SearchCustomerInventory.ARG_SECTION_NUMBER, position + 1);
                fragment.setArguments(args);
                return fragment;
            }

            if (position == ACCOUNTS) {
                Fragment fragment = new Frag_SearchAccounts();
                Bundle args = new Bundle();
                args.putInt(Frag_SearchCustomerInventory.ARG_SECTION_NUMBER, position + 1);
                fragment.setArguments(args);
                return fragment;
            }

            if (position == TICKETS) {
                Fragment fragment = new Frag_SearchTickets();
                Bundle args = new Bundle();
                args.putInt(Frag_SearchCustomerInventory.ARG_SECTION_NUMBER, position + 1);
                fragment.setArguments(args);
                return fragment;
            }

            if (position == OPPORTUNITIES) {
                Fragment fragment = new Frag_SearchOpportunities();
                Bundle args = new Bundle();
                args.putInt(Frag_SearchCustomerInventory.ARG_SECTION_NUMBER, position + 1);
                fragment.setArguments(args);
                return fragment;
            }

            if (position == CONTACTS) {
                Fragment fragment = new Frag_SearchContacts();
                Bundle args = new Bundle();
                args.putInt(Frag_SearchSharePoint.ARG_SECTION_NUMBER, position + 1);
                fragment.setArguments(args);
                return fragment;
            }

            if (position == SHAREPOINT) {
                Fragment fragment = new Frag_SearchSharePoint();
                Bundle args = new Bundle();
                args.putInt(Frag_SearchSharePoint.ARG_SECTION_NUMBER, position + 1);
                fragment.setArguments(args);
                return fragment;
            }
            return null;
        }

        @Override
        public int getCount() {
            if (new MyPreferencesHelper().getEnableSpSearch()) {
                return 6;
            } else {
                return 5;
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {

            curPageIndex = position;

            switch (position) {
                case CUSTOMER_INVENTORY:
                    return "Customer Inventory";
                case ACCOUNTS:
                    return "Accounts";
                case TICKETS:
                    return "Tickets";
                case OPPORTUNITIES:
                    return "Opportunities";
                case CONTACTS:
                    return "Contacts";
                case SHAREPOINT:
                    return "SharePoint";
            }
            return null;
        }
    }

    //region ********************************** FRAGS *****************************************

    public static class Frag_SearchCustomerInventory extends Fragment {
        private static final String TAG = "Frag_CustomerInventory";
        public static final String ARG_SECTION_NUMBER = "section_number";
        TextView txtNoResults;
        public View root;
        public RecyclerView recyclerView;
        RefreshLayout refreshLayout;
        BroadcastReceiver searchReceiver;
        AccountProducts accountProducts;
        BasicObjectRecyclerAdapter adapter;
        Context context;

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                                 @Nullable Bundle savedInstanceState) {
            root = inflater.inflate(R.layout.frag_search_custinventory, container, false);
            this.context = getContext();

            refreshLayout = root.findViewById(R.id.refreshLayout);
            RefreshLayout refreshLayout = root.findViewById(R.id.refreshLayout);
            refreshLayout.setEnableLoadMore(false);
            refreshLayout.setOnRefreshListener(new OnRefreshListener() {
                @Override
                public void onRefresh(RefreshLayout refreshlayout) {
                    doSearch();
                }
            });

            recyclerView = root.findViewById(R.id.recyclerview);
            super.onCreateView(inflater, container, savedInstanceState);


            searchReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (intent.getAction().equals(SEARCH_INITIATED)) {
                        doSearch();
                    }
                }
            };

            doSearch();

            return root;
        }

        @Override
        public void onStop() {
            super.onStop();

        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
        }

        @Override
        public void onResume() {

            txtNoResults = root.findViewById(R.id.txtNoResults);
            txtNoResults.setVisibility(adapter != null && adapter.mData != null && adapter.mData.size() > 0 ? View.INVISIBLE : View.VISIBLE);

            getActivity().registerReceiver(searchReceiver, searchFilter);
            super.onResume();
        }

        @Override
        public void onPause() {
            super.onPause();
            getActivity().unregisterReceiver(searchReceiver);
        }

        void doSearch() {

            if (!Helpers.Numbers.isNumeric(query)) {
                Log.i(TAG, "doSearch Search query is not a number (" + query + ").  Cannot search s/n and will ignore this.");
                return;
            }

            refreshLayout.autoRefreshAnimationOnly();

            try {
                String query = CrmQueries.Search.searchCustInventory(Integer.parseInt(SearchResultsActivity.query));
                Requests.Request request = new Requests.Request(Requests.Request.Function.GET);
                ArrayList<Requests.Argument> args = new ArrayList<>();
                args.add(new Requests.Argument("query", query));
                request.arguments = args;

                Crm crm = new Crm();
                crm.makeCrmRequest(getContext(), request, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        String response = new String(responseBody);
                        accountProducts = new AccountProducts(response);
                        if (getActivity() != null && !getActivity().isFinishing()) {
                            populateList();
                        }
                        Log.i(TAG, "onSuccess ");
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        Toast.makeText(getContext(), error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        refreshLayout.finishRefresh();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        protected void populateList() {

            // Was getting crashes when closing the activity and the populate method was called.
            if (context == null) {
                return;
            }

            // Convert the returned data to BasicObject objects for consumption by the adapter.
            ArrayList<BasicObjects.BasicObject> objects = new ArrayList<>();
            for (AccountProducts.AccountProduct product : accountProducts.list) {
                BasicObjects.BasicObject object = new BasicObjects.BasicObject(product.partNumber + " s/n "
                        + product.serialnumber, product.accountname, product);
                object.topRightText = product.isCapital ? getString(R.string.capital_string) :
                        getString(R.string.not_capital_string);
                object.middleText = product.statusFormatted;
                objects.add(object);
            }

            // Construct the adapter and prep the recyclerview to connect to it.
            adapter = new BasicObjectRecyclerAdapter(context, objects);
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            recyclerView.setAdapter(adapter);

            // Finish the pulldown progress animation.
            refreshLayout.finishRefresh();

            // Hide/Show no results textview based on whether the adapter actually got some data.
            txtNoResults.setVisibility(adapter != null && adapter.mData != null && adapter.mData.size() > 0 ? View.INVISIBLE : View.VISIBLE);
        }
    }

    public static class Frag_SearchAccounts extends Fragment {
        private static final String TAG = "Frag_CustomerInventory";
        public static final String ARG_SECTION_NUMBER = "section_number";
        public View root;
        TextView txtNoResults;
        public RecyclerView recyclerView;
        RefreshLayout refreshLayout;
        BroadcastReceiver searchReceiver;
        CrmEntities.Accounts accounts;
        BasicObjectRecyclerAdapter adapter;

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                                 @Nullable Bundle savedInstanceState) {
            root = inflater.inflate(R.layout.frag_search_results_generic, container, false);

            refreshLayout = root.findViewById(R.id.refreshLayout);
            RefreshLayout refreshLayout = root.findViewById(R.id.refreshLayout);
            refreshLayout.setEnableLoadMore(false);
            refreshLayout.setOnRefreshListener(new OnRefreshListener() {
                @Override
                public void onRefresh(RefreshLayout refreshlayout) {
                    doSearch();
                }
            });

            recyclerView = root.findViewById(R.id.recyclerview);
            super.onCreateView(inflater, container, savedInstanceState);


            searchReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (intent.getAction().equals(SEARCH_INITIATED)) {
                        doSearch();
                    }
                }
            };

            doSearch();

            return root;
        }

        @Override
        public void onStop() {
            super.onStop();

        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
        }

        @Override
        public void onResume() {

            txtNoResults = root.findViewById(R.id.txtNoResults);
            txtNoResults.setVisibility(adapter != null && adapter.mData != null && adapter.mData.size() > 0 ? View.INVISIBLE : View.VISIBLE);

            getActivity().registerReceiver(searchReceiver, searchFilter);
            super.onResume();
        }

        @Override
        public void onPause() {
            super.onPause();
            getActivity().unregisterReceiver(searchReceiver);
        }

        void doSearch() {

            refreshLayout.autoRefreshAnimationOnly();

            try {
                String query = CrmQueries.Search.searchAccounts(SearchResultsActivity.query);
                Requests.Request request = new Requests.Request(Requests.Request.Function.GET);
                ArrayList<Requests.Argument> args = new ArrayList<>();
                args.add(new Requests.Argument("query", query));
                request.arguments = args;

                Crm crm = new Crm();
                crm.makeCrmRequest(getContext(), request, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        String response = new String(responseBody);
                        accounts = new CrmEntities.Accounts(response);
                        if (getActivity() != null && !getActivity().isFinishing()) {
                            populateList();
                        }
                        Log.i(TAG, "onSuccess ");
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        Toast.makeText(getContext(), error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        refreshLayout.finishRefresh();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        protected void populateList() {

            // Convert the returned data to BasicObject objects for consumption by the adapter.
            ArrayList<BasicObjects.BasicObject> objects = new ArrayList<>();
            for (CrmEntities.Accounts.Account account : accounts.list) {
                BasicObjects.BasicObject object = new BasicObjects.BasicObject(account.accountName, account.accountnumber, account);
                object.middleText = account.customerTypeFormatted;
                objects.add(object);
            }

            // Configure the adapter and prep the recyclerview for it.
            adapter = new BasicObjectRecyclerAdapter(getContext(), objects);
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            recyclerView.setAdapter(adapter);
            adapter.setClickListener(new BasicObjectRecyclerAdapter.ItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    Intent intent = new Intent(getContext(), Activity_AccountData.class);
                    intent.setAction(Activity_AccountData.GO_TO_ACCOUNT);
                    intent.putExtra(Activity_AccountData.GO_TO_ACCOUNT_OBJECT, accounts.list.get(position));
                    startActivity(intent);
                }
            });
            refreshLayout.finishRefresh();

            // Hide/Show no results textview based on the presence of adapter data.
            if (txtNoResults != null) {
                txtNoResults.setVisibility(adapter != null && adapter.mData != null && adapter.mData
                        .size() > 0 ? View.INVISIBLE : View.VISIBLE);
            }
        }
    }

    public static class Frag_SearchTickets extends Fragment {
        private static final String TAG = "Frag_CustomerInventory";
        public static final String ARG_SECTION_NUMBER = "section_number";
        public View root;
        TextView txtNoResults;
        public RecyclerView recyclerView;
        RefreshLayout refreshLayout;
        BroadcastReceiver searchReceiver;
        CrmEntities.Tickets tickets;
        BasicObjectRecyclerAdapter adapter;

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                                 @Nullable Bundle savedInstanceState) {
            root = inflater.inflate(R.layout.frag_search_results_generic, container, false);

            refreshLayout = root.findViewById(R.id.refreshLayout);
            RefreshLayout refreshLayout = root.findViewById(R.id.refreshLayout);
            refreshLayout.setEnableLoadMore(false);
            refreshLayout.setOnRefreshListener(new OnRefreshListener() {
                @Override
                public void onRefresh(RefreshLayout refreshlayout) {
                    doSearch();
                }
            });

            recyclerView = root.findViewById(R.id.recyclerview);
            super.onCreateView(inflater, container, savedInstanceState);


            searchReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (intent.getAction().equals(SEARCH_INITIATED)) {
                        doSearch();
                    }
                }
            };

            doSearch();

            return root;
        }

        @Override
        public void onStop() {
            super.onStop();

        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
        }

        @Override
        public void onResume() {

            txtNoResults = root.findViewById(R.id.txtNoResults);
            txtNoResults.setVisibility(adapter != null && adapter.mData != null && adapter.mData.size() > 0 ? View.INVISIBLE : View.VISIBLE);

            getActivity().registerReceiver(searchReceiver, searchFilter);
            super.onResume();
        }

        @Override
        public void onPause() {
            super.onPause();
            getActivity().unregisterReceiver(searchReceiver);
        }

        void doSearch() {

            refreshLayout.autoRefreshAnimationOnly();

            try {
                String query = CrmQueries.Search.searchTickets(SearchResultsActivity.query);
                Requests.Request request = new Requests.Request(Requests.Request.Function.GET);
                ArrayList<Requests.Argument> args = new ArrayList<>();
                args.add(new Requests.Argument("query", query));
                request.arguments = args;

                Crm crm = new Crm();
                crm.makeCrmRequest(getContext(), request, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        String response = new String(responseBody);
                        tickets = new CrmEntities.Tickets(response);
                        if (getActivity() != null && !getActivity().isFinishing()) {
                            populateList();
                        }
                        Log.i(TAG, "onSuccess ");
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        Toast.makeText(getContext(), error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        refreshLayout.finishRefresh();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        protected void populateList() {
            final ArrayList<BasicObjects.BasicObject> objects = new ArrayList<>();
            for (CrmEntities.Tickets.Ticket ticket : tickets.list) {
                BasicObjects.BasicObject object = new BasicObjects.BasicObject(ticket.ticketnumber, ticket.customerFormatted, ticket);
                object.middleText = ticket.statusFormatted;
                objects.add(object);
            }

            if (getContext() == null) {
                return;
            }

            adapter = new BasicObjectRecyclerAdapter(getContext(), objects);
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            recyclerView.setAdapter(adapter);
            adapter.setClickListener(new BasicObjectRecyclerAdapter.ItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    CrmEntities.Tickets.Ticket selectedTicket =
                            (CrmEntities.Tickets.Ticket) objects.get(position).object;
                    Intent intent = new Intent(getContext(), BasicEntityActivity.class);
                    intent.putExtra(BasicEntityActivity.ACTIVITY_TITLE, "Ticket Details");
                    intent.putExtra(BasicEntityActivity.ENTITYID, selectedTicket.entityid);
                    intent.putExtra(BasicEntityActivity.ENTITY_LOGICAL_NAME, "incident");
                    intent.putExtra(BasicEntityActivity.GSON_STRING, selectedTicket.toBasicEntity().toGson());
                    startActivityForResult(intent, BasicEntityActivity.REQUEST_BASIC);

                    try {
                        MileBuddyMetrics.updateMetric(getContext(), MileBuddyMetrics.MetricName.LAST_OPENED_TICKET, DateTime.now());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            refreshLayout.finishRefresh();

            txtNoResults.setVisibility(adapter != null && adapter.mData != null && adapter.mData.size() > 0 ? View.INVISIBLE : View.VISIBLE);
        }
    }

    public static class Frag_SearchOpportunities extends Fragment {
        private static final String TAG = "Frag_CustomerInventory";
        public static final String ARG_SECTION_NUMBER = "section_number";
        public View root;
        TextView txtNoResults;
        public RecyclerView recyclerView;
        RefreshLayout refreshLayout;
        BroadcastReceiver searchReceiver;
        CrmEntities.Opportunities opportunities;
        BasicObjectRecyclerAdapter adapter;

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                                 @Nullable Bundle savedInstanceState) {
            root = inflater.inflate(R.layout.frag_search_results_generic, container, false);

            refreshLayout = root.findViewById(R.id.refreshLayout);
            RefreshLayout refreshLayout = root.findViewById(R.id.refreshLayout);
            refreshLayout.setEnableLoadMore(false);
            refreshLayout.setOnRefreshListener(new OnRefreshListener() {
                @Override
                public void onRefresh(RefreshLayout refreshlayout) {
                    doSearch();
                }
            });

            recyclerView = root.findViewById(R.id.recyclerview);
            super.onCreateView(inflater, container, savedInstanceState);

            searchReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (intent.getAction().equals(SEARCH_INITIATED)) {
                        doSearch();
                    }
                }
            };

            doSearch();

            return root;
        }

        @Override
        public void onStop() {
            super.onStop();

        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
        }

        @Override
        public void onResume() {
            txtNoResults = root.findViewById(R.id.txtNoResults);
            txtNoResults.setVisibility(adapter != null && adapter.mData != null && adapter.mData.size() > 0 ? View.INVISIBLE : View.VISIBLE);
            getActivity().registerReceiver(searchReceiver, searchFilter);
            super.onResume();
        }

        @Override
        public void onPause() {
            super.onPause();
            getActivity().unregisterReceiver(searchReceiver);
        }

        void doSearch() {

            refreshLayout.autoRefreshAnimationOnly();

            try {
                String query = CrmQueries.Search.searchOpportunities(SearchResultsActivity.query);
                Requests.Request request = new Requests.Request(Requests.Request.Function.GET);
                ArrayList<Requests.Argument> args = new ArrayList<>();
                args.add(new Requests.Argument("query", query));
                request.arguments = args;

                Crm crm = new Crm();
                crm.makeCrmRequest(getContext(), request, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        String response = new String(responseBody);
                        opportunities = new CrmEntities.Opportunities(response);
                        if (getActivity() != null && !getActivity().isFinishing()) {
                            populateList();
                        }
                        Log.i(TAG, "onSuccess ");
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        Toast.makeText(getContext(), error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        refreshLayout.finishRefresh();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        protected void populateList() {
            final ArrayList<BasicObjects.BasicObject> objects = new ArrayList<>();
            for (CrmEntities.Opportunities.Opportunity opportunity : opportunities.list) {
                BasicObjects.BasicObject object = new BasicObjects.BasicObject(opportunity.name, opportunity.accountname, opportunity);
                object.middleText = opportunity.statuscodeFormatted;
                objects.add(object);
            }

            if (getContext() == null) {
                return;
            }

            adapter = new BasicObjectRecyclerAdapter(getContext(), objects);
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            recyclerView.setAdapter(adapter);
            adapter.setClickListener(new BasicObjectRecyclerAdapter.ItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    CrmEntities.Opportunities.Opportunity selectedOpportunity =
                            (CrmEntities.Opportunities.Opportunity) objects.get(position).object;
                    Intent intent = new Intent(getContext(), BasicEntityActivity.class);
                    intent.putExtra(BasicEntityActivity.ACTIVITY_TITLE, "Opportunity Details");
                    intent.putExtra(BasicEntityActivity.ENTITYID, selectedOpportunity.entityid);
                    intent.putExtra(BasicEntityActivity.ENTITY_LOGICAL_NAME, "opportunity");
                    intent.putExtra(BasicEntityActivity.GSON_STRING, selectedOpportunity.toBasicEntity().toGson());
                    startActivityForResult(intent, BasicEntityActivity.REQUEST_BASIC);

                    try {
                        MileBuddyMetrics.updateMetric(getContext(), MileBuddyMetrics.MetricName.LAST_OPENED_OPPORTUNITY, DateTime.now());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            refreshLayout.finishRefresh();

            txtNoResults.setVisibility(adapter != null && adapter.mData != null && adapter.mData.size() > 0 ? View.INVISIBLE : View.VISIBLE);
        }
    }

    public static class Frag_SearchContacts extends Fragment {

        final ArrayList<BasicObjects.BasicObject> objects = new ArrayList<>();
        private static final String TAG = "Frag_CustomerInventory";
        public static final String ARG_SECTION_NUMBER = "section_number";
        public View root;
        TextView txtNoResults;
        public RecyclerView recyclerView;
        RefreshLayout refreshLayout;
        BroadcastReceiver searchReceiver;
        CrmEntities.Contacts contacts;
        BasicObjectRecyclerAdapter adapter;
        String attemptedPhonenumber;

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                                 @Nullable Bundle savedInstanceState) {
            root = inflater.inflate(R.layout.frag_search_results_generic, container, false);
            refreshLayout = root.findViewById(R.id.refreshLayout);
            RefreshLayout refreshLayout = root.findViewById(R.id.refreshLayout);
            refreshLayout.setEnableLoadMore(false);
            refreshLayout.setOnRefreshListener(new OnRefreshListener() {
                @Override
                public void onRefresh(RefreshLayout refreshlayout) {
                    doSearch();
                }
            });

            recyclerView = root.findViewById(R.id.recyclerview);
            super.onCreateView(inflater, container, savedInstanceState);


            searchReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (intent.getAction().equals(SEARCH_INITIATED)) {
                        doSearch();
                    }
                }
            };

            doSearch();

            return root;
        }

        @Override
        public void onStop() {
            super.onStop();

        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
            Log.i(TAG, "onDestroyView | Unregistered the entity edited reciever.");
        }

        @Override
        public void onResume() {

            txtNoResults = root.findViewById(R.id.txtNoResults);
            txtNoResults.setVisibility(adapter != null && adapter.mData != null && adapter.mData.size() > 0 ? View.INVISIBLE : View.VISIBLE);

            getActivity().registerReceiver(searchReceiver, searchFilter);
            Log.i(TAG, "onResume | Registered the search receiver!");
            super.onResume();
        }

        @Override
        public void onPause() {
            super.onPause();
            getActivity().unregisterReceiver(searchReceiver);
            Log.i(TAG, "onPause Unregistered the search receiver.");
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

        void doSearch() {

            refreshLayout.autoRefreshAnimationOnly();

            try {
                String query = CrmQueries.Search.searchContacts(SearchResultsActivity.query);
                Requests.Request request = new Requests.Request(Requests.Request.Function.GET);
                ArrayList<Requests.Argument> args = new ArrayList<>();
                args.add(new Requests.Argument("query", query));
                request.arguments = args;

                Crm crm = new Crm();
                crm.makeCrmRequest(getContext(), request, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        String response = new String(responseBody);
                        contacts = new CrmEntities.Contacts(response);
                        if (getActivity() != null && !getActivity().isFinishing()) {
                            populateList();
                        }
                        Log.i(TAG, "onSuccess ");
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        Toast.makeText(getContext(), error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        refreshLayout.finishRefresh();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        protected void populateList() {
            for (CrmEntities.Contacts.Contact contact : contacts.list) {
                BasicObjects.BasicObject object = new BasicObjects.BasicObject(contact.getFullname(), contact.accountFormatted, contact);
                object.middleText = contact.jobtitle;
                objects.add(object);
            }

            if (getContext() == null) {
                return;
            }

            adapter = new BasicObjectRecyclerAdapter(getContext(), objects);
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            recyclerView.setAdapter(adapter);
            adapter.setClickListener(new BasicObjectRecyclerAdapter.ItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    CrmEntities.Contacts.Contact selectedContact =
                            (CrmEntities.Contacts.Contact) objects.get(position).object;

                    ContactActions contactActions = new ContactActions(getActivity(), selectedContact);
                    contactActions.showContactOptions();

                }
            });
            refreshLayout.finishRefresh();

            txtNoResults.setVisibility(adapter != null && adapter.mData != null && adapter.mData.size() > 0 ? View.INVISIBLE : View.VISIBLE);
        }
    }

    public static class Frag_SearchSharePoint extends Fragment {
        private static final String TAG = "Frag_CustomerInventory";
        public static final String ARG_SECTION_NUMBER = "section_number";
        public View root;
        TextView txtNoResults;
        public RecyclerView recyclerView;
        RefreshLayout refreshLayout;
        BroadcastReceiver searchReceiver;
        CrmEntities.Contacts contacts;
        BasicObjectRecyclerAdapter adapter;
        String attemptedPhonenumber;

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                                 @Nullable Bundle savedInstanceState) {
            root = inflater.inflate(R.layout.frag_search_results_generic, container, false);
            refreshLayout = root.findViewById(R.id.refreshLayout);
            RefreshLayout refreshLayout = root.findViewById(R.id.refreshLayout);
            refreshLayout.setEnableLoadMore(false);
            refreshLayout.setOnRefreshListener(new OnRefreshListener() {
                @Override
                public void onRefresh(RefreshLayout refreshlayout) {
                    doSearch();
                }
            });

            recyclerView = root.findViewById(R.id.recyclerview);
            super.onCreateView(inflater, container, savedInstanceState);


            searchReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (intent.getAction().equals(SEARCH_INITIATED)) {
                        doSearch();
                    }
                }
            };

            doSearch();

            return root;
        }

        @Override
        public void onStop() {
            super.onStop();

        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
        }

        @Override
        public void onResume() {

            txtNoResults = root.findViewById(R.id.txtNoResults);
            txtNoResults.setVisibility(adapter != null && adapter.mData != null && adapter.mData.size() > 0 ? View.INVISIBLE : View.VISIBLE);

            getActivity().registerReceiver(searchReceiver, searchFilter);
            super.onResume();
        }

        @Override
        public void onPause() {
            super.onPause();
            getActivity().unregisterReceiver(searchReceiver);
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

        void doSearch() {

            refreshLayout.autoRefreshAnimationOnly();

            try {
                SharePoint.searchSp(getContext(), SearchResultsActivity.query, new SharePoint.SearchListener() {
                    @Override
                    public void onSuccess(@Nullable SharePoint.SharePointItems items) {
                        Toast.makeText(getContext(), items.list.size() + " results!", Toast.LENGTH_SHORT).show();
                        refreshLayout.finishRefresh();
                    }

                    @Override
                    public void onFailure(String msg) {
                        refreshLayout.finishRefresh();
                        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                refreshLayout.finishRefresh();
                e.printStackTrace();
            }

        }

        protected void populateList() {
            final ArrayList<BasicObjects.BasicObject> objects = new ArrayList<>();
            for (CrmEntities.Contacts.Contact contact : contacts.list) {
                BasicObjects.BasicObject object = new BasicObjects.BasicObject(contact.getFullname(), contact.accountFormatted, contact);
                object.middleText = contact.jobtitle;
                objects.add(object);
            }

            if (getContext() == null) {
                return;
            }

            adapter = new BasicObjectRecyclerAdapter(getContext(), objects);
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            recyclerView.setAdapter(adapter);
            adapter.setClickListener(new BasicObjectRecyclerAdapter.ItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    CrmEntities.Contacts.Contact selectedContact =
                            (CrmEntities.Contacts.Contact) objects.get(position).object;

                    ContactActions contactActions = new ContactActions(getActivity(), selectedContact);
                    contactActions.showContactOptions();

                }
            });
            refreshLayout.finishRefresh();

            txtNoResults.setVisibility(adapter != null && adapter.mData != null && adapter.mData.size() > 0 ? View.INVISIBLE : View.VISIBLE);
        }
    }

    // endregion

}





































