package com.fimbleenterprises.medimileage.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import com.fimbleenterprises.medimileage.Crm;
import com.fimbleenterprises.medimileage.CrmQueries;
import com.fimbleenterprises.medimileage.MyPreferencesHelper;
import com.fimbleenterprises.medimileage.MyViewPager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.PagerTitleStrip;
import cz.msebera.android.httpclient.Header;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.fimbleenterprises.medimileage.R;
import com.fimbleenterprises.medimileage.dialogs.fullscreen_pickers.FullscreenAccountTerritoryPicker;
import com.fimbleenterprises.medimileage.dialogs.fullscreen_pickers.FullscreenActivityBasicObjectPicker;
import com.fimbleenterprises.medimileage.objects_and_containers.BasicObjects;
import com.fimbleenterprises.medimileage.objects_and_containers.CrmEntities;
import com.fimbleenterprises.medimileage.objects_and_containers.Requests;
import com.fimbleenterprises.medimileage.objects_and_containers.Territory;
import com.fimbleenterprises.medimileage.activities.ui.CustomViews.MyHyperlinkTextview;
import com.loopj.android.http.AsyncHttpResponseHandler;

import java.util.ArrayList;

import static com.fimbleenterprises.medimileage.dialogs.fullscreen_pickers.FullscreenAccountTerritoryPicker.ACCOUNT_RESULT;

public class CreateQuoteScrollingActivity extends AppCompatActivity {
    private static final String TAG = "CreateQuoteScrollingActivity";
    public static final String FRAG_NUMBER = "FRAG_NUMBER";

    public static CrmEntities.Accounts.Account currentAccount;
    public static CrmEntities.CustomerAddresses currentAccountAddresses;
    public static CrmEntities.CustomerAddresses.CustomerAddress selectedAddress;

    public static ArrayList<Territory> cachedTerritories = null;

    public static MyPreferencesHelper options;
    public static Button btnPrev;
    public static Button btnNext;
    public static MyViewPager mViewPager;
    public static PagerTitleStrip mPagerStrip;
    public static SectionsPagerAdapter sectionsPagerAdapter;
    public static androidx.fragment.app.FragmentManager fragMgr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        options = new MyPreferencesHelper(this);

        setContentView(R.layout.activity_create_quote_scrolling);

        sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager = (MyViewPager) findViewById(R.id.main_pager_yo);
        mViewPager.onRealPageChangedListener = new MyViewPager.OnRealPageChangedListener() {
            @Override
            public void onPageActuallyFuckingChanged(int pageIndex) {
                if (pageIndex == 2) {
                    btnNext.setText("Save");
                    btnNext.setBackgroundResource(R.drawable.btn_glass_gray_orange_border);
                } else {
                    btnNext.setText("Next");
                    btnNext.setBackgroundResource(R.drawable.btn_glass_gray);
                }
            }
        };
        mPagerStrip = (PagerTitleStrip) findViewById(R.id.pager_title_strip);
        mViewPager.setAdapter(sectionsPagerAdapter);
        mViewPager.setOffscreenPageLimit(0);
        mViewPager.setCurrentItem(0);
        mViewPager.setPageCount(3);
        mViewPager.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                Log.i(TAG, "onScrollChange scrollX: " + scrollX + " scrollY: " + scrollY + "" +
                        "oldScrollX: " + oldScrollX + " oldScrollY: " + oldScrollY);
                Log.i(TAG, "onScrollChange Page: " + mViewPager.currentPosition);
            }
        });


        fragMgr = getSupportFragmentManager();

        btnPrev = findViewById(R.id.btn_prev_view);
        btnPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mViewPager.isOnFirstPage()) {
                    mViewPager.setCurrentItem(mViewPager.currentPosition - 1);
                }
            }
        });

        btnNext = findViewById(R.id.btn_next_view);
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mViewPager.isOnLastPage()) {
                    mViewPager.setCurrentItem(mViewPager.currentPosition + 1);
                }

            }
        });

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data != null) {
            if (data.getParcelableArrayListExtra(FullscreenAccountTerritoryPicker.FOUND_TERRITORIES) != null) {
                cachedTerritories = data.getParcelableArrayListExtra(FullscreenAccountTerritoryPicker.FOUND_TERRITORIES);
            }

            if (data.getParcelableExtra(ACCOUNT_RESULT) != null) {
                currentAccount = data.getParcelableExtra(ACCOUNT_RESULT);
            }
        }

        Intent intent = new Intent(ACCOUNT_RESULT);
        intent.putExtra(ACCOUNT_RESULT, currentAccount);
        sendBroadcast(intent);

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
                Fragment fragment = new Frag_QuoteBase();
                Bundle args = new Bundle();
                args.putInt(FRAG_NUMBER, position + 1);
                fragment.setArguments(args);
                return fragment;
            }

            if (position == 1) {
                Fragment fragment = new Frag_QuoteProducts();
                Bundle args = new Bundle();
                args.putInt(FRAG_NUMBER, position + 1);
                fragment.setArguments(args);
                return fragment;
            }

            if (position == 2) {
                Fragment fragment = new Frag_FinancialSolutions();
                Bundle args = new Bundle();
                args.putInt(FRAG_NUMBER, position + 1);
                fragment.setArguments(args);
                return fragment;
            }

            return null;
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {

            switch (position) {
                case 0:
                    return "Quote Basics";
                case 1:
                    return "Quote Products";
                case 2:
                    return "Financial Solutions";
            }
            return null;
        }
    }

    //region ********************************** TRIP FRAGS *****************************************

    public static class Frag_QuoteBase extends Fragment {
        private View rootView;
        public static final String ARG_SECTION_NUMBER = "section_number";
        MyHyperlinkTextview txtAccount;
        MyHyperlinkTextview txtTerritory;
        MyHyperlinkTextview txtChangeAddress;
        BroadcastReceiver accountReceiver;
        CrmEntities.Accounts.Account currentAccount;
        // RadioGroup radioGroupAddresses;
        EditText editTextSelectedAddress;
        BasicObjects addressObjects;

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                                 @Nullable Bundle savedInstanceState) {
            rootView = inflater.inflate(R.layout.frag_quote_basics, container, false);

            txtAccount = rootView.findViewById(R.id.txtAccount);
            txtAccount.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    FullscreenAccountTerritoryPicker.showPicker(getActivity(), cachedTerritories);
                }
            });
            txtChangeAddress = rootView.findViewById(R.id.txtChangeAddress);
            txtChangeAddress.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    FullscreenActivityBasicObjectPicker.showPicker(getActivity(), addressObjects, 555);
                }
            });

            accountReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (intent != null && intent.getParcelableExtra(ACCOUNT_RESULT) != null) {
                        currentAccount = intent.getParcelableExtra(FullscreenAccountTerritoryPicker.ACCOUNT_RESULT);
                        populateUsingSelectedAccount();
                    }
                }
            };

            // Register here?  This reg/unreg dance will not work in pause/unpause, stop/start.
            // Using onDestroy() seems to work but makes me nervous that it won't be called in fringe cases.  Leaving it for now...
            getActivity().registerReceiver(accountReceiver, new IntentFilter(ACCOUNT_RESULT));

            // radioGroupAddresses = rootView.findViewById(R.id.radioGroupAddressNames);
            editTextSelectedAddress = rootView.findViewById(R.id.editTextSelectedAddress);

            return rootView;
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            getActivity().unregisterReceiver(accountReceiver);
        }

        /**
         * Using the user's chosen account, populate the activity.
         */
        void populateUsingSelectedAccount() {
            // Leave if there is no account to reference.
            if (currentAccount == null) {
                return;
            }

            // Set the account name
            txtAccount.setText(currentAccount.accountName);

            // Retrieve the account's addresses
            getAddresses();

        }

        void getAddresses() {
            String query = CrmQueries.Addresses.getAllAddresses(currentAccount.entityid);
            Crm crm = new Crm();
            Requests.Request request = new Requests.Request(Requests.Request.Function.GET);
            request.arguments.add(new Requests.Argument("query", query));
            crm.makeCrmRequest(getContext(), request, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    Log.i(TAG, "onSuccess | Retrieved account addresses");

                    // Cache the returned address(s)
                    String json = new String(responseBody);
                    currentAccountAddresses = new CrmEntities.CustomerAddresses(json);
                    currentAccountAddresses.baseAccount = currentAccount;

                    if (currentAccountAddresses != null && currentAccountAddresses.list.size() > 0) {
                        addressObjects = new BasicObjects();
                        for (CrmEntities.CustomerAddresses.CustomerAddress a : currentAccountAddresses.list) {
                            addressObjects.list.add(new BasicObjects.BasicObject(a.name, a.buildCompositeAddress(), a));
                        }
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    Log.w(TAG, "onFailure: | Failed to retrieve addresses! " + error.getLocalizedMessage());
                }
            });
        }

    }

    public static class Frag_QuoteProducts extends Fragment {
        private View rootView;
        public static final String ARG_SECTION_NUMBER = "section_number";

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                                 @Nullable Bundle savedInstanceState) {
            rootView = inflater.inflate(R.layout.man_trip_title, container, false);
            // title = rootView.findViewById(R.id.textView_title_value);

            return rootView;
        }

    }

    public static class Frag_FinancialSolutions extends Fragment {
        private View rootView;
        public static final String ARG_SECTION_NUMBER = "section_number";

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                                 @Nullable Bundle savedInstanceState) {
            rootView = inflater.inflate(R.layout.man_trip_title, container, false);
            // title = rootView.findViewById(R.id.textView_title_value);

            return rootView;
        }

    }
}