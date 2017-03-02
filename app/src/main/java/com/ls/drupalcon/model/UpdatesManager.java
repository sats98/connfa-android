package com.ls.drupalcon.model;

import com.ls.drupal.DrupalClient;
import com.ls.drupalcon.R;
import com.ls.drupalcon.app.App;
import com.ls.drupalcon.model.data.UpdateDate;
import com.ls.drupalcon.model.database.ILAPIDBFacade;
import com.ls.drupalcon.model.managers.SynchronousItemManager;
import com.ls.http.base.BaseRequest;
import com.ls.http.base.RequestConfig;
import com.ls.http.base.ResponseData;
import com.ls.ui.drawer.DrawerMenu;
import com.ls.ui.drawer.EventMode;
import com.ls.util.ObserverHolder;

import org.jetbrains.annotations.NotNull;

import android.os.AsyncTask;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class UpdatesManager {
    List<UpdateRequestID> updateList = new ArrayList<>();

//    public static final int SETTINGS_REQUEST_ID = 0;
//    public static final int TYPES_REQUEST_ID = 1;
//    public static final int LEVELS_REQUEST_ID = 2;
//    public static final int TRACKS_REQUEST_ID = 3;
//    public static final int SPEAKERS_REQUEST_ID = 4;
//    public static final int LOCATIONS_REQUEST_ID = 5;
//    public static final int FLOOR_PLANS_REQUEST_ID = 6;
//    public static final int PROGRAMS_REQUEST_ID = 7;
//    public static final int BOFS_REQUEST_ID = 8;
//    public static final int SOCIALS_REQUEST_ID = 9;
//    public static final int POIS_REQUEST_ID = 10;
//    public static final int INFO_REQUEST_ID = 11;

    private DrupalClient mClient;
    private ObserverHolder<DataUpdatedListener> mUpdateListeners;

    public static final String IF_MODIFIED_SINCE_HEADER = "If-Modified-Since";
    public static final String LAST_MODIFIED_HEADER = "Last-Modified";

    public static EventMode convertEventIdToEventModePos(int eventModePos) {
        return DrawerMenu.getNavigationDrawerItems().get(eventModePos).getEventMode();
    }

    public UpdatesManager(@NotNull DrupalClient client) {
        mUpdateListeners = new ObserverHolder<>();
        mClient = client;
    }

    public void startLoading(@NotNull final UpdateCallback callback) {
        new AsyncTask<Void, Void, List<Integer>>() {

            @Override
            protected List<Integer> doInBackground(Void... params) {
                return doPerformLoading();
            }

            @Override
            protected void onPostExecute(final List<Integer> result) {
                if (result != null) {
                    mUpdateListeners.notifyAllObservers(new ObserverHolder.ObserverNotifier<DataUpdatedListener>() {
                        @Override
                        public void onNotify(DataUpdatedListener observer) {
                            observer.onDataUpdated(result);
                        }
                    });
                }

                if (result != null) {
                    if (callback != null) {
                        callback.onDownloadSuccess();
                    }
                    mUpdateListeners.notifyAllObservers(new ObserverHolder.ObserverNotifier<DataUpdatedListener>() {
                        @Override
                        public void onNotify(DataUpdatedListener observer) {
                            observer.onDataUpdated(result);
                        }
                    });
                } else {
                    if (callback != null) {
                        callback.onDownloadError();
                    }
                }
            }
        }.execute();
    }

    public void registerUpdateListener(DataUpdatedListener listener) {
        this.mUpdateListeners.registerObserver(listener);
    }

    public void unregisterUpdateListener(DataUpdatedListener listener) {
        this.mUpdateListeners.unregisterObserver(listener);
    }

    /**
     * @return return updated request id's list in case of success or null in case of failure
     */

    private List<Integer> doPerformLoading() {
        RequestConfig config = new RequestConfig();
        config.setResponseFormat(BaseRequest.ResponseFormat.JSON);
        config.setRequestFormat(BaseRequest.RequestFormat.JSON);
        config.setResponseClassSpecifier(UpdateDate.class);
        String baseURL = App.getContext().getString(R.string.api_value_base_url);
        BaseRequest checkForUpdatesRequest = new BaseRequest(BaseRequest.RequestMethod.GET, baseURL + "checkUpdates", config);
        String lastDate = PreferencesManager.getInstance().getLastUpdateDate();
        checkForUpdatesRequest.addRequestHeader(IF_MODIFIED_SINCE_HEADER, lastDate);
        ResponseData updatesData = mClient.performRequest(checkForUpdatesRequest, true);

        int statusCode = updatesData.getStatusCode();
        if (statusCode > 0 && statusCode < 400) {
            UpdateDate updateDate = (UpdateDate) updatesData.getData();
            if (updateDate == null) {
                return new LinkedList<>();
            }
            updateDate.setTime(updatesData.getHeaders().get(LAST_MODIFIED_HEADER));
            return loadData(updateDate);

        } else {
            return null;
        }
    }

    private List<Integer> loadData(UpdateDate updateDate) {

        List<Integer> updateIds = updateDate.getIdsForUpdate();
        if (updateIds == null || updateIds.isEmpty()) {
            return new LinkedList<>();
        }
        for (int i : updateIds) {
            switch (i) {
                case 0:
                    updateList.add(UpdateRequestID.SETTINGS);
                    break;
                case 1:
                    updateList.add(UpdateRequestID.TYPES);
                    break;
                case 2:
                    updateList.add(UpdateRequestID.LEVELS);
                    break;
                case 3:
                    updateList.add(UpdateRequestID.TRACKS);
                    break;
                case 4:
                    updateList.add(UpdateRequestID.SPEAKERS);
                    break;
                case 5:
                    updateList.add(UpdateRequestID.LOCATIONS);
                    break;
                case 6:
                    updateList.add(UpdateRequestID.FLOOR_PLANS);
                    break;
                case 7:
                    updateList.add(UpdateRequestID.PROGRAMS);
                    break;
                case 8:
                    updateList.add(UpdateRequestID.BOFS);
                    break;
                case 9:
                    updateList.add(UpdateRequestID.SOCIALS);
                    break;
                case 10:
                    updateList.add(UpdateRequestID.POIS);
                    break;
                case 11:
                    updateList.add(UpdateRequestID.INFO);
                    break;

            }


        }
//        updateRequests.setRequestId();

        ILAPIDBFacade facade = Model.instance().getFacade();
        try {
            facade.open();
            facade.beginTransactions();
            boolean success = true;
//            for (Integer i : updateIds) {
//                success = sendRequestById(i);
//                if (!success) {
//                    break;
//                }
//            }

            for (UpdateRequestID update : updateList) {
                success = sendRequestById(update);
                if (!success) {
                    break;
                }
            }
            if (success) {
                facade.setTransactionSuccesfull();
                if (!TextUtils.isEmpty(updateDate.getTime())) {
                    PreferencesManager.getInstance().saveLastUpdateDate(updateDate.getTime());
                }
            }
            return success ? updateIds : null;
        } finally {
            facade.endTransactions();
            facade.close();
        }


    }

    private boolean sendRequestById(UpdateRequestID update) {

        SynchronousItemManager manager;
        switch (update) {
            case SETTINGS:
                manager = Model.instance().getSettingsManager();
                break;

            case TYPES:
                manager = Model.instance().getTypesManager();
                break;

            case LEVELS:
                manager = Model.instance().getLevelsManager();
                break;

            case TRACKS:
                manager = Model.instance().getTracksManager();
                break;

            case SPEAKERS:
                manager = Model.instance().getSpeakerManager();
                break;

            case LOCATIONS:
                manager = Model.instance().getLocationManager();
                break;

            case PROGRAMS:
                manager = Model.instance().getProgramManager();
                break;

            case BOFS:
                manager = Model.instance().getBofsManager();
                break;

            case SOCIALS:
                manager = Model.instance().getSocialManager();
                break;

            case POIS:
                manager = Model.instance().getPoisManager();
                break;

            case INFO:
                manager = Model.instance().getInfoManager();
                break;

            case FLOOR_PLANS:
                manager = Model.instance().getFloorPlansManager();
                break;

            default:
                return true;
        }

        if (manager != null) {
            return manager.fetchData();
        }

        return false;
    }

    public interface DataUpdatedListener {

        void onDataUpdated(List<Integer> requestIds);
    }

    public void checkForDatabaseUpdate() {
        ILAPIDBFacade facade = Model.instance().getFacade();
        facade.open();
        facade.close();
    }
}
