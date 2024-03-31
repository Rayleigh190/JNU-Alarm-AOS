package com.jnu_alarm.android.ui.settings;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.jnu_alarm.android.R;
import com.jnu_alarm.android.api.ApiClient;
import com.jnu_alarm.android.api.ApiService;
import com.jnu_alarm.android.api.response.ContactApiResponse;
import com.jnu_alarm.android.api.response.NotificationApiResponse;
import com.jnu_alarm.android.data.ContactData;
import com.jnu_alarm.android.data.NotificationData;
import com.jnu_alarm.android.data.SubscriptionData;
import com.jnu_alarm.android.ui.notifications.NotificationsListAdapter;

import java.util.ArrayList;
import java.util.List;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ContactFragment extends Fragment {
    private static final String TAG = "ContactFragment";
    private ApiService apiService;
    EditText emailEditText;
    EditText titleEditText;
    EditText bodyEditText;
    Button submitButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_contact, container, false);

        emailEditText = rootView.findViewById(R.id.email_edit_text);
        titleEditText = rootView.findViewById(R.id.title_edit_text);
        bodyEditText = rootView.findViewById(R.id.body_edit_text);
        submitButton = rootView.findViewById(R.id.submit_button);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = String.valueOf(emailEditText.getText());
                String title = String.valueOf(titleEditText.getText());
                String body = String.valueOf(bodyEditText.getText());

                if (!isEditTextEmpty(getContext(), email, title, body)) {return;}
                if (!isEmailValid(getContext(), email)) {return;}

                Log.d(TAG, "이메일: " + email);
                Log.d(TAG, "제목: " + title);
                Log.d(TAG, "내용: " + body);

                postContact(email, title, body);
            }
        });

        return rootView;
    }

    Boolean isEditTextEmpty(Context context, String email, String title, String body) {
        if (email.isEmpty() || title.isEmpty() || body.isEmpty()) {
            Toast.makeText(context,"입력하지 않은 내용이 있습니다.", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    Boolean isEmailValid(Context context, String email) {
        String emailPattern = "[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}";

        if (!email.matches(emailPattern)) {
            // 이메일 형식이 올바르지 않을 경우
            Toast.makeText(context, "올바른 이메일 주소를 입력하세요.", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    Void postContact(String email, String title, String body) {
        apiService = ApiClient.getClient().create(ApiService.class);

        ContactData contactData = new ContactData(email, title, body);
        // POST 요청 보내기
        Call<ContactApiResponse> call = apiService.postContact(contactData);
        call.enqueue(new Callback<ContactApiResponse>() {
            @Override
            public void onResponse(Call<ContactApiResponse> call, Response<ContactApiResponse> response) {
                if (response.isSuccessful()) {
                    ContactApiResponse contactApiResponse = response.body();
                    if (contactApiResponse != null && contactApiResponse.isSuccess()) {
                        Toast.makeText(getContext(), "제출 되었습니다.", Toast.LENGTH_SHORT).show();
                        emailEditText.setText("");
                        titleEditText.setText("");
                        bodyEditText.setText("");
                    } else {
                        Log.e(TAG, "응답 처리 실패");
                    }
                } else {
                    Log.e(TAG, "postContact 요청 실패");
                    // 요청 실패 처리
                    Log.d(TAG, response.toString());
                }
            }

            @Override
            public void onFailure(Call<ContactApiResponse> call, Throwable t) {
                Log.e(TAG, "네트워크 오류: " + t.getMessage());
                // 네트워크 오류 등 요청 실패 시 처리
                Toast.makeText(getContext(), "제출 실패 했습니다.", Toast.LENGTH_SHORT).show();
            }
        });
        return null;
    }
}