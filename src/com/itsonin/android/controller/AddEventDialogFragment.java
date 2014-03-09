package com.itsonin.android.controller;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.itsonin.android.R;
import com.itsonin.android.model.Host;
import com.squareup.timessquare.CalendarPickerView;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: johnarleyburns
 * Date: 3/9/14
 * Time: 11:33 AM
 * To change this template use File | Settings | File Templates.
 */
public class AddEventDialogFragment extends DialogFragment {

    private static final String CATEGORY_INDEX_KEY = "categoryIndex";
    private static final String EVENT_DATE_KEY = "eventDate";
    private static final String START_TIME_KEY = "startTime";
    private static final String END_TIME_KEY = "endTime";

    private Host host;
    private int categoryIndex;
    private Date eventDate;
    private Date startTime;
    private Date endTime;

    private String[] categories;
    private View overlay;
    private ProgressBar progressBar;
    private TextView eventDateView;
    private TextView startTimeView;
    private TextView endTimeView;
    private ArrayAdapter<String> hostAutoAdapter;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            restoreBundleVariables(savedInstanceState);
        }

        View view = createLayout(savedInstanceState);

        initDates(view.getContext());

        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.add_event)
                .setView(view)
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                            }
                        }
                )
                .setNegativeButton(android.R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                            }
                        }
                )
                .create();
    }

    private void restoreBundleVariables(Bundle bundle) {
        if (bundle != null) {
            categoryIndex = bundle.getInt(CATEGORY_INDEX_KEY);
            try {
                eventDate = DateFormat.getDateInstance().parse(bundle.getString(EVENT_DATE_KEY));
            }
            catch (ParseException e) {
                eventDate = Calendar.getInstance().getTime();
            }
            try {
                startTime = DateFormat.getDateInstance().parse(bundle.getString(START_TIME_KEY));
            }
            catch (ParseException e) {
                startTime = Calendar.getInstance().getTime();
            }
            try {
                endTime = DateFormat.getDateInstance().parse(bundle.getString(END_TIME_KEY));
            }
            catch (ParseException e) {
                endTime = Calendar.getInstance().getTime();
            }
        }
    }
    
    
    private View createLayout(Bundle savedInstanceState) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.add_event_layout, null);

        overlay = view.findViewById(R.id.overlay);
        progressBar = (ProgressBar)view.findViewById(R.id.progress);

        host = Host.load(view.getContext());
        categories = getResources().getStringArray(R.array.event_categories);
        categoryIndex = 0;

        hostAutoAdapter = new ArrayAdapter<String>(view.getContext(),
                android.R.layout.simple_dropdown_item_1line, host.rememberedNames());
        AutoCompleteTextView textView = (AutoCompleteTextView)
                view.findViewById(R.id.host);
        textView.setAdapter(hostAutoAdapter);
        textView.setOnFocusChangeListener(hostListener);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(view.getContext(),
                R.array.event_categories_readable, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner spinner = (Spinner)view.findViewById(R.id.category);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(eventCategoryListener);
        spinner.setSelection(categoryIndex);

        eventDateView = (TextView)view.findViewById(R.id.date);
        eventDateView.setOnFocusChangeListener(eventDateFocusListener);
        eventDateView.setOnClickListener(eventDateClickListener);

        startTimeView = (TextView)view.findViewById(R.id.start_time);
        startTimeView.setOnFocusChangeListener(startTimeFocusListener);
        startTimeView.setOnClickListener(startTimeClickListener);

        endTimeView = (TextView)view.findViewById(R.id.end_time);
        endTimeView.setOnFocusChangeListener(endTimeFocusListener);
        endTimeView.setOnClickListener(endTimeClickListener);

        return view;
    }

    private View.OnFocusChangeListener hostListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (!hasFocus) {
                String name = ((EditText)v).getText().toString();
                boolean exist = false;
                for (int i = 0; i < hostAutoAdapter.getCount(); i++) {
                    if (name.equals(hostAutoAdapter.getItem(i))) {
                        exist = true;
                        break;
                    }
                }
                if (exist) {
                    hostAutoAdapter.add(name);
                }
                host.names.add(name);
                host.store(v.getContext());
            }
        }
    };

    private Spinner.OnItemSelectedListener eventCategoryListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            categoryIndex = position;
        }
        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    };

    private View.OnFocusChangeListener eventDateFocusListener = new View.OnFocusChangeListener() {
        private static final String TAG = "eventDateFocusListener";
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus) {
                overlay.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.VISIBLE);
                new EventDateDialog().show(getFragmentManager(), TAG);
            }
        }
    };
    
    private View.OnClickListener eventDateClickListener = new View.OnClickListener() {
        private static final String TAG = "eventDateClickListener";
        @Override
        public void onClick(View v) {
            overlay.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.VISIBLE);
            new EventDateDialog().show(getFragmentManager(), TAG);
        }
    };

    private View.OnFocusChangeListener startTimeFocusListener = new View.OnFocusChangeListener() {
        private static final String TAG = "startTimeFocusListener";
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus) {
                new StartTimeDialog().show(getFragmentManager(), TAG);
            }
        }
    };

    private View.OnClickListener startTimeClickListener = new View.OnClickListener() {
        private static final String TAG = "startTimeFocusListener";
        @Override
        public void onClick(View v) {
            new StartTimeDialog().show(getFragmentManager(), TAG);
        }
    };

    private View.OnFocusChangeListener endTimeFocusListener = new View.OnFocusChangeListener() {
        private static final String TAG = "endTimeFocusListener";
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus) {
                new EndTimeDialog().show(getFragmentManager(), TAG);
            }
        }
    };

    private View.OnClickListener endTimeClickListener = new View.OnClickListener() {
            private static final String TAG = "endTimeFocusListener";
            @Override
            public void onClick(View v) {
                new EndTimeDialog().show(getFragmentManager(), TAG);
            }
    };
    
    private class EventDateDialog extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            View view = getActivity().getLayoutInflater().inflate(R.layout.calendar_dialog, null);

            Dialog d = new AlertDialog.Builder(getActivity())
                    //.setTitle(R.string.date_hint)
                    .setView(view)
                    .create();

            Calendar nextYear = Calendar.getInstance();
            nextYear.add(Calendar.YEAR, 1);

            CalendarPickerView calendarView = (CalendarPickerView) view.findViewById(R.id.calendar_view);
            Date today = new Date();
            calendarView.init(today, nextYear.getTime())
                    .withSelectedDate(eventDate);
            calendarView.setOnDateSelectedListener(new CalendarPickerView.OnDateSelectedListener() {
                @Override
                public void onDateSelected(Date date) {
                    eventDate = date;
                    DateFormat df = DateFormat.getDateInstance();
                    String s = df.format(eventDate);
                    eventDateView.setText(s);
                    dismissAllowingStateLoss();
                }
                @Override
                public void onDateUnselected(Date date) {
                }
            });

            overlay.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
            return d;
        }
    }

    private void initDates(Context context) {
        initEventDate(context);
        initStartTime(context);
        initEndTime(context);
    }

    private void initEventDate(Context context) {
        Date today = new Date();
        if (eventDate == null) {
            eventDate = today;
        }
        DateFormat df = DateFormat.getDateInstance();
        String s = df.format(eventDate);
        eventDateView.setText(s);
    }

    private void initStartTime(Context context) {
        Calendar c = Calendar.getInstance();
        c.roll(Calendar.HOUR, 1);
        c.set(Calendar.MINUTE, 0);
        startTime = c.getTime();
        DateFormat df = android.text.format.DateFormat.getTimeFormat(context);
        String s = df.format(startTime);
        startTimeView.setText(s);
    }

    private void initEndTime(Context context) {
        Calendar c = Calendar.getInstance();
        c.setTime(startTime);
        c.roll(Calendar.HOUR, 1);
        endTime = c.getTime();
        DateFormat df = android.text.format.DateFormat.getTimeFormat(context);
        String s = df.format(endTime);
        endTimeView.setText(s);
    }

    private class StartTimeDialog extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Calendar c = Calendar.getInstance();
            c.setTime(startTime);
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);
            return new TimePickerDialog(getActivity(), this, hour, minute,
                    android.text.format.DateFormat.is24HourFormat(getActivity()));
        }
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            Calendar c = Calendar.getInstance();
            c.set(Calendar.HOUR_OF_DAY, hourOfDay);
            c.set(Calendar.MINUTE, minute);
            startTime = c.getTime();
            //DateFormat df = DateFormat.getTimeInstance();
            DateFormat df = android.text.format.DateFormat.getTimeFormat(view.getContext());
            String s = df.format(startTime);
            startTimeView.setText(s);
            if (c.before(endTime)) {
                initEndTime(view.getContext());
            }
            dismissAllowingStateLoss();
        }
    }

    private class EndTimeDialog extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Calendar c = Calendar.getInstance();
            if (endTime == null) {
                c.roll(Calendar.HOUR, 2);
                endTime = c.getTime();
            }
            else {
                c.setTime(endTime);
            }
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);
            return new TimePickerDialog(getActivity(), this, hour, minute,
                    android.text.format.DateFormat.is24HourFormat(getActivity()));
        }
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            Calendar now = Calendar.getInstance();
            now.set(Calendar.HOUR_OF_DAY, hourOfDay);
            now.set(Calendar.MINUTE, minute);
            endTime = now.getTime();
            //DateFormat df = DateFormat.getTimeInstance();
            DateFormat df = android.text.format.DateFormat.getTimeFormat(view.getContext());
            String s = df.format(endTime);
            endTimeView.setText(s);
            dismissAllowingStateLoss();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putInt(CATEGORY_INDEX_KEY, categoryIndex);
        bundle.putString(EVENT_DATE_KEY, eventDate.toString());
        bundle.putString(START_TIME_KEY, startTime.toString());
        bundle.putString(END_TIME_KEY, endTime.toString());
    }

}
