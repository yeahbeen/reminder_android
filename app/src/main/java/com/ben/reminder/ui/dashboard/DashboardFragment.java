package com.ben.reminder.ui.dashboard;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import com.ben.reminder.MainActivity;
import com.ben.reminder.databinding.FragmentDashboardBinding;
import java.util.ArrayList;

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    final String TAG = Thread.currentThread().getStackTrace()[2].getFileName().replace(".java","");
    final int PORT = 6066;
    private String ip;
    private String endpoint;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Log.e(TAG,"in dash create");
        DashboardViewModel dashboardViewModel =
                new ViewModelProvider(getActivity()).get(DashboardViewModel.class);

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        Log.e(TAG,String.valueOf(dashboardViewModel.getIp().getValue()));
        Log.e(TAG,String.valueOf(dashboardViewModel.getEndpoint().getValue()));

        final TextView info = binding.infotext;

        ArrayList<MutableLiveData<String>> infoArr = dashboardViewModel.getInfoArr();
        for(int i=0;i<infoArr.size();i++){
            infoArr.get(i).setValue("");
            //注册观察者,观察数据的变化
            infoArr.get(i).observe(getViewLifecycleOwner(), value -> {
                Log.e(TAG, value);
                info.append(value);
                int offset=(int)(info.getLineCount()*info.getLineHeight()*1.2); //这个1.2是估算加上去的
                if(offset>info.getHeight()){
                    info.scrollTo(0,offset-info.getHeight());
                }
            });
        }

        info.setMovementMethod(ScrollingMovementMethod.getInstance()); //滚动条

        final TextView ip = binding.localip;
        dashboardViewModel.getIp().observe(getViewLifecycleOwner(), ip::setText);

        final TextView endpoint = binding.remoteip;
        dashboardViewModel.getEndpoint().observe(getViewLifecycleOwner(), endpoint::setText);
        final Button btnSendFile = binding.btnSendFile;
        btnSendFile.setOnClickListener(view -> {
            ((MainActivity)getActivity()).onClickSendFile();
        });
        final Button btnRefresh = binding.btnRefresh;
        btnRefresh.setOnClickListener(view -> {
            ((MainActivity)getActivity()).onClickRefresh();
        });
        info.setText(dashboardViewModel.saveInfo);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.e(TAG,"in dash destroy");
        binding = null;
    }
}