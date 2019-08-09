package com.example.preferenceroom_kotlin;

@FunctionalInterface
public interface PreferenceResultContract<Result> {
    void onKeyAvailable(Result result);
}
