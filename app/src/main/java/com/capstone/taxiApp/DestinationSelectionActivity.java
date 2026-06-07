package com.capstone.taxiApp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import java.util.List;

public class DestinationSelectionActivity extends AppCompatActivity {

    private String selectedTargetName;
    private long selectedTargetZoneId;
    private String selectedDirectionType;
    private TextView selectionSummaryTextView;
    private LinearLayout destinationContainer;
    private ServiceZoneRepository serviceZoneRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_destination_selection);

        serviceZoneRepository = new ServiceZoneRepository();
        selectionSummaryTextView = findViewById(R.id.selectionSummaryTextView);
        destinationContainer = findViewById(R.id.destinationContainer);
        Button nextButton = findViewById(R.id.nextToLocationCheckButton);
        Button backButton = findViewById(R.id.backToHomeButton);

        nextButton.setOnClickListener(v -> {
            if (selectedTargetName == null) {
                Toast.makeText(this, "목적지를 먼저 선택해 주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(this, LocationCheckActivity.class);
            intent.putExtra(LocationCheckActivity.EXTRA_TARGET_ZONE_ID, selectedTargetZoneId);
            intent.putExtra(LocationCheckActivity.EXTRA_TARGET_ZONE_NAME, selectedTargetName);
            intent.putExtra(LocationCheckActivity.EXTRA_DIRECTION_TYPE, selectedDirectionType);
            startActivity(intent);
        });

        backButton.setOnClickListener(v -> finish());
        loadServiceZones();
    }

    private void loadServiceZones() {
        selectionSummaryTextView.setText("서비스 존을 불러오는 중입니다...");

        FirebaseTokenHelper.fetchIdToken(new FirebaseTokenHelper.TokenCallback() {
            @Override
            public void onSuccess(@NonNull String idToken) {
                new Thread(() -> {
                    try {
                        List<ServiceZoneRepository.ServiceZoneItem> serviceZones =
                                serviceZoneRepository.fetchMyServiceZones(idToken);
                        runOnUiThread(() -> renderServiceZones(serviceZones));
                    } catch (Exception exception) {
                        runOnUiThread(() -> {
                            selectionSummaryTextView.setText("서비스 존을 불러오지 못했습니다.");
                            Toast.makeText(
                                    DestinationSelectionActivity.this,
                                    exception.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        });
                    }
                }).start();
            }

            @Override
            public void onFailure(@NonNull String message) {
                selectionSummaryTextView.setText("토큰 발급 실패");
                Toast.makeText(DestinationSelectionActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void renderServiceZones(@NonNull List<ServiceZoneRepository.ServiceZoneItem> serviceZones) {
        destinationContainer.removeAllViews();
        selectionSummaryTextView.setText("목적지를 선택해 주세요.");

        if (serviceZones.isEmpty()) {
            selectionSummaryTextView.setText("활성화된 목적지가 없습니다.");
            return;
        }

        for (ServiceZoneRepository.ServiceZoneItem item : serviceZones) {
            MaterialButton button = new MaterialButton(this);
            button.setText(item.zoneName() + " (" + item.zoneType() + ")");
            button.setOnClickListener(v -> selectServiceZone(item));
            destinationContainer.addView(button);
        }
    }

    private void selectServiceZone(@NonNull ServiceZoneRepository.ServiceZoneItem item) {
        selectedTargetName = item.zoneName();
        selectedTargetZoneId = item.zoneId();
        selectedDirectionType = resolveDirectionType(item.zoneType());
        selectionSummaryTextView.setText(
                "선택 목적지: " + selectedTargetName + "\n방향: " + selectedDirectionType
        );
        Toast.makeText(this, "목적지: " + selectedTargetName, Toast.LENGTH_SHORT).show();
    }

    @NonNull
    private String resolveDirectionType(@NonNull String zoneType) {
        if ("SCHOOL".equalsIgnoreCase(zoneType)) {
            return "STATION_TO_SCHOOL";
        }
        return "SCHOOL_TO_STATION";
    }
}
