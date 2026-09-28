package com.example.pfsm.ui.theme.util


private val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

fun validateUsername(name: String): String? = when {
    name.isBlank() -> "Enter your name"
    name.trim().length < 2 -> "Name is too short"
    else -> null
}

fun validateEmail(email: String): String? = when {
    email.isBlank() -> "Enter your email"
    !EMAIL_REGEX.matches(email.trim()) -> "Enter a valid email (e.g. name@example.com)"
    else -> null
}


fun validatePassword(password: String): String? {
    if (password.isEmpty()) return "Enter a password"
    if (password.length < 6) return "Password must be at least 6 characters (now ${password.length})"
    if (password.contains(" ")) return "Password must not contain spaces"

    val missing = mutableListOf<String>()
    if (!password.any { it.isUpperCase() }) missing.add("an uppercase letter")
    if (!password.any { it.isDigit() }) missing.add("a number")
    if (!password.any { !it.isLetterOrDigit() && !it.isWhitespace() }) missing.add("a special character (e.g. @ # \$ !)")

    return if (missing.isEmpty()) null else "Add " + missing.joinToString(", ")
}

fun validateConfirmPassword(password: String, confirm: String): String? = when {
    confirm.isEmpty() -> "Confirm your password"
    confirm != password -> "Passwords don't match"
    else -> null
}