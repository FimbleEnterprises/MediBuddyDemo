package com.fimbleenterprises.medimileage;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import cz.msebera.android.httpclient.Header;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpResponseHandler;

import java.util.ArrayList;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FullscreenActivityChooseTerritory extends AppCompatActivity {

    private static final String TAG = "FullscreenActivityChooseTerritory";

    Context context;
    RecyclerView listView;
    ArrayList<BasicObject> objects = new ArrayList<>();
    BasicObjectRecyclerAdapter adapter;
    public static final int REQUESTCODE = 011;
    public static final String TERRITORY_RESULT = "TERRITORY_RESULT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.context = this;
        setContentView(R.layout.activity_fullscreen_choose_territory);
        listView = findViewById(R.id.rvTerritories);

        getTerritories();

    }

    void getTerritories() {
        final MyProgressDialog dialog = new MyProgressDialog(this, "Getting territories...");
        dialog.show();

        Crm crm = new Crm();
        ArrayList<Requests.Argument> args = new ArrayList<>();
        Requests.Argument argument = new Requests.Argument("query", Queries.Territories
                .getTerritoriesWithManagersAssigned());
        args.add(argument);
        Requests.Request request = new Requests.Request(Requests.Request.Function.GET, args);

        crm.makeCrmRequest(this, request, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String result = new String(responseBody);
                ArrayList<Territory> territories = Territory.createMany(result);
                objects.clear();
                for (Territory t : territories) {
                    BasicObject basicObject = new BasicObject(t.territoryName, t.repName, t);
                    objects.add(basicObject);
                    dialog.dismiss();
                }
                populateTerritories();
                Log.i(TAG, "onSuccess Response is " + result.length() + " chars long");
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Toast.makeText(context, "Failed!\n" + error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    void populateTerritories() {
        adapter = new BasicObjectRecyclerAdapter(this, objects);
        listView.setAdapter(adapter);
        listView.setLayoutManager(new LinearLayoutManager(context));
        adapter.setClickListener(new BasicObjectRecyclerAdapter.ItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Territory territory = (Territory) objects.get(position).object;
                Intent intent = new Intent(TERRITORY_RESULT);
                intent.putExtra(TERRITORY_RESULT, territory);
                setResult(RESULT_OK, intent);
                finish();
                Log.i(TAG, "onItemClick Position: " + position);
            }
        });
    }

}