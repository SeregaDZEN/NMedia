package ru.netology.nmedia.error

import java.lang.RuntimeException

sealed class AppError (val code: Int, val info : String) : RuntimeException(info)

class ApiException(code: Int, message : String): AppError(code, message)

data object NetworkException : AppError(-1, "no_network")
data object UnknownException : AppError(-1, "xz")



