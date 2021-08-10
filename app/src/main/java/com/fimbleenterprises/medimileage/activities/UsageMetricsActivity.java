package com.fimbleenterprises.medimileage.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerTitleStrip;
import cz.msebera.android.httpclient.Header;

import android.content.BroadcastReceiver;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import com.fimbleenterprises.medimileage.Crm;
import com.fimbleenterprises.medimileage.ExpandableBasicObjectListviewAdapter;
import com.fimbleenterprises.medimileage.Helpers;
import com.fimbleenterprises.medimileage.MyViewPager;
import com.fimbleenterprises.medimileage.QueryFactory;
import com.fimbleenterprises.medimileage.R;
import com.fimbleenterprises.medimileage.adapters.BasicObjectsExpandableListviewAdapter;
import com.fimbleenterprises.medimileage.adapters.OrderLineRecyclerAdapter;
import com.fimbleenterprises.medimileage.objects_and_containers.BasicObjects;
import com.fimbleenterprises.medimileage.objects_and_containers.CrmEntities;
import com.fimbleenterprises.medimileage.objects_and_containers.ExpandableListDataPump;
import com.fimbleenterprises.medimileage.objects_and_containers.Requests;
import com.fimbleenterprises.medimileage.objects_and_containers.Territory;
import com.fimbleenterprises.medimileage.objects_and_containers.UserUsageMetrics;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnLoadMoreListener;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class UsageMetricsActivity extends AppCompatActivity {

    public static MyViewPager mViewPager;
    public static PagerTitleStrip mPagerStrip;
    public static SectionsPagerAdapter sectionsPagerAdapter;
    private int curPageIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usage_metrics);

        sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager = findViewById(R.id.main_pager_usage_metrics);
        mViewPager.onRealPageChangedListener = new MyViewPager.OnRealPageChangedListener() {
            @Override
            public void onPageActuallyFuckingChanged(int pageIndex) {
                setTitle(sectionsPagerAdapter.getPageTitle(pageIndex));
            }
        };
        mPagerStrip = (PagerTitleStrip) findViewById(R.id.pager_title_strip_usage_metrics);
        mViewPager.setAdapter(sectionsPagerAdapter);
        mViewPager.setOffscreenPageLimit(0);
        mViewPager.setCurrentItem(0);
        mViewPager.setPageCount(6);
        mViewPager.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {

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

        this.setTitle(R.string.title_activity_metrics);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        private static final String TAG = "SectionsPagerAdapter";

        public static final int PAGE_ONE = 0;

        public SectionsPagerAdapter(androidx.fragment.app.FragmentManager fm) {
            super(fm);
            sectionsPagerAdapter = this;
        }

        @Override
        public Fragment getItem(int position) {

            Log.d("getItem", "Creating Fragment in pager at index: " + position);
            Log.w(TAG, "getItem: PAGER POSITION: " + position);

            if (position == PAGE_ONE) {
                Fragment fragment = new Frag_BasicUsage();
                Bundle args = new Bundle();
                args.putInt(null, position + 1);
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

            switch (position) {
                case PAGE_ONE:
                    return "Users";
            }
            return null;
        }
    }

    //region ********************************** FRAGS *****************************************
    public static class Frag_BasicUsage extends Fragment {
        private static final String TAG = "Frag_BasicUsage";
        public static final String ARG_SECTION_NUMBER = "section_number";
        public View root;
        public RecyclerView recyclerView;
        RefreshLayout refreshLayout;
        OrderLineRecyclerAdapter adapter;
        Territory curTerritory;
        BroadcastReceiver territoryChangedReceiver;
        ArrayList<CrmEntities.OrderProducts.OrderProduct> allOrders = new ArrayList<>();
        TextView txtNoSales;
        BroadcastReceiver salesLinesReceiver;

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                                 @Nullable Bundle savedInstanceState) {
            root = inflater.inflate(R.layout.fragment_metrics, container, false);
            txtNoSales = root.findViewById(R.id.txtNoContacts);
            refreshLayout = root.findViewById(R.id.refreshLayout);
            RefreshLayout refreshLayout = root.findViewById(R.id.refreshLayout);
            refreshLayout.setEnableLoadMore(false);
            refreshLayout.setOnRefreshListener(new OnRefreshListener() {
                @Override
                public void onRefresh(RefreshLayout refreshlayout) {
                    getStats();
                }
            });
            refreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
                @Override
                public void onLoadMore(RefreshLayout refreshlayout) {
                    refreshlayout.finishLoadMore(500/*,false*/);
                }
            });

            recyclerView = root.findViewById(R.id.orderLinesRecyclerview);
            super.onCreateView(inflater, container, savedInstanceState);

            refreshLayout.autoRefreshAnimationOnly();
            try {
                getStats();
            } catch (Exception e) {
                refreshLayout.finishRefresh();
                e.printStackTrace();
            }

            return root;
        }


        @Override
        public void onStop() {
            super.onStop();

        }

        @Override
        public void onPause() {

            super.onPause();
        }

        public void getStats() {

            QueryFactory factory = new QueryFactory("systemuser");
            factory.addColumn("fullname");
            factory.addColumn("systemuserid");
            factory.addColumn("msus_use_trip_minder");
            factory.addColumn("msus_trip_minder_value");
            factory.addColumn("msus_milebuddy_version");
            factory.addColumn("msus_milebuddy_last_accessed_territory_data");
            factory.addColumn("msus_milebuddy_last_accessed_territory_changer");
            factory.addColumn("msus_medibuddy_version");
            factory.addColumn("msus_last_viewed_mileage_stats");
            factory.addColumn("msus_last_used_milebuddy");
            factory.addColumn("msus_last_synced_mileage");
            factory.addColumn("msus_last_opened_ticket");
            factory.addColumn("msus_last_opened_settings");
            factory.addColumn("msus_last_opened_opportunity");
            factory.addColumn("msus_last_generated_receipt_milebuddy");
            factory.addColumn("msus_last_generated_receipt");
            factory.addColumn("msus_last_created_note");
            factory.addColumn("msus_last_accessed_territory_data");
            factory.addColumn("msus_last_accessed_search");
            factory.addColumn("msus_last_accessed_other_user_trips");
            factory.addColumn("msus_last_accessed_milebuddy");
            factory.addColumn("msus_last_accessed_medibuddy");
            factory.addColumn("msus_last_accessed_account_data");

            factory.filter = new QueryFactory.Filter(QueryFactory.Filter.FilterType.AND,
                    new QueryFactory.Filter.FilterCondition("msus_is_mileage_user",
                            QueryFactory.Filter.Operator.EQUALS, "1"));

            Requests.Request request = new Requests.Request(Requests.Request.Function.GET);
            ArrayList<Requests.Argument> args = new ArrayList<>();
            args.add(new Requests.Argument("query", factory.construct()));
            request.arguments = args;

            new Crm().makeCrmRequest(getContext(), request, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

                    Log.i(TAG, "onSuccess | " + new String(responseBody));

                    ArrayList<BasicObjectsExpandableListviewAdapter.BasicObjectGroup> groups = new ArrayList<>();
                    UserUsageMetrics metrics = new UserUsageMetrics(new String(responseBody));

                    for (UserUsageMetrics.UserUsageMetric metric : metrics.list) {
                        BasicObjectsExpandableListviewAdapter.BasicObjectGroup group = new BasicObjectsExpandableListviewAdapter.BasicObjectGroup();
                        group.title = metric.fullname;
                        group.rightText = (metric.msus_milebuddy_version != null) ? "ver: " + metric.msus_milebuddy_version : "ver: n/a";
                        if (metric.msus_milebuddy_version != null) {
                            group.children.add(new BasicObjects.BasicObject("Version:", metric.msus_milebuddy_version));
                            group.children.add(new BasicObjects.BasicObject("Trip minder interval:",
                                    metric.msus_trip_minder_value));
                            group.children.add(new BasicObjects.BasicObject("Last accessed account data:", metric.msus_last_accessed_account_data == null ? "n/a" :
                                    Helpers.DatesAndTimes.getPrettyDateAndTime(metric.msus_last_accessed_account_data)));
                            group.children.add(new BasicObjects.BasicObject("Last accessed app:",  metric.msus_last_accessed_milebuddy == null ? "n/a" :
                                    Helpers.DatesAndTimes.getPrettyDateAndTime(metric.msus_last_accessed_milebuddy)));
                            group.children.add(new BasicObjects.BasicObject("Last accessed other user trips:", metric.msus_last_accessed_other_user_trips == null ? "n/a" :
                                    Helpers.DatesAndTimes.getPrettyDateAndTime(metric.msus_last_accessed_other_user_trips)));
                            group.children.add(new BasicObjects.BasicObject("Last performed search:",  metric.msus_last_accessed_search == null ? "n/a" :
                                    Helpers.DatesAndTimes.getPrettyDateAndTime(metric.msus_last_accessed_search)));
                            group.children.add(new BasicObjects.BasicObject("Last created note:",  metric.msus_last_created_note == null ? "n/a" :
                                    Helpers.DatesAndTimes.getPrettyDateAndTime(metric.msus_last_created_note)));
                            group.children.add(new BasicObjects.BasicObject("Last generated receipt:",  metric.msus_last_generated_receipt == null ? "n/a" :
                                    Helpers.DatesAndTimes.getPrettyDateAndTime(metric.msus_last_generated_receipt)));
                            group.children.add(new BasicObjects.BasicObject("Last opened opportunity:",  metric.msus_last_opened_opportunity == null ? "n/a" :
                                    Helpers.DatesAndTimes.getPrettyDateAndTime(metric.msus_last_opened_opportunity)));
                            group.children.add(new BasicObjects.BasicObject("Last accessed preferences:",  metric.msus_last_opened_settings == null ? "n/a" :
                                    Helpers.DatesAndTimes.getPrettyDateAndTime(metric.msus_last_opened_settings)));
                            group.children.add(new BasicObjects.BasicObject("Last opened a ticket:",  metric.msus_last_opened_ticket == null ? "n/a" :
                                    Helpers.DatesAndTimes.getPrettyDateAndTime(metric.msus_last_opened_ticket)));
                            group.children.add(new BasicObjects.BasicObject("Last synced mileage:",  metric.msus_last_synced_mileage == null ? "n/a" :
                                    Helpers.DatesAndTimes.getPrettyDateAndTime(metric.msus_last_synced_mileage)));
                            group.children.add(new BasicObjects.BasicObject("Last viewed mileage stats:",  metric.msus_last_viewed_mileage_stats == null ? "n/a" :
                                    Helpers.DatesAndTimes.getPrettyDateAndTime(metric.msus_last_viewed_mileage_stats)));
                            group.children.add(new BasicObjects.BasicObject("Last accessed territory data:",  metric.msus_milebuddy_last_accessed_territory_data == null ? "n/a" :
                                    Helpers.DatesAndTimes.getPrettyDateAndTime(metric.msus_milebuddy_last_accessed_territory_data)));
                            group.children.add(new BasicObjects.BasicObject("Last accessed territory changer:",  metric.msus_milebuddy_last_accessed_territory_changer == null ? "n/a" :
                                    Helpers.DatesAndTimes.getPrettyDateAndTime(metric.msus_milebuddy_last_accessed_territory_changer)));
                        }
                        groups.add(group);
                    }

                    populateList(groups);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    Toast.makeText(getActivity(), error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    refreshLayout.finishRefresh();
                }
            });

        }

        public void populateList(ArrayList<BasicObjectsExpandableListviewAdapter.BasicObjectGroup> groups) {

            ExpandableListView expandableListView = (ExpandableListView) root.findViewById(R.id.expandableListView);
            BasicObjectsExpandableListviewAdapter expandableListAdapter = new
                    BasicObjectsExpandableListviewAdapter(getContext(), groups);
            expandableListView.setAdapter(expandableListAdapter);
            expandableListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
                @Override
                public void onGroupExpand(int groupPosition) {

                }
            });

            expandableListView.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {
                @Override
                public void onGroupCollapse(int groupPosition) {

                }
            });

            expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
                @Override
                public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                    return false;
                }
            });

            refreshLayout.finishRefresh();

        }

    }
}