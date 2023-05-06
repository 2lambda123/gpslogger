/*
 * Copyright (C) 2016 mendhak
 *
 * This file is part of GPSLogger for Android.
 *
 * GPSLogger for Android is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * GPSLogger for Android is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GPSLogger for Android.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mendhak.gpslogger.ui.fragments.display;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.documentfile.provider.DocumentFile;

import android.os.Environment;
import android.provider.DocumentsContract;
import android.text.Html;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.dd.processbutton.iml.ActionProcessButton;
import com.mendhak.gpslogger.BuildConfig;
import com.mendhak.gpslogger.R;
import com.mendhak.gpslogger.common.EventBusHook;
import com.mendhak.gpslogger.common.PreferenceHelper;
import com.mendhak.gpslogger.common.Session;
import com.mendhak.gpslogger.common.Strings;
import com.mendhak.gpslogger.common.events.ServiceEvents;
import com.mendhak.gpslogger.common.slf4j.Logs;

import org.slf4j.Logger;

import java.io.File;


public class GpsSimpleViewFragment extends GenericViewFragment implements View.OnClickListener {

    Context context;
    private static final Logger LOG = Logs.of(GpsSimpleViewFragment.class);
    private PreferenceHelper preferenceHelper = PreferenceHelper.getInstance();
    private Session session = Session.getInstance();

    private View rootView;
    private ActionProcessButton actionButton;

    public GpsSimpleViewFragment() {

    }

    public static GpsSimpleViewFragment newInstance() {

        GpsSimpleViewFragment fragment = new GpsSimpleViewFragment();
        Bundle bundle = new Bundle(1);
        bundle.putInt("a_number", 1);

        fragment.setArguments(bundle);
        return fragment;


    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_simple_view, container, false);


        if (getActivity() != null) {
            this.context = getActivity().getApplicationContext();

        }

        setImageTooltips();
        showPreferencesSummary();

        actionButton = (ActionProcessButton)rootView.findViewById(R.id.btnActionProcess);
        actionButton.setMode(ActionProcessButton.Mode.ENDLESS);
        actionButton.setBackgroundColor(ContextCompat.getColor(context, (R.color.accentColor)));

        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestToggleLogging();
            }
        });


        if (session.hasValidLocation()) {
            displayLocationInfo(session.getCurrentLocationInfo());
        }

        return rootView;
    }

    private void setActionButtonStart(){
        actionButton.setText(R.string.btn_start_logging);
        actionButton.setBackgroundColor(ContextCompat.getColor(context, R.color.accentColor));
        actionButton.setAlpha(0.8f);
    }

    private void setActionButtonStop(){
        actionButton.setText(R.string.btn_stop_logging);
        actionButton.setBackgroundColor( ContextCompat.getColor(context, R.color.accentColorComplementary));
        actionButton.setAlpha(0.8f);
    }

    private void showPreferencesSummary() {
        showCurrentFileName(Strings.getFormattedFileName());


        ImageView imgGpx = (ImageView) rootView.findViewById(R.id.simpleview_imgGpx);
        ImageView imgKml = (ImageView) rootView.findViewById(R.id.simpleview_imgKml);
        ImageView imgCsv = (ImageView) rootView.findViewById(R.id.simpleview_imgCsv);
        ImageView imgNmea = (ImageView) rootView.findViewById(R.id.simpleview_imgNmea);
        ImageView imgLink = (ImageView) rootView.findViewById(R.id.simpleview_imgLink);
        ImageView imgJson = (ImageView)rootView.findViewById(R.id.simpleview_imgjson);

        if (preferenceHelper.shouldLogToGpx()) {

            imgGpx.setVisibility(View.VISIBLE);
        } else {
            imgGpx.setVisibility(View.GONE);
        }

        if (preferenceHelper.shouldLogToKml()) {

            imgKml.setVisibility(View.VISIBLE);
        } else {
            imgKml.setVisibility(View.GONE);
        }

        if (preferenceHelper.shouldLogToNmea()) {
            imgNmea.setVisibility(View.VISIBLE);
        } else {
            imgNmea.setVisibility(View.GONE);
        }

        if (preferenceHelper.shouldLogToCSV()) {

            imgCsv.setVisibility(View.VISIBLE);
        } else {
            imgCsv.setVisibility(View.GONE);
        }

        if (preferenceHelper.shouldLogToCustomUrl()) {
            imgLink.setVisibility(View.VISIBLE);
        } else {
            imgLink.setVisibility(View.GONE);
        }

        if(preferenceHelper.shouldLogToGeoJSON()){
            imgJson.setVisibility(View.VISIBLE);
        }
        else {
            imgJson.setVisibility(View.GONE);
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static String getRealPathFromURI_API19(Context context, Uri uri) {
        String filePath = "";

        // ExternalStorageProvider
        String docId = DocumentsContract.getDocumentId(uri);
        String[] split = docId.split(":");
        String type = split[0];

        if ("primary".equalsIgnoreCase(type)) {
            return Environment.getExternalStorageDirectory() + "/" + split[1];
        } else {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
                //getExternalMediaDirs() added in API 21
                File[] external = context.getExternalMediaDirs();
                if (external.length > 1) {
                    filePath = external[1].getAbsolutePath();
                    filePath = filePath.substring(0, filePath.indexOf("Android")) + split[1];
                }
            } else {
                filePath = "/storage/" + type + "/" + split[1];
            }
            return filePath;
        }
    }

    // https://stackoverflow.com/questions/36869602/convert-file-path-to-treeuri-storage-access-framework
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static Uri[] getSafUris (Context context, File file) {

        Uri[] uri = new Uri[2];
        String scheme = "content";
        String authority = "com.android.externalstorage.documents";

        // Separate each element of the File path
        // File format: "/storage/XXXX-XXXX/sub-folder1/sub-folder2..../filename"
        // (XXXX-XXXX is external removable number
        String[] ele = file.getPath().split(File.separator);
        //  ele[0] = not used (empty)
        //  ele[1] = not used (storage name)
        //  ele[2] = storage number
        //  ele[3 to (n)] = folders


        // Construct folders strings using SAF format
        StringBuilder folders = new StringBuilder();
        if (ele.length > 4) {
            folders.append(ele[3]);
            for (int i = 4; i < ele.length ; ++i) folders.append("%2F").append(ele[i]);
        }

        String common = ele[2] + "%3A" + folders.toString();

        // Construct TREE Uri
        Uri.Builder builder = new Uri.Builder();
        builder.scheme(scheme);
        builder.authority(authority);
        builder.encodedPath("/tree/" + common);
        uri[0] = builder.build();

        // Construct DOCUMENT Uri
        builder = new Uri.Builder();
        builder.scheme(scheme);
        builder.authority(authority);
        if (ele.length > 4) common = common + "%2F";
        builder.encodedPath("/document/" + common + file.getName());
        uri[1] = builder.build();

        return uri;
    }

    private void showCurrentFileName(String newFileName) {
        TextView txtFilename = (TextView) rootView.findViewById(R.id.simpleview_txtfilepath);

        txtFilename.setVisibility(View.VISIBLE);
        txtFilename.setTextIsSelectable(true);
        txtFilename.setSelectAllOnFocus(true);

        txtFilename.setText(
                Html.fromHtml( preferenceHelper.getGpsLoggerFolder() + "<br /><strong>" + Strings.getFormattedFileName() + "</strong>"));


        txtFilename.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File file = new File( preferenceHelper.getGpsLoggerFolder());

                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Uri[] output = getSafUris(context, file);
                    Uri folderUri = DocumentsContract.buildDocumentUriUsingTree(
                            Uri.parse("content://com.android.externalstorage.documents/tree/primary"),
                            DocumentsContract.getTreeDocumentId(output[0]));
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    //intent.setDataAndType(folderUri, "resource/folder");
                    //intent.setDataAndType(folderUri, "vnd.android.document/directory");
                    intent.setDataAndType(Uri.parse("content://com.android.externalstorage.documents/document/primary%3AAndroid%2Fdata%2Fcom.mendhak.gpslogger%2Ffiles"), "vnd.android.document/directory");
                    intent.addCategory(Intent.CATEGORY_DEFAULT);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, Uri.parse("content://com.android.externalstorage.documents/document/primary%3AAndroid%2Fdata%2Fcom.mendhak.gpslogger%2Ffiles"));
                    // content://com.android.externalstorage.documents/document/primary%3AAndroid%2Fdata%2Fcom.mendhak.gpslogger%2Ffiles
                    // content://com.android.externalstorage.documents/tree/emulated%3A0%2FAndroid%2Fdata%2Fcom.mendhak.gpslogger%2Ffiles
                    // content://com.android.externalstorage.documents/document/emulated%3A0%2FAndroid%2Fdata%2Fcom.mendhak.gpslogger%2Ffiles
                    // content://com.mendhak.gpslogger.fileprovider/ext/20230331.gpx
                    startActivity(intent);
                }

//                Intent intent = new Intent(Intent.ACTION_VIEW);
//                Uri uri = FileProvider.getUriForFile(context, "com.mendhak.gpslogger.fileprovider", file); // get the content URI to your app's "files" folder
//                intent.setDataAndType(uri, "*/*");
//                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);
//                startActivity(Intent.createChooser(intent, "Open folder"));

            }
        });
    }

    private enum IconColorIndicator {
        Good,
        Warning,
        Bad,
        Inactive
    }

    private void clearColor(ImageView imgView){
        setColor(imgView, IconColorIndicator.Inactive);
    }

    private void setColor(ImageView imgView, IconColorIndicator colorIndicator){
        imgView.clearColorFilter();

        if(colorIndicator == IconColorIndicator.Inactive){
            return;
        }

        int color = -1;
        switch(colorIndicator){
            case Bad:
                color = Color.parseColor("#FFEEEE");
                break;
            case Good:
                color = ContextCompat.getColor(context, R.color.accentColor);
                break;
            case Warning:
                color = Color.parseColor("#D4FFA300");
                break;
        }

        imgView.setColorFilter(color);

    }

    private void setImageTooltips() {
        ImageView imgSatellites = (ImageView) rootView.findViewById(R.id.simpleview_imgSatelliteCount);
        imgSatellites.setOnClickListener(this);

        ImageView imgAccuracy = (ImageView) rootView.findViewById(R.id.simpleview_imgAccuracy);
        imgAccuracy.setOnClickListener(this);

        ImageView imgElevation = (ImageView) rootView.findViewById(R.id.simpleview_imgAltitude);
        imgElevation.setOnClickListener(this);

        ImageView imgBearing = (ImageView) rootView.findViewById(R.id.simpleview_imgDirection);
        imgBearing.setOnClickListener(this);

        ImageView imgDuration = (ImageView) rootView.findViewById(R.id.simpleview_imgDuration);
        imgDuration.setOnClickListener(this);

        ImageView imgSpeed = (ImageView) rootView.findViewById(R.id.simpleview_imgSpeed);
        imgSpeed.setOnClickListener(this);

        ImageView imgDistance = (ImageView) rootView.findViewById(R.id.simpleview_distance);
        imgDistance.setOnClickListener(this);

        ImageView imgPoints = (ImageView) rootView.findViewById(R.id.simpleview_points);
        imgPoints.setOnClickListener(this);

        ImageView imgLink = (ImageView) rootView.findViewById(R.id.simpleview_imgLink);
        imgLink.setOnClickListener(this);

    }

    @Override
    public void onStart() {

        setActionButtonStop();
        super.onStart();
    }

    @Override
    public void onResume() {
        showPreferencesSummary();

        if(session.isStarted()){
            setActionButtonStop();
        }
        else {
            setActionButtonStart();
        }
        super.onResume();
    }

    @Override
    public void onPause() {

        super.onPause();
    }


    @EventBusHook
    public void onEventMainThread(ServiceEvents.LocationUpdate locationUpdate){
        displayLocationInfo(locationUpdate.location);
    }

    @EventBusHook
    public void onEventMainThread(ServiceEvents.SatellitesVisible satellitesVisible){
        setSatelliteCount(satellitesVisible.satelliteCount);
    }

    @EventBusHook
    public void onEventMainThread(ServiceEvents.WaitingForLocation waitingForLocation){
        onWaitingForLocation(waitingForLocation.waiting);
    }

    @EventBusHook
    public void onEventMainThread(ServiceEvents.LoggingStatus loggingStatus){

        if(loggingStatus.loggingStarted){
            showPreferencesSummary();
            clearLocationDisplay();
            setActionButtonStop();
        }
        else {
            setSatelliteCount(-1);
            setActionButtonStart();
        }
    }

    @EventBusHook
    public void onEventMainThread(ServiceEvents.FileNamed fileNamed){
        showCurrentFileName(fileNamed.newFileName);
    }

    public void displayLocationInfo(Location locationInfo){
        showPreferencesSummary();

        EditText txtLatitude = (EditText) rootView.findViewById(R.id.simple_lat_text);
        txtLatitude.setText(Strings.getFormattedLatitude(locationInfo.getLatitude()));

        EditText txtLongitude = (EditText) rootView.findViewById(R.id.simple_lon_text);
        txtLongitude.setText(Strings.getFormattedLongitude(locationInfo.getLongitude()));

        ImageView imgAccuracy = (ImageView) rootView.findViewById(R.id.simpleview_imgAccuracy);
        clearColor(imgAccuracy);

        if (locationInfo.hasAccuracy()) {

            TextView txtAccuracy = (TextView) rootView.findViewById(R.id.simpleview_txtAccuracy);
            float accuracy = locationInfo.getAccuracy();
            txtAccuracy.setText(Strings.getDistanceDisplay(getActivity(), accuracy, preferenceHelper.shouldDisplayImperialUnits(), true));

            if (accuracy > 500) {
                setColor(imgAccuracy, IconColorIndicator.Warning);
            }

            if (accuracy > 900) {
                setColor(imgAccuracy, IconColorIndicator.Bad);
            } else {
                setColor(imgAccuracy, IconColorIndicator.Good);
            }
        }

        ImageView imgAltitude = (ImageView)rootView.findViewById(R.id.simpleview_imgAltitude);
        clearColor(imgAltitude);

        if (locationInfo.hasAltitude()) {
            setColor(imgAltitude, IconColorIndicator.Good);
            TextView txtAltitude = (TextView) rootView.findViewById(R.id.simpleview_txtAltitude);

            txtAltitude.setText(Strings.getDistanceDisplay(getActivity(), locationInfo.getAltitude(), preferenceHelper.shouldDisplayImperialUnits(), false));
        }

        ImageView imgSpeed = (ImageView)rootView.findViewById(R.id.simpleview_imgSpeed);
        clearColor(imgSpeed);

        if (locationInfo.hasSpeed()) {

            setColor(imgSpeed, IconColorIndicator.Good);

            TextView txtSpeed = (TextView) rootView.findViewById(R.id.simpleview_txtSpeed);
            txtSpeed.setText(Strings.getSpeedDisplay(getActivity(), locationInfo.getSpeed(), preferenceHelper.shouldDisplayImperialUnits()));
        }

        ImageView imgDirection = (ImageView) rootView.findViewById(R.id.simpleview_imgDirection);
        clearColor(imgDirection);

        if (locationInfo.hasBearing()) {
            setColor(imgDirection, IconColorIndicator.Good);
            imgDirection.setRotation(locationInfo.getBearing());

            TextView txtDirection = (TextView) rootView.findViewById(R.id.simpleview_txtDirection);
            txtDirection.setText(String.valueOf(Math.round(locationInfo.getBearing())) + getString(R.string.degree_symbol));
        }

        TextView txtDuration = (TextView) rootView.findViewById(R.id.simpleview_txtDuration);

        long startTime = session.getStartTimeStamp();
        long currentTime = System.currentTimeMillis();

        txtDuration.setText(Strings.getTimeDisplay(getActivity(), currentTime - startTime));

        double distanceValue = session.getTotalTravelled();

        TextView txtPoints = (TextView) rootView.findViewById(R.id.simpleview_txtPoints);
        TextView txtTravelled = (TextView) rootView.findViewById(R.id.simpleview_txtDistance);

        txtTravelled.setText(Strings.getDistanceDisplay(getActivity(), distanceValue, preferenceHelper.shouldDisplayImperialUnits(), true));
        txtPoints.setText(session.getNumLegs() + " " + getString(R.string.points));

        String providerName = locationInfo.getProvider();
        if (!providerName.equalsIgnoreCase(LocationManager.GPS_PROVIDER)) {
            setSatelliteCount(-1);
        }
    }


    private void clearLocationDisplay() {

        EditText txtLatitude = (EditText) rootView.findViewById(R.id.simple_lat_text);
        txtLatitude.setText("");

        EditText txtLongitude = (EditText) rootView.findViewById(R.id.simple_lon_text);
        txtLongitude.setText("");

        ImageView imgAccuracy = (ImageView)rootView.findViewById(R.id.simpleview_imgAccuracy);
        clearColor(imgAccuracy);

        TextView txtAccuracy = (TextView) rootView.findViewById(R.id.simpleview_txtAccuracy);
        txtAccuracy.setText("");

        ImageView imgAltitude = (ImageView)rootView.findViewById(R.id.simpleview_imgAltitude);
        clearColor(imgAltitude);

        TextView txtAltitude = (TextView) rootView.findViewById(R.id.simpleview_txtAltitude);
        txtAltitude.setText("");

        ImageView imgDirection = (ImageView)rootView.findViewById(R.id.simpleview_imgDirection);
        clearColor(imgDirection);

        TextView txtDirection = (TextView) rootView.findViewById(R.id.simpleview_txtDirection);
        txtDirection.setText("");

        ImageView imgSpeed = (ImageView)rootView.findViewById(R.id.simpleview_imgSpeed);
        clearColor(imgSpeed);

        TextView txtSpeed = (TextView) rootView.findViewById(R.id.simpleview_txtSpeed);
        txtSpeed.setText("");


        TextView txtDuration = (TextView) rootView.findViewById(R.id.simpleview_txtDuration);
        txtDuration.setText("");

        TextView txtPoints = (TextView) rootView.findViewById(R.id.simpleview_txtPoints);
        TextView txtTravelled = (TextView) rootView.findViewById(R.id.simpleview_txtDistance);

        txtPoints.setText("");
        txtTravelled.setText("");
    }



    public void setSatelliteCount(int count) {
        ImageView imgSatelliteCount = (ImageView) rootView.findViewById(R.id.simpleview_imgSatelliteCount);
        TextView txtSatelliteCount = (TextView) rootView.findViewById(R.id.simpleview_txtSatelliteCount);

        if(count > -1) {
            setColor(imgSatelliteCount, IconColorIndicator.Good);

            AlphaAnimation fadeIn = new AlphaAnimation(0.6f, 1.0f);
            fadeIn.setDuration(1200);
            fadeIn.setFillAfter(true);
            txtSatelliteCount.startAnimation(fadeIn);
            txtSatelliteCount.setText(String.valueOf(count));
        }
        else {
            clearColor(imgSatelliteCount);
            txtSatelliteCount.setText("");
        }

    }

    public void onWaitingForLocation(boolean inProgress) {

        LOG.debug(inProgress + "");

        if(!session.isStarted()){
            actionButton.setProgress(0);
            setActionButtonStart();
            return;
        }

        if(inProgress){
            actionButton.setProgress(1);
            setActionButtonStop();
        }
        else {
            actionButton.setProgress(0);
            setActionButtonStop();
        }
    }


    @Override
    public void onClick(View view) {
        Toast toast = new Toast(getActivity());
        switch (view.getId()) {
            case R.id.simpleview_imgSatelliteCount:
                toast = getToast(R.string.txt_satellites);
                break;
            case R.id.simpleview_imgAccuracy:
                toast = getToast(R.string.txt_accuracy);
                break;

            case R.id.simpleview_imgAltitude:
                toast = getToast(R.string.txt_altitude);
                break;

            case R.id.simpleview_imgDirection:
                toast = getToast(R.string.txt_direction);
                break;

            case R.id.simpleview_imgDuration:
                toast = getToast(R.string.txt_travel_duration);
                break;

            case R.id.simpleview_imgSpeed:
                toast = getToast(R.string.txt_speed);
                break;

            case R.id.simpleview_distance:
                toast = getToast(R.string.txt_travel_distance);
                break;

            case R.id.simpleview_points:
                toast = getToast(R.string.txt_number_of_points);
                break;

            case R.id.simpleview_imgLink:
                toast = getToast(preferenceHelper.getCustomLoggingUrl());
                break;

        }

        int location[] = new int[2];
        view.getLocationOnScreen(location);
        toast.setGravity(Gravity.TOP | Gravity.LEFT, location[0], location[1]);
        toast.show();
    }

    private Toast getToast(String message) {
        return Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT);
    }

    private Toast getToast(int stringResourceId) {
        return getToast(getString(stringResourceId).replace(":", ""));
    }
}
