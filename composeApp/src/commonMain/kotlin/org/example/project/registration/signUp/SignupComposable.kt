package org.example.project.registration.signUp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.example.project.components.button.ButtonComposable
import org.example.project.components.email.EmailTextFieldComposable
import org.example.project.components.errorText.ErrorTextComposable
import org.example.project.components.password.PasswordTextFieldComposable
import org.example.project.dimens.Dimens.spacing16
import org.example.project.dimens.Dimens.spacing8
import org.example.project.dimens.Dimens.webSpacing
import org.example.project.strings.AppLanguage
import org.example.project.strings.SmartTutorStrings
import org.example.project.theme.SmartTutorStyles
import org.example.project.theme.SmartTutorStyles.defaultTextFieldColors
import org.example.project.utils.validation.Validation
import org.example.project.experiment.ExperimentConfig
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusProperties

@Composable
fun SignupComposable(
    signUp: (String, String, String, onSuccess: () -> Unit) -> Unit,
    isWebUi: Boolean = false,
    currentLanguage: AppLanguage,
) {
    val focusManager = LocalFocusManager.current
    var email by remember { mutableStateOf("") }
    var email2 by remember { mutableStateOf("") }
    val emailHasOnlyLatinCharacters =
        email.isNotBlank() && Validation.containsOnlyLatinEmailCharacters(email)

    val email2HasOnlyLatinCharacters =
        email2.isNotBlank() && Validation.containsOnlyLatinEmailCharacters(email2)

    var username by remember { mutableStateOf("") }
    var password1 by remember { mutableStateOf("") }
    var password2 by remember { mutableStateOf("") }

    val isEmailValid = Validation.isValidEmail(email)
    val isDuthEmailAllowed =
        ExperimentConfig.isSignupAllowed(email)
    val isEmail2Valid = Validation.isValidEmail(email2)

    val isUsernameValid = username.isNotBlank()
    val isPassword1Valid = Validation.isValidPassword(password1)
    val isPassword2Valid = Validation.isValidPassword(password2)
    val arePasswordsSame: Boolean =
        password1 == password2 && password1.isNotEmpty() && password2.isNotEmpty()
    val areEmailsSame =
        email.trim() == email2.trim() && email.trim().isNotEmpty() && email2.trim().isNotEmpty()
    val isFormValid by derivedStateOf {
        isEmailValid &&
                isDuthEmailAllowed &&
                isEmail2Valid &&
                emailHasOnlyLatinCharacters &&
                email2HasOnlyLatinCharacters &&
                areEmailsSame &&
                isUsernameValid &&
                isPassword1Valid &&
                isPassword2Valid &&
                arePasswordsSame
    }
    var showVerificationInfo by remember { mutableStateOf(false) }
    var verificationEmail by remember { mutableStateOf("") }

    val emailFocus = remember { FocusRequester() }
    val confirmEmailFocus = remember { FocusRequester() }
    val usernameFocus = remember { FocusRequester() }
    val passwordFocus = remember { FocusRequester() }
    val confirmPasswordFocus = remember { FocusRequester() }
    val signupFocus = remember { FocusRequester() }

    Column(
        modifier = Modifier.verticalScroll(rememberScrollState()).padding(spacing16),
        verticalArrangement = Arrangement.spacedBy(spacing8),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        EmailTextFieldComposable(
            value = email,
            onValueChange = { email = it },
            isWebUi = isWebUi,
            imeAction = ImeAction.Next,
            modifier = Modifier
                .focusRequester(emailFocus)
                .focusProperties {
                    next = confirmEmailFocus
                },
        )

        if (
            ExperimentConfig.IS_EXPERIMENT_ACTIVE &&
            email.isNotBlank() &&
            !isDuthEmailAllowed
        ) {
            ErrorTextComposable(
                "Επιτρέπονται μόνο email με κατάληξη @cs.duth.gr."
            )
        }

        if (email.isNotEmpty()) {
            Column(
                modifier = if (isWebUi) Modifier.width(webSpacing) else Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                ValidationRule(
                    text = SmartTutorStrings.email_latin_only,
                    isValid = emailHasOnlyLatinCharacters
                )

                ValidationRule(
                    text = SmartTutorStrings.email_valid_format,
                    isValid = isEmailValid
                )
            }
        }
        EmailTextFieldComposable(
            value = email2,
            onValueChange = { email2 = it },
            labelHint = SmartTutorStrings.confirm_email,
            isWebUi = isWebUi,
            imeAction = ImeAction.Next,
            modifier = Modifier
                .focusRequester(confirmEmailFocus)
                .focusProperties {
                    previous = emailFocus
                    next = usernameFocus
                },
        )
        if (email2.isNotEmpty()) {
            Column(
                modifier = if (isWebUi) Modifier.width(webSpacing) else Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                ValidationRule(
                    text = SmartTutorStrings.emails_match,
                    isValid = areEmailsSame
                )
            }
        }

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text(SmartTutorStrings.username) },
            singleLine = true,
            colors = defaultTextFieldColors(),

            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = {
                    focusManager.moveFocus(FocusDirection.Next)
                }
            ),
            modifier = Modifier
                .focusRequester(usernameFocus)
                .focusProperties {
                    previous = confirmEmailFocus
                    next = passwordFocus
                }
                .then(
                    if (isWebUi) Modifier.width(webSpacing)
                    else Modifier.fillMaxWidth()
                ),
            )

        if (!isUsernameValid && username.isNotEmpty()) {
            ErrorTextComposable(SmartTutorStrings.username_not_empty)
        }

        PasswordTextFieldComposable(
            value = password1,
            onValueChange = { password1 = it },
            label = SmartTutorStrings.passwordHint,
            isWebUi = isWebUi,
            imeAction = ImeAction.Next,
            modifier = Modifier
                .focusRequester(passwordFocus)
                .focusProperties {
                    previous = usernameFocus
                    next = confirmPasswordFocus
                },
        )

        if (password1.isNotEmpty()) {
            Column(
                modifier = if (isWebUi) Modifier.width(webSpacing) else Modifier.fillMaxWidth()
            ) {
                ValidationRule(
                    text = SmartTutorStrings.password_min_length,
                    isValid = password1.length >= 6
                )

                ValidationRule(
                    text = SmartTutorStrings.password_has_letter,
                    isValid = password1.any { it.isLetter() }
                )

                ValidationRule(
                    text = SmartTutorStrings.password_has_number,
                    isValid = password1.any { it.isDigit() }
                )

                ValidationRule(
                    text = SmartTutorStrings.password_has_special,
                    isValid = password1.any { !it.isLetterOrDigit() }
                )
            }
        }

        PasswordTextFieldComposable(
            value = password2,
            onValueChange = { password2 = it },
            label = SmartTutorStrings.confirm_password,
            isWebUi = isWebUi,
            imeAction = ImeAction.Done,
            modifier = Modifier
                .focusRequester(confirmPasswordFocus)
                .focusProperties {
                    previous = passwordFocus
                    next = signupFocus
                },
        )

        if (password2.isNotEmpty()) {
            Column(
                modifier = if (isWebUi) Modifier.width(webSpacing) else Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {

                ValidationRule(
                    text = SmartTutorStrings.passwords_match,
                    isValid = arePasswordsSame
                )

            }
        }
        ButtonComposable(
            text = SmartTutorStrings.signup,
            enabled = isFormValid,
            onClick = {
                val submittedEmail = email.trim()


                signUp(
                    submittedEmail,
                    password1,
                    username
                ) {
                    verificationEmail = submittedEmail
                    showVerificationInfo = true
                }
                email = ""
                email2 = ""
                username = ""
                password1 = ""
                password2 = ""
            },

            modifier = Modifier
                .focusRequester(signupFocus)
                .focusProperties {
                    previous = confirmPasswordFocus
                }
                .then(
                    if (isWebUi) {
                        Modifier.width(webSpacing)
                            .padding(vertical = spacing8)
                            .align(Alignment.CenterHorizontally)
                    } else {
                        SmartTutorStyles.buttonModifier
                    }
                )

        )
        if (showVerificationInfo) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF2E7D32),
                    modifier = Modifier.size(40.dp)
                )

                Text(
                    text = SmartTutorStrings.account_created,
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = SmartTutorStrings.verification_email_sent,
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = verificationEmail,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = SmartTutorStrings.verify_before_login,
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = SmartTutorStrings.check_spam_folder,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }

}

@Composable
private fun ValidationRule(
    text: String,
    isValid: Boolean,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = if (isValid) {
                Icons.Filled.CheckCircle
            } else {
                Icons.Filled.Cancel
            },
            contentDescription = null,
            tint = if (isValid) {
                Color(0xFF2E7D32)
            } else {
                MaterialTheme.colorScheme.error
            },
            modifier = Modifier.size(16.dp)
        )

        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = if (isValid) {
                Color(0xFF2E7D32)
            } else {
                MaterialTheme.colorScheme.error
            }
        )
    }
}