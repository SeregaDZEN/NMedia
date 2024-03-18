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
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.FragmentRegistrBinding
import ru.netology.nmedia.viewmodel.RegistrationViewModel

class RegistrFragment : Fragment() {
    private val viewModel: RegistrationViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val bindingReg = FragmentRegistrBinding.inflate(inflater, container, false)

        bindingReg.buttonReg.setOnClickListener {
            val log = bindingReg.login.text.toString()
            val pas = bindingReg.password.text.toString()
            val confirm = bindingReg.confirmPassword.text.toString()
            val name = bindingReg.name.text.toString()

            if (pas != confirm){
                Toast.makeText(bindingReg.root.context, R.string.Password_mismatch, Toast.LENGTH_SHORT)
                    .show()
            } else{
                viewModel.registerUser(log, pas, name)
            }

        }

        lifecycleScope.launch {

            viewModel._authState.collect { authState ->
                // Обновляем UI в соответствии с результатом аутентификации
                if (authState.error != null) {
                    Toast.makeText(bindingReg.root.context, R.string.NoInternet, Toast.LENGTH_SHORT)
                        .show()
                } else if (authState.id != 0L) {
                    // Переходим на главный экран приложения или показываем профиль пользователя

                    findNavController().navigateUp()
                }
            }

        }

        return bindingReg.root
    }

}
