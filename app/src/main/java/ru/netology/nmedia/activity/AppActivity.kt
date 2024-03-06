package ru.netology.nmedia.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.findNavController
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import ru.netology.nmedia.R
import ru.netology.nmedia.activity.NewPostFragment.Companion.textArg
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.viewmodel.AuthViewModel

class AppActivity : AppCompatActivity(R.layout.activity_app) {

    val viewModel by viewModels<AuthViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestNotificationsPermission()

        intent?.let {
            if (it.action != Intent.ACTION_SEND) {
                return@let
            }

            val text = it.getStringExtra(Intent.EXTRA_TEXT)
            if (text?.isNotBlank() != true) {
                return@let
            }

            intent.removeExtra(Intent.EXTRA_TEXT)
            findNavController(R.id.nav_host_fragment)
                .navigate(
                    R.id.action_feedFragment_to_newPostFragment,
                    Bundle().apply {
                        textArg = text
                    }
                )
        }

        /**
        lifecycleScope.launch + lifecycle.repeatOnLifecycle использовать в активити, для того чтобы
         flow прекращалась вместе с ЖЗ активти ! и только State.RESUMED !!
         */
        lifecycleScope.launch{
            lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.data.collect {
                    invalidateOptionsMenu()
                }
            }
        }
        val navController = findNavController(R.id.nav_host_fragment)




        addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_main, menu)
            }

            override fun onPrepareMenu(menu: Menu) {
                menu.setGroupVisible(R.id.authenticated, viewModel.authenticated)
                menu.setGroupVisible(R.id.unauthenticated, !viewModel.authenticated)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.signin -> {
                        findNavController(R.id.nav_host_fragment)
                            .navigate(
                                R.id.authFragment,
                            )
                        true
                    }

                    R.id.signup -> {
                        findNavController(R.id.nav_host_fragment)
                            .navigate(
                                R.id.registrFragment,
                            )
                        true
                    }

                    R.id.signout -> {
                        // TODO
                        AppAuth.getInstance().removeAuth()
                        true
                    }

                    else -> false
                }
            }

        })

        checkGoogleApiAvailability()
    }
    /**
    добавить в актионбар стрелку <- кнопку, но тут без активации
     */
    override fun onStart() {
        super.onStart()
        findNavController(R.id.nav_host_fragment).addOnDestinationChangedListener { controller, destination, arguments ->
            supportActionBar?.setDisplayHomeAsUpEnabled(destination.id == R.id.newPostFragment)
        }

    }

    private fun requestNotificationsPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return
        }

        val permission = Manifest.permission.POST_NOTIFICATIONS

        if (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED) {
            return
        }

        requestPermissions(arrayOf(permission), 1)
    }

    private fun checkGoogleApiAvailability() {
        with(GoogleApiAvailability.getInstance()) {
            val code = isGooglePlayServicesAvailable(this@AppActivity)
            if (code == ConnectionResult.SUCCESS) {
                return@with
            }
            if (isUserResolvableError(code)) {
                getErrorDialog(this@AppActivity, code, 9000)?.show()
                return
            }
            Toast.makeText(this@AppActivity, R.string.google_play_unavailable, Toast.LENGTH_LONG)
                .show()
        }

        FirebaseMessaging.getInstance().token.addOnSuccessListener {
            println(it)
        }
    }
}