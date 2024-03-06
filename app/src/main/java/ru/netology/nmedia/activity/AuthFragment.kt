package ru.netology.nmedia.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.launch
import ru.netology.nmedia.databinding.FragmentAuthBinding
import ru.netology.nmedia.viewmodel.AuthorizationViewModel

class AuthFragment : Fragment() {
    private val viewModel: AuthorizationViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val bindingAuth = FragmentAuthBinding.inflate(inflater, container, false)

        bindingAuth.buttonLogin.setOnClickListener {
            val log = bindingAuth.login.text.toString()
            val pas = bindingAuth.password.text.toString()

            viewModel.authenticate(log,pas)
        }

        lifecycleScope.launch {

            viewModel._authState.collect { authState ->
                // Обновляем UI в соответствии с результатом аутентификации
                if (authState.error != null) {
                    Toast.makeText(bindingAuth.root.context, "Интернета нету", Toast.LENGTH_SHORT)
                        .show()
                } else if (authState.id != 0L) {
                    // Переходим на главный экран приложения или показываем профиль пользователя

                    findNavController().navigateUp()
                }
            }

        }

        return bindingAuth.root
    }

}
