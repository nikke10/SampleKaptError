package com.skydoves.preferenceroomdemo.entities;

import com.example.preferenceroom_kotlin.annotation.KeyName;
import com.example.preferenceroom_kotlin.annotation.PreferenceEntity;

@PreferenceEntity(value = "offlinekyc_config")
public class KYCConfig {

    private static final String KEY_IS_OFFLINE_KYC_ENABLED = "KEY_OFFLINE_KYC_ENABLED";
    private static final String KEY_OFFLINE_KYC_STATUS = "KEY_OFFLINE_KYC_STATUS";

        @KeyName(KEY_IS_OFFLINE_KYC_ENABLED)
    protected final boolean isOfflineKycEnabled = false;

        @KeyName(KEY_OFFLINE_KYC_STATUS)
    protected final String offlineKycStatus = "INITIATED";
}
