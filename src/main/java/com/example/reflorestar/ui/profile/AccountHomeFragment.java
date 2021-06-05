package com.example.reflorestar.ui.profile;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import com.example.reflorestar.CameraActivity;
import com.example.reflorestar.R;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import java.util.Arrays;
import java.util.List;

import static android.app.Activity.RESULT_OK;
import static android.content.ContentValues.TAG;

public class AccountHomeFragment extends Fragment {

    private ProfileViewModel profileViewModel;
    private boolean authenticated = false;
    private ConstraintLayout fragmentContainer;
    private static final int RC_SIGN_IN = 123;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_account_home, container, false);
        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        //DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("users");
        fragmentContainer = root.findViewById(R.id.account_home_container);
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build());
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            // User is signed in
            accessProfile(fragmentContainer);
        } else {
            // No user is signed in
            Button buttonLogin = root.findViewById(R.id.buttonLogin);
            Button buttonCreateAccount = root.findViewById(R.id.buttonCreateAccount);

            buttonLogin.setOnClickListener(v -> {
                //loginPage(fragmentContainer);
                startActivityForResult(
                        AuthUI.getInstance()
                                .createSignInIntentBuilder()
                                .setAvailableProviders(providers)
                                .setLogo(R.drawable.ic_politecnico_h)
                                .build(),
                        RC_SIGN_IN);
            });

            buttonCreateAccount.setOnClickListener(v -> {
                createAccount(fragmentContainer);
            });
        }

        return root;
    }



    // [START auth_fui_result]
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                        .setDisplayName("Enrico Gomes")
                        .setPhotoUri(Uri.parse("https://ownyourbits.com/wp-content/uploads/2017/01/chameleon3.jpg"))
                        .build();

                user.updateProfile(profileUpdates)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "User profile updated.");
                                }
                            }
                        });
                accessProfile(fragmentContainer);
                // ...
            } else {
                Toast.makeText(getActivity().getApplicationContext(), "Incorrect password", Toast.LENGTH_SHORT).show();
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                // ...
            }
        }
    }
    /* [END auth_fui_result] */

    private void loginPage(ConstraintLayout fragmentContainer) {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        fm.beginTransaction().replace(R.id.account_home_container, new AccountLoginFragment()).addToBackStack( "account_home" ).commit();
        fragmentContainer.removeAllViews();
    }

    private void createAccount(ConstraintLayout fragmentContainer) {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        fm.beginTransaction().replace(R.id.account_home_container, new AccountCreateFragment()).addToBackStack( "account_home" ).commit();
        fragmentContainer.removeAllViews();
    }

    private void accessProfile(ConstraintLayout fragmentContainer) {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        fm.beginTransaction().replace(R.id.account_home_container, new ProfileFragment()).addToBackStack( "account_home" ).commit();
        fragmentContainer.removeAllViews();
    }
}