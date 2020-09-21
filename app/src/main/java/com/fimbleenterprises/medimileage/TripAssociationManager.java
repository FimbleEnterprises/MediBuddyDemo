package com.fimbleenterprises.medimileage;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.fimbleenterprises.medimileage.TripAssociationManager.TripAssociationExeption.TripHasNoIdException;
import com.fimbleenterprises.medimileage.TripAssociationManager.TripAssociationExeption.TripMissingRequiredEntriesException;
import com.loopj.android.http.AsyncHttpResponseHandler;

import java.util.ArrayList;

import androidx.annotation.Nullable;
import cz.msebera.android.httpclient.Header;

class TripAssociationManager {
    private static final String TAG = "AssociatedTripManager";
    FullTrip fullTrip;
    Context context;
    MySettingsHelper options;
    
    public TripAssociationManager(Context context, FullTrip fullTrip) throws TripAssociationExeption {

        // Must have a FullTrip guid (meaning the trip has been submitted) to proceed.
        if (fullTrip == null || fullTrip.getTripGuid() == null) {
            throw new TripHasNoIdException("Supplied trip must have a server-side entity id!");
        }

        if (fullTrip.tripEntries == null || fullTrip.tripEntries.size() < 2) {
            throw new TripMissingRequiredEntriesException("Not enough trip entries in supplied trip." +
                    "  Must have at least two entries (start and end, presumably).");
        }

        this.context = context;
        this.options = new MySettingsHelper(context);
        this.fullTrip = fullTrip;

    }

    /**
     * This function will evaluate accounts and opportunities near the start and end locations
     * of a trip.  It will then add or update trip associations on the server.  Existing associations
     * are deleted and recreated so there is no need to check for pre-existing associations.
     */
    public void manageTripAssociations(final MyInterfaces.CreateManyListener listener) {

        try {
            TripEntry startEntry = fullTrip.tripEntries.get(0);
            TripEntry endEntry = fullTrip.tripEntries.get(fullTrip.tripEntries.size() - 1);

            CrmEntities.CrmAddresses accountAddresses = options.getAllSavedCrmAddresses();
            double thresh = options.getDistanceThreshold();

            // See if anything is close to the start and end of the trip
            Log.i(TAG, "detectAccountsAtStartOrEnd: Distance threshold: " + thresh + " meters");

            // Create a new arraylist of associated accounts that are within the start and end distance thresholds
            final CrmEntities.TripAssociations pendingAssociations = new CrmEntities.TripAssociations();

            // populate the array
            for (CrmEntities.CrmAddresses.CrmAddress address : accountAddresses.list) {
                double distFromStart = startEntry.distanceTo(address.getLatLng());
                double distFromEnd = endEntry.distanceTo(address.getLatLng());

                float milesFromStart = Helpers.Geo.convertMetersToMiles(distFromStart, 4);
                float milesFromEnd = Helpers.Geo.convertMetersToMiles(distFromEnd, 4);

                Log.i(TAG, "manageTripAssociations milesFromStart: " + milesFromStart + " miles");
                Log.i(TAG, "manageTripAssociations milesFromEnd: " + milesFromEnd + " miles");

                if (distFromStart <= thresh) {
                    CrmEntities.TripAssociations.TripAssociation association =
                            new CrmEntities.TripAssociations.TripAssociation(fullTrip.getDateTime());
                    association.associated_account_id = address.accountid;
                    association.associated_trip_id = fullTrip.tripGuid;
                    pendingAssociations.addAssociation(association);
                    association.tripDisposition = CrmEntities.TripAssociations.TripAssociation
                            .TripDisposition.START;
                    pendingAssociations.addAssociation(association);
                    Log.i(TAG, "createAssociations Added an account close to the trip's start");
                } else if(distFromEnd <= thresh) {
                    CrmEntities.TripAssociations.TripAssociation association =
                            new CrmEntities.TripAssociations.TripAssociation(fullTrip.getDateTime());
                    association.associated_account_id = address.accountid;
                    association.associated_trip_id = fullTrip.tripGuid;
                    pendingAssociations.addAssociation(association);
                    association.tripDisposition = CrmEntities.TripAssociations.TripAssociation
                            .TripDisposition.END;
                    pendingAssociations.addAssociation(association);
                    Log.i(TAG, "createAssociations Added an account close to the trip's end");
                }
            } // for each saved address on file

            // If we have any close accounts...
            if (pendingAssociations.list.size() > 0) {
                Log.i(TAG, "createAssociations Found: " + pendingAssociations.list.size() + " nearby accounts.");

                // Retrieve any existing server-side associations so they can be deleted if necessary
                retrieveAssociations(new MyInterfaces.TripAssociationsListener() {
                    @Override
                    public void onSuccess(CrmEntities.TripAssociations associations) {
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

                    }
                });

            } else {
                Log.i(TAG, "createAssociations No nearby accounts were found for this trip.");
            }


        } catch (Exception e) {
            e.printStackTrace();
            listener.onError(e.getLocalizedMessage());
        }
    }

    /**
     * This will simply remove any and all TripAssociation entity entries from the server containing
     * this trip's TripAssociationId.
     * @param listener A simple yes or no result listener.
     */
    public void removeAssociations(final MyInterfaces.DeleteManyListener listener) {
        try {
            // First get the server-side associations
            retrieveAssociations(new MyInterfaces.TripAssociationsListener() {
                @Override
                public void onSuccess(CrmEntities.TripAssociations associations) {
                    // Now delete the from the server.
                    deleteCrmAssociations(associations, new MyInterfaces.DeleteManyListener() {
                        @Override
                        public void onResult(CrmEntities.DeleteManyResponses responses) {
                            listener.onResult(responses);
                        }

                        @Override
                        public void onError(String msg) {
                            listener.onError(msg);
                        }
                    });
                }

                @Override
                public void onFailure(String msg) {

                }
            });

            // First remove any existing TripAssociation entities on the server

        } catch (Exception e) {
            e.printStackTrace();
            listener.onError(e.getLocalizedMessage());
        }
    }

    public void retrieveAssociations(final MyInterfaces.TripAssociationsListener listener) {
        String query = Queries.TripAssociation.getAssociationsByTripid(this.fullTrip.tripGuid);
        Requests.Request request = new Requests.Request(Requests.Request.Function.GET);
        ArrayList<Requests.Argument> args = new ArrayList<>();
        Requests.Argument argument1 = new Requests.Argument("query", query);
        request.arguments.add(argument1);

        Crm crm = new Crm();
        crm.makeCrmRequest(context, request, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                CrmEntities.TripAssociations associations = new CrmEntities.TripAssociations(new String(responseBody));
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

    private void deleteCrmAssociations(CrmEntities.TripAssociations associations, final MyInterfaces.DeleteManyListener listener) {

        String[] guids = new String[associations.list.size()];
        for (int i = 0; i < associations.list.size(); i++) {
            guids[i] = associations.list.get(i).id;
        }

        Requests.Request request = new Requests.Request(Requests.Request.Function.DELETE_MANY);
        Requests.Argument argument1 = new Requests.Argument("entityname", "msus_mileageassociation");
        Requests.Argument argument2 = new Requests.Argument("guids", guids);
        Requests.Argument argument3 = new Requests.Argument("asuserid", MediUser.getMe().systemuserid);
        request.arguments.add(argument1);
        request.arguments.add(argument2);
        request.arguments.add(argument3);

        Crm crm = new Crm();
        crm.makeCrmRequest(context, request, new AsyncHttpResponseHandler() {
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

    private void uploadCrmAssociations(CrmEntities.TripAssociations associations, final MyInterfaces.CreateManyListener listener) {
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
        crm.makeCrmRequest(context, request, new AsyncHttpResponseHandler() {
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

    public static class TripAssociationExeption extends Exception {

        public static class TripHasNoIdException extends TripAssociationExeption {
            public TripHasNoIdException(String msg) {
            }
        }

        public static class TripMissingRequiredEntriesException extends TripAssociationExeption {
            public TripMissingRequiredEntriesException(String msg) {
            }
        }
    }

}
