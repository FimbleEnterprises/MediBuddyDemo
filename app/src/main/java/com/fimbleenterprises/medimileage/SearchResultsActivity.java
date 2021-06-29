package com.fimbleenterprises.medimileage;

import android.app.Activity;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

/*import com.anychart.APIlib;
import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Cartesian;*/
import com.fimbleenterprises.medimileage.CrmEntities.AccountProducts;
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

    public Context context;
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
        setContentView(R.layout.activity_search_results);
        this.context = this;

        sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager = (MyViewPager) findViewById(R.id.main_pager_yo_search_results);
        mViewPager.onRealPageChangedListener = new MyViewPager.OnRealPageChangedListener() {
            @Override
            public void onPageActuallyFuckingChanged(int pageIndex) {
                setTitle("Search (" + sectionsPagerAdapter.getPageTitle(pageIndex) + ")");
            }
        };
        mPagerStrip = findViewById(R.id.pager_title_strip_search_results);
        mViewPager.setAdapter(sectionsPagerAdapter);
        mViewPager.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {

            }
        });

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

    private void handleIntent(Intent intent) {

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            query = intent.getStringExtra(SearchManager.QUERY);
            Intent newIntent = new Intent(SEARCH_INITIATED);
            newIntent.putExtra(SEARCH_QUERY, query);
            sendBroadcast(newIntent);

            if (Helpers.Numbers.isNumeric(query)) {
                mViewPager.setCurrentItem(SectionsPagerAdapter.CUSTOMER_INVENTORY, true);
            } else {
                mViewPager.setCurrentItem(SectionsPagerAdapter.ACCOUNTS, true);
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
                Fragment fragment = new Frag_SearchSharePoint();
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
            if (new MySettingsHelper().getEnableSpSearch()) {
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
                String query = Queries.Search.searchCustInventory(Integer.parseInt(SearchResultsActivity.query));
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

            ArrayList<BasicObjects.BasicObject> objects = new ArrayList<>();
            for (AccountProducts.AccountProduct product : accountProducts.list) {
                BasicObjects.BasicObject object = new BasicObjects.BasicObject(product.partNumber + " s/n "
                        + product.serialnumber, product.accountname, product);
                object.middleText = product.statusFormatted;
                objects.add(object);
            }

            adapter = new BasicObjectRecyclerAdapter(context, objects);
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            recyclerView.setAdapter(adapter);

            refreshLayout.finishRefresh();
        }
    }

    public static class Frag_SearchAccounts extends Fragment {
        private static final String TAG = "Frag_CustomerInventory";
        public static final String ARG_SECTION_NUMBER = "section_number";
        public View root;
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
                String query = Queries.Search.searchAccounts(SearchResultsActivity.query);
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
            ArrayList<BasicObjects.BasicObject> objects = new ArrayList<>();
            for (CrmEntities.Accounts.Account account : accounts.list) {
                BasicObjects.BasicObject object = new BasicObjects.BasicObject(account.accountName, account.accountnumber, account);
                object.middleText = account.customerTypeFormatted;
                objects.add(object);
            }

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
        }
    }

    public static class Frag_SearchTickets extends Fragment {
        private static final String TAG = "Frag_CustomerInventory";
        public static final String ARG_SECTION_NUMBER = "section_number";
        public View root;
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
                String query = Queries.Search.searchTickets(SearchResultsActivity.query);
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
                    intent.putExtra(BasicEntityActivity.ENTITYID, selectedTicket.ticketid);
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
        }
    }

    public static class Frag_SearchOpportunities extends Fragment {
        private static final String TAG = "Frag_CustomerInventory";
        public static final String ARG_SECTION_NUMBER = "section_number";
        public View root;
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
                String query = Queries.Search.searchOpportunities(SearchResultsActivity.query);
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
                    intent.putExtra(BasicEntityActivity.ENTITYID, selectedOpportunity.opportunityid);
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
        }
    }

    public static class Frag_SearchContacts extends Fragment {
        private static final String TAG = "Frag_CustomerInventory";
        public static final String ARG_SECTION_NUMBER = "section_number";
        public View root;
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
                String query = Queries.Search.searchContacts(SearchResultsActivity.query);
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
        }
    }

    public static class Frag_SearchSharePoint extends Fragment {
        private static final String TAG = "Frag_CustomerInventory";
        public static final String ARG_SECTION_NUMBER = "section_number";
        public View root;
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
        }
    }

    // endregion

}





































