package com.fimbleenterprises.medimileage;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.location.Location;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Environment;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.Layout;
import android.text.SpannableString;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.math.RoundingMode;
import java.nio.channels.FileChannel;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;


import androidx.annotation.DrawableRes;
import androidx.annotation.RequiresApi;

import static android.content.Context.MODE_PRIVATE;

public class Helpers {

    public static class Application {

        public static float getAppVersion(Context context) {
            try {
                PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
                return Float.parseFloat(pInfo.versionName);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
                return 0;
            }
        }

        public static void openAppSettings(Context context) {

            Uri packageUri = Uri.fromParts( "package", context.getPackageName(), null );

            Intent applicationDetailsSettingsIntent = new Intent();

            applicationDetailsSettingsIntent.setAction( Settings.ACTION_APPLICATION_DETAILS_SETTINGS );
            applicationDetailsSettingsIntent.setData( packageUri );
            applicationDetailsSettingsIntent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );

            context.startActivity( applicationDetailsSettingsIntent );

        }
    }

    public static class Bitmaps {
        public static Bitmap getBitmapFromResource(Context context, @DrawableRes int resource) {
            return BitmapFactory.decodeResource(context.getResources(),
                    resource);
        }

        public static File createPngFileFromString(String text, String fileName) throws IOException {

           fileName = fileName.replace(".txt",".png");
            if (!fileName.endsWith(".png")) {
                fileName += ".png";
            }

            final Rect bounds = new Rect();
            TextPaint textPaint = new TextPaint() {
                {
                    setColor(Color.WHITE);
                    setTextAlign(Paint.Align.LEFT);
                    setTextSize(20f);
                    setAntiAlias(true);
                }
            };
            textPaint.getTextBounds(text, 0, text.length(), bounds);
            StaticLayout mTextLayout = new StaticLayout(text, textPaint,
                    bounds.width(), Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
            int maxWidth = -1;
            for (int i = 0; i < mTextLayout.getLineCount(); i++) {
                if (maxWidth < mTextLayout.getLineWidth(i)) {
                    maxWidth = (int) mTextLayout.getLineWidth(i);
                }
            }
            final Bitmap bmp = Bitmap.createBitmap(maxWidth , mTextLayout.getHeight(),
                    Bitmap.Config.ARGB_8888);
            bmp.eraseColor(Color.BLACK);// just adding black background
            final Canvas canvas = new Canvas(bmp);
            mTextLayout.draw(canvas);
            File outputFile = new File(Helpers.Files.getAppTempDirectory(), fileName);
            FileOutputStream stream = new FileOutputStream(outputFile); //create your FileOutputStream here
            bmp.compress(Bitmap.CompressFormat.PNG, 85, stream);
            bmp.recycle();
            stream.close();
            return outputFile;
        }

        public static File createJpegFileFromString(String text, String fileName) throws IOException {

            fileName = fileName.replace(".txt",".jpeg");
            if (!fileName.endsWith(".jpeg")) {
                fileName += ".jpeg";
            }

            final Rect bounds = new Rect();
            TextPaint textPaint = new TextPaint() {
                {
                    setColor(Color.WHITE);
                    setTextAlign(Paint.Align.LEFT);
                    setTextSize(20f);
                    setAntiAlias(true);
                }
            };
            textPaint.getTextBounds(text, 0, text.length(), bounds);
            StaticLayout mTextLayout = new StaticLayout(text, textPaint,
                    bounds.width(), Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
            int maxWidth = -1;
            for (int i = 0; i < mTextLayout.getLineCount(); i++) {
                if (maxWidth < mTextLayout.getLineWidth(i)) {
                    maxWidth = (int) mTextLayout.getLineWidth(i);
                }
            }
            final Bitmap bmp = Bitmap.createBitmap(maxWidth , mTextLayout.getHeight(),
                    Bitmap.Config.ARGB_8888);
            bmp.eraseColor(Color.BLACK);// just adding black background
            final Canvas canvas = new Canvas(bmp);
            mTextLayout.draw(canvas);
            File outputFile = new File(Helpers.Files.getAppTempDirectory(), fileName);
            FileOutputStream stream = new FileOutputStream(outputFile); //create your FileOutputStream here
            bmp.compress(Bitmap.CompressFormat.JPEG, 85, stream);
            bmp.recycle();
            stream.close();
            return outputFile;
        }
    }

    public static class Colors {
        public static final String YELLOW = "#EFC353";
        public static final String MEDISTIM_ORANGE = "#AAF37021";
        public static final String GREEN = "#2D9B01";
        public static final String RED = "#FF0000";
        public static final String MAROON = "#7F0000";
        public static final String SOFT_BLACK = "#3C4F5F";
        public static final String BLUE = "#0026FF";
        public static final String DISABLED_GRAY = "#808080";

        public static int getColor(String color) {
            return Color.parseColor(color);
        }
    }

    public static class BytesAndBits {
        public static long convertBytesToKb(long total) {
            return total / (1024);
        }

        public static long convertBytesToMb(long total) {
            return total / (1024 * 1024);
        }

        public static long convertBytesToGb(long total) {
            return total / (1024 * 1024 * 1024);
        }
    }

    public static class DatesAndTimes {

        private static final Map<String, String> DATE_FORMAT_REGEXPS = new HashMap<String, String>() {{
            put("^\\d{8}$", "yyyyMMdd");
            put("^\\d{1,2}-\\d{1,2}-\\d{4}$", "dd-MM-yyyy");
            put("^\\d{4}-\\d{1,2}-\\d{1,2}$", "yyyy-MM-dd");
            put("^\\d{1,2}/\\d{1,2}/\\d{4}$", "MM/dd/yyyy");
            put("^\\d{4}/\\d{1,2}/\\d{1,2}$", "yyyy/MM/dd");
            put("^\\d{1,2}\\s[a-z]{3}\\s\\d{4}$", "dd MMM yyyy");
            put("^\\d{1,2}\\s[a-z]{4,}\\s\\d{4}$", "dd MMMM yyyy");
            put("^\\d{12}$", "yyyyMMddHHmm");
            put("^\\d{8}\\s\\d{4}$", "yyyyMMdd HHmm");
            put("^\\d{1,2}-\\d{1,2}-\\d{4}\\s\\d{1,2}:\\d{2}$", "dd-MM-yyyy HH:mm");
            put("^\\d{4}-\\d{1,2}-\\d{1,2}\\s\\d{1,2}:\\d{2}$", "yyyy-MM-dd HH:mm");
            put("^\\d{1,2}/\\d{1,2}/\\d{4}\\s\\d{1,2}:\\d{2}$", "MM/dd/yyyy HH:mm");
            put("^\\d{4}/\\d{1,2}/\\d{1,2}\\s\\d{1,2}:\\d{2}$", "yyyy/MM/dd HH:mm");
            put("^\\d{1,2}\\s[a-z]{3}\\s\\d{4}\\s\\d{1,2}:\\d{2}$", "dd MMM yyyy HH:mm");
            put("^\\d{1,2}\\s[a-z]{4,}\\s\\d{4}\\s\\d{1,2}:\\d{2}$", "dd MMMM yyyy HH:mm");
            put("^\\d{14}$", "yyyyMMddHHmmss");
            put("^\\d{8}\\s\\d{6}$", "yyyyMMdd HHmmss");
            put("^\\d{1,2}-\\d{1,2}-\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}$", "dd-MM-yyyy HH:mm:ss");
            put("^\\d{4}-\\d{1,2}-\\d{1,2}\\s\\d{1,2}:\\d{2}:\\d{2}$", "yyyy-MM-dd HH:mm:ss");
            put("^\\d{1,2}/\\d{1,2}/\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}$", "MM/dd/yyyy HH:mm:ss");
            put("^\\d{4}/\\d{1,2}/\\d{1,2}\\s\\d{1,2}:\\d{2}:\\d{2}$", "yyyy/MM/dd HH:mm:ss");
            put("^\\d{1,2}\\s[a-z]{3}\\s\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}$", "dd MMM yyyy HH:mm:ss");
            put("^\\d{1,2}\\s[a-z]{4,}\\s\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}$", "dd MMMM yyyy HH:mm:ss");
        }};

        /**
         * Converts a Crm formatted string representing a date to a DateTime object.
         * @param datetime The date to attempt to convert
         * @return A DateTime object if successful, null if not.
         */
        public static DateTime parseCrmDateTime(String datetime) {
            try {
                DateTimeFormatter format = DateTimeFormat.forPattern("M/d/yyyy h:mm tt");
                DateTime result = DateTimeFormat.forPattern("M/d/yyyy h:mm tt").parseDateTime(datetime);
                return result;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        /**
         * Converts a Crm formatted string representing a date to a DateTime object.
         * @param date The date to attempt to convert
         * @return A DateTime object if successful, null if not.
         */
        public static DateTime parseCrmDateOnly(String date) {
            try {
                DateTimeFormatter format = DateTimeFormat.forPattern("M/d/yyyy");
                DateTime result = DateTimeFormat.forPattern("M/d/yyyy").parseDateTime(date);
                return result;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        public static String toCrmDate(DateTime datetime) {
            try {
                DateTimeFormatter format = DateTimeFormat.forPattern("M/d/yyyy");
                String result = datetime.toString(format);
                return result;
            } catch (Exception e) {
                e.printStackTrace();
                return datetime.toLocalDateTime().toString();
            }
        }

        /**
         * Converts the supplied milisecond value into minutes
         **/
        public static int convertMilisToMinutes(double milis) {
            int result = (int) milis / (1000 * 60);
            return result;
        }

        /**
         * Returns the current week of the year from 1 - 52 (e.g. 23)
         **/
        public static int returnDayOfYear(DateTime date) {

            Calendar c = Calendar.getInstance();
            c.setMinimalDaysInFirstWeek(7);//anything more than 1 will work in this year
            DateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            try {
                c.setTime(sdf.parse(date.getDayOfMonth() + "/" + date.getMonthOfYear() + "/" + date.getYearOfCentury()));
            } catch (ParseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            return c.get(Calendar.DAY_OF_YEAR);
        }

        /**
         * Returns the current week of the year from 1 - 52 (e.g. 23)
         **/
        public static int returnWeekOfYear(DateTime date) {

            Calendar c = Calendar.getInstance();
            c.setMinimalDaysInFirstWeek(7);//anything more than 1 will work in this year
            DateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            try {
                c.setTime(sdf.parse(date.getDayOfMonth() + "/" + date.getMonthOfYear() + "/" + date.getYearOfCentury()));
            } catch (ParseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            return c.get(Calendar.WEEK_OF_YEAR);
        }

        /**
         * Returns the current week of the year from 1 - 52 (e.g. 23)
         **/
        public static int returnMonthOfYear(DateTime date) {

            Calendar c = Calendar.getInstance();
            c.setMinimalDaysInFirstWeek(7);//anything more than 1 will work in this year
            DateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            try {
                c.setTime(sdf.parse(date.getDayOfMonth() + "/" + date.getMonthOfYear() + "/" + date.getYearOfCentury()));
            } catch (ParseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            return c.get(Calendar.MONTH);
        }

        public static String returnMonthName(int monthNumber, boolean abbreviateMonthName) {
            String monthString = "";

            switch (monthNumber) {
                case 1:
                    monthString = "January";
                    break;
                case 2:
                    monthString = "Febuary";
                    break;

                case 3:
                    monthString = "March";
                    break;
                case 4:
                    monthString = "April";
                    break;
                case 5:
                    monthString = "May";
                    break;
                case 6:
                    monthString = "June";
                    break;
                case 7:
                    monthString = "July";
                    break;
                case 8:
                    monthString = "August";
                    break;
                case 9:
                    monthString = "September";
                    break;
                case 10:
                    monthString = "October";
                    break;
                case 11:
                    monthString = "November";
                    break;
                case 12:
                    monthString = "December";
                    break;
            }

            if (abbreviateMonthName == true) {
                monthString = monthString.substring(0, 3);
            }

            return monthString;
        }

        public static String getPrettyDate(DateTime now) {

            String day = String.valueOf(now.getDayOfMonth());
            String month = String.valueOf(now.getMonthOfYear());
            String year = String.valueOf(now.getYear());

            return month + "/" + day + "/" + year;

        }

        public static DateTime parseDate(String strDate) {
            DateTimeFormatter df = DateTimeFormat.forPattern("M/d/yyyy h:mm a");
            try {
                DateTime dateTime = df.parseDateTime(strDate);
                return dateTime;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return new DateTime();
        }

        public static String getPrettyDateAndTime(DateTime now) {

            String day = String.valueOf(now.getDayOfMonth());
            String month = String.valueOf(now.getMonthOfYear());
            String year = String.valueOf(now.getYear());
            String amPm = "am";

            int intHour = now.getHourOfDay();
            if (intHour == 12) {
                amPm = "pm";
            }
            if (intHour > 12) {
                intHour = intHour - 12;
                amPm = "pm";
            }
            String hour = String.valueOf(intHour);
            int intMinutes = now.getMinuteOfHour();
            String minutes = String.valueOf(intMinutes);

            switch (intMinutes) {
                case 0:
                    minutes = "00";
                    break;
                case 1:
                    minutes = "01";
                    break;
                case 2:
                    minutes = "02";
                    break;
                case 3:
                    minutes = "03";
                    break;
                case 4:
                    minutes = "04";
                    break;
                case 5:
                    minutes = "05";
                    break;
                case 6:
                    minutes = "06";
                    break;
                case 7:
                    minutes = "07";
                    break;
                case 8:
                    minutes = "08";
                    break;
                case 9:
                    minutes = "09";
                    break;
            }

            return month + "/" + day + "/" + year + " " + hour + ":" + minutes + " " + amPm;

        }

        // This method returns today's date as a short date string
        public static String getTodaysDate() {
            Calendar c = Calendar.getInstance();
            SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy");
            String formattedDate = df.format(c.getTime());

            Log.d("GetTodaysDate", "Today's date is: '" + formattedDate + "'");

            return formattedDate;
        }

        // This method returns yesterday's date as a short date string
        public static String getYesterdaysDate() {

            // Get today as a Calendar
            Calendar c = Calendar.getInstance();
            SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy");
            // Subtract 1 day
            c.add(Calendar.DATE, -1);
            String formattedDate = df.format(c.getTime());

            Log.d("GetYesterdaysDate", "Yesterday's date is: '" + formattedDate + "'");

            return formattedDate;
        }

        // This method returns the first day of the week as a short date string
        public static String getFirstOfWeek() {

            // get today and clear time of day
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, 0); // ! clear would not reset the hour of
            // day !
            cal.clear(Calendar.MINUTE);
            cal.clear(Calendar.SECOND);
            cal.clear(Calendar.MILLISECOND);

            // get start of this week as a formal date
            cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());

            // instantiate a formatter
            SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy");

            // format the formal date
            String formattedDate = df.format(cal.getTime());

            // log the result
            Log.d("getFirstOfWeek()", "First day of this week is: '" + formattedDate + "'");

            // return the result
            return formattedDate;

        }

        // This method returns the first day of the month as a short date string
        public static String getFirstOfMonth() {

            // get today and clear time of day
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, 0); // ! clear would not reset the hour of
            // day !
            cal.clear(Calendar.MINUTE);
            cal.clear(Calendar.SECOND);
            cal.clear(Calendar.MILLISECOND);

            // get start of this week as a formal date
            cal.set(Calendar.DAY_OF_MONTH, 1);

            // instantiate a formatter
            SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy");

            // format the formal date
            String formattedDate = df.format(cal.getTime());

            // log the result
            Log.d("getFirstOfMonth()", "First day of this month is: '" + formattedDate + "'");

            // return the result
            return formattedDate;
        }

        public static String getLastDayOfMonth(int month, int year) {
            String result = "";
            // month = month + 1; // Zero based month index
            if (month == 0) {
                month = 1;
            }
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
            Date convertedDate = null;
            String dateString = String.valueOf(month) + "/1/" + String.valueOf(year);
            try {
                convertedDate = dateFormat.parse(dateString);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            Calendar c = Calendar.getInstance();
            c.setTime(convertedDate);
            c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH));
            String d, m, y;
            d = String.valueOf(c.get(Calendar.DAY_OF_MONTH));
            m = String.valueOf(c.get(Calendar.MONTH) + 1);
            y = String.valueOf(c.get(Calendar.YEAR));
            result = m + "/" + d + "/" + y;
            return result;
        }

        /**
         * Returns the string fullname of the requested month number
         *
         * @param monthNumber The month number as an int
         * @return String value of the month number.
         */
        public static String getMonthName(int monthNumber) {
            String prettyMonth = "";
            switch (monthNumber) {
                case 1:
                    prettyMonth = "January ";
                    break;
                case 2:
                    prettyMonth = "February ";
                    break;
                case 3:
                    prettyMonth = "March ";
                    break;
                case 4:
                    prettyMonth = "April ";
                    break;
                case 5:
                    prettyMonth = "May ";
                    break;
                case 6:
                    prettyMonth = "June ";
                    break;
                case 7:
                    prettyMonth = "July ";
                    break;
                case 8:
                    prettyMonth = "August ";
                    break;
                case 9:
                    prettyMonth = "September ";
                    break;
                case 10:
                    prettyMonth = "October ";
                    break;
                case 11:
                    prettyMonth = "November ";
                    break;
                case 12:
                    prettyMonth = "December ";
                    break;
                default:
                    prettyMonth = "";
                    break;
            }
            return prettyMonth;
        }

        public static String getPrettyDate2(DateTime dateTime) {
            String monthName = getMonthName(dateTime.getMonthOfYear()).trim();
            String year = Integer.toString(dateTime.getYear());
            String day = Integer.toString(dateTime.getDayOfMonth());
            String result = monthName + " " + day + ", " + year;
            return result;
        }

        /**
         * Determine SimpleDateFormat pattern matching with the given date string. Returns null if
         * format is unknown. You can simply extend DateUtil with more formats if needed.
         * @param dateString The date string to determine the SimpleDateFormat pattern for.
         * @return The matching SimpleDateFormat pattern, or null if format is unknown.
         * @see SimpleDateFormat
         */
        public static String determineDateFormat(String dateString) {
            for (String regexp : DATE_FORMAT_REGEXPS.keySet()) {
                if (dateString.toLowerCase().matches(regexp)) {
                    return DATE_FORMAT_REGEXPS.get(regexp);
                }
            }
            return null; // Unknown format.
        }
    }

    public static class Geo {
        /**
         * Returns a Location object from the supplied LatLng object.
         * Note that only lat and lng are really populated.
         **/
        public static Location createLocFromLatLng(LatLng ll) {
            Location location = new Location("");
            location.setLatitude(ll.latitude);
            location.setLongitude(ll.longitude);
            location.setTime(System.currentTimeMillis());
            location.setAccuracy(0);

            return location;
        }

        public static String calculateBearing(float bearing) {

            String prettyBearing = "";

            if ((bearing >= 337.5 && bearing <= 360) || (bearing >= 0 && bearing < 22.5)) {
                prettyBearing = "N";
            }

            if (bearing >= 22.5 && bearing < 67.5) {
                prettyBearing = "NE";
            }

            if (bearing >= 67.5 && bearing < 112.5) {
                prettyBearing = "E";
            }

            if (bearing >= 112.5 && bearing < 157.5) {
                prettyBearing = "SE";
            }

            if (bearing >= 157.5 && bearing < 202.5) {
                prettyBearing = "S";
            }

            if (bearing >= 202.5 && bearing < 247.5) {
                prettyBearing = "SW";
            }

            if (bearing >= 247.5 && bearing < 292.5) {
                prettyBearing = "W";
            }

            if (bearing >= 292.5 && bearing < 337.5) {
                prettyBearing = "NW";
            }

            return prettyBearing;
        }

        /**
         * Returns an integer between the values of 0 and 100 which represents a percentage.  Higher is more accurate
         **/
        public static int getCurrentAccAsPct(float accuracy) {
            float a = accuracy;
            if (a > 100f) {
                a = 100f;
            }
            float d = a / 100f; // should rslt in a decimal between 0 and 1.  Higher is worse.
            float pct = 1f - d;
            float rslt = pct * 100;
            int intRslt = (int) rslt;
            return intRslt;
        }

        /**
         * Takes the supplied meters value and converts it to either miles or kilometers.
         * If you supply true to the appendToMakePretty parameter it will append the correct
         * measurement unit to the end of the result (e.g. "miles" or "km"
         **/
        public static float convertMetersToMiles(double meters, int decimalCount) {

            if (meters == 0) {
                return 0f;
            }

            double kilometers = meters / 1000d;
            double feet = (meters * 3.280839895d);
            double miles = (feet / 5280d);

            DecimalFormat df = new DecimalFormat("#.#");
            df.setMaximumFractionDigits(decimalCount);
            String result = "";

            result = df.format((miles));

            return Float.parseFloat(result);
        }

        /**
         * Takes the supplied meters value and converts it to either miles or kilometers.
         * If you supply true to the appendToMakePretty parameter it will append the correct
         * measurement unit to the end of the result (e.g. "miles" or "km"
         **/
        public static float convertMilesToMeters(float miles, int decimalCount) {

            float meters = (miles * 1609.34f);

            DecimalFormat df = new DecimalFormat("#.#");
            df.setMaximumFractionDigits(decimalCount);
            String result = df.format((meters));

            return Float.parseFloat(result);
        }

        /**
         * Takes the supplied meters value and converts it to either miles or kilometers.
         * If you supply true to the appendToMakePretty parameter it will append the correct
         * measurement unit to the end of the result (e.g. "miles" or "km"
         **/
        public static String convertMetersToMiles(double meters, boolean appendToMakePretty) {

            if (meters == 0) {
                return "0";
            }

            double kilometers = meters / 1000d;
            double feet = (meters * 3.280839895d);
            double miles = (feet / 5280d);

            DecimalFormat df = new DecimalFormat("#.#");
            String result = "";

            result = df.format((miles));
            if (appendToMakePretty) {
                result += " miles";
            }

            return result;
        }

        /**
         * Takes the supplied meters value and converts it to either miles or kilometers.
         * If you supply true to the appendToMakePretty parameter it will append the correct
         * measurement unit to the end of the result (e.g. "miles" or "km"
         **/
        public static String convertMetersToFeet(double meters, Context context, boolean appendToMakePretty) {

            if (meters == 0) {
                return "0";
            }

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            String measUnit = prefs.getString("MEASUREUNIT", "IMPERIAL");
            double feet = (meters * 3.280839895);
            DecimalFormat df = new DecimalFormat("#.#");

            String result = "";

            if (measUnit.equals("IMPERIAL")) {
                result = df.format((feet));
                if (appendToMakePretty) {
                    result += " feet";
                }
            }
            return result;
        }

        /**
         * Returns a speed in MPH or KPH (depends on user's settings) for the supplied meters per second value
         * <br/><br/>
         * Returns the value as a String in a #.# format.
         * <br/><br/>
         * If the user specifies true for 'appendAppropriateMetric' then either " mph" or " kph" will be appended to the back of the result.
         **/
        public static String getSpeedInMph(float metersPerSecond, Context appContext, boolean appendLetters,
                                           boolean returnLotsOfDecimalPlaces) {

            String rslt = "0";

            try {
                double kmPerHour = ((metersPerSecond * 3600) / 1000);
                double milesPerHour = (metersPerSecond) / (1609.344 / 3600);
                double feetPerSecond = (milesPerHour * 5280) / 3600;

                DecimalFormat df = new DecimalFormat("#.##");

                String decimalMask = "";

                if (returnLotsOfDecimalPlaces) {
                    df.setMaximumFractionDigits(8);
                }

                String mph = (df.format(milesPerHour));
                String fps = (df.format(feetPerSecond));
                String kph = (df.format(kmPerHour));
                String mps = (df.format(metersPerSecond));

                // Assign the mph to the value we're going to return
                rslt = mph;

                // If the user wants to append the mph value to the returned string then we oblige here
                if (appendLetters == true) {
                    rslt += " mph";
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
                return "0";
            }

            return rslt;

        }

        public static float getSpeedInMph(float metersPerSecond, boolean includeTwoDecimalPlaces) {
            int decimalPlaces = 0;
            if (includeTwoDecimalPlaces) decimalPlaces = 2;
            String spdString = getSpeedInMph(metersPerSecond, false, decimalPlaces);
            return Float.parseFloat(spdString);
        }

        public static float getSpeedInMph(float metersPerSecond, int decimals) {
            String spdString = getSpeedInMph(metersPerSecond, false, decimals);
            return Float.parseFloat(spdString);
        }


        /**
         * Returns a speed in MPH or KPH (depends on user's settings) for the supplied meters per second value
         * <br/><br/>
         * Returns the value as a String in a #.# format.
         * <br/><br/>
         * If the user specifies true for 'appendAppropriateMetric' then either " mph" or " kph" will be appended to the back of the result.
         **/
        public static String getSpeedInMph(float metersPerSecond, boolean appendLetters,
                                           int decimalPlaces) {
            String rslt = "0";

            try {
                double kmPerHour = ((metersPerSecond * 3600) / 1000);
                double milesPerHour = (metersPerSecond) / (1609.344 / 3600);
                double feetPerSecond = (milesPerHour * 5280) / 3600;

                DecimalFormat df = new DecimalFormat("#.##");

                df.setMaximumFractionDigits(decimalPlaces);


                String mph = (df.format(milesPerHour));
                String fps = (df.format(feetPerSecond));
                String kph = (df.format(kmPerHour));
                String mps = (df.format(metersPerSecond));

                // Assign the mph to the value we're going to return
                rslt = mph;

                // If the user wants to append the mph value to the returned string then we oblige here
                if (appendLetters == true) {
                    rslt += " mph";
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
                return "0";
            }

            return rslt;
        }

        /**
         * Returns the supplied float meters/sec into integer miles/hr
         *
         * @param metersPerSecond
         * @return
         */
        public static int getSpeedInMph(float metersPerSecond) {
            double dblSpd = (metersPerSecond) / (1609.344 / 3600);
            return (int) dblSpd;
        }

        /**
         * Calculates the distance between two points in miles as a string
         *
         * @param a           Point A (LatLng)
         * @param b           Point B (LatLng)
         * @param appendMiles Whether or not to append, " Miles" to the end of the result
         * @return Distance in miles (as the crow flies)
         */
        public static String getDistanceBetweenInMiles(Location a, Location b, boolean appendMiles) {
            Location loc1 = new Location("");
            loc1.setLatitude(a.getLatitude());
            loc1.setLongitude(a.getLongitude());

            Location loc2 = new Location("");
            loc2.setLatitude(b.getLatitude());
            loc2.setLongitude(b.getLongitude());

            float distanceInMeters = loc1.distanceTo(loc2);

            return convertMetersToMiles(distanceInMeters, appendMiles);
        }

        /**
         * Calculates the distance between two points
         *
         * @param locA Point A (Location)
         * @param locB Point B (Location)
         * @return Distance in meters (as the crow flies)
         */
        public static float getDistanceBetweenInMeters(Location locA, Location locB) {
            Location loc1 = new Location("");
            loc1.setLatitude(locA.getLatitude());
            loc1.setLongitude(locA.getLongitude());

            Location loc2 = new Location("");
            loc2.setLatitude(locB.getLatitude());
            loc2.setLongitude(locB.getLongitude());

            return loc1.distanceTo(loc2);
        }
    }

    public static class Numbers {

        /**
         * Checks if a number is numeric (kind of an expensive operation so if it needs to be done a
         * ton then roll a different way)
         **/
        public static boolean isNumeric(String str) {
            try {
                float d = Float.parseFloat(str);
            } catch (NumberFormatException nfe) {
                return false;
            }
            return true;
        }

        public static int makeRandomInt() {
            String strLng = Long.toString(System.currentTimeMillis());
            String subStrLng = strLng.substring(5);
            return Integer.parseInt(subStrLng);
        }

        public static double formatAsTwoDecimalPointNumber(double number, RoundingMode roundingMode) {
            DecimalFormat df2 = new DecimalFormat("#.##");
            df2.setRoundingMode(roundingMode);
            return Double.parseDouble(df2.format(number));
        }

        public static double formatAsOneDecimalPointNumber(double number, RoundingMode roundingMode) {
            DecimalFormat df2 = new DecimalFormat("#.#");
            df2.setRoundingMode(roundingMode);
            return Double.parseDouble(df2.format(number));
        }

        public static double formatAsOneDecimalPointNumber(double number) {
            DecimalFormat df2 = new DecimalFormat("#.#");
            // df2.setRoundingMode(roundingMode);
            return Double.parseDouble(df2.format(number));
        }

        public static int formatAsZeroDecimalPointNumber(double number, RoundingMode roundingMode) {
            DecimalFormat df2 = new DecimalFormat("#");
            df2.setRoundingMode(roundingMode);
            return Integer.parseInt(df2.format(number));
        }

        public static String convertToCurrency(double amount) {
            NumberFormat nf = NumberFormat.getCurrencyInstance();
            return nf.format(amount);
        }

        public static String convertToPercentage(double value) {
            NumberFormat numberFormat = NumberFormat.getPercentInstance();
            numberFormat.setMaximumFractionDigits(1);
            return numberFormat.format(value);
        }

        public static int getRandom(int low, int high) {
            Random r = new Random();
            int i1 = r.nextInt((high + 1) - low) + low;
            return i1;
        }

        public static boolean isEven(int number) {
            if ((number % 2) == 0) {
                return true;
            } else {
                return false;
            }
        }
    }

    public static class Notify {


        /**
         * Returns a media player object that plays the system's notification sound.  Can be told to play immediately as well as whether or not to loop
         **/
        public static MediaPlayer playSound(Context context, boolean playImmediately,
                                            boolean setLooping) throws IllegalArgumentException, SecurityException, IllegalStateException, IOException {
            Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            MediaPlayer mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setDataSource(context, soundUri);
            final AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            if (audioManager.getStreamVolume(AudioManager.STREAM_ALARM) != 0) {
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
                mMediaPlayer.setLooping(setLooping);
                mMediaPlayer.prepare();
                if (playImmediately == true) {
                    mMediaPlayer.start();
                }
                return mMediaPlayer;
            } else {
                return null;
            }
        }
    }

    public static class Email {
        /**
         * Launches a dialog to open an googleEmail while populating the to, subject and body fields.  User
         * must still press send.
         *
         * @param recipients example: new String[]{"recipient@example.com"}
         * @param body       The body of the googleEmail message
         * @param subject    The subject of the googleEmail message
         * @param context    The sending method's context
         */
        public static void sendEmail(String[] recipients, String body, String subject, Context context) {
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("message/rfc822");
            i.putExtra(Intent.EXTRA_EMAIL, recipients);
            i.putExtra(Intent.EXTRA_BCC, recipients);
            i.putExtra(Intent.EXTRA_SUBJECT, subject);
            i.putExtra(Intent.EXTRA_TEXT, body);
            try {
                context.startActivity(Intent.createChooser(i, "Send mail..."));
            } catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(context, "There are no googleEmail clients installed.", Toast.LENGTH_SHORT).show();
            }
        }

        /**
         * Launches a dialog to open an googleEmail while populating the to, subject and body fields.  User
         * must still press send.
         *
         * @param recipients example: new String[]{"recipient@example.com"}
         * @param body       The body of the googleEmail message
         * @param subject    The subject of the googleEmail message
         * @param context    The sending method's context
         */
        public static void sendEmail(String[] recipients, String body, String subject, Context context,
                                     File attachment) {
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("message/rfc822");
            i.putExtra(Intent.EXTRA_EMAIL, recipients);
            i.putExtra(Intent.EXTRA_BCC, recipients);
            i.putExtra(Intent.EXTRA_SUBJECT, subject);
            i.putExtra(Intent.EXTRA_TEXT, body);
            i.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + attachment));
            try {
                context.startActivity(Intent.createChooser(i, "Send mail..."));
            } catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(context, "There are no googleEmail clients installed.", Toast.LENGTH_SHORT).show();
            }
        }

        /**
         * Launches a dialog to open an googleEmail while populating the to, subject and body fields.  User
         * must still press send.
         *
         * @param recipients example: new String[]{"recipient@example.com"}
         * @param body       The body of the googleEmail message
         * @param subject    The subject of the googleEmail message
         * @param context    The sending method's context
         */
        public static void sendEmail(String[] recipients, String body, String subject, Context context,
                                     File attachment, boolean copyMe) {

            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("message/rfc822");
            i.putExtra(Intent.EXTRA_EMAIL, recipients);
            if (copyMe) {
                i.putExtra(Intent.EXTRA_BCC, recipients);
            }
            i.putExtra(Intent.EXTRA_SUBJECT, subject);
            i.putExtra(Intent.EXTRA_TEXT, body);
            i.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + attachment));
            try {
                context.startActivity(Intent.createChooser(i, "Send mail..."));
            } catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(context, "There are no googleEmail clients installed.", Toast.LENGTH_SHORT).show();
            }
        }

        public static void sendEmail(String[] recipients, String[] bccRecipients, String body, String subject, Context context) {
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("message/rfc822");
            i.putExtra(Intent.EXTRA_EMAIL, recipients);
            i.putExtra(Intent.EXTRA_BCC, bccRecipients);
            i.putExtra(Intent.EXTRA_SUBJECT, subject);
            i.putExtra(Intent.EXTRA_TEXT, body);
            try {
                context.startActivity(Intent.createChooser(i, "Send mail..."));
            } catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(context, "There are no googleEmail clients installed.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public static class Battery {

        /**
         * Returns whether or not the device is plugged ino to AC/DC power or USB
         **/
        public static boolean deviceIsPluggedIn(Context context) {
            Intent intent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
            int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
            return plugged == BatteryManager.BATTERY_PLUGGED_AC || plugged == BatteryManager.BATTERY_PLUGGED_USB;
        }

        /**
         * Returns an integer which can be compared to the BatteryManager constants.  1 == AC power and 2 == USB power
         **/
        public static int deviceIsPluggedInto(Context context) {
            Intent intent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
            int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
            return plugged;
        }

        /**
         * Returns whether or not the screen is currently on or off
         **/
        public static boolean isScreenOn(Context context) {
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            return pm.isScreenOn();
        }
    }

    public static class Debug {

        private static final String TAG = "Debug";

        public static final String LOG_FILE_NAME = "medibuddy_debug_log";
        public static final String TEMP_LOG_FILE_NAME = "temp_medibuddy_debug_log";

        /***
         * Reads the device's logcat log and returns it as a string.
         * @return String object representing the logcat information.
         */
        public static String sendLogcat(final Context context) {
            Toast.makeText(context, "Please wait while I gather debugging data...", Toast.LENGTH_LONG).show();

            final StringBuilder text = new StringBuilder();
            try {
                final File logFile = new File(Environment.getExternalStorageDirectory(), LOG_FILE_NAME);
                Log.d(TAG, "Gathering logcat data...");

                new OutputStreamWriter(context.openFileOutput(TEMP_LOG_FILE_NAME, MODE_PRIVATE));
                final File tempFile = new File(context.getFilesDir(), TEMP_LOG_FILE_NAME);
                Runtime.getRuntime().exec("logcat -d -v time -f " + tempFile.getAbsolutePath());

                try {
                    Log.d(TAG, "Reading logcat file...");
                    BufferedReader br = new BufferedReader(new FileReader(tempFile));
                    String line;
                    while ((line = br.readLine()) != null) {
                        text.append(line);
                        text.append('\n');
                    }
                    br.close();
                    Log.d(TAG, "Finished reading - appending it to our log file...");
                    tempFile.delete();

                    FileWriter fw = new FileWriter(logFile, true);
                    fw.write(text.toString() + "\n\n");
                    fw.close();
                    Log.d(TAG, "Finished appending - creating googleEmail intent...");
                    Email.sendEmail(new String[]{"matt.weber@medistimusa.com"}, "LogCat stuff, yo.", "LogCat data " +
                            "from MediBuddy", context, logFile);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return "";
        }
    }

    public static class Animations {

        public enum AnimationType {
            WOBBLER, PULSE, PULSE_HARDER
        }

        public static void pulseAnimation(View target) {
            ObjectAnimator scaleDown = ObjectAnimator.ofPropertyValuesHolder(target,
                    PropertyValuesHolder.ofFloat("scaleX", 1.00f),
                    PropertyValuesHolder.ofFloat("scaleY", 1.15f));
            scaleDown.setDuration(750);

            scaleDown.setRepeatCount(250);
            scaleDown.setRepeatMode(ObjectAnimator.REVERSE);

            scaleDown.start();
        }

        public static void pulseAnimation(View target, float scaleX, float scaleY, int repeatCount, int scaleDownDuration) {
            ObjectAnimator scaleDown = ObjectAnimator.ofPropertyValuesHolder(target,
                    PropertyValuesHolder.ofFloat("scaleX", scaleX),
                    PropertyValuesHolder.ofFloat("scaleY", scaleY));
            scaleDown.setDuration(scaleDownDuration);

            if (repeatCount > 0) {
                scaleDown.setRepeatCount(repeatCount);
            }

            scaleDown.setRepeatMode(ObjectAnimator.REVERSE);

            scaleDown.start();
        }

        public static void fadeOut(View view, int duration, Animation.AnimationListener callback) {
            Animation fade = new AlphaAnimation(1, 0);
            fade.setInterpolator(new AccelerateInterpolator()); //and this
            fade.setStartOffset(1000);
            fade.setDuration(duration);

            AnimationSet animation = new AnimationSet(false); //change to false
            animation.addAnimation(fade);

            animation.setAnimationListener(callback);

            view.setAnimation(animation);
        }

        public static void fadeIn(View view, int duration, Animation.AnimationListener callback) {
            Animation fade = new AlphaAnimation(0, 1);
            fade.setInterpolator(new AccelerateInterpolator()); //and this
            fade.setStartOffset(1000);
            fade.setDuration(duration);

            AnimationSet animation = new AnimationSet(false); //change to false
            animation.addAnimation(fade);

            animation.setAnimationListener(callback);

            view.setAnimation(animation);
        }

        public static void fadeOut(View view, int duration) {
            Animation fade = new AlphaAnimation(1, 0);
            fade.setInterpolator(new AccelerateInterpolator()); //and this
            fade.setStartOffset(1000);
            fade.setDuration(duration);

            AnimationSet animation = new AnimationSet(false); //change to false
            animation.addAnimation(fade);

            view.setAnimation(animation);
        }

        public static void fadeIn(View view, int duration) {
            Animation fade = new AlphaAnimation(0, 1);
            fade.setInterpolator(new AccelerateInterpolator()); //and this
            fade.setStartOffset(1000);
            fade.setDuration(duration);

            AnimationSet animation = new AnimationSet(false); //change to false
            animation.addAnimation(fade);

            view.setAnimation(animation);
        }

        public static void animateView(View view, Context context, AnimationType animationType) {
            int resourceId;
            switch (animationType) {
                case WOBBLER:
                    resourceId = R.anim.wobbler;
                    break;
                case PULSE:
                    resourceId = R.anim.pulse;
                    break;
                case PULSE_HARDER:
                    resourceId = R.anim.pulse_harder;
                    break;
                default:
                    resourceId = R.anim.pulse;
                    break;
            }
            final Animation b = AnimationUtils.loadAnimation(context, resourceId);
            b.reset();
            b.setRepeatCount(Animation.INFINITE);
            view.startAnimation(b);
        }

        public static Animation outToLeftAnimation() {
            Animation outtoLeft = new TranslateAnimation(
                    Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, -1.0f,
                    Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f
            );
            outtoLeft.setDuration(175);
            outtoLeft.setInterpolator(new AccelerateInterpolator());
            return outtoLeft;
        }

        public static Animation inFromLeftAnimation() {
            Animation inFromLeft = new TranslateAnimation(
                    Animation.RELATIVE_TO_PARENT, -1.0f, Animation.RELATIVE_TO_PARENT, 0.0f,
                    Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f
            );
            inFromLeft.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {

                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            inFromLeft.setDuration(175);
            inFromLeft.setInterpolator(new AccelerateInterpolator());
            return inFromLeft;
        }

        public static Animation outToRightAnimation() {
            Animation outtoRight = new TranslateAnimation(
                    Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, -1.0f,
                    Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f
            );
            outtoRight.setDuration(175);
            outtoRight.setInterpolator(new AccelerateInterpolator());
            return outtoRight;
        }

        public static Animation inFromRightAnimation() {
            Animation inFromRight = new TranslateAnimation(
                    Animation.RELATIVE_TO_PARENT, +1.0f, Animation.RELATIVE_TO_PARENT, 0.0f,
                    Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f
            );
            inFromRight.setDuration(175);
            inFromRight.setInterpolator(new AccelerateInterpolator());
            return inFromRight;
        }

        public static Animation outToTop() {
            Animation animation = new TranslateAnimation(
                    Animation.RELATIVE_TO_PARENT, +0.0f,
                    Animation.RELATIVE_TO_PARENT, 0.0f,
                    Animation.RELATIVE_TO_PARENT, 0.0f,
                    Animation.RELATIVE_TO_PARENT, 1.0f);
            animation.setDuration(175);
            animation.setInterpolator(new AccelerateInterpolator());
            return animation;
        }

        public static Animation inFromTop() {
            Animation animation = new TranslateAnimation(
                    Animation.RELATIVE_TO_PARENT, +0.0f,
                    Animation.RELATIVE_TO_PARENT, 0.0f,
                    Animation.RELATIVE_TO_PARENT, 1.0f,
                    Animation.RELATIVE_TO_PARENT, 0.0f);
            animation.setDuration(175);
            animation.setInterpolator(new AccelerateInterpolator());
            return animation;
        }
    }

    public static class Files {

        private static final String TAG = "Files";

        public static boolean copy(File source, File dest) {
            try {
                FileChannel src = new FileInputStream(source).getChannel();
                @SuppressWarnings("resource")
                FileChannel dst = new FileOutputStream(dest).getChannel();
                long bytes = dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();
                return dest.exists();
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        public static long convertBytesToKb(long total) {
            return total / (1024);
        }

        public static long convertBytesToMb(long total) {
            return total / (1024 * 1024);
        }

        public static float convertBytesToMb(long total, boolean decimals) {
            DecimalFormat df = new DecimalFormat("0.00");
            String strResult =  df.format((float) total / (1024 * 1024));
            return Float.parseFloat(strResult);
        }

        public static long convertBytesToGb(long total) {
            return total / (1024 * 1024 * 1024);
        }

        /**
         * Returns the supplied file's extension (e.g. .png).  Returns null if any errors are thrown.
         *
         * @param fileName Either a fully qualified file or just a file fullname.
         * @return A (always) lowercase string, which includes the period, representing the file's extension. (e.g. .png)
         */
        public static String getExtension(String fileName) {
            String extension = "";

            try {
                int lastPeriod = fileName.lastIndexOf(".");
                extension = fileName.substring(lastPeriod);
                extension = extension.toLowerCase();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return null;

            }
            return extension;
        }

        /**
         * Attempts to parse out the filename from a url or filesystem file (assuming the filesystem
         * uses a forward slash as the file separator)
         *
         * @param path A url or fully qualified path to the file to parse.
         * @return The filename or whatever comes after the final "/" found in the string
         */
        public static String parseFileNameFromPath(String path) {
            int fSlashIndex = path.lastIndexOf(File.separator);
            String filename = path.substring(fSlashIndex + 1);
            return filename;
        }

        // Checks for the existence of a file. Returns boolean.
        public static boolean fileExists(String path, String filename) {

            boolean result = false;

            java.io.File file = new java.io.File(path, filename);
            if (file.exists()) {
                result = true;
                Log.d("fileExists", "Found the file at: " + path + filename);
                // b.setCompoundDrawablesWithIntrinsicBounds(null, PLAYLOGO , null,
                // null);
                result = true;

            } else {
                Log.d("fileExists", "Couldn't find the file at: " + path + filename);
                result = false;
            }

            return result;
        }

        /**
         * Deletes all the files in the specified directory
         **/
        public static boolean deleteDirectory(String filePath) {
            File path = new File(filePath);
            if (path.exists()) {
                File[] files = path.listFiles();
                for (int i = 0; i < files.length; i++) {
                    try {
                        files[i].delete();
                        Log.d(TAG, "Deleted: " + files[i].getName());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            return (path.delete());
        }// END deleteDirectory()

        public static File getAppDirectory() {
                makeAppDirectory();
                Context context = MyApp.getAppContext();
                File dir = new File(context.getExternalFilesDir(null), "MileBuddy");
                Log.i(TAG, "getAppDirectory: " + dir.getAbsolutePath());
                return dir;
        }

        public static void makeAppDirectory() {

            Context context = MyApp.getAppContext();

            File dir = new File(context.getExternalFilesDir(null).getAbsolutePath());

            if (!dir.exists() || !dir.isDirectory()) {
                Log.i(TAG, "makeAppDirectory: " + dir.mkdirs());
            } else {
                Log.i(TAG, "makeAppDirectory: App directory exists");
            }
        }

        /*
        public static void makeBackupDirectory() {

            Context context = MyApp.getAppContext();

            File dir = new File(getAppDirectory().getPath(), "Backups");

            if (!dir.exists() || !dir.isDirectory()) {
                Log.i(TAG, "makeBackupDirectory: " + dir.mkdirs());;
            } else {
                Log.i(TAG, "makeBackupDirectory: Backup directory exists");
            }
        }
        */

        public static File getAppDownloadDirectory() {
            return new File(MyApp.getAppContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString());
        }

        public static File getAppTempDirectory() {
            File tmp = new File(MyApp.getAppContext().getExternalFilesDir(null).toString() + File.separator
                    + "temp");
            if (!tmp.exists()) {
                tmp.mkdirs();
            }
            return tmp;
        }

        public static boolean deleteAppTempDirectory() {
            boolean result = false;

            File tempDir = getAppTempDirectory();
            if (tempDir.exists()) {
                if (tempDir.isDirectory()) {
                    for (File f : tempDir.listFiles()) {
                        result = f.delete();
                        Log.i(TAG, "deleteAppTempDirectory | deleted a file (" + f.getName() + ")");
                    }
                    tempDir.delete();
                }
            }

            Log.i(TAG, "deleteAppTempDirectory " + !tempDir.exists());
            return result;
        }

        /*public static File getBackupDirectory() {
            makeAppDirectory();
            File dir = new File(getAppDirectory(), "Backups");
            Log.i(TAG, "getBackupDirectory: " + dir.getAbsolutePath());
            return dir;
        }*/
    }

    public static class Fonts {


    }

    public static class Strings {

        public static SpannableString makeUnderlined(String txt) {
            SpannableString content = new SpannableString(txt);
            content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
            return content;
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        public static byte[]decodeBase64(String encodedData){
            byte[] decodedBytes = Base64.getDecoder().decode(encodedData.getBytes());

            return decodedBytes ;
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        public static String encodeBase64(byte [] encodeMe){
            byte[] encodedBytes = Base64.getEncoder().encode(encodeMe);
            return new String(encodedBytes) ;
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        public static String decodeBase64AsString(String encodedData){
            byte[] decodedBytes = Base64.getDecoder().decode(encodedData.getBytes());
            String b64 = new String(decodedBytes);
            return b64;
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        public static String encodeBase64(String encodeMe){
            byte[] encodedBytes = Base64.getEncoder().encode(encodeMe.getBytes());
            return new String(encodedBytes) ;
        }

    }




















































}
