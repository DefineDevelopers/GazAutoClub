package net.webdefine.gazautoclub.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import net.webdefine.gazautoclub.R


class UserSettingsFragment : Fragment() {
    companion object {
        private const val USER_ID = "user_id"

        fun newInstance(id: Int): UserSettingsFragment {
            val args = Bundle()
            args.putSerializable(USER_ID, id)
            val fragment = UserSettingsFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        return inflater!!.inflate(R.layout.fragment_users_settings, container,
                false)
    }
}