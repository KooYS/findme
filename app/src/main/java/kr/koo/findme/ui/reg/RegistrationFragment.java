package kr.koo.findme.ui.reg;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.github.kimkevin.cachepot.CachePot;

import javax.annotation.Nonnull;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.viewpager.widget.ViewPager;
import kr.koo.findme.MainActivity;
import kr.koo.findme.R;
import kr.koo.findme.UserQuery;
import kr.koo.findme.card.CardPagerAdapter;
import kr.koo.findme.card.ShadowTransformer;
import kr.koo.findme.type.UserWhereUniqueInput;

public class RegistrationFragment extends Fragment {

    private RegistrationViewModel homeViewModel;
    private  ViewPager viewPager;
    public RegistrationFragment(){
    }
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(RegistrationViewModel.class);

        View root = inflater.inflate(R.layout.fragment_reg, container, false);
        final TextView textView = root.findViewById(R.id.text_home);
        final ViewPager fragment_viewPager = root.findViewById(R.id.viewPager);
        homeViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });

        return root;

    }


}