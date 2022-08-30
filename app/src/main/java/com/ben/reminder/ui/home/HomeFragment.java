package com.ben.reminder.ui.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.ben.reminder.databinding.FragmentHomeBinding;
import com.ben.reminder.databinding.TimePickerDlgBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private TimePickerDlgBinding time_binding;
    final String TAG = Thread.currentThread().getStackTrace()[2].getFileName().replace(".java","");
    private FloatingActionButton fab;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Log.e(TAG,"in home create");
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        time_binding = TimePickerDlgBinding.inflate(inflater, container, false);

        AlarmView alarmView = binding.alarmview;
        alarmView.init(binding,time_binding);

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.e(TAG,"in home destroy");
        binding = null;
    }

}