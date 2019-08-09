package com.skydoves.preferenceroomdemo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.skydoves.preferenceroomdemo.dagger.StoresPreferenceComponent
import com.skydoves.preferenceroomdemo.entities.Preference_StoresConfig
import kotlinx.android.synthetic.main.activity_login.*
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast
import javax.inject.Inject

class LoginActivity : AppCompatActivity() {

    /**
     * UserProfile entity.
     * [com.skydoves.preferenceroomdemo.entities.Profile]
     */
    @Inject lateinit var storePref : Preference_StoresConfig

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        StoresPreferenceComponent.Initializer.init(this).inject(this)

        login_button.setOnClickListener {
            val inputNick = login_editText_nick.text.toString()
            val inputAge = login_editText_age.text.toString()
            when(inputNick.isNotEmpty() && inputAge.isNotEmpty()) {
                true -> {
                    storePref.putAge(inputAge.toInt())
                    storePref.putNickName(inputNick)
                    startActivity<MainActivity>()
                    finish()
                }
                false -> toast("please fill all inputs")
            }
        }
    }
}
