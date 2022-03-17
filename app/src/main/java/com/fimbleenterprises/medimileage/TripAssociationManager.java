package com.fimbleenterprises.medimileage;

import android.util.Log;

import com.fimbleenterprises.medimileage.objects_and_containers.CrmEntities;
import com.fimbleenterprises.medimileage.objects_and_containers.Opportunities;
import com.fimbleenterprises.medimileage.objects_and_containers.Opportunities.Opportunity;
import com.fimbleenterprises.medimileage.objects_and_containers.CrmEntities.TripAssociations;
import com.fimbleenterprises.medimileage.objects_and_containers.FullTrip;
import com.fimbleenterprises.medimileage.objects_and_containers.MediUser;
import com.fimbleenterprises.medimileage.objects_and_containers.Requests;
import com.fimbleenterprises.medimileage.objects_and_containers.TripEntry;
import com.loopj.android.http.AsyncHttpResponseHandler;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import cz.msebera.android.httpclient.Header;

/**
 * A class containing static methods used to manage trip associations both locally and server-side.
 */
public class TripAssociationManager {

    public static final int DEPARTURE = 745820000;
    public static final int ARRIVAL = 745820001;

    private static final String TAG = "AssociatedTripManager";

    /**
     * Creates TripAssociation objects using cached account addresses.
     * This method does not query the CRM server and will only be useful if there are saved account
     * addresses. Also, as this queries only local data it is fast and can be called as often as
     * needed without worry.  <b>Will return null if there are no cached account addresses.</b>
     *
     * @param trip The FullTrip object to use when determining associations.
     * @return A new TripAssociations object containing all associations or null if no
     * associations were found.
     */
    public static TripAssociations getNearbyAccountsAndOpportunities(@NonNull FullTrip trip) {

        // Get shared preferences and identify the departure and destination locations for the specified trip
        MyPreferencesHelper options = new MyPreferencesHelper(MyApp.getAppContext());
        TripEntry startEntry = trip.tripEntries.get(0);
        TripEntry endEntry = trip.tripEntries.get(trip.tripEntries.size() - 1);

        // Retrieve the cached account addresses
        CrmEntities.CrmAddresses accountAddresses = options.getAllSavedCrmAddresses();
        Opportunities savedOpportunities = options.getSavedOpportunities();

        // Verify that there are indeed saved addresses - we can not proceed without them.
        if (accountAddresses == null || accountAddresses.list.size() < 1) {
            Log.w(TAG, "getNearbyAssociationsFromCache: Could not do associations - no cached " +
                    "addresses were found.");
            return null;
        }

        // This is the distance threshold that will be used to determine what is considered, "nearby"
        double thresh = options.getDistanceThreshold();

        // See if anything is close to the start and end of the trip
        Log.i(TAG, "detectAccountsAtStartOrEnd: Distance threshold: " + thresh + " meters");

        // Create a new arraylist of associated accounts that are within the start and end distance thresholds
        final TripAssociations pendingAssociations = new TripAssociations();

        // populate the array
        for (CrmEntities.CrmAddresses.CrmAddress address : accountAddresses.list) {
            double distFromStart = startEntry.distanceTo(address.getLatLng());
            double distFromEnd = endEntry.distanceTo(address.getLatLng());

            float milesFromStart = Helpers.Geo.convertMetersToMiles(distFromStart, 4);
            float milesFromEnd = Helpers.Geo.convertMetersToMiles(distFromEnd, 4);

            Log.i(TAG, "manageTripAssociations milesFromStart: " + milesFromStart + " miles");
            Log.i(TAG, "manageTripAssociations milesFromEnd: " + milesFromEnd + " miles");

            // Evaluate the distances from the start and end of the trip and create a TripAssociation
            // object if the locations are within the threshold
            if (distFromStart <= thresh) { // Trip departure location
                // First see if there are opportunities saved for this address' accountid and create
                // pending associations accordingly
                if (savedOpportunities.accountHasOpportunity(address.accountid)) {
                    for (Opportunity opp : savedOpportunities.getOpportunities(address.accountid)) {
                        TripAssociations.TripAssociation association2 =
                                new TripAssociations.TripAssociation(trip.getDateTime());
                        association2.associated_account_id = address.accountid;
                        association2.associated_trip_id = trip.tripGuid;
                        association2.associated_opportunity_name = opp.name;
                        association2.associated_opportunity_id = opp.entityid;
                        association2.tripDispositionValue = DEPARTURE;
                        pendingAssociations.addAssociation(association2);
                    }
                } else { // No opportunities found
                    // Since there are no opportunities found just create an association without referenced opportunities
                    TripAssociations.TripAssociation association =
                            new TripAssociations.TripAssociation(trip.getDateTime());
                    association.associated_account_id = address.accountid;
                    association.associated_trip_id = trip.tripGuid;
                    association.tripDispositionValue = DEPARTURE;
                    pendingAssociations.addAssociation(association);
                }

                Log.i(TAG, "createAssociations Added an account close to the trip's start");
            } else if(distFromEnd <= thresh) { // Trip destination location
                // First see if there are opportunities saved for this address' accountid and create
                // pending associations accordingly
                if (savedOpportunities.accountHasOpportunity(address.accountid)) {
                    for (Opportunity opp : savedOpportunities.getOpportunities(address.accountid)) {
                        TripAssociations.TripAssociation association2 =
                                new TripAssociations.TripAssociation(trip.getDateTime());
                        association2.associated_account_id = address.accountid;
                        association2.associated_trip_id = trip.tripGuid;
                        association2.associated_opportunity_name = opp.name;
                        association2.associated_opportunity_id = opp.entityid;
                        association2.tripDispositionValue = ARRIVAL;
                        pendingAssociations.addAssociation(association2);
                    }
                } else { // No opportunities found
                    // Since there are no opportunities found just create an association without referenced opportunities
                    TripAssociations.TripAssociation association =
                            new TripAssociations.TripAssociation(trip.getDateTime());
                    association.associated_account_id = address.accountid;
                    association.associated_trip_id = trip.tripGuid;
                    association.tripDispositionValue = ARRIVAL;
                    pendingAssociations.addAssociation(association);
                }
            }
        } // for each saved address on file

        // If even one association was created, return a TripAssociations object containing it/them
        if (pendingAssociations != null && pendingAssociations.list.size() > 0) {
            return pendingAssociations;
        } else {
            return null;
        }
    }

    /**
     * This function will evaluate accounts and opportunities near the start and end locations
     * of a trip.  It will then add or update trip associations on the server.  Existing associations
     * are deleted and recreated so there is no need to check for pre-existing associations.
     */
    public static void manageTripAssociations(FullTrip fullTrip, final MyInterfaces.CreateManyListener listener) {

        try {
            // Use the cached addresses and opportunities to try to build trip associations based on
            // the departure and destination locations of the specified trip.
            final TripAssociations pendingAssociations = getNearbyAccountsAndOpportunities(fullTrip);

            // If we have any close accounts then delete and recreate them in CRM.
            if (pendingAssociations.list.size() > 0) {
                Log.i(TAG, "createAssociations Found: " + pendingAssociations.list.size() + " nearby accounts.");

                // Retrieve any existing server-side associations so they can be deleted if necessary
                retrieveAssociations(fullTrip.getTripGuid(), new MyInterfaces.TripAssociationsListener() {
                    @Override
                    public void onSuccess(TripAssociations associations) {
                        if (associations != null && associations.list.size() > 0) {
                            // First remove any existing TripAssociation entities on the server
                            Log.i(TAG, "createAssociations Deleting any existing server-side associations...");
                            deleteCrmAssociations(associations, new MyInterfaces.DeleteManyListener() {
                                @Override
                                public void onResult(CrmEntities.DeleteManyResponses responses) {
                                    if (!responses.wasFaulted) {
                                        Log.i(TAG, "onResult deleted " + responses.responses.size() + " associations");
                                        // Now add new associations on the server
                                        Log.i(TAG, "onResult Creating trip associations on the server...");
                                        uploadCrmAssociations(pendingAssociations, new MyInterfaces.CreateManyListener() {
                                            @Override
                                            public void onResult(CrmEntities.CreateManyResponses responses) {
                                                Log.i(TAG, "onResult Created: " + responses.responses.size() + " associations!");
                                                listener.onResult(responses);
                                            }

                                            @Override
                                            public void onError(String msg) {
                                                Log.w(TAG, "onError: Create associations failed with: " + msg);
                                                listener.onError(msg);
                                            }
                                        });
                                    } else {
                                        Log.w(TAG, "onResult: Error: Delete response was faulted!");
                                        listener.onError("Failed to delete exisitng associations prior to (re)creating new associations");
                                    }
                                }

                                @Override
                                public void onError(String msg) {
                                    listener.onError(msg);
                                    Log.w(TAG, "onError: Eelete associations failed: " + msg);
                                }
                            });
                        } else {
                            // Add new associations on the server
                            Log.i(TAG, "Creating trip associations on the server...");
                            uploadCrmAssociations(pendingAssociations, new MyInterfaces.CreateManyListener() {
                                @Override
                                public void onResult(CrmEntities.CreateManyResponses responses) {
                                    Log.i(TAG, "onResult Created: " + responses.responses.size() + " associations!");
                                    listener.onResult(responses);
                                }

                                @Override
                                public void onError(String msg) {
                                    Log.w(TAG, "onError: Create associations failed with: " + msg);
                                    listener.onError(msg);
                                }
                            });
                        }
                    }
                    @Override
                    public void onFailure(String msg) {
                        listener.onError(msg);
                    }
                });

            } else {
                Log.i(TAG, "createAssociations No nearby accounts were found for this trip.");
                listener.onError("No associations were found.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            listener.onError(e.getLocalizedMessage());
        }
    }

    /**
     * Retrieves trip associations that exist on the server.
     * @param tripid The trip guid to use when querying associations on the server.
     * @param listener A callback that will return a TripAssociations object (which will be null if
     *                 none were found but no errors occured) or an error message if an error was thrown.
     */
    public static void retrieveAssociations(String tripid, final MyInterfaces.TripAssociationsListener listener) {
        String query = CrmQueries.TripAssociation.getAssociationsByTripid(tripid);
        Requests.Request request = new Requests.Request(Requests.Request.Function.GET);
        ArrayList<Requests.Argument> args = new ArrayList<>();
        Requests.Argument argument1 = new Requests.Argument("query", query);
        request.arguments.add(argument1);

        Crm crm = new Crm();
        crm.makeCrmRequest(MyApp.getAppContext(), request, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                TripAssociations associations = new TripAssociations(new String(responseBody));
                Log.i(TAG, "onSuccess Found: " + associations.list.size() + " associations.");
                listener.onSuccess(associations);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.w(TAG, "onFailure: " + error.getLocalizedMessage());
                listener.onFailure(error.getLocalizedMessage());
            }
        });
    }

    /**
     * Deletes any trip associations on the server.
     * @param associations A TripAssociations object that contains TripAssociation objects which <i>must
     *                     have</i> msus_mileageassociationid values.  That is the only value that is needed
     *                     for each TripAssociation object within - no other attributes are evaluated.
     * @param listener A simple success/failure type of listener that onSuccess will return a DeleteManyResponses
     *                 object containing the associated DeleteManyResponse objects (one for each
     *                 delete attempt) or a string error message on error.
     */
    private static void deleteCrmAssociations(TripAssociations associations, final MyInterfaces.DeleteManyListener listener) {

        String[] guids = new String[associations.list.size()];
        for (int i = 0; i < associations.list.size(); i++) {
            guids[i] = associations.list.get(i).entityid;
        }

        Requests.Request request = new Requests.Request(Requests.Request.Function.DELETE_MANY);
        Requests.Argument argument1 = new Requests.Argument("entityname", "msus_mileageassociation");
        Requests.Argument argument2 = new Requests.Argument("guids", guids);
        Requests.Argument argument3 = new Requests.Argument("asuserid", MediUser.getMe().systemuserid);
        request.arguments.add(argument1);
        request.arguments.add(argument2);
        request.arguments.add(argument3);

        Crm crm = new Crm();
        crm.makeCrmRequest(MyApp.getAppContext(), request, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                CrmEntities.DeleteManyResponses responses = new CrmEntities.DeleteManyResponses(new String(responseBody));
                listener.onResult(responses);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                listener.onError(error.getLocalizedMessage());
            }
        });

    }

    /**
     * Uploads constructed TripAssociation objects to the server.  This method will do zero validation
     * as to whether these associations already exist.  If they do exist they will be (effectively) duplicated.
     * @param associations A TripAssociations object containing TripAssociation objects.
     * @param listener A basic success/failure listener that will return a populated CreateManyResponses
     *                 object that contains a CreateManyResponse for each trip association that was created.
     */
    private static void uploadCrmAssociations(TripAssociations associations, final MyInterfaces.CreateManyListener listener) {
        if (associations.list.size() == 0) {
            listener.onError("No associations to upload!");
        }

        Requests.Request request = new Requests.Request(Requests.Request.Function.CREATE_MANY);

        ArrayList<Requests.Argument> args = new ArrayList<>();
        Requests.Argument argument1 = new Requests.Argument("entityName", "msus_mileageassociation");
        Requests.Argument argument2 = new Requests.Argument("asUserid", MediUser.getMe().systemuserid);
        Requests.Argument argument3 = new Requests.Argument("containers", associations.toContainers().toJson());;
        args.add(argument1);
        args.add(argument2);
        args.add(argument3);
        request.arguments = args;

        Crm crm = new Crm();
        crm.makeCrmRequest(MyApp.getAppContext(), request, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                CrmEntities.CreateManyResponses responses = new CrmEntities.CreateManyResponses(new String(responseBody));
                listener.onResult(responses);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                listener.onError(error.getLocalizedMessage());
            }
        });

    }

}
